# Email Scheduler

Email scheduler as part of a larger commissioned project. Any sensitive code has been removed.

Schedules events via the [Quartz Enterprize Job Scheduler](http://www.quartz-scheduler.org/), and sends emails via the [JavaMail API](https://www.oracle.com/technetwork/java/javamail/index.html).

Settings input to the program are via two JSON files, and the program runs indefinitely.

## Files

The main class can be found within [emailscheduler/Main.java](emailscheduler/Main.java), which utilises the classes found within [emailscheduler/lib](emailscheduler/lib).
