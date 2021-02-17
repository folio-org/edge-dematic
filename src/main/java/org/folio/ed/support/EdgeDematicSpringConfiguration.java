package org.folio.ed.support;

import org.folio.ed.util.StagingDirectorSerializerDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EdgeDematicSpringConfiguration {
  @Bean
  public StagingDirectorSerializerDeserializer stagingDirectorSerializerDeserializer() {
    return new StagingDirectorSerializerDeserializer();
  }
}
