package sumdu.edu.ua.core.domain;

public class Book {
    long id;
    String title;
    String author;
    int pubYear;

    public Book(long id, String title, String author, int pubYear) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.pubYear = pubYear;
    }

    public int getPubYear() {
        return pubYear;
    }

    public void setPubYear(int pubYear) {
        this.pubYear = pubYear;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
