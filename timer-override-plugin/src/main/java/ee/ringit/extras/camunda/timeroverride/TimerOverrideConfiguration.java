package ee.ringit.extras.camunda.timeroverride;

import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType;

import java.util.*;

public class TimerOverrideConfiguration {

  private final Set<String> excludedTimers = new HashSet<>();
  private final Map<String, String> customTimerExpressions = new HashMap<>();
  private final Map<TimerDeclarationType, String> defaultTimerExpressions = new EnumMap<>(TimerDeclarationType.class);

  public Set<String> getExcludedTimers() {
    return excludedTimers;
  }

  public Map<String, String> getCustomTimerExpressions() {
    return customTimerExpressions;
  }

  public Map<TimerDeclarationType, String> getDefaultTimerExpressions() {
    return defaultTimerExpressions;
  }

}
