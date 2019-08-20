/*
    The manifest main class.
    Copyright © 2019 Omar Tanner
 */

package com.omartanner.emailscheduler;

import com.omartanner.emailscheduler.lib.Data;
import com.omartanner.emailscheduler.lib.DbConnection;
import com.omartanner.emailscheduler.lib.EmailerJob;
import com.omartanner.emailscheduler.lib.EmailerJobListener;
import org.apache.log4j.BasicConfigurator;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import org.quartz.impl.matchers.KeyMatcher;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.*;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.*;

public class Main {
    // DbConnection Object which shall be used to obtain the data from the Access Database.
    private static DbConnection dbConnection;

    // Logger that may be accessed by other classes to log info (initialized in main)
    private static Logger log;

    public static void main(String[] args) {
        try {
            BasicConfigurator.configure();
            log = Data.getNewLogger("EmailSchedulerLog");
        }
        catch (URISyntaxException | IOException e) { // Failure obtaining logger
            throw new RuntimeException("Failure to initialise logger! Stack trace: " + e.getStackTrace());
        }

        // Attempt to obtain main properties from the JSON
        Map<String, Object> propertiesJson = null;
        try {
            propertiesJson = Data.loadMainProperties();
        } catch (URISyntaxException | FileNotFoundException e) {
            String errorStr = "Failure to load main properties JSON! Stack trace: " + e.getStackTrace().toString();
            log.severe("[Main] " + errorStr);
            throw new RuntimeException(errorStr);
        }

        // Obtain db-path and db-password from JSON to construct DbConnection object
        String dbPath= (String) propertiesJson.get("db-path");
        String dbPassword = (String) propertiesJson.get("db-password");

        // Attempt to construct DbConnection object using above auth data
        try {
            dbConnection = new DbConnection(dbPath, dbPassword);
        } catch (IOException e) { // Failure to connect to database
            String errorStr = "Failure to connect to database! Stack trace: " + e.getStackTrace().toString();
            log.severe("[Main] " + errorStr);
            throw new RuntimeException(errorStr);
        }

        // Construct emailer job
        JobDetail job = newJob(EmailerJob.class)
                .withIdentity("emailer_job", "group1")
                .build();

        // Obtain hour and minute from the JSON
        Integer hour = ((Double) propertiesJson.get("hour")).intValue();
        Integer minute = ((Double) propertiesJson.get("hour-minute")).intValue();

        // Construct daily trigger
        CronTrigger trigger = newTrigger()
                .withIdentity("daily_trigger", "group1")
                .startNow()
                .withSchedule(dailyAtHourAndMinute(hour, minute)) // fire every day at <hour>:<minute> time
                .build();

        // Attempt to obtain scheduler, initialize it and start it
        try {
            // Obtain scheduler
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();

            // Schedule job
            scheduler.scheduleJob(job, trigger);

            // Add listener
            scheduler.getListenerManager().addJobListener(new EmailerJobListener("emailer-job-listener"), KeyMatcher.keyEquals(jobKey("emailer_job", "group1")));

            // Start the scheduler
            scheduler.start();

        }
        catch (SchedulerException e) { // Failure initializing or starting scheduler
            String errorStr = "Failure initializing or starting scheduler! Stack trace: " + e.getStackTrace().toString();
            log.severe("[Main] " + errorStr);
            throw new RuntimeException(errorStr);
        }

        log.info("[Main] SUCCESS - Successfully started scheduler!");
    }

    // Accesser method for the DbConnection, utillised by the EmailerJob to obtain the data from the database
    public static DbConnection getDbConnection() {
        return dbConnection;
    }

    // Accesser method for the Logger, utilised by other classes (e.g EmailerJobListener) to log centrally
    public static Logger getLogger() {
        return log;
    }

}
