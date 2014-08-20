/**
 * Copyright 2000-2012 InterTrust LTD.
 *
 * All rights reserved.
 *
 * Visit our web-site: www.intertrust.ru.
 */
package pro.redsoft.openxml;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author V.Solovjev
 */
public class DigestServiceException extends Exception {


  public DigestServiceException(String message, Exception e) {
    super(message,e);
  }


    public DigestServiceException(String message) {
        super(message);
    }
}
