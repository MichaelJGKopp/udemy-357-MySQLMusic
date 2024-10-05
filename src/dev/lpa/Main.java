package dev.lpa;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
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

    try (Connection connection = DriverManager.getConnection( // will clause automatically later
      CONN_STRING, username, String.valueOf(password))) {

      System.out.println("Success!! Connection made to the music database");
      Arrays.fill(password, ' '); // best practice, remove password from memory immediately
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
