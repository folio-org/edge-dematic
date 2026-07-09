package org.folio.ed.controller;

import org.folio.ed.service.StagingDirectorIntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReadinessController {

  private final StagingDirectorIntegrationService stagingDirectorIntegrationService;

  @GetMapping("/admin/ready")
  public ResponseEntity<String> checkReadiness() {
    if (stagingDirectorIntegrationService.isReady()) {
      return ResponseEntity.ok("READY");
    } else {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("NOT READY");
    }
  }
}
