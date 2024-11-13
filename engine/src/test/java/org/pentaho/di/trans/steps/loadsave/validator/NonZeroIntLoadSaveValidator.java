/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
package org.pentaho.di.trans.steps.loadsave.validator;

public class NonZeroIntLoadSaveValidator extends IntLoadSaveValidator {

  public NonZeroIntLoadSaveValidator() {
    super();
  }

  public NonZeroIntLoadSaveValidator( Integer maxValue ) {
    super( maxValue );
  }

  @Override
  public Integer getTestObject() {
    Integer rtn = super.getTestObject();
    if ( rtn.intValue() == 0 ) {
      return 1;
    } else {
      return rtn;
    }
  }

}
