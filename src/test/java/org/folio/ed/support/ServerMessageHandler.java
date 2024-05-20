package org.folio.ed.support;

import static org.folio.ed.util.MessageTypes.TRANSACTION_RESPONSE;
import static org.folio.ed.util.StagingDirectorErrorCodes.SUCCESS;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.folio.ed.util.StagingDirectorMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.GenericHandler;
import org.springframework.messaging.MessageHeaders;

public class ServerMessageHandler implements GenericHandler<String> {
  public static final String TRANSACTION_RESPONSE_MESSAGE = "TR0000120200101121212000";

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerMessageHandler.class);

  @Override
  public String handle(String payload, MessageHeaders messageHeaders) {
    LOGGER.info("Server message handler income: {}", payload);
    return "TR".equals(payload.substring(0, 2)) ? null :
      "PR".equals(payload.substring(0,2)) ? createTransactionMessage(payload) : TRANSACTION_RESPONSE_MESSAGE;
  }

  private String createTransactionMessage(String payload) {
    var payloadTran = StagingDirectorMessageHelper.extractTransactionNumber(payload);
    LOGGER.info("payloadTran " + payloadTran);
    var tranMessage = TRANSACTION_RESPONSE.getCode() +
      payloadTran +
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
      SUCCESS.getValue();
    LOGGER.info("tranMessage " + tranMessage);
    return tranMessage;
  }
}
