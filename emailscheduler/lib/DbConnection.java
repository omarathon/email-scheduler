/*
    A class that handles the connection to the database and obtains the relevant data (the certificates that shall expire soon)
    Copyright © 2019 Omar Tanner
 */

package com.omartanner.emailscheduler.lib;

import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.util.RowFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DbConnection {
    private Database db;
    private RowFilter filter;

    // Throws: IOConnection when failing to connect to db.
    public DbConnection(String path, String password) throws IOException {
        // Construct Database object from DatabaseBuilder
        db = new DatabaseBuilder(new File(path))
                .setReadOnly(true)
                .setCodecProvider(new CryptCodecProvider("PASSWORD HIDDEN"))
                .open();

        // Construct RowFilter to obtain certificates with the property
        filter = new RowFilter() {
            @Override
            public boolean matches(Row row) {
                // Obtain expiry date
                Date expireDate = (Date) row.get("FIELD HIDDEN");
                // Construct a Calendar object set to the date we wish to warn at, which is 3 months before expire date
                Calendar warnCalendar = Calendar.getInstance();
                warnCalendar.setTime(expireDate);
                warnCalendar.add(Calendar.MONTH, -3);

                // Obtain Calendar of today#'s date
                Calendar today = Calendar.getInstance();

                // If today is after the warn date then we have exceeded the warning, thus it's a match, otherwise not a match (since before warn date)
                return today.after(warnCalendar);
            }
        };
    }

    // Throws: IOException if ailed to obtain FIELD HIDDEN Table from database
    public ArrayList<Row> getCertificates() throws IOException {
        // Apply the filter to the FIELD HIDDEN Iterable of Rows
        Iterable<Row> certificates = filter.apply(db.getTable("FIELD HIDDEN"));
        // Now put results in an ArrayList (for easier usage, doesn't increase time complexity due to previous filter being O(n) already)
        ArrayList<Row> data = new ArrayList<>();
        for (Row certificate : certificates) {
            data.add(certificate);
        }
        return data;
    }
}
