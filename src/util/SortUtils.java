package util;

import model.Book;
import java.util.*;

public class SortUtils {
    public static void mergeSortByTitle(List<Book> books) {
        if (books.size() < 2) return;
        int mid = books.size() / 2;
        List<Book> left = new ArrayList<>(books.subList(0, mid));
        List<Book> right = new ArrayList<>(books.subList(mid, books.size()));
        mergeSortByTitle(left);
        mergeSortByTitle(right);
        merge(books, left, right);
    }

    private static void merge(List<Book> books, List<Book> left, List<Book> right) {
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (left.get(i).title.compareToIgnoreCase(right.get(j).title) <= 0) {
                books.set(k++, left.get(i++));
            } else {
                books.set(k++, right.get(j++));
            }
        }
        while (i < left.size()) books.set(k++, left.get(i++));
        while (j < right.size()) books.set(k++, right.get(j++));
    }
}
