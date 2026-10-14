import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

class Book {
    private final int id;
    private String title;
    private String author;
    private String category;
    private boolean available;

    public Book(int id, String title, String author, String category, boolean available) {
        if (id <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than zero.");
        }
        this.id = id;
        setTitle(title);
        setAuthor(author);
        setCategory(category);
        this.available = available;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }

    public void setTitle(String title) { this.title = requireText(title, "Title"); }
    public void setAuthor(String author) { this.author = requireText(author, "Author"); }
    public void setCategory(String category) { this.category = requireText(category, "Category"); }
    public void setAvailable(boolean available) { this.available = available; }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Title: %s | Author: %s | Category: %s | Status: %s",
                id, title, author, category, available ? "Available" : "Borrowed");
    }
}

class LibraryManager {
    public enum AvailabilityStatus { NOT_FOUND, AVAILABLE, BORROWED }

    private final ArrayList<Book> books = new ArrayList<>();

    /** Adds a book. Returns false if another book already has the same ID. */
    public boolean addBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null.");
        }
        if (findById(book.getId()).isPresent()) {
            return false;
        }
        books.add(book);
        return true;
    }

    public void displayAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books are registered.");
            return;
        }
        for (Book book : books) {
            System.out.println(book);
        }
    }

    public Optional<Book> findById(int id) {
        for (Book book : books) {
            if (book.getId() == id) {
                return Optional.of(book);
            }
        }
        return Optional.empty();
    }

    /** Case-insensitive partial-title search. */
    public List<Book> searchByTitle(String searchTerm) {
        String term = normalizeSearchTerm(searchTerm);
        List<Book> matches = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase(Locale.ROOT).contains(term)) {
                matches.add(book);
            }
        }
        return matches;
    }

    /** Case-insensitive exact author-name search. */
    public List<Book> searchByAuthor(String author) {
        String term = normalizeSearchTerm(author);
        List<Book> matches = new ArrayList<>();
        for (Book book : books) {
            if (book.getAuthor().equalsIgnoreCase(term)) {
                matches.add(book);
            }
        }
        return matches;
    }

    /** Returns a new list of books in the requested category (case-insensitive). */
    public List<Book> filterByCategory(String category) {
        String term = normalizeSearchTerm(category);
        List<Book> matches = new ArrayList<>();
        for (Book book : books) {
            if (book.getCategory().equalsIgnoreCase(term)) {
                matches.add(book);
            }
        }
        return matches;
    }

    /** Updates only non-null fields. Returns false when the ID does not exist. */
    public boolean updateBookDetails(int id, String title, String author, String category) {
        Optional<Book> result = findById(id);
        if (!result.isPresent()) {
            return false;
        }
        validateOptionalText(title, "Title");
        validateOptionalText(author, "Author");
        validateOptionalText(category, "Category");

        Book book = result.get();
        if (title != null) book.setTitle(title);
        if (author != null) book.setAuthor(author);
        if (category != null) book.setCategory(category);
        return true;
    }

    public Optional<Book> removeBook(int id) {
        Optional<Book> book = findById(id);
        book.ifPresent(books::remove);
        return book;
    }

    /** Marks a currently available book as borrowed. */
    public boolean borrowBook(int id) {
        Optional<Book> result = findById(id);
        if (!result.isPresent() || !result.get().isAvailable()) {
            return false;
        }
        result.get().setAvailable(false);
        return true;
    }

    /** Marks a currently borrowed book as available again. */
    public boolean returnBook(int id) {
        Optional<Book> result = findById(id);
        if (!result.isPresent() || result.get().isAvailable()) {
            return false;
        }
        result.get().setAvailable(true);
        return true;
    }

    public int countBooks() {
        return books.size();
    }

    public int countAvailableBooks() {
        int count = 0;
        for (Book book : books) {
            if (book.isAvailable()) count++;
        }
        return count;
    }

    public List<Book> findBorrowedBooks() {
        List<Book> borrowed = new ArrayList<>();
        for (Book book : books) {
            if (!book.isAvailable()) borrowed.add(book);
        }
        return borrowed;
    }

    public List<Book> sortBooksByTitle() {
        books.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Book::getTitle));
        return getAllBooks();
    }

    public Optional<String> findMostCommonCategory() {
        if (books.isEmpty()) return Optional.empty();

        Map<String, Integer> counts = new HashMap<>();
        Map<String, String> displayNames = new HashMap<>();
        for (Book book : books) {
            String key = book.getCategory().toLowerCase(Locale.ROOT);
            counts.put(key, counts.getOrDefault(key, 0) + 1);
            displayNames.putIfAbsent(key, book.getCategory());
        }

        String mostCommonKey = null;
        int greatestCount = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > greatestCount
                    || (entry.getValue() == greatestCount
                    && (mostCommonKey == null || entry.getKey().compareTo(mostCommonKey) < 0))) {
                greatestCount = entry.getValue();
                mostCommonKey = entry.getKey();
            }
        }
        return Optional.of(displayNames.get(mostCommonKey));

    public int removeUnavailableBooks(Predicate<Book> removalCondition) {
        if (removalCondition == null) {
            throw new IllegalArgumentException("Removal condition cannot be null.");
        }
        int before = books.size();
        books.removeIf(book -> !book.isAvailable() && removalCondition.test(book));
        return before - books.size();
    }

    public AvailabilityStatus checkAvailability(int id) {
        Optional<Book> result = findById(id);
        if (!result.isPresent()) return AvailabilityStatus.NOT_FOUND;
        return result.get().isAvailable() ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.BORROWED;
    }

    public List<Book> getAllBooks() {
        return Collections.unmodifiableList(new ArrayList<>(books));
    }

    private static String normalizeSearchTerm(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be empty.");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static void validateOptionalText(String value, String fieldName) {
        if (value != null && value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }
}

public class LibraryManagementSystem {
    private static void printBooks(String heading, List<Book> books) {
        System.out.println("\n" + heading);
        if (books.isEmpty()) {
            System.out.println("No matching books.");
            return;
        }
        for (Book book : books) System.out.println(book);
    }

    public static void main(String[] args) {
        LibraryManager library = new LibraryManager();

        // An array seeds the library; LibraryManager stores the books in an ArrayList.
        Book[] starterBooks = {
            new Book(1001, "To Kill a Mockingbird", "Harper Lee", "Fiction", true),
            new Book(1002, "1984", "George Orwell", "Dystopian", true),
            new Book(1003, "Animal Farm", "George Orwell", "Political Fiction", true),
            new Book(1004, "The Hobbit", "J.R.R. Tolkien", "Fantasy", true),
            new Book(1005, "Dune", "Frank Herbert", "Science Fiction", true),
            new Book(1006, "The Martian", "Andy Weir", "Science Fiction", true),
            new Book(1007, "Pride and Prejudice", "Jane Austen", "Romance", true),
            new Book(1008, "The Great Gatsby", "F. Scott Fitzgerald", "Classic", true),
            new Book(1009, "Fahrenheit 451", "Ray Bradbury", "Dystopian", true),
            new Book(1010, "Java: The Complete Reference", "Herbert Schildt", "Programming", true)
        };
        for (Book book : starterBooks) library.addBook(book);

        System.out.println("=== Initial collection ===");
        library.displayAllBooks();

        System.out.println("\nSearch ID 1005: " + library.findById(1005).orElse(null));
        printBooks("Titles containing 'java':", library.searchByTitle("java"));
        printBooks("Books by George Orwell:", library.searchByAuthor("George Orwell"));
        printBooks("Science Fiction books:", library.filterByCategory("Science Fiction"));

        System.out.println("\nUpdate book 1010: "
                + library.updateBookDetails(1010, "Java: The Complete Reference (12th Edition)", null, null));
        System.out.println("Add new book: " + library.addBook(
                new Book(1011, "Withdrawn Copy", "Library Staff", "Fiction", true)));
        System.out.println("Reject duplicate ID 1001: " + !library.addBook(
                new Book(1001, "Duplicate ID Example", "Unknown Author", "Example", true)));

        System.out.println("\nBorrow book 1001: " + library.borrowBook(1001));
        System.out.println("Borrow book 1002: " + library.borrowBook(1002));
        System.out.println("Borrow book 1011: " + library.borrowBook(1011));
        System.out.println("Borrowed books: " + library.findBorrowedBooks().size());
        printBooks("Currently borrowed:", library.findBorrowedBooks());
        System.out.println("Total books: " + library.countBooks());
        System.out.println("Available books: " + library.countAvailableBooks());
        System.out.println("Availability of 1001: " + library.checkAvailability(1001));
        System.out.println("Availability of 1003: " + library.checkAvailability(1003));
        System.out.println("Availability of 9999: " + library.checkAvailability(9999));

        System.out.println("\nReturn book 1001: " + library.returnBook(1001));
        System.out.println("Remove book 1004: " + library.removeBook(1004).isPresent());
        System.out.println("Most common category: "
                + library.findMostCommonCategory().orElse("No categories"));

        int withdrawnRemoved = library.removeUnavailableBooks(book -> book.getId() == 1011);
        System.out.println("Withdrawn unavailable books removed: " + withdrawnRemoved);

        printBooks("Collection sorted by title:", library.sortBooksByTitle());
    }
}
