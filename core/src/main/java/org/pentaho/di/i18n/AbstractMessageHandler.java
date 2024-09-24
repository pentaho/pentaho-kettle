/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.i18n;

import java.util.Locale;

/**
 * Standard Message handler that takes a root package, plus key and resolves that into one/more resultant messages. This
 * Handler is used by all message types to enable flexible look and feel as well as i18n to be implemented in variable
 * ways.
 *
 * @author dhushon
 *
 */
public abstract class AbstractMessageHandler implements MessageHandler {

  /**
   * forced override to allow singleton instantiation through dynamic class loader
   *
   * @see org.pentaho.di.i18n.GlobalMessages for sample
   *
   * @return MessageHandler
   */
  public static MessageHandler getInstance() {
    return null;
  }

  /**
   * forced override, concrete implementations must provide implementation
   *
   * @return Locale
   */
  public static Locale getLocale() {
    return null;
  }

  /**
   * forced override, concrete implementations must provide implementation
   *
   * @param newLocale
   */
  public static void setLocale( Locale newLocale ) {
  }

}
