package org.folio.ed.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.dataformat.xml.XmlMapper;

@Configuration
public class XmlConfig {

  @Bean
  public JacksonXmlHttpMessageConverter jacksonXmlHttpMessageConverter() {
    XmlMapper xmlMapper = XmlMapper.builder()
      .defaultUseWrapper(false)
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .build();

    return new JacksonXmlHttpMessageConverter(xmlMapper);
  }
}
