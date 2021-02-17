package org.folio.ed.support;

import org.folio.ed.util.MessageTypes;

public class ServerMessageHelper {
  public static final String HEARTBEAT_MESSAGE = "HM0000120200101121212";
  public static final String INVENTORY_CONFIRM_MESSAGE = "IC0000120200101121212697685458679  000";
  public static final String STATUS_MESSAGE_MESSAGE = "SM0000120200101121212697685458679  007";

  private MessageTypes messageTypes;

  public String getMessage() {
    switch (messageTypes) {
      case INVENTORY_CONFIRM:
        return INVENTORY_CONFIRM_MESSAGE;
      case STATUS_MESSAGE:
        return STATUS_MESSAGE_MESSAGE;
      default:
        return HEARTBEAT_MESSAGE;
    }
  }

  public void setMessageType(MessageTypes messageTypes) {
    this.messageTypes = messageTypes;
  }
}
