# Timer Override Plugin Spring Boot Starter sample application

Run the application as a normal Spring Boot app and check out the logs.
They should print something like this:

```
2020-08-25 14:45:57.957  INFO 41409 --- [aTaskExecutor-1] e.r.e.c.t.sample.StepLoggingDelegate     : Executing Step 1
2020-08-25 14:46:13.058  INFO 41409 --- [aTaskExecutor-2] e.r.e.c.t.sample.StepLoggingDelegate     : Executing Step 2
2020-08-25 14:46:28.104  INFO 41409 --- [aTaskExecutor-3] e.r.e.c.t.sample.StepLoggingDelegate     : Executing Step 3
2020-08-25 14:46:28.126  INFO 41409 --- [aTaskExecutor-3] e.r.e.c.t.sample.StepLoggingDelegate     : Executing Step 4
2020-08-25 14:47:43.157  INFO 41409 --- [aTaskExecutor-1] e.r.e.c.t.sample.StepLoggingDelegate     : Executing Step 5
```

Timers between Step 1 and Step 2 and between Step 2 and Step 3 were overridden to 10 seconds,
resulting in a ~15 second wait due to the default job executor polling frequency of 15 second.
Step 4 continued almost immediately after Step 3 as per default overriding value.
Step 5 executed after ~1 minute as defined in the last timer, because it was excluded from overriding.    

Check out the timers in the `sample.bpmn` process definition.
Compare it to the overrides defined in `application.properties`.

