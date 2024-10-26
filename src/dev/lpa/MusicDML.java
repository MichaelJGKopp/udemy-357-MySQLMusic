package dev.lpa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MusicDML {
  
  public static void main(String[] args) {
    
    // driver before jdbc 4.0, loaded explicitly
    try {
      Class.forName("com.mysql.jdbc.Driver"); // allows to switch drivers during runtime
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    
    try (Connection connection = DriverManager.getConnection(
      "jdbc:mysql://localhost:3306/music",
      System.getenv("MYSQL_USER"),
      System.getenv("MYSQL_PASS"));
         Statement statement = connection.createStatement();
    ) {
      String artist = "Elf";
      String query = "SELECT * FROM artists WHERE artist_name ='%s'"
                       .formatted(artist);
      boolean result = statement.execute(query);
      System.out.println("result = " + result);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
