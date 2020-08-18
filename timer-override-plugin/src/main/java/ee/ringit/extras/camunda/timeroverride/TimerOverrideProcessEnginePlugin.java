package ee.ringit.extras.camunda.timeroverride;

import ee.ringit.extras.camunda.timeroverride.impl.TimerOverridingBpmnParseFactory;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class TimerOverrideProcessEnginePlugin extends AbstractProcessEnginePlugin {

  private static final String BPMN_PARSE_FACTORY_FIELD = "bpmnParseFactory";

  private final BpmnParseFactory bpmnParseFactory;

  public TimerOverrideProcessEnginePlugin(TimerOverrideConfiguration config) {
    this.bpmnParseFactory = new TimerOverridingBpmnParseFactory(config);
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl configuration) {
    overrideBpmParseFactory(configuration);
  }

  protected void overrideBpmParseFactory(ProcessEngineConfigurationImpl configuration) {
    Field factoryField = ReflectionUtils.findField(ProcessEngineConfigurationImpl.class, BPMN_PARSE_FACTORY_FIELD, BpmnParseFactory.class);
    Assert.state(factoryField != null,
        "ProcessEngineConfigurationImpl." + BPMN_PARSE_FACTORY_FIELD + " field not found. Unsupported Camunda BPM version?");
    ReflectionUtils.makeAccessible(factoryField);
    ReflectionUtils.setField(factoryField, configuration, bpmnParseFactory);
  }

}
