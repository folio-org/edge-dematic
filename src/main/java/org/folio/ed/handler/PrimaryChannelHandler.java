package org.folio.ed.handler;

import static java.util.Objects.nonNull;
import static org.folio.ed.util.MessageTypes.PICK_REQUEST;
import static org.folio.ed.util.MessageTypes.TRANSACTION_RESPONSE;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import lombok.RequiredArgsConstructor;
import org.folio.ed.service.RemoteStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PrimaryChannelHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(PrimaryChannelHandler.class);

  private final Map<String, String> picksMap = new HashMap<>();

  private final RemoteStorageService remoteStorageService;

  public Object handle(String payload, String configId) {
    LOGGER.info("Primary channel income: {}", payload);
    if (resolveMessageType(payload) == PICK_REQUEST) {
      picksMap.put(configId, extractBarcode(payload));
    }
    if (resolveMessageType(payload) == TRANSACTION_RESPONSE) {
      if (nonNull(picksMap.get(configId))) {
        remoteStorageService.setRetrievedAsync(picksMap.get(configId));
        remoteStorageService.checkInItemByBarcode(configId, picksMap.get(configId));
        picksMap.remove(configId);
      }
      return null;
    }
    return payload;
  }
}
