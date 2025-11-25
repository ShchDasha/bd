import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Database
{
    private File dbFile;
    private final Map<Long, Expense> data;

    public Database() { this.data = new ConcurrentHashMap<>(); }

    public void create(String filename) throws IOException
    {
        this.dbFile = new File(filename);
        if (dbFile.exists()) throw new IOException("File already exists");
        data.clear();
        save();
    }

    public void open(String filename) throws IOException
    {
        this.dbFile = new File(filename);
        if (!dbFile.exists()) throw new FileNotFoundException("Database file not found");
        load();
    }

    public void delete() throws IOException
    {
        if (dbFile != null && dbFile.exists())
        {
            if (!dbFile.delete()) throw new IOException("Failed to delete database file");
            data.clear();
            dbFile = null;
        }
    }

    public void clear() throws IOException
    {
        data.clear();
        save();
    }

    public void save() throws IOException
    {
        if (dbFile == null) throw new IOException("No database file selected");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dbFile)))
        {
            for (Expense expense : data.values())
            {
                writer.write(expense.toCSV());
                writer.newLine();
            }
        }
    }

    private void load() throws IOException
    {
        data.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(dbFile)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.trim().isEmpty()) continue;
                Expense expense = Expense.fromCSV(line);
                data.put(expense.getPurchaseId(), expense);
            }
        }
        catch (Exception e) { throw new IOException("Invalid data format in database file", e); }
    }

    public boolean addRecord(Expense expense)
    {
        if (data.containsKey(expense.getPurchaseId())) return false;
        data.put(expense.getPurchaseId(), expense);
        return true;
    }

    public boolean deleteByKey(Long purchaseId) { return data.remove(purchaseId) != null; }

    public List<Expense> deleteByField(String fieldName, String value)
    {
        List<Expense> deleted = new ArrayList<>();
        Iterator<Map.Entry<Long, Expense>> iterator = data.entrySet().iterator();

        while (iterator.hasNext())
        {
            Map.Entry<Long, Expense> entry = iterator.next();
            Expense expense = entry.getValue();

            if (matchesField(expense, fieldName, value))
            {
                deleted.add(expense);
                iterator.remove();
            }
        }

        return deleted;
    }

    public Expense searchByKey(Long purchaseId) { return data.get(purchaseId); }

    public List<Expense> searchByField(String fieldName, String value)
    {
        List<Expense> results = new ArrayList<>();
        for (Expense expense : data.values()) if (matchesField(expense, fieldName, value)) results.add(expense);
        return results;
    }

    public boolean updateRecord(Long oldPurchaseId, Expense newExpense)
    {
        if (!oldPurchaseId.equals(newExpense.getPurchaseId()) && data.containsKey(newExpense.getPurchaseId())) return false;

        if (!oldPurchaseId.equals(newExpense.getPurchaseId())) data.remove(oldPurchaseId);

        data.put(newExpense.getPurchaseId(), newExpense);
        return true;
    }

    public void createBackup(String backupFilename) throws IOException
    {
        if (dbFile == null || !dbFile.exists()) throw new IOException("No database to backup");

        File backupFile = new File(backupFilename);
        try (FileInputStream fis = new FileInputStream(dbFile); FileOutputStream fos = new FileOutputStream(backupFile))
        {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) fos.write(buffer, 0, length);
        }
    }

    public void restoreFromBackup(String backupFilename) throws IOException
    {
        File backupFile = new File(backupFilename);
        if (!backupFile.exists()) throw new FileNotFoundException("Backup file not found");

        if (dbFile == null) dbFile = new File(backupFilename.replace(".backup", ".db"));

        try (FileInputStream fis = new FileInputStream(backupFile); FileOutputStream fos = new FileOutputStream(dbFile))
        {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) fos.write(buffer, 0, length);
        }

        load();
    }

    public List<Expense> getAllRecords() { return new ArrayList<>(data.values()); }

    private boolean matchesField(Expense expense, String fieldName, String value)
    {
        switch (fieldName.toLowerCase())
        {
            case "purchaseid": return expense.getPurchaseId().toString().equals(value);
            case "category": return expense.getCategory().equalsIgnoreCase(value);
            case "amount": return String.valueOf(expense.getAmount()).equals(value);
            case "timezone": return String.valueOf(expense.getTimezone()).equals(value);
            case "purchasetime": return expense.getPurchaseTime().toString().contains(value);
            default: return false;
        }
    }

    public boolean isOpen() { return dbFile != null && dbFile.exists(); }

    public String getFilename() { return dbFile != null ? dbFile.getName() : ""; }

    public static Long generatePurchaseId() { return System.currentTimeMillis(); }
}