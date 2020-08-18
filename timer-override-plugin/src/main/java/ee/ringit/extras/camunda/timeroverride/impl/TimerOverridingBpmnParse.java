package ee.ringit.extras.camunda.timeroverride.impl;

import ee.ringit.extras.camunda.timeroverride.TimerOverrideConfiguration;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class TimerOverridingBpmnParse extends BpmnParse {

  private final static Logger log = LoggerFactory.getLogger(TimerOverridingBpmnParse.class);

  private final TimerOverrideConfiguration config;

  TimerOverridingBpmnParse(BpmnParser parser, TimerOverrideConfiguration config) {
    super(parser);
    this.config = config;
  }

  @Override
  public TimerDeclarationImpl parseTimer(Element timerEventDefinition, ActivityImpl timerActivity, String jobHandlerType) {
    TimerDeclarationType timerType = findTimerDeclarationType(timerEventDefinition);
    String timerActivityId = timerActivity.getActivityId();
    if (timerType == null) {
      log.warn("Unknown timer type for '{}' in '{}'", timerActivityId, this.name);
    } else if (config.getExcludedTimers().contains(timerActivityId)) {
      log.info("Skipping '{}' timer '{}' in '{}' because it is excluded", timerType, timerActivityId, this.name);
    } else {
      overrideDefinition(timerType, timerActivityId, timerEventDefinition);
    }
    return super.parseTimer(timerEventDefinition, timerActivity, jobHandlerType);
  }

  private void overrideDefinition(TimerDeclarationType timerType, String timerActivityId, Element timerEventDefinition) {
    String newExpression = config.getCustomTimerExpressions().get(timerActivityId);
    if (newExpression == null) {
      newExpression = config.getDefaultTimerExpressions().get(timerType);
    }
    if (newExpression != null) {
      Element expressionElement = getValueElementForType(timerEventDefinition, timerType);
      if (expressionElement != null) {
        log.info("Replacing '{}' timer '{}' definition in '{}': '{}' -> '{}'",
            timerType, timerActivityId, this.name, expressionElement.getText(), newExpression);
        replaceElementText(expressionElement, newExpression);
      }
    } else {
      log.info("Skipping '{}' timer '{}' in '{}' because no override is defined for this type of timers",
          timerType, timerActivityId, this.name);
    }
  }

  private void replaceElementText(Element element, String value) {
    Assert.notNull(value, "value cannot be null");
    Field textField = ReflectionUtils.findField(Element.class, "text", StringBuilder.class);
    Assert.state(textField != null, "Element.text field of type StringBuilder not found");
    ReflectionUtils.makeAccessible(textField);
    ReflectionUtils.setField(textField, element, new StringBuilder(value));
  }

  private static TimerDeclarationType findTimerDeclarationType(Element timerEventDefinition) {
    if (timerEventDefinition.element("timeDate") != null) {
      return TimerDeclarationType.DATE;
    } else if (timerEventDefinition.element("timeDuration") != null) {
      return TimerDeclarationType.DURATION;
    } else if (timerEventDefinition.element("timeCycle") != null) {
      return TimerDeclarationType.CYCLE;
    } else {
      return null;
    }
  }

  private static Element getValueElementForType(Element eventDefinition, TimerDeclarationType type) {
    switch (type) {
      case DATE:
        return eventDefinition.element("timeDate");
      case DURATION:
        return eventDefinition.element("timeDuration");
      case CYCLE:
        return eventDefinition.element("timeCycle");
      default:
        throw new UnsupportedOperationException("Unknown TimerDeclarationType value: " + type);
    }
  }

}
