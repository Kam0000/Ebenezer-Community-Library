package model;

import java.time.LocalDate;

public class Transaction {
    public String isbn, borrowerId;
    public LocalDate borrowDate, returnDate;
    public boolean returned;

    public Transaction(String isbn, String borrowerId, LocalDate borrowDate, LocalDate returnDate) {
        this.isbn = isbn;
        this.borrowerId = borrowerId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.returned = false;
    }

    @Override
    public String toString() {
        return String.format("ISBN: %s | Borrower ID: %s | Borrow: %s | Return: %s | Returned: %s",
                isbn, borrowerId, borrowDate, returnDate, returned);
    }
}
