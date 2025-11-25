import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class main extends JFrame
{
    private Database database;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private final String[] COLUMN_NAMES = {"ID покупки", "Время покупки", "Категория", "Сумма", "Часовой пояс"};

    public main()
    {
        database = new Database();
        initializeGUI();
    }

    private void initializeGUI()
    {
        setTitle("Трекер ежедневных трат");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createMenuPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(table);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("Готов к работе");
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createMenuPanel()
    {
        JPanel menuPanel = new JPanel(new FlowLayout());

        JButton createBtn = new JButton("Создать БД");
        JButton openBtn = new JButton("Открыть БД");
        JButton deleteBtn = new JButton("Удалить БД");
        JButton clearBtn = new JButton("Очистить БД");
        JButton saveBtn = new JButton("Сохранить");

        JButton addBtn = new JButton("Добавить запись");
        JButton editBtn = new JButton("Редактировать");
        JButton deleteRecordBtn = new JButton("Удалить запись");
        JButton searchBtn = new JButton("Поиск");

        JButton backupBtn = new JButton("Создать Backup");
        JButton restoreBtn = new JButton("Восстановить");
        JButton exportBtn = new JButton("Экспорт в CSV");

        createBtn.addActionListener(this::createDatabase);
        openBtn.addActionListener(this::openDatabase);
        deleteBtn.addActionListener(this::deleteDatabase);
        clearBtn.addActionListener(this::clearDatabase);
        saveBtn.addActionListener(this::saveDatabase);

        addBtn.addActionListener(this::addRecord);
        editBtn.addActionListener(this::editRecord);
        deleteRecordBtn.addActionListener(this::deleteRecord);
        searchBtn.addActionListener(this::searchRecords);

        backupBtn.addActionListener(this::createBackup);
        restoreBtn.addActionListener(this::restoreFromBackup);
        exportBtn.addActionListener(this::exportToCSV);

        menuPanel.add(createBtn);
        menuPanel.add(openBtn);
        menuPanel.add(deleteBtn);
        menuPanel.add(clearBtn);
        menuPanel.add(saveBtn);
        menuPanel.add(new JSeparator(SwingConstants.VERTICAL));
        menuPanel.add(addBtn);
        menuPanel.add(editBtn);
        menuPanel.add(deleteRecordBtn);
        menuPanel.add(searchBtn);
        menuPanel.add(new JSeparator(SwingConstants.VERTICAL));
        menuPanel.add(backupBtn);
        menuPanel.add(restoreBtn);
        menuPanel.add(exportBtn);

        return menuPanel;
    }

    private void createDatabase(ActionEvent e)
    {
        String filename = JOptionPane.showInputDialog(this, "Введите имя файла БД:", "expenses.db");
        if (filename != null && !filename.trim().isEmpty())
        {
            try
            {
                database.create(filename);
                refreshTable();
                updateStatus("База данных создана: " + filename);
            }
            catch (IOException ex) { showError("Ошибка создания БД: " + ex.getMessage()); }
        }
    }

    private void openDatabase(ActionEvent e)
    {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                database.open(fileChooser.getSelectedFile().getAbsolutePath());
                refreshTable();
                updateStatus("База данных открыта: " + database.getFilename());
            }
            catch (IOException ex) { showError("Ошибка открытия БД: " + ex.getMessage()); }
        }
    }

    private void deleteDatabase(ActionEvent e)
    {
        if (confirmAction("Удалить базу данных?"))
        {
            try
            {
                database.delete();
                refreshTable();
                updateStatus("База данных удалена");
            }
            catch (IOException ex) { showError("Ошибка удаления БД: " + ex.getMessage()); }
        }
    }

    private void clearDatabase(ActionEvent e)
    {
        if (confirmAction("Очистить базу данных?"))
        {
            try
            {
                database.clear();
                refreshTable();
                updateStatus("База данных очищена");
            }
            catch (IOException ex) { showError("Ошибка очистки БД: " + ex.getMessage()); }
        }
    }

    private void saveDatabase(ActionEvent e)
    {
        try
        {
            database.save();
            updateStatus("База данных сохранена");
        }
        catch (IOException ex) { showError("Ошибка сохранения БД: " + ex.getMessage()); }
    }

    private void addRecord(ActionEvent e)
    {
        if (!database.isOpen())
        {
            showError("Сначала откройте или создайте базу данных");
            return;
        }

        ExpenseWindows dialog = new ExpenseWindows(this, "Добавить запись", null);
        dialog.setVisible(true);

        if (dialog.isConfirmed())
        {
            Expense expense = dialog.getExpense();
            if (database.addRecord(expense))
            {
                try
                {
                    database.save();
                    refreshTable();
                    updateStatus("Запись добавлена");
                }
                catch (IOException ex) { showError("Ошибка сохранения: " + ex.getMessage()); }
            }
            else showError("Запись с таким ID покупки уже существует");
        }
    }

    private void editRecord(ActionEvent e)
    {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1)
        {
            showError("Выберите запись для редактирования");
            return;
        }

        Long purchaseId = (Long) tableModel.getValueAt(selectedRow, 0);
        Expense currentExpense = database.searchByKey(purchaseId);

        ExpenseWindows dialog = new ExpenseWindows(this, "Редактировать запись", currentExpense);
        dialog.setVisible(true);

        if (dialog.isConfirmed())
        {
            Expense updatedExpense = dialog.getExpense();
            if (database.updateRecord(purchaseId, updatedExpense))
            {
                try
                {
                    database.save();
                    refreshTable();
                    updateStatus("Запись обновлена");
                }
                catch (IOException ex) { showError("Ошибка сохранения: " + ex.getMessage()); }
            }
            else showError("Ошибка обновления записи");
        }
    }

    private void deleteRecord(ActionEvent e)
    {
        if (!database.isOpen())
        {
            showError("Сначала откройте или создайте базу данных");
            return;
        }

        String[] options = {"По ID покупки", "По категории", "По сумме", "По часовому поясу", "По времени покупки"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "Удалить по:", "Удаление записей",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice != null)
        {
            String field = getFieldFromChoice(choice);
            String value = JOptionPane.showInputDialog(this, "Введите значение для поля '" + field + "':");

            if (value != null && !value.trim().isEmpty())
            {
                if (field.equals("purchaseid"))
                {
                    try
                    {
                        Long purchaseId = Long.parseLong(value);
                        if (database.deleteByKey(purchaseId))
                        {
                            try
                            {
                                database.save();
                                refreshTable();
                                updateStatus("Запись удалена");
                            }
                            catch (IOException ex) { showError("Ошибка сохранения: " + ex.getMessage()); }
                        }
                        else showError("Запись с указанным ID покупки не найдена");
                    }
                    catch (NumberFormatException ex) { showError("Неверный формат ID человека"); }
                }
                else
                {
                    List<Expense> deleted = database.deleteByField(field, value);
                    if (!deleted.isEmpty())
                    {
                        try
                        {
                            database.save();
                            refreshTable();
                            updateStatus("Удалено записей: " + deleted.size());
                        }
                        catch (IOException ex) { showError("Ошибка сохранения: " + ex.getMessage()); }
                    }
                    else showError("Записи не найдены");
                }
            }
        }
    }

    private void searchRecords(ActionEvent e)
    {
        if (!database.isOpen())
        {
            showError("Сначала откройте или создайте базу данных");
            return;
        }

        String[] options = {"По ID покупки", "По категории", "По сумме", "По часовому поясу", "По времени покупки"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "Искать по:", "Поиск записей",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice != null)
        {
            String field = getFieldFromChoice(choice);
            String value = JOptionPane.showInputDialog(this, "Введите значение для поиска:");

            if (value != null && !value.trim().isEmpty())
            {
                List<Expense> results;
                if (field.equals("purchaseid"))
                {
                    try
                    {
                        Long purchaseId = Long.parseLong(value);
                        Expense result = database.searchByKey(purchaseId);
                        results = result != null ? List.of(result) : List.of();
                    }
                    catch (NumberFormatException ex)
                    {
                        showError("Неверный формат ID покупки");
                        return;
                    }
                }
                else results = database.searchByField(field, value);

                showSearchResults(results);
            }
        }
    }

    private void createBackup(ActionEvent e)
    {
        if (!database.isOpen()) {
            showError("Сначала откройте или создайте базу данных");
            return;
        }

        String filename = JOptionPane.showInputDialog(this, "Введите имя backup файла:", "expenses.backup");
        if (filename != null && !filename.trim().isEmpty())
        {
            try
            {
                database.createBackup(filename);
                updateStatus("Backup создан: " + filename);
            }
            catch (IOException ex) { showError("Ошибка создания backup: " + ex.getMessage()); }
        }
    }

    private void restoreFromBackup(ActionEvent e)
    {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                database.restoreFromBackup(fileChooser.getSelectedFile().getAbsolutePath());
                refreshTable();
                updateStatus("База данных восстановлена из backup");
            }
            catch (IOException ex) { showError("Ошибка восстановления: " + ex.getMessage()); }
        }
    }

    private void exportToCSV(ActionEvent e)
    {
        if (!database.isOpen())
        {
            showError("Сначала откройте или создайте базу данных");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("expenses.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.getName().toLowerCase().endsWith(".csv")) selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");

                CSVExporter.exportToCSV(database.getAllRecords(), selectedFile);
                updateStatus("Данные экспортированы в CSV: " + selectedFile.getName());
            }
            catch (IOException ex) { showError("Ошибка экспорта: " + ex.getMessage()); }
        }
    }

    private void refreshTable()
    {
        tableModel.setRowCount(0);
        if (database.isOpen())
        {
            for (Expense expense : database.getAllRecords())
            {
                tableModel.addRow(new Object[]{
                        expense.getPurchaseId(),
                        expense.getPurchaseTime(),
                        expense.getCategory(),
                        expense.getAmount(),
                        expense.getTimezone()
                });
            }
        }
    }

    private void showSearchResults(List<Expense> results)
    {
        if (results.isEmpty()) JOptionPane.showMessageDialog(this, "Записи не найдены", "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
        else
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Найдено записей: ").append(results.size()).append("\n\n");
            for (Expense expense : results) sb.append(expense.toString()).append("\n");

            JTextArea textArea = new JTextArea(sb.toString(), 15, 80);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(this, scrollPane, "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String getFieldFromChoice(String choice)
    {
        switch (choice)
        {
            case "По ID покупки": return "purchaseid";
            case "По категории": return "category";
            case "По сумме": return "amount";
            case "По часовому поясу": return "timezone";
            case "По времени покупки": return "purchasetime";
            default: return "category";
        }
    }

    private void updateStatus(String message) { statusLabel.setText(message + " | " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())); }

    private void showError(String message) { JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE); }

    private boolean confirmAction(String message)
    { return JOptionPane.showConfirmDialog(this, message, "Подтверждение", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> {new main().setVisible(true);}); }
}