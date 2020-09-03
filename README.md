# Timer Override Plugin

A [Camunda BPM](https://camunda.com/) [Process Engine plugin](https://docs.camunda.org/manual/7.13/user-guide/process-engine/process-engine-plugins/)
that overrides timer definitions with custom duration or due date expressions.
The primary use case is to simplify manual testing in non-production environments.
Instead of adding conditional logic to your code, use this plugin override all or individual
timers. When you need the real stuff, simply disable it.


## Getting started using Spring Boot starter

The easies way to start is to use [Spring Boot starter](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-starter).
Just add the following dependency:

**Maven**

```xml
<dependency>
    <groupId>ee.ringit.extras.camunda</groupId>
    <artifactId>timer-override-plugin-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'ee.ringit.extras.camunda:timer-override-plugin-spring-boot-starter:1.0.0'
```

Additionally, you **need to explicitly enable the plugin** by setting `camunda.bpm.timer.override.enabled=true`.
This will override all timers to default override values. Have a look at the configuration options to customize
this behaviour.


### Configuration properties

| Property                                              | Description                                                                                                         | Default                                                  |
|-------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| `camunda.bpm.timer.override.enabled`                  | Enables or disables overriding. Should be explicitly enabled for the plugin to work.                                | false                                                    |
| `camunda.bpm.timer.override.custom-timer-expressions` | Map of custom timer expression overrides, applied to timers specified by ID. Precedes default expression overrides. | empty map                                                |
| `camunda.bpm.timer.override.default-timer-expressions`    | Map of default timer expression overrides, applied to all times except the excluded ones.                           | date=${now()}<br/> duration=PT1S<br/> cycle=R1/PT1S      |
| `camunda.bpm.timer.override.excluded-timers`              | List of timer IDs to exclude from any overrides. Precedes every other property.                                | empty list                                               |

Example:

```properties
camunda.bpm.timer.override.enabled=true

camunda.bpm.timer.override.custom-timer-expressions.Timer1=PT30S
camunda.bpm.timer.override.custom-timer-expressions.Timer2=${dateTime().plusSeconds(10).toDate()}
camunda.bpm.timer.override.custom-timer-expressions.Timer3=2021-06-20T10:00:00Z
camunda.bpm.timer.override.default-timer-expressions.date=${now()}
camunda.bpm.timer.override.default-timer-expressions.duration=PT1S
camunda.bpm.timer.override.default-timer-expressions.cycle=R1/PT1S
camunda.bpm.timer.override.excluded-timers=MyExcludedTimer,AnotherExcludedTimer
```

Things to note:

1. Because overriding timer definitions by accident in production may lead to very nasty consequences,
this plugin should be enabled explicitly (`camunda.bpm.timer.override.enabled=true`).

2. All expressions specified in the properties are treated as strings and simply replace the ones in timer definitions
as you would replace them in Camunda Modeler. They will be evaluated by Camunda Process Engine at the time of execution.
Expressions should return dates or intervals in ISO 8601 as usually expected by Camunda. Read more on
[Timer Events](https://docs.camunda.org/manual/latest/reference/bpmn20/events/timer-events/).

3. The order of precedence is as follows: excluded timers -> custom timer expressions -> default timer expressions.   

4. Timer matching by ID supports neither wildcards, nor a way to specify which process the timer belongs to.
The latter means that we cannot have different overrides for timers with the same ID in different processes. 
Create an issue if you need this functionality.

5. This plugin cannot alter timer type, e.g. make a duration timer from a date timer. 


## Getting started manually

If you are not willing to use the starter, you can directly add this dependency:

```xml
<dependency>
    <groupId>ee.ringit.extras.camunda</groupId>
    <artifactId>camunda-engine-timer-override-plugin</artifactId>
    <version>1.0.0</version>
</dependency>
```

*Gradle*

```groovy
implementation 'ee.ringit.extras.camunda:timer-override-plugin:1.0.0'
```

**Please note that this plugin depends on `camunda-engine-spring` either way.**

The auto-configuration simply creates a bean of type `TimerOverrideProcessEnginePlugin`, which extends
`AbstractProcessEnginePlugin`. Camunda Process Engine with Spring Framework integration [finds all such beans](https://docs.camunda.org/manual/7.13/user-guide/spring-framework-integration/configuration/#configure-a-process-engine-plugin)
and applies them. Internally, the plugin replaces the `BpmnParseFactory` in `ProcessEngineConfigurationImpl`
with a custom one (`TimerOverridingBpmnParseFactory`). The catch is that this field has protected access.   


## Sample application

There is a sample Camunda BPM Spring Boot Starter process application showcasing this plugin.

## How it works

The plugin works by tapping into [BPMN parsing](https://github.com/camunda/camunda-bpm-platform/blob/6fd62f5c7492b98f0187f0392dc1ea2a94342cda/engine/src/main/java/org/camunda/bpm/engine/impl/bpmn/parser/BpmnParse.java)
mechanism. When a timer definition is parsed, its expression is replaced (or preserved) according to the settings.

This means that you have all the freedom of defining your own expressions as complex as you want. This switch does not
create a new version of BPMN process. Disabling or enabling this plugin requires reloading
of process definitions, e.g. via Camunda Process Engine restart.

Under the hood the plugin uses reflection to swap the timer expressions as the DOM implementation used by Camunda BPM
when parsing BPMN files is too restricted compared to something like `org.w3c.dom`.

## Alternatives

There are other ways to simplify overriding timer expressions or triggering timers on demand. 

### Configurable timer expressions

Externalize all timer expressions from BPMN, providing them at runtime from a configuration bean. For example, if 
you use Spring's property binding, then you can also use something like `TimerProperties` from the code snippet below
to configure all timer expressions:

```java
@ConfigurationProperties(prefix = "app.timers")
public class TimerProperties {

  private Map<String,String> expressions = new HashMap<>();

  // getters and setters omitted for brevity

}
```

Then, in `application.properties`:

```properties
app.timers.expressions.my-timer-id=PT1S
```

Using Spring Boot profiles, you can override default values in different environments.

Finally, use Camunda's integration with Spring context to link to those values in the BPMN. For example,
in Camunda Modeler set the "Timer definition" field to the following expression:

```
${timerProperties.expressions.my-timer-id}
```

The obvious drawbacks of this method are lost observability (can't see the actual timer duration in the BPMN)
and added boilerplate/maintenance.

### Manually triggering individual timers

This method is suitable for manual testing of individual processes. When your process
is waiting for a timer, you can trigger it to continue immediately. The catch is that there is no tool for this job
out of the box.

One way is to create a simple UI for searching and triggering timers. This is easy to do with the API provided by the
`ManagementService`. Moreover, it can be done through Camunda's REST API.   

Another way is to trigger the timer via a database update. Of course, this requires access to the database with
all the consequences that this might bring. Nevertheless, the update statement itself is very easy:

```sql
UPDATE act_ru_job
SET duedate_ = localtimestamp
WHERE type_ = 'timer'
  AND handler_cfg_ = ':YourTimerId'
  AND process_def_key_ = ':YourProcessDefinitionKey';
```

Just replace the placeholders *:YourTimerId* and *:YourProcessDefinitionKey* with the correct values. The easiest
way to get an ID of a timer is using Camunda Modeler.  

## Credits

2020 [RingIT](https://ringit.ee/)
