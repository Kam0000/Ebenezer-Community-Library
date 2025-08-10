package service;

import model.Borrower;
import java.util.*;

public class BorrowerRegistry {
    private Map<String, Borrower> borrowers = new HashMap<>();

    public void addBorrower(Borrower b) {
        borrowers.put(b.id, b);
    }

    public Borrower getBorrower(String id) {
        return borrowers.get(id);
    }

    public List<Borrower> getAllBorrowers() {
        return new ArrayList<>(borrowers.values());
    }

    public Borrower findBorrowerRecursive(List<Borrower> list, String id, int index) {
        if (index >= list.size()) return null;
        if (list.get(index).id.equals(id)) return list.get(index);
        return findBorrowerRecursive(list, id, index + 1);
    }
}
