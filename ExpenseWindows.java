import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;

public class ExpenseWindows extends JDialog
{
    private Expense expense;
    private boolean confirmed = false;

    private JTextField purchaseIdField;
    private JTextField purchaseTimeField;
    private JComboBox<String> categoryCombo;
    private JTextField amountField;
    private JTextField timezoneField;

    public ExpenseWindows(Frame owner, String title, Expense existingExpense)
    {
        super(owner, title, true);
        this.expense = existingExpense;
        initializeDialog();
    }

    private void initializeDialog()
    {
        setSize(400, 300);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 5, 5));

        mainPanel.add(new JLabel("ID покупки:"));
        purchaseIdField = new JTextField();
        if (expense != null) purchaseIdField.setText(String.valueOf(expense.getPurchaseId()));
        else purchaseIdField.setText(String.valueOf(Database.generatePurchaseId()));
        mainPanel.add(purchaseIdField);

        mainPanel.add(new JLabel("Время покупки:"));
        purchaseTimeField = new JTextField();
        if (expense != null) purchaseTimeField.setText(expense.getPurchaseTime().toString());
        else purchaseTimeField.setText(LocalDateTime.now().toString());
        mainPanel.add(purchaseTimeField);

        mainPanel.add(new JLabel("Категория:"));
        String[] categories = {"Еда", "Транспорт", "Развлечения", "Одежда", "Здоровье", "Образование", "Другое"};
        categoryCombo = new JComboBox<>(categories);
        if (expense != null) categoryCombo.setSelectedItem(expense.getCategory());
        mainPanel.add(categoryCombo);

        mainPanel.add(new JLabel("Сумма:"));
        amountField = new JTextField();
        if (expense != null) amountField.setText(String.valueOf(expense.getAmount()));
        mainPanel.add(amountField);

        mainPanel.add(new JLabel("Часовой пояс:"));
        timezoneField = new JTextField();
        if (expense != null) timezoneField.setText(String.valueOf(expense.getTimezone()));
        else timezoneField.setText("3");
        mainPanel.add(timezoneField);

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Отмена");
        JButton generateIdButton = new JButton("Сгенерировать ID");

        okButton.addActionListener(this::okAction);
        cancelButton.addActionListener(e -> setVisible(false));
        generateIdButton.addActionListener(e -> purchaseIdField.setText(String.valueOf(Database.generatePurchaseId())));

        buttonPanel.add(generateIdButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void okAction(ActionEvent e)
    {
        try
        {
            Long purchaseId = Long.parseLong(purchaseIdField.getText());
            LocalDateTime purchaseTime = LocalDateTime.parse(purchaseTimeField.getText());
            double amount = Double.parseDouble(amountField.getText());
            int timezone = Integer.parseInt(timezoneField.getText());

            if (amount <= 0)
            {
                JOptionPane.showMessageDialog(this, "Сумма должна быть положительной", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (timezone < -12 || timezone > 14)
            {
                JOptionPane.showMessageDialog(this, "Часовой пояс должен быть от -12 до +14", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            expense = new Expense(
                    purchaseId,
                    purchaseTime,
                    (String) categoryCombo.getSelectedItem(),
                    amount,
                    timezone );

            confirmed = true;
            setVisible(false);

        }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Неверный формат числа", "Ошибка", JOptionPane.ERROR_MESSAGE); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ошибка в данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE); }
    }

    public Expense getExpense() { return expense; }

    public boolean isConfirmed() { return confirmed; }
}