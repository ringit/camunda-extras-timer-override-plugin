package ee.ringit.extras.camunda.timeroverride.impl;

import ee.ringit.extras.camunda.timeroverride.TimerOverrideConfiguration;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationType.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimerOverridingBpmnParseTest {

  @Mock
  private ActivityImpl activity;

  private TimerOverridingBpmnParse parse;

  @BeforeEach
  void setUp() {
    Context.setProcessEngineConfiguration(mock(ProcessEngineConfigurationImpl.class));

    TimerOverrideConfiguration config = new TimerOverrideConfiguration();
    config.getDefaultTimerExpressions().put(DURATION, "PT1S");
    config.getDefaultTimerExpressions().put(DATE, "2020-06-20T12:00:00Z");
    config.getDefaultTimerExpressions().put(CYCLE, "R1/PT1S");
    config.getCustomTimerExpressions().put("testId2", "PT2S");
    config.getExcludedTimers().add("testId1");

    when(activity.getProcessDefinition()).thenReturn(mock(ProcessDefinitionEntity.class));

    BpmnParser bpmnParser = new BpmnParser(new ExpressionManager(), new TimerOverridingBpmnParseFactory(config));

    parse = new TimerOverridingBpmnParse(bpmnParser, config);
  }

  @ParameterizedTest
  @CsvSource({
      "timeDuration,testId3,P1D,PT1S",
      "timeDate,testId3,2030-06-20T12:00:00Z,2020-06-20T12:00:00Z",
      "timeCycle,testId3,R3/PT10H,R1/PT1S",
      "timeDuration,testId2,P1D,PT2S",
      "timeDuration,testId1,P1D,P1D",
  })
  void overridesOrPreservesTimerDefinition(String type, String id, String expression, String expectedExpression) {
    Element timeElement = new Element("", null, type, null, null);
    timeElement.appendText(expression);
    Element timerElement = new Element("", null, "timerEventDefinition", null, null);
    timerElement.add(timeElement);
    when(activity.getActivityId()).thenReturn(id);
    parse.parseTimer(timerElement, activity, "timerHandler");
    assertThat(timeElement.getText()).isEqualTo(expectedExpression);
  }

}
