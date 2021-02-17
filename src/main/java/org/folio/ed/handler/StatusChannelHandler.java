package org.folio.ed.handler;

import static org.folio.ed.util.MessageTypes.INVENTORY_CONFIRM;
import static org.folio.ed.util.MessageTypes.ITEM_RETURNED;
import static org.folio.ed.util.StagingDirectorErrorCodes.SUCCESS;
import static org.folio.ed.util.StagingDirectorMessageHelper.buildTransactionResponseMessage;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractErrorCode;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import lombok.RequiredArgsConstructor;
import org.folio.ed.service.RemoteStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatusChannelHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(StatusChannelHandler.class);

  private final RemoteStorageService remoteStorageService;

  public Object handle(String payload, String configId) {
    LOGGER.info("Status channel handler income: {}", payload);
    if ((resolveMessageType(payload) == INVENTORY_CONFIRM) &&
      (extractErrorCode(payload) == SUCCESS)) {
      remoteStorageService.setAccessionedByBarcode(extractBarcode(payload));
    } else if (resolveMessageType(payload) == ITEM_RETURNED) {
      remoteStorageService.checkInItemByBarcode(configId, extractBarcode(payload));
    }
    return buildTransactionResponseMessage(payload);
  }
}
