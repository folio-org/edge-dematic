package org.folio.ed.integration;

import static org.awaitility.Awaitility.await;
import static org.folio.ed.support.ServerMessageHandler.TRANSACTION_RESPONSE_MESSAGE;
import static org.folio.ed.support.ServerMessageHelper.HEARTBEAT_MESSAGE;
import static org.folio.ed.util.MessageTypes.HEARTBEAT;
import static org.folio.ed.util.MessageTypes.INVENTORY_CONFIRM;
import static org.folio.ed.util.MessageTypes.STATUS_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.folio.ed.TestBase;
import org.folio.ed.service.RemoteStorageService;
import org.folio.ed.service.StagingDirectorFlowsService;
import org.folio.ed.support.ServerMessageHandler;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.config.MockServerConfig;
import org.folio.ed.support.ServerMessageHelper;
import org.folio.ed.handler.PrimaryChannelHandler;
import org.folio.ed.handler.StatusChannelHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.context.IntegrationFlowContext;

import java.util.concurrent.TimeUnit;

@Import(MockServerConfig.class)
public class StagingDirectorTest extends TestBase {
  private static final String BARCODE = "697685458679";
  @Autowired
  private StagingDirectorFlowsService flowsService;

  @Autowired
  private IntegrationFlowContext integrationFlowContext;

  @Autowired
  private ServerMessageHelper serverMessageHelper;

  @SpyBean
  private RemoteStorageService remoteStorageService;

  @SpyBean
  private StatusChannelHandler statusChannelHandler;

  @SpyBean
  private PrimaryChannelHandler primaryChannelHandler;

  @SpyBean
  private ServerMessageHandler serverMessageHandler;

  @Test
  void shouldReceiveServerResponseOnHeartbeatMessage() {
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerPrimaryChannelOutboundGateway(buildConfiguration());
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelHeartbeatPoller(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(serverMessageHandler).handle(matches("HM\\d{19}"), any());
      verify(primaryChannelHandler).handle(eq(TRANSACTION_RESPONSE_MESSAGE), any());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldReceiveHeartbeatMessageFromStatusChannel() {
    serverMessageHelper.setMessageType(HEARTBEAT);
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerStatusChannelFlow(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(statusChannelHandler).handle(eq(HEARTBEAT_MESSAGE), any());
      verify(serverMessageHandler).handle(matches("TR\\d{19}000"), any());
    });

    integrationFlowContext.remove(f1.getId());
  }

  @Test
  void shouldSendInventoryAddMessageWhenNewItemIsPresent() {
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerPrimaryChannelOutboundGateway(buildConfiguration());
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelAccessionPoller(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(serverMessageHandler)
        .handle(matches("IA\\d{19}697685458679\\s{2}some-callnumber\\s{35}Nod\\s{32}Barnes, Adrian\\s{21}"), any());
      verify(primaryChannelHandler).handle(eq(TRANSACTION_RESPONSE_MESSAGE), any());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldSendStatusCheckMessageWhenNewRetrievalIsPresent() {
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerPrimaryChannelOutboundGateway(buildConfiguration());
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelRetrievalPoller(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      verify(serverMessageHandler).handle(matches("SC\\d{19}697685458679\\s{2}"), any());
      verify(primaryChannelHandler).handle(eq(TRANSACTION_RESPONSE_MESSAGE), any());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldSetAccessionedWhenInventoryConfirmReceived() {
    serverMessageHelper.setMessageType(INVENTORY_CONFIRM);
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerFeedbackChannelListener(buildConfiguration());
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerStatusChannelFlow(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
        verify(serverMessageHandler).handle(matches("TR\\d{19}000"), any());
        verify(remoteStorageService).setAccessionedByBarcode(BARCODE);
      });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldSendPickRequestMessageAndSetRetrievalWhenStatusMessageSucceeded() {
    remoteStorageService.getRetrievalQueueRecords(buildConfiguration().getId());
    serverMessageHelper.setMessageType(STATUS_MESSAGE);

    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerFeedbackChannelListener(buildConfiguration());
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelOutboundGateway(buildConfiguration());
    IntegrationFlowContext.IntegrationFlowRegistration f3 =
      flowsService.registerStatusChannelFlow(buildConfiguration());

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
        verify(statusChannelHandler).handle(matches("SM\\d{19}697685458679\\s{2}007"), any());
        verify(serverMessageHandler).handle(matches("PR\\d{19}697685458679\\s{2}loc\\s{4}7137806\\s{13}sample patron\\s{27}some-callnumber\\s{35}Nod\\s{32}Barnes, Adrian\\s{21}"), any());
        verify(serverMessageHandler).handle(matches("TR\\d{19}000"), any());
        verify(remoteStorageService).setRetrievalByBarcode(matches(BARCODE));
      });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
    integrationFlowContext.remove(f3.getId());
  }

  @Test
  void shouldSetReturnedWhenItemReturnedMessageReceived() {

  }

  private Configuration buildConfiguration() {
    Configuration configuration = new Configuration();
    configuration.setId("de17bad7-2a30-4f1c-bee5-f653ded15629");
    configuration.setName("RS1");
    configuration.setProviderName("Dematic_SD");
    configuration.setUrl("localhost:10001");
    configuration.setStatusUrl("localhost:10002");
    configuration.setAccessionDelay(2);
    configuration.setAccessionTimeUnit("minutes");
    return configuration;
  }
}
