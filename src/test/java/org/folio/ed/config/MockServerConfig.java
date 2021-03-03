package org.folio.ed.config;

import org.folio.ed.support.ServerMessageHandler;
import org.folio.ed.support.ServerMessageHelper;
import org.folio.ed.util.StagingDirectorSerializerDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.event.inbound.ApplicationEventListeningMessageProducer;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.dsl.Tcp;
import org.springframework.integration.ip.dsl.TcpServerConnectionFactorySpec;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.integration.support.MessageBuilder;

@TestConfiguration
public class MockServerConfig {
  @Value("${mock.server.primary.port}")
  private int primaryPort;

  @Value("${mock.server.status.port}")
  private int statusPort;

  @Autowired
  private ServerMessageHelper serverMessageHelper;

  @Autowired
  private StagingDirectorSerializerDeserializer serializerDeserializer;

  // primary channel stub server
  @Bean
  public TcpServerConnectionFactorySpec primaryChannelFactory() {
    return Tcp.netServer(primaryPort)
      .serializer(serializerDeserializer)
      .deserializer(serializerDeserializer);
  }

  @Bean
  public IntegrationFlow primaryChannelFlow() {
    return IntegrationFlows
      .from(Tcp.inboundGateway(primaryChannelFactory()))
      .handle(String.class, serverMessageHandler())
      .get();
  }

  // status channel stub server
  @Bean
  public TcpServerConnectionFactorySpec statusChannelFactory() {
    return Tcp.netServer(statusPort)
      .serializer(serializerDeserializer)
      .deserializer(serializerDeserializer);
  }

  @Bean
  public ApplicationEventListeningMessageProducer eventsProducer() {
    ApplicationEventListeningMessageProducer producer = new ApplicationEventListeningMessageProducer();
    producer.setEventTypes(TcpConnectionOpenEvent.class);
    return producer;
  }

  @Bean
  public IntegrationFlow statusChannelOutboundFlow() {
    return IntegrationFlows.from(eventsProducer())
      .transform(e -> MessageBuilder.withPayload(serverMessageHelper.getMessage())
        .setHeader(IpHeaders.CONNECTION_ID, ((TcpConnectionOpenEvent) e).getConnectionId()).build())
      .handle(Tcp.outboundAdapter(statusChannelFactory()))
      .get();
  }

  @Bean
  public IntegrationFlow statusChannelInboundFlow() {
    return IntegrationFlows.from(Tcp.inboundAdapter(statusChannelFactory()))
      .transform(Transformers.objectToString())
      .handle(String.class, serverMessageHandler())
      .get();
  }

  @Bean
  public ServerMessageHandler serverMessageHandler() {
    return new ServerMessageHandler();
  }

  @Bean
  public ServerMessageHelper serverMessageHelper() {
    return new ServerMessageHelper();
  }
}
