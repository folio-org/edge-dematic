package org.folio.ed.handler;

import static java.util.Objects.nonNull;
import static org.folio.ed.util.MessageTypes.PICK_REQUEST;
import static org.folio.ed.util.MessageTypes.TRANSACTION_RESPONSE;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractBarcode;
import static org.folio.ed.util.StagingDirectorMessageHelper.extractTransactionNumber;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import lombok.RequiredArgsConstructor;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.service.RemoteStorageService;
import org.folio.ed.service.DematicSecurityManagerService;
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
  private final DematicSecurityManagerService sms;

  public Object handle(String payload, Configuration configuration) {
    LOGGER.info("Primary channel handler income: \"{}\"", payload);
    var configId = configuration.getId();
    if (resolveMessageType(payload) == PICK_REQUEST) {
      picksMap.put(extractTransactionNumber(payload), extractBarcode(payload));
    }
    if (resolveMessageType(payload) == TRANSACTION_RESPONSE) {
      remoteStorageService.updateLastMessageTime(configId);
      var barcode = picksMap.get(extractTransactionNumber(payload));
      if (nonNull(barcode)) {
        var tenantId = configuration.getTenantId();
        remoteStorageService.setRetrievedAsync(barcode, tenantId,
          sms.getStagingDirectorConnectionParameters(tenantId)
          .getOkapiToken().accessToken());
        picksMap.remove(barcode);
        remoteStorageService.removeBarcodeFromMap(barcode, configId);
      }
      return null;
    }
    LOGGER.info("Primary channel sending: \"{}\"", payload);
    return payload;
  }
}
