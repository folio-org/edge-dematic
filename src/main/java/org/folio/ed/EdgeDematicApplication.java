package org.folio.ed;


import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.folio.common.utils.tls.FipsChecker.getFipsChecksResultString;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class })
@EnableAsync
@EnableCaching
@Log4j2
public class EdgeDematicApplication {
  public static void main(String[] args) {
    log.info(getFipsChecksResultString());
    SpringApplication.run(EdgeDematicApplication.class, args);
  }
}
