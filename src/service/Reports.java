package service;

import model.Book;
import model.Borrower;
import model.Transaction;
import java.util.*;

public class Reports {
    public static void mostBorrowedBooks(List<Transaction> transactions) {
        Map<String, Integer> count = new HashMap<>();
        for (Transaction t : transactions) {
            count.put(t.isbn, count.getOrDefault(t.isbn, 0) + 1);
        }
        count.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(5)
                .forEach(e -> System.out.println("ISBN: " + e.getKey() + " | Borrows: " + e.getValue()));
    }

    public static void topBorrowersByFines(List<Borrower> borrowers) {
        borrowers.stream()
                .sorted((a, b) -> Double.compare(b.fines, a.fines))
                .limit(5)
                .forEach(System.out::println);
    }

    public static void inventoryByCategory(Map<String, List<Book>> booksByCategory) {
        booksByCategory.forEach((cat, list) ->
                System.out.println(cat + ": " + list.size() + " books"));
    }
}
