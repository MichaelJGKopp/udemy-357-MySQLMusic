package dev.lpa;

import com.mysql.cj.jdbc.MysqlDataSource;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class Main {

  private final static String CONN_STRING = "jdbc:mysql://localhost:3306/music";

  public static void main(String[] args) {

    String username = JOptionPane.showInputDialog(
      null, "Enter DB Username");

    JPasswordField pf = new JPasswordField();
    int okCxl = JOptionPane.showConfirmDialog(null, pf,
      "Enter DB Password", JOptionPane.OK_CANCEL_OPTION);
    final char[] password =
      (okCxl == JOptionPane.OK_OPTION) ? pf.getPassword() : null; // getPassword returns car[]
    // so pw is not saved as string somewhere

    var dataSource = new MysqlDataSource(); // DataSource newer and preferred over DriverManager
//    dataSource.setUrl(CONN_STRING);
    dataSource.setServerName("localhost");
    dataSource.setPort(3306);
    dataSource.setDatabaseName("music");
//    dataSource.setUser(username);
//    dataSource.setPassword(String.valueOf(password));

//    try (Connection connection = DriverManager.getConnection( // will clause automatically later
//      CONN_STRING, username, String.valueOf(password))) {
    try (Connection connection = dataSource.getConnection(username, String.valueOf(password))) {
      // could use no arguments if datasource gotten by JNDI
      System.out.println("Success!! Connection made to the music database");
      Arrays.fill(password, ' '); // best practice, remove password from memory immediately
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
