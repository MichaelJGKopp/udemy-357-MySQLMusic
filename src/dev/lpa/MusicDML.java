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
      String tableName = "music.artists";
      String columnName = "artist_name";
      String columnValue = "Bob Dylan";
      if (!executeSelect(statement, tableName, columnName, columnValue)) {
        insertArtistAlbum(statement, columnValue, columnValue);
      } else {
        try {
          deleteArtistAlbum(connection, statement, columnValue, columnValue);
        } catch (SQLException e) {
          e.printStackTrace();
        }
        executeSelect(statement, "music.albumview", "album_name", columnValue);
        executeSelect(statement, "music.albums", "album_name", columnValue);
      }
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
      for (int i = 1; i <= meta.getColumnCount(); i++) {
        System.out.printf("%-15s", resultSet.getString(i));
      }
      System.out.println();
      foundData = true;
    }
    return foundData;
  }
  
  private static boolean executeSelect(Statement statement, String table, String columnName,
                                       String columnValue) throws SQLException {
    String query = "SELECT * FROM %s WHERE %s='%s'"
                     .formatted(table, columnName, columnValue);
    var rs = statement.executeQuery(query);
    if (rs != null) {
      return printRecords(rs);
    }
    return false;
  }
  
  private static boolean insertRecord(Statement statement, String table, String[] columnNames,
                                      String[] columnValues) throws SQLException {
    
    String colName = String.join(",", columnNames);
    String colValues = String.join("','", columnValues);
    String query = "INSERT INTO %s (%s) VALUES ('%s')"
                     .formatted(table, colName, colValues);
    System.out.println(query);
    boolean insertResult = statement.execute(query);
    int recordsInserted = statement.getUpdateCount(); // NOTE: rows/records affected
    if (recordsInserted > 0) {
      executeSelect(statement, table, columnNames[0], columnValues[0]);
    }
    return recordsInserted > 0;
  }
  
  private static boolean deleteRecord(Statement statement, String table,
                                      String columnName, String columnValue) throws SQLException {
    
    String query = "DELETE FROM %s WHERE %s='%s'"
                     .formatted(table, columnName, columnValue);
    System.out.println(query);
    statement.execute(query);
    int recordsDeleted = statement.getUpdateCount();  // use for UPDATE INSERT DELETE statement
    if (recordsDeleted > 0) {
      executeSelect(statement, table, columnName, columnValue);
    }
    return recordsDeleted > 0;
  }
  
  private static boolean updateRecord(Statement statement, String table,
                                      String matchedColumn, String matchedValue,
                                      String updateColumn, String updatedValue) throws SQLException {
    
    String query = "UPDATE %s SET %s='%s' WHERE %s='%s'"
                     .formatted(table, updateColumn, updatedValue, matchedColumn, matchedValue);
    System.out.println(query);
    statement.execute(query);
    int recordsUpdated = statement.getUpdateCount();  // use for UPDATE INSERT DELETE statement
    if (recordsUpdated > 0) {
      executeSelect(statement, table, updateColumn, updatedValue);
    }
    return recordsUpdated > 0;
  }
  
  private static void insertArtistAlbum(Statement statement, String artistName, String albumName)
    throws SQLException {
    
    String artistInsert = "INSERT INTO music.artists (artist_name) VALUES (%s)"
                            .formatted(statement.enquoteLiteral(artistName)); // NOTE: use if
    // supported
    System.out.println(artistInsert);
    statement.execute(artistInsert, Statement.RETURN_GENERATED_KEYS);
    
    ResultSet rs = statement.getGeneratedKeys();
    int artistId = (rs != null && rs.next()) ? rs.getInt(1) : -1;
    // NOTE: if -1 returned you would abort operation, assumed here to be successful
    String albumInsert = ("INSERT INTO music.albums (album_name, artist_id) " +
                            "VALUES (%s, %d)")
                           .formatted(statement.enquoteLiteral(albumName), artistId);
    System.out.println(albumInsert);
    statement.execute(albumInsert, Statement.RETURN_GENERATED_KEYS);
    rs = statement.getGeneratedKeys();
    int albumId = (rs != null && rs.next()) ? rs.getInt(1) : -1;
    
    String[] songs = new String[]{
      "You're No Good",
      "Talkin' New York",
      "In My Time of Dyin'",
      "Man of Constant Sorrow",
      "Fixin' to Die",
      "Pretty Peggy-O",
      "Highway 51 Blues"
    };
    String songInsert = ("INSERT INTO music.songs " +
                           "(track_number, song_title, album_id) VALUES (%d, %s, %d)");
    
    for (int i = 0; i < songs.length; i++) {
      String query = songInsert.formatted(i + 1, statement.enquoteLiteral(songs[i]), albumId);
      System.out.println(query);
      
      statement.execute(query);
    }
    
    executeSelect(statement, "music.albumview",
      "album_name", "Bob Dylan");
  }
  
  private static void deleteArtistAlbum(Connection conn, Statement statement,
                                        String artistName, String albumName) throws SQLException {
    
    try {
      System.out.println("AUTOCOMMIT = " + conn.getAutoCommit());
      conn.setAutoCommit(false);
      String deleteSongs = """
        
        DELETE FROM music.songs WHERE album_id =
        (SELECT ALBUM_ID from music.albums WHERE album_name = %s)"""
                             .formatted(statement.enquoteLiteral(albumName));
      int deletedSongs = statement.executeUpdate(deleteSongs);
      System.out.printf("Deleted %d rows from music.songs%n", deletedSongs);
      String deleteAlbums = "DELETE FROM music.albums WHERE album_name = '%s'"
                              .formatted(albumName);
      int deletedAlbums = statement.executeUpdate(deleteAlbums);
      System.out.printf("Deleted %d rows from music.albums%n", deletedAlbums);
      String deleteArtist = "DELETE FROM music.artists WHERE artist_name = '%s'"
                              .formatted(artistName);
      int deletedArtists = statement.executeUpdate(deleteArtist);
      System.out.printf("Deleted %d rows from music.artists%n", deletedArtists);
      conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      conn.rollback();
    }
    conn.setAutoCommit(true);
  }
}
