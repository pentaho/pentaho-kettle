/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
  public static synchronized MessageHandler getInstance() {
    return null;
  }

  /**
   * forced override, concrete implementations must provide implementation
   *
   * @return Locale
   */
  public static synchronized Locale getLocale() {
    return null;
  }

  /**
   * forced override, concrete implementations must provide implementation
   *
   * @param newLocale
   */
  public static synchronized void setLocale( Locale newLocale ) {
  }

}
