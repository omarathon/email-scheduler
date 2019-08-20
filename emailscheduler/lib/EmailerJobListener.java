/*
    A listener for the EmailerJob which shall log the info regarding each execution of it.
    Copyright © 2019 Omar Tanner
 */

package com.omartanner.emailscheduler.lib;

import com.omartanner.emailscheduler.Main;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.util.logging.Logger;

public class EmailerJobListener implements JobListener {
    // Logger from Main
    private Logger log;

    private String name;

    public EmailerJobListener(String name) {
        this.name = name;
        // Initialise log with Logger from Main
        this.log = Main.getLogger();
    }

    public String getName() {
        return name;
    }

    // Just before the EmailerJob is excuted
    public void jobToBeExecuted(JobExecutionContext context) {
        // Obtain Logger from Main and log the execution
        log.warning("[EmailerJobListener] Beginning execution of an EmailerJob! Attempt count: " + context.getRefireCount());
    }

    public void jobWasExecuted(JobExecutionContext context,
                               JobExecutionException jobException) {
        // Job result equal to 0 if not successful, if so log the error
        if (context.getResult().equals(0)) { // Failure
            log.severe("[EmailerJobListener] FAILURE - Failed to send email! Proceeding to log error!");
            log.severe("[ERROR STRING] " + jobException.toString());
            log.severe("[STACK TRACE] " + jobException.getStackTrace());
        }
        else  { // Otherwise successfully sent, so obtain the result which is a HashMap of the message info and log it
            log.info("[EmailerJobListener] SUCCESS - Successfully sent email!");
            log.info("[EmailerJobListener] SENT MESSAGE INFO: " + context.getResult().toString());
        }
    }

    public void jobExecutionVetoed(JobExecutionContext context) {
        // do something with the event
    }
}

