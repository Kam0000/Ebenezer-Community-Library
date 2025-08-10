package model;

public class Book {
    public String title, author, isbn, category, publisher, shelfLocation;
    public int year;

    public Book(String title, String author, String isbn, String category, int year, String publisher, String shelfLocation) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.year = year;
        this.publisher = publisher;
        this.shelfLocation = shelfLocation;
    }

    @Override
    public String toString() {
        return String.format("%s by %s | ISBN: %s | Category: %s | Year: %d | Publisher: %s | Shelf: %s",
                title, author, isbn, category, year, publisher, shelfLocation);
    }
}
