/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ee.ringit.extras.camunda.timeroverride.sample;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;

@SpringBootApplication
@EnableProcessApplication
public class SampleCamundaBpmApplication {

  public static void main(String[] args) {
    SpringApplication.run(SampleCamundaBpmApplication.class, args);
  }

  private final Logger log = LoggerFactory.getLogger(SampleCamundaBpmApplication.class);

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private RuntimeService runtimeService;

  @Bean
  public JavaDelegate sayHelloDelegate() {
    return execution -> log.info("Hello!");
  }

  @EventListener
  public void notify(final PostDeployEvent unused) {
    final ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("Sample").singleResult();

    log.info("Found deployed process: {}", processDefinition);
    Assert.notNull(processDefinition, "process 'Sample' should be deployed!");

    log.info("Starting an instance of 'Sample' process");
    runtimeService.startProcessInstanceById(processDefinition.getId());
  }

}
