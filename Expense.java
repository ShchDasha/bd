import java.time.LocalDateTime;

public class Expense
{
    private Long purchaseId;
    private LocalDateTime purchaseTime;
    private String category;
    private double amount;
    private int timezone;

    public Expense(Long purchaseId, LocalDateTime purchaseTime, String category, double amount, int timezone)
    {
        this.purchaseId = purchaseId;
        this.purchaseTime = purchaseTime;
        this.category = category;
        this.amount = amount;
        this.timezone = timezone; }

    public static Long generatePurchaseId() { return System.currentTimeMillis(); }

    public String toCSV()
    {
        return String.join(";",
                String.valueOf(purchaseId),
                purchaseTime.toString(),
                category,
                String.valueOf(amount),
                String.valueOf(timezone) );
    }

    public static Expense fromCSV(String csvLine)
    {
        String[] parts = csvLine.split(";");
        if (parts.length != 5) throw new IllegalArgumentException("Invalid CSV format");
        return new Expense(
                Long.parseLong(parts[0]),
                LocalDateTime.parse(parts[1]),
                parts[2],
                Double.parseDouble(parts[3]),
                Integer.parseInt(parts[4]) );
    }

    public Long getPurchaseId() { return purchaseId; }
    public LocalDateTime getPurchaseTime() { return purchaseTime; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public int getTimezone() { return timezone; }

    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }
    public void setPurchaseTime(LocalDateTime purchaseTime) { this.purchaseTime = purchaseTime; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setTimezone(int timezone) { this.timezone = timezone; }

    @Override
    public String toString() { return String.format("ID: %d | Время: %s | Категория: %s | Сумма: %.2f | Часовой пояс: %d", purchaseId, purchaseTime, category, amount, timezone); }
}