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

    System.out.println("MySQL JDBC Driver Registered!");

    try {

      connection = DriverManager.getConnection(
          "jdbc:mysql://localhost:3306/ivotobd", "bd",
          "bd");

    } catch (SQLException e) {
      e.printStackTrace();
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

    public ArrayList<String> submitQuery(String var1) {
        System.out.println("Comando sql:" + var1);
        ArrayList<String> var2 = new ArrayList<String>();

        try {
            Statement var3 = connection.createStatement();
            ResultSet var4 = var3.executeQuery(var1);
            ResultSetMetaData var5 = var4.getMetaData();
            int var6 = var5.getColumnCount();

            while(var4.next()) {
                for(int var7 = 0; var7 < var6; ++var7) {
                    var2.add(var4.getString(var7 + 1));
                }
            }

            var4.close();
            var3.close();
            return var2;
        } catch (SQLException var8) {
            System.out.println(var8);
            System.out.println("SQLException");
            return var2;
        }
    }
}

