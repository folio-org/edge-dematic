package org.folio.ed.integration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static org.awaitility.Awaitility.await;
import static org.folio.ed.util.StagingDirectorMessageHelper.buildHeartbeatMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import lombok.extern.log4j.Log4j2;
import org.folio.ed.TestBase;
import org.folio.ed.service.RemoteStorageService;
import org.folio.ed.service.StagingDirectorIntegrationService;
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

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
@Import(MockServerConfig.class)
public class StagingDirectorIntegrationTest extends TestBase {
  private static final Pattern HEARTBEAT_PATTERN = Pattern.compile("HM\\d{19}");
  private static final Pattern TRANSACTION_RESPONSE_PATTERN = Pattern.compile("TR\\d{19}000");

  @Autowired
  private StagingDirectorIntegrationService flowsService;

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
  void shouldSendHeartbeatMessageViaPrimaryChannelAndReceiveServerResponse() {
    log.info("===== Send Heartbeat (HM) and receive response (TR): successful =====");
    Configuration configuration = buildConfiguration();

    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerPrimaryChannelOutboundGateway(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelHeartbeatPoller(configuration);

    await().atMost(1, SECONDS).untilAsserted(() -> {
      verify(serverMessageHandler).handle(matches(HEARTBEAT_PATTERN), any());
      verify(primaryChannelHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldReceiveHeartbeatMessageViaStatusChannelAndSendResponse() {
    log.info("===== Receive Heartbeat (HM) and send response (TR) : successful =====");
    serverMessageHelper.setMessage(buildHeartbeatMessage());
    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerStatusChannelFlow(buildConfiguration());

    await().atMost(1, SECONDS).untilAsserted(() -> {
      verify(statusChannelHandler).handle(matches(HEARTBEAT_PATTERN), any());
      verify(serverMessageHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any());
    });

    integrationFlowContext.remove(f1.getId());
  }

  @Test
  void shouldSendInventoryAddMessageWhenNewItemIsPresent() {
    log.info("===== Get accession queue records and send Inventory Add (IA) : successful =====");
    Configuration configuration = buildConfiguration();

    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerPrimaryChannelOutboundGateway(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelAccessionPoller(configuration);

    await().atMost(1, SECONDS).untilAsserted(() -> {
      verify(serverMessageHandler)
        .handle(matches("IA\\d{19}697685458679\\s{2}some-callnumber\\s{35}Nod\\s{32}Barnes, Adrian\\s{21}"), any());
      verify(primaryChannelHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldSetAccessionedWhenInventoryConfirmSuccessful() {
    log.info("===== Receive successful Inventory Confirm (IC) and set accessioned by barcode : successful =====");
    serverMessageHelper.setMessage("IC0000120200101121212697685458679  000");
    Configuration configuration = buildConfiguration();

    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerFeedbackChannelListener(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerStatusChannelFlow(configuration);

    await().atMost(1, SECONDS).untilAsserted(() ->
      verify(serverMessageHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any()));

    Map<String, ServeEvent> serveEvents = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    assertThat(serveEvents.size(), is(1));
    ServeEvent setAccessionEvent = serveEvents.get("/remote-storage/accessions/barcode/697685458679");
    assertThat(setAccessionEvent.getResponse().getStatus(), is(204));

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldDoNothingWhenInventoryConfirmRejected() {
    log.info("===== Receive rejected Inventory Confirm (IC) and do nothing : successful =====");
    serverMessageHelper.setMessage("IC0000120200101121212697685458679  008");
    Configuration configuration = buildConfiguration();

    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerFeedbackChannelListener(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerStatusChannelFlow(configuration);

    await().atMost(1, SECONDS).untilAsserted(() ->
      verify(serverMessageHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any()));

    Map<String, ServeEvent> serveEvents = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    // no requests expected
    assertThat(serveEvents.size(), is(0));

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldSendStatusCheckMessageWhenNewRetrievalIsPresent() {
    log.info("===== Get retrieval queue records and send Status Check (SC) : successful =====");
    Configuration configuration = buildConfiguration();

    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerPrimaryChannelOutboundGateway(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelRetrievalPoller(configuration);

    await().atMost(1, SECONDS).untilAsserted(() -> {
      verify(serverMessageHandler).handle(matches("SC\\d{19}697685458679\\s{2}"), any());
      verify(primaryChannelHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any());
    });

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
  }

  @Test
  void shouldSendPickRequestMessageSetRetrievedAndCheckInWhenStatusMessageSuccessful() {
    log.info("===== Receive successful Status Message (SM), send Pick Request (PR), set retrieval by barcode and check-in item : successful =====");
    Configuration configuration = buildConfiguration();
    remoteStorageService.getRetrievalQueueRecords(configuration.getId());
    serverMessageHelper.setMessage("SM0000120200101121212697685458679  007");

    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerFeedbackChannelListener(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelOutboundGateway(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f3 =
      flowsService.registerStatusChannelFlow(configuration);

    await().atMost(1, SECONDS).untilAsserted(() -> {
        verify(statusChannelHandler).handle(matches("SM\\d{19}697685458679\\s{2}007"), any());
        verify(serverMessageHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any());
        verify(serverMessageHandler).handle(matches("PR\\d{19}697685458679\\s{2}pickup 987654321\\s{11}Some Patron Name\\s{24}some call number\\s{34}Some title\\s{25}Some Author\\s{24}"), any());
        verify(primaryChannelHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any());
      });

    Map<String, ServeEvent> serveEvents = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    // verify set retrieval
    ServeEvent serveEvent = serveEvents.get("/remote-storage/retrievals/barcode/697685458679");
    assertThat(serveEvent.getResponse().getStatus(), is(204));

    // verify check-in
    serveEvent = serveEvents.get("/remote-storage/retrieve/de17bad7-2a30-4f1c-bee5-f653ded15629/checkInItem");
    assertThat(serveEvent.getRequest()
      .getBodyAsString(), containsString("{\"itemBarcode\":\"697685458679\"}"));

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
    integrationFlowContext.remove(f3.getId());
  }

  @Test
  void shouldDoNothingWhenStatusMessageRejected() {
    log.info("===== Receive rejected Status Message (SM) and do noting : successful =====");
    Configuration configuration = buildConfiguration();
    serverMessageHelper.setMessage("SM0000120200101121212697685458679  010");

    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerFeedbackChannelListener(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerPrimaryChannelOutboundGateway(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f3 =
      flowsService.registerStatusChannelFlow(configuration);

    await().atMost(1, SECONDS).untilAsserted(() -> {
      verify(statusChannelHandler).handle(matches("SM\\d{19}697685458679\\s{2}010"), any());
      verify(serverMessageHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any());
    });

    Map<String, ServeEvent> serveEvents = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    // no requests expected
    assertThat(serveEvents.size(), is(0));

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
    integrationFlowContext.remove(f3.getId());
  }

  @Test
  void shouldCallReturnItemWhenItemReturnedMessageReceived() {
    log.info("===== Receive Item Returned (IR) and check-in item : successful =====");
    serverMessageHelper.setMessage("IR0000120200101121212697685458679  000");
    Configuration configuration = buildConfiguration();

    IntegrationFlowContext.IntegrationFlowRegistration f1 =
      flowsService.registerFeedbackChannelListener(configuration);
    IntegrationFlowContext.IntegrationFlowRegistration f2 =
      flowsService.registerStatusChannelFlow(configuration);

    await().atMost(1, SECONDS).untilAsserted(() -> {
      verify(statusChannelHandler).handle(matches("IR\\d{19}697685458679  000"), any());
      verify(serverMessageHandler).handle(matches(TRANSACTION_RESPONSE_PATTERN), any());
    });

    Map<String, ServeEvent> serveEvents = wireMockServer.getAllServeEvents()
      .stream()
      .collect(Collectors.toMap(e -> e.getRequest()
        .getUrl(), identity()));

    assertThat(serveEvents.size(), is(1));
    ServeEvent checkInServeEvent = serveEvents.get("/remote-storage/return/de17bad7-2a30-4f1c-bee5-f653ded15629");
    assertThat(checkInServeEvent.getRequest()
      .getBodyAsString(), containsString("{\"itemBarcode\":\"697685458679\"}"));

    integrationFlowContext.remove(f1.getId());
    integrationFlowContext.remove(f2.getId());
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
