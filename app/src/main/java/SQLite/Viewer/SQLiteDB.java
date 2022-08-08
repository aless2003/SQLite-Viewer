package SQLite.Viewer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class SQLiteDB {

  private static final Pattern DB_PATTERN = Pattern.compile("FROM (\\w+).*;");

  Connection conn;

  public SQLiteDB(String file) {
    System.out.println("SQLiteDB: " + file);
    Connection conn;
    try {
      String url = "jdbc:sqlite:" + file;
      conn = DriverManager.getConnection(url);

      this.conn = conn;
      System.out.println("SQLiteDB: connected to " + file);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public String[] getTables() {
    String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'";
    try {
      var result = conn.createStatement().executeQuery(sql);
      List<String> tables = new java.util.ArrayList<>();
      while (result.next()) {
        tables.add(result.getString(1));
      }
      return tables.toArray(new String[0]);
    } catch (SQLException e) {
      return new String[0];
    }
  }

  public Optional<DefaultTableModel> execute(String sql) {
    try {
      ResultSet result = conn.createStatement().executeQuery(sql);
      var matcher = DB_PATTERN.matcher(sql);
      int rowCount = 0;
      if (matcher.find()) {
        String getRowCountSql = String.format("SELECT COUNT(*) FROM %s;", matcher.group(1));
        rowCount = conn.createStatement()
            .executeQuery(getRowCountSql)
            .getInt(1);
      }
      int columnCount = result.getMetaData().getColumnCount();
      String[] columns = new String[columnCount];
      for (int i = 0; i < columns.length; i++) {
        columns[i] = result.getMetaData().getColumnName(i + 1);
      }

      String[][] data = new String[rowCount][columnCount];

      while (result.next()) {
        for (int i = 0; i < columns.length; i++) {
          data[result.getRow() - 1][i] = result.getString(i + 1);
        }
      }

      DefaultTableModel model = new DefaultTableModel(data, columns);
      return Optional.of(model);

    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, e.getMessage());
      System.err.println("SQLiteDB Exec Error: " + e.getMessage());
      return Optional.empty();
    }
  }
}
