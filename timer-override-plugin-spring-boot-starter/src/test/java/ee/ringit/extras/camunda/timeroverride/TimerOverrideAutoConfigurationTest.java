package ee.ringit.extras.camunda.timeroverride;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TimerOverrideAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(TimerOverrideAutoConfiguration.class));

  @Test
  void configuresPluginIfEnabled() {
    this.contextRunner.withPropertyValues("camunda.bpm.timer.override.enabled=true", "debug=true").run((context) -> {
      assertThat(context).hasSingleBean(TimerOverrideProcessEnginePlugin.class);
    });
  }

  @Test
  void doesNotConfigurePluginByDefault() {
    this.contextRunner.run((context) -> {
      assertThat(context).doesNotHaveBean(TimerOverrideProcessEnginePlugin.class);
    });
  }

}
