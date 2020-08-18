package ee.ringit.extras.camunda.timeroverride;

import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;


@Validated
@ConfigurationProperties(prefix = "camunda.bpm.timer.override")
public class TimerOverrideProperties {

  /**
   * Enables timer overrides. Disabled by default so that it would not be accidentally
   * enabled in production.
   */
  private boolean enabled = false;

  /**
   * List of timer names which will not be overridden.
   */
  @NotNull
  private final Set<@NotBlank String> excludedTimers = new HashSet<>();

  /**
   * Custom expressions for particular timers (matched by name).
   */
  @NotNull
  private final Map<@NotBlank String, @NotBlank String> customTimerExpressions = new HashMap<>();

  /**
   * Default expressions to override timers with (matched by type). Will be applied to all timers which are not
   * excluded and which do not have custom overriding expressions defined.
   */
  @NotNull
  private final Map<@NotNull TimerDeclarationType, @NotBlank String> defaultTimerExpressions = new EnumMap<>(TimerDeclarationType.class);

  {
    defaultTimerExpressions.put(TimerDeclarationType.DURATION, "PT1S");
    defaultTimerExpressions.put(TimerDeclarationType.DATE, "${now()}");
    defaultTimerExpressions.put(TimerDeclarationType.CYCLE, "R1/PT1S");
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Set<String> getExcludedTimers() {
    return excludedTimers;
  }

  public Map<String, String> getCustomTimerExpressions() {
    return customTimerExpressions;
  }

  public Map<TimerDeclarationType, String> getDefaultTimerExpressions() {
    return defaultTimerExpressions;
  }

  public TimerOverrideConfiguration toConfig() {
    TimerOverrideConfiguration config = new TimerOverrideConfiguration();
    config.getExcludedTimers().addAll(this.getExcludedTimers());
    config.getCustomTimerExpressions().putAll(this.getCustomTimerExpressions());
    config.getDefaultTimerExpressions().putAll(this.getDefaultTimerExpressions());
    return config;
  }

}
