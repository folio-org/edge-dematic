package org.folio.ed.controller;

import javax.validation.Valid;

import org.folio.ed.service.RemoteStorageService;
import org.folio.rs.domain.dto.UpdateAsrItem;
import org.folio.rs.rest.resource.UpdateAsrItemStatusAvailableApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/asrService/asr/")
public class UpdateAsrItemStatusAvailableController implements UpdateAsrItemStatusAvailableApi {

  private final RemoteStorageService remoteStorageService;

  @Override
  public ResponseEntity<Void> updateAsrItem(
      @ApiParam(required = true) @PathVariable("remoteStorageConfigurationId") String remoteStorageConfigurationId,
      @ApiParam(required = true) @Valid @RequestBody UpdateAsrItem updateAsrItem) {
    remoteStorageService.checkInItemByBarcode(remoteStorageConfigurationId, updateAsrItem.getItemBarcode());
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
