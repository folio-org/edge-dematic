package org.folio.ed.config;

import org.folio.ed.client.RemoteStorageClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfiguration {

  @Bean
  public RemoteStorageClient remoteStorageClient(
      @Qualifier("edgeHttpServiceProxyFactory") HttpServiceProxyFactory factory) {
    return factory.createClient(RemoteStorageClient.class);
  }
}
