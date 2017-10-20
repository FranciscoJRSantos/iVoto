import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseConnection{

  static Connection connection = null;
  // Construtor 
  public DatabaseConnection(String ip, int port){
    try {

      Class.forName("com.mysql.jdbc.Driver");

    } catch (ClassNotFoundException e) {

      System.out.println("Where is your MySQL JDBC Driver?");
      System.out.println("ClassNotFoundException");
      return;

    }

    System.out.println("Oracle JDBC Driver Registered!");

    try {

      connection = DriverManager.getConnection(
          "jdbc:mysql:@localhost:1521:XE", "bd",
          "bd");

    } catch (SQLException e) {

      System.out.println("Connection Failed! Check output console");
      System.out.println("SQLException");
      return;

    }

    if (connection != null) {
      System.out.println("You made it, take control your database now!");
    } else {
      System.out.println("Failed to make connection!");
    }
  }
}
