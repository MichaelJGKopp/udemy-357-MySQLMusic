package dev.lpa;

import java.sql.*;

public class MusicDML {
  
  public static void main(String[] args) {
    
//    // driver before jdbc 4.0, loaded explicitly
//    try {
//      Class.forName("com.mysql.jdbc.Driver"); // allows to switch drivers during runtime
//    } catch (ClassNotFoundException e) {
//      throw new RuntimeException(e);
//    }
    
    try (Connection connection = DriverManager.getConnection(
      "jdbc:mysql://localhost:3306/music",
      System.getenv("MYSQL_USER"),
      System.getenv("MYSQL_PASS"));
         Statement statement = connection.createStatement();
    ) {
      String artist = "Neil Young"; // not in table
      String query = "SELECT * FROM artists WHERE artist_name ='%s'"
                       .formatted(artist);
      boolean result = statement.execute(query);  // returns always true with SELECT
      System.out.println("result = " + result);
      var rs = statement.getResultSet();
      boolean found = (rs != null && rs.next());
      System.out.println("Artist was " + (found ? "found" : "not found"));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  private static boolean printRecords(ResultSet resultSet) throws SQLException {
    
    boolean foundData = false;
    var meta = resultSet.getMetaData();
    
    System.out.println("==============================");
    
    for (int i = 1; i <= meta.getColumnCount(); i++) {
      System.out.printf("%-15s", meta.getColumnName(i).toUpperCase());
    }
    System.out.println();
    
    while (resultSet.next()) {
      for (int i = 1; i<= meta.getColumnCount(); i++) {
        System.out.printf("%-15s", resultSet.getString(i));
      }
      System.out.println();
      foundData = true;
    }
    return foundData;
  }
}
