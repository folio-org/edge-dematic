package org.folio.ed.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ed.domain.dto.UpdateAsrItem;
import org.folio.ed.rest.resource.UpdateASRItemStatusBeingRetrievedApi;
import org.folio.ed.service.RemoteStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/asrService/asr/")
public class UpdateASRItemStatusBeingRetrievedController implements UpdateASRItemStatusBeingRetrievedApi {

  private final RemoteStorageService remoteStorageService;

  @Override
  public ResponseEntity<Void> updateAsrItemCheckIn(
    @PathVariable("remoteStorageConfigurationId") String remoteStorageConfigurationId,
    @RequestHeader(value = "x-okapi-token") String xOkapiToken,
    @RequestHeader(value = "x-okapi-tenant") String xOkapiTenant,
    @RequestBody UpdateAsrItem updateAsrItem) {
    remoteStorageService.checkInItemByBarcode(remoteStorageConfigurationId, updateAsrItem.getItemBarcode(), xOkapiTenant,
      xOkapiToken);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
