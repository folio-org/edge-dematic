package org.folio.ed.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.folio.ed.service.StagingDirectorIntegrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ReadinessControllerTest {

  @Mock
  private StagingDirectorIntegrationService stagingDirectorIntegrationService;

  @InjectMocks
  private ReadinessController readinessController;

  @Test
  void testReadinessControllerReady() {
    when(stagingDirectorIntegrationService.isReady()).thenReturn(true);
    
    ResponseEntity<String> response = readinessController.checkReadiness();
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("READY", response.getBody());
  }

  @Test
  void testReadinessControllerNotReady() {
    when(stagingDirectorIntegrationService.isReady()).thenReturn(false);
    
    ResponseEntity<String> response = readinessController.checkReadiness();
    
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    assertEquals("NOT READY", response.getBody());
  }
}
