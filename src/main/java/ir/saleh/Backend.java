package ir.saleh;

import de.tallerik.MySQL;
import de.tallerik.utils.Result;
import de.tallerik.utils.Row;
import de.tallerik.utils.Select;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Backend {
    public static void main(String[] args) {
        MySQL sql = new MySQL();
        sql.setHost("localhost");
        sql.setUser("saleh");
        sql.setDb("database_saleh");
        sql.setPort(3306); // Optional. Default: 3306
        sql.connect();

        showAll(sql);
    }

    private static void showAll(@NotNull MySQL sql) {
        // sql.rowSelect();
        Select select = new Select();
        select.setTable("Alerts");
        select.setColumns("*"); // Optional default '*'
        Result res = sql.rowSelect(select);
        List<Row> rowList = res.getRows();
        for(Row r : rowList) {
            System.out.println(r.get("component") + "   " + r.get("Alert") + "   " + r.get("message"));
        }
    }


}
