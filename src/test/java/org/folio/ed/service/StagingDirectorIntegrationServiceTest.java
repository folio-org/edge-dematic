package org.folio.ed.service;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import org.folio.ed.TestBase;
import org.folio.ed.config.MockServerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

@Import(MockServerConfig.class)
@TestPropertySource(properties = "app.scheduling.enable=true")
@DirtiesContext
public class StagingDirectorIntegrationServiceTest extends TestBase {
  @Autowired
  private IntegrationFlowContext integrationFlowContext;

  @Autowired
  private StagingDirectorIntegrationService integrationService;

  @Test
  void shouldGetConfigurationAndCreateIntegrationFlows() {
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() ->
      assertThat(integrationFlowContext.getRegistry().size(), is(6)));
  }

  @Test
  void shouldCreateExceptionInCreateIntegrationFlows() throws Exception {

    IntegrationFlowContext integrationFlowContext1 = mock(IntegrationFlowContext.class);
    RemoteStorageService remoteStorageService = mock(RemoteStorageService.class);
    SecurityManagerService sms = mock(SecurityManagerService.class);
    StagingDirectorIntegrationService stagingDirectorIntegrationService = new StagingDirectorIntegrationService(integrationFlowContext1,remoteStorageService,null, null, null, null,sms);
    doThrow(new RuntimeException("test exception")).when(sms).getStagingDirectorTenantsUsers();

    Method privateMethod = StagingDirectorIntegrationService.class.getDeclaredMethod("createIntegrationFlows");
    privateMethod.setAccessible(true);
    privateMethod.invoke(stagingDirectorIntegrationService);

    verify(sms,times(1)).getStagingDirectorTenantsUsers();
    var res = verify(sms).getStagingDirectorTenantsUsers();
    assertEquals(null,res);
  }


  @Test
  void shouldRemoveIntegrationFlows() throws Exception {
    integrationService.removeExistingFlows();
    assertThat(integrationFlowContext.getRegistry().size(), is(0));
  }
}
