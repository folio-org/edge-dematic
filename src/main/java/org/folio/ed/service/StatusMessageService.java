package org.folio.ed.service;

import static org.folio.ed.util.StagingDirectorErrorCodes.INVENTORY_IS_AVAILABLE;
import static org.folio.ed.util.StagingDirectorErrorCodes.SUCCESS;
import static org.folio.ed.util.StagingDirectorMessageHelper.buildTransactionResponseMessage;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractErrorCode;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import lombok.RequiredArgsConstructor;
import org.folio.ed.client.RemoteStorageClient;
import org.folio.ed.domain.dto.CheckInItem;
import org.folio.ed.util.StagingDirectorErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

//@Service
//@RequiredArgsConstructor
public class StatusMessageService {
//  private static final Logger LOGGER = LoggerFactory.getLogger(StatusMessageService.class);
//
//  private final RemoteStorageClient remoteStorageClient;
//
//  public String handle(String payload, String configurationId) {
//    switch (resolveMessageType(payload)) {
//      case PICK_COMPLETED:
//        remoteStorageClient.checkInItem(configurationId, new CheckInItem(extractBarcode(payload)));
//      case STATUS_MESSAGE:
//        LOGGER.info("Status message: itemBarcode={}, result={}", extractBarcode(payload), extractErrorCode(payload));
//        if (extractErrorCode(payload) == INVENTORY_IS_AVAILABLE)
//      default:
//        return buildTransactionResponseMessage(payload);
//    }
//  }
}
