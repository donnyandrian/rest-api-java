package com.pbo2.p7.LibraryApp;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    @SuppressWarnings("FieldMayBeFinal")
    private JTextField titleField;
    @SuppressWarnings("FieldMayBeFinal")
    private JTextField authorField;

    public BookForm() {
        setTitle("Book Manager GUI");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(2, 1));

        JPanel inputPanel = new JPanel(new FlowLayout());
        titleField = new JTextField(15);
        authorField = new JTextField(15);
        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Author:"));
        inputPanel.add(authorField);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> addBookViaAPi());
        refreshButton.addActionListener(e -> loadDataFromAPI());

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);

        controlPanel.add(inputPanel);
        controlPanel.add(buttonPanel);

        add(controlPanel, BorderLayout.SOUTH);

        loadDataFromAPI();
    }

    @SuppressWarnings({ "UseSpecificCatch", "ConvertToTryWithResources", "deprecation" })
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

    @SuppressWarnings({ "UseSpecificCatch", "deprecation" })
    private void addBookViaAPi() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Judul dan Penulis harus diisi!");
            return;
        }

        try {
            URL url = new URL("http://localhost:4567/api/books");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonBody = new Gson().toJson(new Book(0, title, author));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                JOptionPane.showMessageDialog(this, "Buku berhasil ditambahkan!");
                titleField.setText("");
                authorField.setText("");
                loadDataFromAPI();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan buku. Code: " + responseCode);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error:\n" + ex.getMessage());
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
