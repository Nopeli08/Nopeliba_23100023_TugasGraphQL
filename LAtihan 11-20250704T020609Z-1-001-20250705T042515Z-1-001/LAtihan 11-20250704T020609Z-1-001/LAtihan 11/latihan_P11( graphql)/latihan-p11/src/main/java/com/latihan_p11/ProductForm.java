package com.latihan_p11;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ProductForm extends JFrame {
    private JTextField tfName = new JTextField();
    private JTextField tfPrice = new JTextField();
    private JTextField tfCategory = new JTextField();

    private JTable table;
    private DefaultTableModel tableModel;

    public ProductForm() {
        setTitle("GraphQL Product Form");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Input form
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(tfName);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(tfPrice);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(tfCategory);

        // Tombol
        JButton btnAdd = new JButton("Add Product");
        JButton btnFetch = new JButton("Show All");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnFetch);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Tabel
        String[] columnNames = {"ID", "Name", "Price", "Category"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.getTableHeader().setReorderingAllowed(false);

        // Listener agar field otomatis terisi saat baris diklik
        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                tfName.setText(tableModel.getValueAt(selectedRow, 1).toString());
                tfPrice.setText(tableModel.getValueAt(selectedRow, 2).toString());
                tfCategory.setText(tableModel.getValueAt(selectedRow, 3).toString());
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Event tombol
        btnAdd.addActionListener(e -> tambahProduk());
        btnFetch.addActionListener(e -> ambilSemuaProduk());
        btnEdit.addActionListener(e -> editProduk());
        btnDelete.addActionListener(e -> hapusProduk());

        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void tambahProduk() {
        try {
            String name = tfName.getText().trim();
            String price = tfPrice.getText().trim();
            String category = tfCategory.getText().trim();

            if (name.isEmpty() || price.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua Kolom Harus Diisi!");
                return;
            }

            String query = String.format(
                "mutation { addProduct(name: \"%s\", price: %s, category: \"%s\") { id name } }",
                name, price, category
            );

            String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
            sendGraphQLRequest(jsonRequest);

            JOptionPane.showMessageDialog(this, "Produk Berhasil Ditambahkan!");
            ambilSemuaProduk();

            tfName.setText("");
            tfPrice.setText("");
            tfCategory.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Add Error: " + e.getMessage());
        }
    }

    private void ambilSemuaProduk() {
        try {
            String query = "query { allProducts { id name price category } }";
            String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
            String response = sendGraphQLRequest(jsonRequest);

            tableModel.setRowCount(0);
            Gson gson = new Gson();
            JsonObject jsonObj = gson.fromJson(response, JsonObject.class);
            JsonArray products = jsonObj.getAsJsonObject("data").getAsJsonArray("allProducts");

            for (int i = 0; i < products.size(); i++) {
                JsonObject p = products.get(i).getAsJsonObject();
                Object[] row = {
                    p.get("id").getAsString(),
                    p.get("name").getAsString(),
                    p.get("price").getAsString(),
                    p.get("category").getAsString()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fetch Error: " + e.getMessage());
        }
    }

    private void hapusProduk() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String id = tableModel.getValueAt(selectedRow, 0).toString();

            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus produk ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                // Gunakan format sesuai schema: jika ID! maka pakai tanda kutip
                String query = String.format("mutation { deleteProduct(id: \"%s\") }", id);

                String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
                String response = sendGraphQLRequest(jsonRequest);

                // Hapus baris dari JTable setelah berhasil dihapus
                tableModel.removeRow(selectedRow);

                JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Delete Error: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih produk yang ingin dihapus.");
        }
    }

    private void editProduk() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String id = tableModel.getValueAt(selectedRow, 0).toString();
            String name = tfName.getText().trim();
            String price = tfPrice.getText().trim();
            String category = tfCategory.getText().trim();

            if (name.isEmpty() || price.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi.");
                return;
            }

            try {
                String query = String.format(
                    "mutation { updateProduct(id: \"%s\", name: \"%s\", price: %s, category: \"%s\") { id name price category } }",
                    id, name, price, category
                );
                String jsonRequest = new Gson().toJson(new GraphQLQuery(query));
                String response = sendGraphQLRequest(jsonRequest);

                // Update row in JTable after successful edit
                tableModel.setValueAt(name, selectedRow, 1);
                tableModel.setValueAt(price, selectedRow, 2);
                tableModel.setValueAt(category, selectedRow, 3);

                JOptionPane.showMessageDialog(this, "Produk berhasil diperbarui!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Edit Error: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih Produk untuk diedit.");
        }

        tfName.setText("");
        tfPrice.setText("");
        tfCategory.setText("");
    }

    private String sendGraphQLRequest(String json) throws Exception {
        URL url = new URL("http://localhost:4567/graphql");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProductForm::new);
    }

        class GraphQLQuery {
        String query;

        GraphQLQuery(String query) {
            this.query = query;
        }
    }
}