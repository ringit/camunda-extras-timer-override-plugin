package ee.ringit.extras.camunda.timeroverride.impl;

import ee.ringit.extras.camunda.timeroverride.TimerOverrideConfiguration;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerOverridingBpmnParseFactory implements BpmnParseFactory {

  private final static Logger log = LoggerFactory.getLogger(TimerOverridingBpmnParse.class);

  private final TimerOverrideConfiguration config;

  public TimerOverridingBpmnParseFactory(TimerOverrideConfiguration config) {
    log.warn("Overriding Camunda Process Engine timers. Timers {} have custom expression, all other timers will be overridden to {}, except {}",
        config.getCustomTimerExpressions().keySet(), config.getDefaultTimerExpressions(), config.getExcludedTimers());
    this.config = config;
  }

  public BpmnParse createBpmnParse(BpmnParser bpmnParser) {
    return new TimerOverridingBpmnParse(bpmnParser, config);
  }

}
