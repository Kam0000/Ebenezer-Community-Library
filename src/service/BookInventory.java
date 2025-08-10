package service;

import model.Book;
import util.SearchUtils;
import util.SortUtils;
import java.util.*;

public class BookInventory {
    private Map<String, List<Book>> booksByCategory = new HashMap<>();

    public void addBook(Book book) {
        booksByCategory.putIfAbsent(book.category, new ArrayList<>());
        booksByCategory.get(book.category).add(book);
    }

    public void removeBook(String isbn) {
        for (List<Book> list : booksByCategory.values()) {
            list.removeIf(b -> b.isbn.equals(isbn));
        }
    }

    public List<Book> getAllBooks() {
        List<Book> all = new ArrayList<>();
        for (List<Book> list : booksByCategory.values()) {
            all.addAll(list);
        }
        return all;
    }

    public void listBooksSortedByTitle() {
        List<Book> all = getAllBooks();
        SortUtils.mergeSortByTitle(all);
        all.forEach(System.out::println);
    }

    public Book searchByISBN(String isbn) {
        return SearchUtils.linearSearchByISBN(getAllBooks(), isbn);
    }

    public Book searchByTitle(String title) {
        List<Book> sorted = getAllBooks();
        SortUtils.mergeSortByTitle(sorted);
        return SearchUtils.binarySearchByTitle(sorted, title);
    }

    public Map<String, List<Book>> getBooksByCategory() {
        return booksByCategory;
    }
}
