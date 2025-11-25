import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter
{
    public static void exportToCSV(List<Expense> expenses, java.io.File file) throws IOException
    {
        try (FileWriter writer = new FileWriter(file))
        {
            writer.write("PurchaseID;PurchaseTime;Category;Amount;Timezone\n");
            for (Expense expense : expenses) writer.write(String.format("%d;%s;%s;%.2f;%d\n",
                        expense.getPurchaseId(),
                        expense.getPurchaseTime(),
                        expense.getCategory(),
                        expense.getAmount(),
                        expense.getTimezone()
                ));
        }
    }
}