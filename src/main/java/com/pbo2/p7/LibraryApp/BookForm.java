package com.pbo2.p7.LibraryApp;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BookForm extends JFrame {
    @SuppressWarnings("FieldMayBeFinal")
    private JTable table;
    @SuppressWarnings("FieldMayBeFinal")
    private DefaultTableModel tableModel;
    private final String[] columnNames = { "ID", "Title", "Author" };

    public BookForm() {
        setTitle("Book Manager GUI");
        setSize(600, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadDataFromAPI());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadDataFromAPI();
    }

    @SuppressWarnings({"UseSpecificCatch", "ConvertToTryWithResources", "deprecation"})
    private void loadDataFromAPI() {
        try {
            URL url = new URL("http://localhost:4567/api/books");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String json = in.lines().collect(Collectors.joining());
            in.close();

            List<Book> books = new Gson().fromJson(json, new TypeToken<List<Book>>() {
            }.getType());

            tableModel.setRowCount(0);

            for (Book book : books) {
                Object[] row = { book.getId(), book.getTitle(), book.getAuthor() };
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data:\n" + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BookForm gui;
            gui = new BookForm();
            gui.setVisible(true);
        });
    }
}
