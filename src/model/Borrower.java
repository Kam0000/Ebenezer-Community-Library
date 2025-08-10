package model;

import java.util.ArrayList;
import java.util.List;

public class Borrower {
    public String name, id, contact;
    public List<Book> borrowedBooks = new ArrayList<>();
    public double fines;

    public Borrower(String name, String id, String contact) {
        this.name = name;
        this.id = id;
        this.contact = contact;
        this.fines = 0.0;
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %s) | Contact: %s | Fines: %.2f", name, id, contact, fines);
    }
}
