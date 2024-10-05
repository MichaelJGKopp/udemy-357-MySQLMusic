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

    String query = "SELECT * FROM music.artists";

    var dataSource = new MysqlDataSource();
    dataSource.setServerName(props.getProperty("serverName"));
    dataSource.setPort(Integer.parseInt(props.getProperty("port")));
    dataSource.setDatabaseName(props.getProperty("databaseName"));

    try (var connection = dataSource.getConnection(
      props.getProperty("user"),
      System.getenv("MySQL_PASS"));
      Statement statement = connection.createStatement();
    ) {
      ResultSet resultSet = statement.executeQuery(query); // is closed when statement closed
      // or re-executed or used to retrieve next result from sequence of multiple

      while (resultSet.next()) {
        System.out.printf("%d %s %n",
          resultSet.getInt(1),
          resultSet.getString("artist_name")
        );
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
