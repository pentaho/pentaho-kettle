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

package org.pentaho.di.trans.steps.excelinput;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Extended {@link KettleException} to allow passing of extra context info up the chain (sheet, row, and column IDs).
 *
 * If we were really obsessive, we'd cache both the names and indexes of all the items, including the input file. But
 * this will do for a start.
 *
 * @author timh
 * @since 14-FEB-2008
 */

public class KettleCellValueException extends KettleException {

  private static Class<?> PKG = ExcelInputMeta.class; // for i18n purposes, needed by Translator2!!

  private static final long serialVersionUID = 1L;

  private final int sheetnr;
  private final int rownr;
  private final int colnr;
  private final String fieldName;

  /**
   * Standard constructor.
   * <p/>
   * <em>Note:</em> All indexes below have a 0-origin (internal index), but are reported with a 1-origin (human index).
   *
   * @param ex
   *          The Exception to wrap.
   * @param sheetnr
   *          Sheet number
   * @param rownr
   *          Row number
   * @param colnr
   *          Column number
   * @param fieldName
   *          The name of the field being converted
   */
  public KettleCellValueException( KettleException ex, int sheetnr, int rownr, int colnr, String fieldName ) {
    super( ex );
    // Note that internal indexes start at 0
    this.sheetnr = sheetnr + 1;
    this.rownr = rownr + 1;
    this.colnr = colnr + 1;
    this.fieldName = fieldName;
  }

  @Override
  public String getMessage() {
    String msgText =
      BaseMessages.getString( PKG, "KettleCellValueException.CannotConvertFieldFromCell", Integer
        .toString( sheetnr ), Integer.toString( rownr ), Integer.toString( colnr ), fieldName, super
        .getMessage() );
    return msgText;
  }

}
