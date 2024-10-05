package dev.lpa;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
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

    var dataSource = new MysqlDataSource();
    dataSource.setServerName(props.getProperty("serverName"));
    dataSource.setPort(Integer.parseInt(props.getProperty("port")));
    dataSource.setDatabaseName(props.getProperty("databaseName"));

    try (var connection = dataSource.getConnection(
      props.getProperty("user"),
      System.getenv("MySQL_PASS"))
    ) {
      System.out.println("Success!");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
