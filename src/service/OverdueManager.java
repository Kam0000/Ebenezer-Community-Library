package service;

import model.Transaction;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.PriorityQueue;

public class OverdueManager {
    private PriorityQueue<Transaction> overdueQueue =
            new PriorityQueue<>(Comparator.comparing(t -> t.returnDate));

    public void addTransaction(Transaction t) {
        overdueQueue.add(t);
    }

    public void checkOverdue() {
        LocalDate today = LocalDate.now();
        while (!overdueQueue.isEmpty() && overdueQueue.peek().returnDate.isBefore(today.minusDays(14))) {
            Transaction t = overdueQueue.poll();
            System.out.println("Overdue: " + t);
        }
    }
}
