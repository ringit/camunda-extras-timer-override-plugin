package ee.ringit.extras.camunda.timeroverride;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "camunda.bpm.timer.override", name = "enabled", havingValue = "true")
@Configuration
@EnableConfigurationProperties(TimerOverrideProperties.class)
public class TimerOverrideAutoConfiguration {

  @Bean
  public TimerOverrideProcessEnginePlugin timerOverrideProcessEnginePlugin(TimerOverrideProperties props) {
    return new TimerOverrideProcessEnginePlugin(props.toConfig());
  }

}
