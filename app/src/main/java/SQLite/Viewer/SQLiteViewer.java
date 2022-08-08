package SQLite.Viewer;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;

import SQLite.Viewer.SQLiteDB;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SQLiteViewer extends JFrame {

  private SQLiteDB db;
  private JTextArea sqlText;
  private JTable table;
  private JButton execBtn;
  private JComboBox<String> tableSelector;

  public SQLiteViewer() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(700, 900);
    setResizable(false);
    setTitle("SQLite Viewer");

    JPanel content = new JPanel(new BorderLayout());

    JPanel center = getCenter();
    JPanel north = getNorth();
    JPanel south = getSouth();

    JPanel centerContent = new JPanel(new BorderLayout());
    centerContent.add(center, NORTH);
    centerContent.add(south, CENTER);

    content.add(north, NORTH);
    content.add(centerContent, CENTER);

    add(content);

    setVisible(true);
  }

  private JPanel getCenter() {
    JPanel center = new JPanel(new BorderLayout());
    sqlText = new JTextArea();
    execBtn = new JButton("Execute");
    execBtn.setEnabled(false);

    sqlText.setName("QueryTextArea");
    sqlText.setEnabled(false);
    execBtn.setName("ExecuteQueryButton");

    execBtn.addActionListener(e -> {
      String query = sqlText.getText();
      var optModel = db.execute(query);

      optModel.ifPresent(defaultTableModel -> table.setModel(defaultTableModel));
    });

    center.add(sqlText, CENTER);
    center.add(execBtn, EAST);

    return center;
  }

  private JPanel getSouth() {
    JPanel south = new JPanel(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    table = new JTable();
    table.setName("Table");
    scrollPane.setViewportView(table);
    south.add(scrollPane, CENTER);

    return south;
  }

  private JPanel getNorth() {
    JPanel north = new JPanel(new BorderLayout());
    JTextField fileNameInput = new JTextField(8);
    JButton openBtn = new JButton("Open");

    fileNameInput.setName("FileNameTextField");
    openBtn.setName("OpenFileButton");

    north.add(fileNameInput, BorderLayout.CENTER);
    north.add(openBtn, BorderLayout.EAST);

    tableSelector = new JComboBox<>();
    tableSelector.setEnabled(false);
    tableSelector.setName("TablesComboBox");

    tableSelector.addActionListener(e -> {
      String table = (String) tableSelector.getSelectedItem();
      String query = "SELECT * FROM " + table + ";";
      sqlText.setText(query);
    });

    openBtn.addActionListener(e -> {
      String file = fileNameInput.getText();

      File f = new File(file);
      if (!f.exists()) {
        execBtn.setEnabled(false);
        sqlText.setEnabled(false);
        tableSelector.setEnabled(false);
        JOptionPane.showMessageDialog(this, "File not found");
        return;
      }

      if (file.isBlank()) {
        execBtn.setEnabled(false);
        sqlText.setEnabled(false);
        tableSelector.setEnabled(false);
        return;
      }
      execBtn.setEnabled(true);
      tableSelector.setEnabled(true);
      sqlText.setEnabled(true);
      db = new SQLiteDB(file);
      tableSelector.removeAllItems();
      Arrays.stream(db.getTables()).forEach(tableSelector::addItem);
    });

    north.add(tableSelector, BorderLayout.SOUTH);
    return north;
  }
}
