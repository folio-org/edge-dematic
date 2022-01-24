package org.folio.ed.support;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerMessageHelper {
  private static String message;

  public static String getMessage() {
    return message;
  }

  public static void setMessage(String msg) {
    message = msg;
  }
}
