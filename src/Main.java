import model.Book;
import model.Borrower;
import model.Transaction;
import service.BookInventory;
import service.BorrowerRegistry;
import service.LendingTracker;
import service.OverdueManager;
import service.Reports;
import util.SortUtils;
import util.SearchUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Main.java
 * Top-level console application that wires the multi-file library system together.
 *
 * Files used for persistence (CSV-ish):
 *  - books.txt         -> title,author,isbn,category,year,publisher,shelf
 *  - borrowers.txt     -> name,id,contact,fines (fines optional, default 0)
 *  - transactions.txt  -> isbn,borrowerId,borrowDate(ISO),returnDate(ISO),returned(true/false)
 *
 * Keep the remaining services & utils in the packages described in the project layout.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE;

    // core services
    private static final BookInventory inventory = new BookInventory();
    private static final BorrowerRegistry registry = new BorrowerRegistry();
    private static final LendingTracker lending = new LendingTracker();
    private static final OverdueManager overdueManager = new OverdueManager();

    public static void main(String[] args) {
        System.out.println("=== Ebenezer Community Library (Console) ===");
        // Attempt to load persisted data if available
        loadAll();

        while (true) {
            showMainMenu();
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1": addBook(); break;
                case "2": listBooks(); break;
                case "3": searchBooks(); break;
                case "4": sortBooksByTitle(); break;
                case "5": addBorrower(); break;
                case "6": listBorrowers(); break;
                case "7": borrowBook(); break;
                case "8": returnBook(); break;
                case "9": checkOverduesAndApplyFines(); break;
                case "10": showReportsMenu(); break;
                case "11": saveAll(); System.out.println("Saved."); break;
                case "0": saveAll(); System.out.println("Bye."); return;
                default: System.out.println("Invalid option."); break;
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("\n==== EBENEZER COMMUNITY LIBRARY ====");
        System.out.println("Main Menu:");
        System.out.println(" 1) Add Book");
        System.out.println(" 2) List Books (by category)");
        System.out.println(" 3) Search Books (linear/binary)");
        System.out.println(" 4) Sort Books by Title (merge sort)");
        System.out.println(" 5) Add Borrower");
        System.out.println(" 6) List Borrowers");
        System.out.println(" 7) Borrow a Book");
        System.out.println(" 8) Return a Book");
        System.out.println(" 9) Overdue Check & Apply Fines");
        System.out.println("10) Reports");
        System.out.println("11) Save Now");
        System.out.println(" 0) Exit (auto-save)");
        System.out.print("Select: ");
    }

    // ---------- Book actions ----------
    private static void addBook() {
        System.out.print("Title: "); String title = scanner.nextLine().trim();
        System.out.print("Author: "); String author = scanner.nextLine().trim();
        System.out.print("ISBN: "); String isbn = scanner.nextLine().trim();
        System.out.print("Category: "); String category = scanner.nextLine().trim();
        System.out.print("Year: "); int year = safeIntInput(2020);
        System.out.print("Publisher: "); String publisher = scanner.nextLine().trim();
        System.out.print("Shelf Location: "); String shelf = scanner.nextLine().trim();
        Book b = new Book(title, author, isbn, category, year, publisher, shelf);
        inventory.addBook(b);
        System.out.println("Book added: " + title);
    }

    private static void listBooks() {
        inventory.listBooksSortedByTitle(); // uses merge sort internally for display (stable)
    }

    private static void searchBooks() {
        System.out.print("Search by (title/author/isbn): ");
        String field = scanner.nextLine().trim().toLowerCase();
        System.out.print("Query: ");
        String q = scanner.nextLine().trim();
        List<Book> all = inventory.getAllBooks();
        if (all.isEmpty()) { System.out.println("No books in inventory."); return; }

        switch (field) {
            case "isbn":
                Book byIsbn = SearchUtils.linearSearchByISBN(all, q);
                System.out.println(byIsbn == null ? "Not found." : byIsbn);
                break;
            case "title":
                // try binary on sorted list for exact match; fallback to substring linear scanning
                List<Book> sorted = new ArrayList<>(all);
                SortUtils.mergeSortByTitle(sorted);
                Book exact = SearchUtils.binarySearchByTitle(sorted, q);
                if (exact != null) System.out.println("Exact match: " + exact);
                else {
                    System.out.println("No exact title; substring matches:");
                    for (Book b : sorted) if (b.title.toLowerCase().contains(q.toLowerCase())) System.out.println(" - " + b);
                }
                break;
            case "author":
                List<Book> found = new ArrayList<>();
                for (Book b : all) if (b.author.toLowerCase().contains(q.toLowerCase())) found.add(b);
                if (found.isEmpty()) System.out.println("No matches.");
                else found.forEach(System.out::println);
                break;
            default:
                System.out.println("Unknown field.");
        }
    }

    private static void sortBooksByTitle() {
        List<Book> all = inventory.getAllBooks();
        SortUtils.mergeSortByTitle(all);
        System.out.println("Books sorted by title:");
        all.forEach(System.out::println);
    }

    // ---------- Borrower ----------
    private static void addBorrower() {
        System.out.print("Name: "); String name = scanner.nextLine().trim();
        System.out.print("ID: "); String id = scanner.nextLine().trim();
        System.out.print("Contact: "); String contact = scanner.nextLine().trim();
        Borrower b = new Borrower(name, id, contact);
        registry.addBorrower(b);
        System.out.println("Borrower added: " + name + " (ID: " + id + ")");
    }

    private static void listBorrowers() {
        List<Borrower> list = registry.getAllBorrowers();
        if (list.isEmpty()) { System.out.println("No borrowers registered."); return; }
        list.sort(Comparator.comparing(br -> br.name.toLowerCase()));
        list.forEach(System.out::println);
    }

    // ---------- Borrow & Return ----------
    private static void borrowBook() {
        System.out.print("Borrower ID: "); String bid = scanner.nextLine().trim();
        Borrower br = registry.getBorrower(bid);
        if (br == null) { System.out.println("No borrower with that ID. Add them first."); return; }
        System.out.print("Book ISBN: "); String isbn = scanner.nextLine().trim();
        Book book = inventory.searchByISBN(isbn);
        if (book == null) { System.out.println("Book not found."); return; }

        System.out.print("Borrow period in days (e.g. 30): ");
        int days = safeIntInput(30);
        LocalDate borrowDate = LocalDate.now();
        Transaction t = new Transaction(isbn, bid, borrowDate, borrowDate.plusDays(days));
        lending.borrowBook(t);
        overdueManager.addTransaction(t);
        System.out.println("Borrow recorded: " + book.title + " for " + br.name + " (due " + t.returnDate.format(DF) + ")");
    }

    private static void returnBook() {
        System.out.print("Borrower ID: "); String bid = scanner.nextLine().trim();
        Borrower br = registry.getBorrower(bid);
        if (br == null) { System.out.println("Unknown borrower."); return; }
        System.out.print("Book ISBN to return: "); String isbn = scanner.nextLine().trim();
        // mark transaction returned in lending tracker
        lending.returnBook(isbn, bid);
        // simple fine logic demonstration: OverdueManager will process on check
        System.out.println("Return processed (transaction flagged as returned). Run Overdue Check to apply fines if needed.");
    }

    // ---------- Overdue / Fines ----------
    private static void checkOverduesAndApplyFines() {
        System.out.println("Running overdue check (shows items > 14 days overdue)...");
        overdueManager.checkOverdue();
        // NOTE: to actually apply fines to Borrower objects you'd locate overdue transactions
        // and update the Borrower.fines value here based on days overdue and your fine policy.
        // For brevity, the OverdueManager currently prints overdue items. You can extend it to
        // accept a reference to the registry and apply fines directly.
    }

    // ---------- Reports ----------
    private static void showReportsMenu() {
        while (true) {
            System.out.println("\nReports:");
            System.out.println(" 1) Most borrowed books (all time)");
            System.out.println(" 2) Top borrowers by fines");
            System.out.println(" 3) Inventory distribution by category");
            System.out.println(" 0) Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("0")) return;
            switch (choice) {
                case "1":
                    Reports.mostBorrowedBooks(lending.getAllTransactions());
                    break;
                case "2":
                    Reports.topBorrowersByFines(registry.getAllBorrowers());
                    break;
                case "3":
                    Reports.inventoryByCategory(inventory.getBooksByCategory());
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    // ---------- Persistence (simple CSV readers/writers) ----------
    private static final String BOOKS_FILE = "books.txt";
    private static final String BORROWERS_FILE = "borrowers.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    private static void saveAll() {
        try {
            saveBooks();
            saveBorrowers();
            saveTransactions();
            System.out.println("Data saved.");
        } catch (IOException ex) {
            System.out.println("Failed to save: " + ex.getMessage());
        }
    }

    private static void loadAll() {
        try {
            loadBooks();
            loadBorrowers();
            loadTransactions();
            System.out.println("Loaded persisted data (if any).");
        } catch (IOException ex) {
            System.out.println("No persisted data found or failed to load: " + ex.getMessage());
        }
    }

    private static void saveBooks() throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book b : inventory.getAllBooks()) {
                // title,author,isbn,category,year,publisher,shelf
                w.write(escapeCsv(b.title) + "," + escapeCsv(b.author) + "," + b.isbn + "," +
                        escapeCsv(b.category) + "," + b.year + "," + escapeCsv(b.publisher) + "," + escapeCsv(b.shelfLocation));
                w.newLine();
            }
        }
    }

    private static void loadBooks() throws IOException {
        File f = new File(BOOKS_FILE);
        if (!f.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = splitCsvLine(line, 7);
                if (p.length < 7) continue;
                Book b = new Book(unescapeCsv(p[0]), unescapeCsv(p[1]), p[2], unescapeCsv(p[3]), Integer.parseInt(p[4]), unescapeCsv(p[5]), unescapeCsv(p[6]));
                inventory.addBook(b);
            }
        }
    }

    private static void saveBorrowers() throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(BORROWERS_FILE))) {
            for (Borrower br : registry.getAllBorrowers()) {
                // name,id,contact,fines
                w.write(escapeCsv(br.name) + "," + br.id + "," + escapeCsv(br.contact) + "," + br.fines);
                w.newLine();
            }
        }
    }

    private static void loadBorrowers() throws IOException {
        File f = new File(BORROWERS_FILE);
        if (!f.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = splitCsvLine(line, 4);
                if (p.length < 3) continue;
                String name = unescapeCsv(p[0]);
                String id = p[1];
                String contact = unescapeCsv(p[2]);
                Borrower br = new Borrower(name, id, contact);
                if (p.length >= 4) {
                    try { br.fines = Double.parseDouble(p[3]); } catch (Exception ignored) {}
                }
                registry.addBorrower(br);
            }
        }
    }

    private static void saveTransactions() throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(TRANSACTIONS_FILE))) {
            for (Transaction t : lending.getAllTransactions()) {
                // isbn,borrowerId,borrowDate(return ISO),returnDate,returned
                w.write(t.isbn + "," + t.borrowerId + "," + t.borrowDate.format(DF) + "," + t.returnDate.format(DF) + "," + t.returned);
                w.newLine();
            }
        }
    }

    private static void loadTransactions() throws IOException {
        File f = new File(TRANSACTIONS_FILE);
        if (!f.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length < 5) continue;
                Transaction t = new Transaction(p[0], p[1], LocalDate.parse(p[2], DF), LocalDate.parse(p[3], DF));
                t.returned = Boolean.parseBoolean(p[4]);
                lending.borrowBook(t);           // add to the lending queue / storage
                overdueManager.addTransaction(t); // add to overdue manager
            }
        }
    }

    // ---------- Small helpers ----------
    private static int safeIntInput(int defaultVal) {
        String s = scanner.nextLine().trim();
        if (s.isEmpty()) return defaultVal;
        try { return Integer.parseInt(s); } catch (Exception e) { return defaultVal; }
    }

    // Simple escaping/unescaping for commas in text fields (very small CSV helper)
    private static String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace(",", ";;"); // lightweight escape
    }
    private static String unescapeCsv(String s) { return s == null ? "" : s.replace(";;", ","); }

    // split but preserve empty fields; limit specifies expected columns
    private static String[] splitCsvLine(String line, int limit) {
        String[] parts = line.split(",", -1);
        // if more parts than limit, join extras into last column
        if (parts.length <= limit) return parts;
        String[] out = new String[limit];
        System.arraycopy(parts, 0, out, 0, limit - 1);
        // join remaining parts into last
        StringBuilder sb = new StringBuilder(parts[limit - 1]);
        for (int i = limit; i < parts.length; i++) {
            sb.append(",").append(parts[i]);
        }
        out[limit - 1] = sb.toString();
        return out;
    }
}
