package dev.lpa;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class QueryMusic {
  
  public static void main(String[] args) {
    
    // research best practices for your environment
    // not recommending this approach for a production application
    
    Properties props = new Properties();
    try {
      props.load(Files.newInputStream(Path.of("music.properties"), StandardOpenOption.READ));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    String albumName = "Tapestry";
    String query = "SELECT * FROM music.albumview WHERE album_name='%s'"
                     .formatted(albumName); // ordered by not needed since already ordered by
    // song id
    
    var dataSource = new MysqlDataSource();
    dataSource.setServerName(props.getProperty("serverName"));
    dataSource.setPort(Integer.parseInt(props.getProperty("port")));
    dataSource.setDatabaseName(props.getProperty("databaseName"));
    
    try (var connection = dataSource.getConnection(
      props.getProperty("user"),
      System.getenv("MySQL_PASS"));
         Statement statement = connection.createStatement()
    ) {
      ResultSet resultSet = statement.executeQuery(query); // is closed when statement closed
      // or re-executed or used to retrieve next result from sequence of multiple
      
      // to see columns: name, type when you are not certain
      var meta = resultSet.getMetaData();
      for (int i = 1; i <= meta.getColumnCount(); i++) {
        System.out.printf("%d %s %s%n",
          i,
          meta.getColumnName(i),
          meta.getColumnTypeName(i)
        );
      }
      System.out.println("==============================");
      
      while (resultSet.next()) {
        System.out.printf("%d %s %s %n",
          resultSet.getInt("track_number"),
          resultSet.getString("artist_name"),
          resultSet.getString("song_title")
        );
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
