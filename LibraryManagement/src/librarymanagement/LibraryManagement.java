/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package librarymanagement;
import java.sql.*;
import java.util.Scanner;

public class LibraryManagement {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_management";
    private static final String DB_USER = "root";  // Update with your MySQL username
    private static final String DB_PASSWORD = "";  // Update with your MySQL password

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            do {
                System.out.println("Library Managementco System:");
                System.out.println("1. Add Book");
                System.out.println("2. Borrow Book");
                System.out.println("3. Return Book");
                System.out.println("4. View Available Books");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addBook(connection, scanner);
                        break;
                    case 2:
                        borrowBook(connection, scanner);
                        break;
                    case 3:
                        returnBook(connection, scanner);
                        break;
                    case 4:
                        viewAvailableBooks(connection);
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } while (choice != 5);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    private static void addBook(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine();
        System.out.print("Enter Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Author: ");
        String author = scanner.nextLine();
        System.out.print("Enter Publication Year: ");
        int year = scanner.nextInt();

        String query = "INSERT INTO books (isbn, title, author, publication_year) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, isbn);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, author);
            preparedStatement.setInt(4, year);
            preparedStatement.executeUpdate();
            System.out.println("Book added successfully.");
        }
    }

    private static void borrowBook(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter ISBN of the book to borrow: ");
        String isbn = scanner.nextLine();

        String checkAvailabilityQuery = "SELECT is_borrowed FROM books WHERE isbn = ?";
        String borrowBookQuery = "UPDATE books SET is_borrowed = TRUE WHERE isbn = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkAvailabilityQuery);
             PreparedStatement borrowStmt = connection.prepareStatement(borrowBookQuery)) {
            checkStmt.setString(1, isbn);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                boolean isBorrowed = rs.getBoolean("is_borrowed");
                if (!isBorrowed) {
                    borrowStmt.setString(1, isbn);
                    borrowStmt.executeUpdate();
                    System.out.println("Book borrowed successfully.");
                } else {
                    System.out.println("Book is already borrowed.");
                }
            } else {
                System.out.println("Book not found.");
            }
        }
    }

    private static void returnBook(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter ISBN of the book to return: ");
        String isbn = scanner.nextLine();

        String checkBorrowedQuery = "SELECT is_borrowed FROM books WHERE isbn = ?";
        String returnBookQuery = "UPDATE books SET is_borrowed = FALSE WHERE isbn = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkBorrowedQuery);
             PreparedStatement returnStmt = connection.prepareStatement(returnBookQuery)) {
            checkStmt.setString(1, isbn);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                boolean isBorrowed = rs.getBoolean("is_borrowed");
                if (isBorrowed) {
                    returnStmt.setString(1, isbn);
                    returnStmt.executeUpdate();
                    System.out.println("Book returned successfully.");
                } else {
                    System.out.println("Book was not borrowed.");
                }
            } else {
                System.out.println("Book not found.");
            }
        }
    }

    private static void viewAvailableBooks(Connection connection) throws SQLException {
        String query = "SELECT * FROM books WHERE is_borrowed = FALSE";
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(query)) {
            System.out.println("Available Books:");
            while (rs.next()) {
                System.out.println("ISBN: " + rs.getString("isbn") +
                                   ", Title: " + rs.getString("title") +
                                   ", Author: " + rs.getString("author") +
                                   ", Publication Year: " + rs.getInt("publication_year"));
            }
        }
    }
}
