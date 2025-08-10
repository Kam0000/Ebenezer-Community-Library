package service;

import model.Transaction;
import java.util.*;

public class LendingTracker {
    private Queue<Transaction> transactions = new LinkedList<>();

    public void borrowBook(Transaction t) {
        transactions.add(t);
    }

    public void returnBook(String isbn, String borrowerId) {
        for (Transaction t : transactions) {
            if (t.isbn.equals(isbn) && t.borrowerId.equals(borrowerId) && !t.returned) {
                t.returned = true;
                break;
            }
        }
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }
}
