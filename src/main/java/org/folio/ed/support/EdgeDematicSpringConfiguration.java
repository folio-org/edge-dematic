package org.folio.ed.support;

import org.folio.ed.util.StagingDirectorSerializerDeserializer;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Configuration
public class EdgeDematicSpringConfiguration {
  @Bean
  public StagingDirectorSerializerDeserializer stagingDirectorSerializerDeserializer() {
    return new StagingDirectorSerializerDeserializer();
  }

  @Bean
  public FolioSpringLiquibase folioSpringLiquibase() {
    return new FolioSpringLiquibase();
  }

  @Bean
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(new SingleConnectionDataSource());
  }
}
