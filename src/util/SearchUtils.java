package util;

import model.Book;
import java.util.List;

public class SearchUtils {
    public static Book linearSearchByISBN(List<Book> books, String isbn) {
        for (Book b : books) {
            if (b.isbn.equals(isbn)) return b;
        }
        return null;
    }

    public static Book binarySearchByTitle(List<Book> sortedBooks, String title) {
        int low = 0, high = sortedBooks.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = sortedBooks.get(mid).title.compareToIgnoreCase(title);
            if (cmp == 0) return sortedBooks.get(mid);
            else if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }
}
