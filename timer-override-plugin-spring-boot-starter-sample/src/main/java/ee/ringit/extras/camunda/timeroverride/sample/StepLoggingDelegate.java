package ee.ringit.extras.camunda.timeroverride.sample;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StepLoggingDelegate implements JavaDelegate {

  private final static Logger log = LoggerFactory.getLogger(StepLoggingDelegate.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("Executing Step {}", execution.getVariable("step"));
  }

}
