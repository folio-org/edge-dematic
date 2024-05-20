package org.folio.ed.support;

import org.folio.ed.client.logger.SensitiveDataProtectionLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;

@Configuration
public class FeignConfig {


  @Bean
  public Logger feignLogger() {
    return new SensitiveDataProtectionLogger();
  }

}
