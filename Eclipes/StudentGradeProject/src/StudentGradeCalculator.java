import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class StudentGradeCalculator extends JFrame {

    private JTextField subjectField, markField, creditField;
    private JTable table;
    private DefaultTableModel model;
    private DecimalFormat df = new DecimalFormat("#.##");

    public StudentGradeCalculator() {
        setTitle("CGPA Calculator (Credit-weighted)");
        setSize(820, 580);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        subjectField = new JTextField();
        creditField = new JTextField();
        markField = new JTextField();

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Subject Name :"));
        inputPanel.add(subjectField);
        inputPanel.add(new JLabel("Credit (e.g. 1, 3, 4):"));
        inputPanel.add(creditField);
        inputPanel.add(new JLabel("Marks:"));
        inputPanel.add(markField);

        JButton addButton = createStyledButton("➕ Add Result", new Color(46, 204, 113));
        JButton deleteButton = createStyledButton("🗑 Delete Selected", new Color(231, 76, 60));
        JButton csvButton = createStyledButton("📂 Export CSV", new Color(52, 152, 219));
        JButton cgpaButton = createStyledButton("📊 Calculate CGPA", new Color(241, 196, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(csvButton);
        buttonPanel.add(cgpaButton);

        String[] columns = {"Subject", "Credit", "Marks", "Grade", "Result", "Point", "Credit×Point"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        JScrollPane tableScroll = new JScrollPane(table);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        addButton.addActionListener(e -> addResult());
        deleteButton.addActionListener(e -> deleteSelected());
        csvButton.addActionListener(e -> exportCSV());
        cgpaButton.addActionListener(e -> calculateCGPA());
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color hoverColor = bgColor.brighter();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void addResult() {
        String subject = subjectField.getText().trim();
        String creditStr = creditField.getText().trim();
        String markStr = markField.getText().trim();

        if (subject.isEmpty() || creditStr.isEmpty() || markStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter subject, credit and marks!");
            return;
        }

        if (!subject.matches("^[A-Za-z0-9\\s\\-]+$")) {
            JOptionPane.showMessageDialog(this, "Subject contains invalid characters!");
            return;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equalsIgnoreCase(subject)) {
                JOptionPane.showMessageDialog(this, "This subject already exists!");
                return;
            }
        }

        try {
            double credit = Double.parseDouble(creditStr);

            int marks = Integer.parseInt(markStr);

            String grade;
            double point;

            // ======== CUSTOM CREDIT-WISE GRADING SYSTEM =========
            if (credit == 1.0) {
                // 1 Credit → 25 Marks
                if (marks > 25 || marks < 0) {
                    JOptionPane.showMessageDialog(this, "1 Credit Subject Marks must be 0–25!");
                    return;
                }

                if (marks >= 19) { grade = "A+"; point = 4.0; }
                else if (marks >= 17) { grade = "A"; point = 3.5; }
                else if (marks >= 15) { grade = "B"; point = 3.0; }
                else if (marks >= 13) { grade = "C"; point = 2.5; }
                else if (marks >= 10) { grade = "D"; point = 2.0; }
                else { grade = "F"; point = 0.0; }

            } else if (credit == 4.0) {
                // 4 Credit → 100 Marks
                if (marks < 0 || marks > 100) {
                    JOptionPane.showMessageDialog(this, "4 Credit Subject Marks must be 0–100!");
                    return;
                }

                if (marks >= 80) { grade = "A+"; point = 4.0; }
                else if (marks >= 70) { grade = "A"; point = 3.5; }
                else if (marks >= 60) { grade = "B"; point = 3.0; }
                else if (marks >= 50) { grade = "C"; point = 2.5; }
                else if (marks >= 40) { grade = "D"; point = 2.0; }
                else { grade = "F"; point = 0.0; }

            } else {
                // Default 100 Mark System for other credits
                if (marks < 0 || marks > 100) {
                    JOptionPane.showMessageDialog(this, "Marks must be between 0 and 100!");
                    return;
                }

                if (marks >= 80) { grade = "A+"; point = 4.0; }
                else if (marks >= 70) { grade = "A"; point = 3.5; }
                else if (marks >= 60) { grade = "B"; point = 3.0; }
                else if (marks >= 50) { grade = "C"; point = 2.5; }
                else if (marks >= 40) { grade = "D"; point = 2.0; }
                else { grade = "F"; point = 0.0; }
            }

            String result = (point > 0) ? "Pass" : "Fail";
            double creditTimesPoint = credit * point;

            model.addRow(new Object[]{
                    subject,
                    df.format(credit),
                    marks,
                    grade,
                    result,
                    df.format(point),
                    df.format(creditTimesPoint)
            });

            subjectField.setText("");
            creditField.setText("");
            markField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid credit or mark input!");
        }
    }

    private void deleteSelected() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "No row selected!");
            return;
        }
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            model.removeRow(selectedRows[i]);
        }
    }

    private void exportCSV() {
        try {
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No data to export!");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save CSV File");
            fileChooser.setSelectedFile(new File("grades.csv"));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                FileWriter writer = new FileWriter(fileToSave);

                for (int j = 0; j < model.getColumnCount(); j++) {
                    writer.write(model.getColumnName(j));
                    if (j < model.getColumnCount() - 1) writer.write(",");
                }
                writer.write("\n");

                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        writer.write(model.getValueAt(i, j).toString());
                        if (j < model.getColumnCount() - 1) writer.write(",");
                    }
                    writer.write("\n");
                }

                writer.close();
                JOptionPane.showMessageDialog(this, "Data exported to " + fileToSave.getAbsolutePath());
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting CSV: " + e.getMessage());
        }
    }

    private void calculateCGPA() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No subjects added!");
            return;
        }

        double totalCreditTimesPoint = 0.0;
        double totalCredit = 0.0;

        try {
            for (int i = 0; i < model.getRowCount(); i++) {
                double credit = Double.parseDouble(model.getValueAt(i, 1).toString());
                double creditTimesPoint = Double.parseDouble(model.getValueAt(i, 6).toString());
                totalCredit += credit;
                totalCreditTimesPoint += creditTimesPoint;
            }

            double cgpa = totalCreditTimesPoint / totalCredit;
            JOptionPane.showMessageDialog(this, "🎓 Your CGPA is: " + df.format(cgpa));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error calculating CGPA!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentGradeCalculator().setVisible(true));
    }
}
