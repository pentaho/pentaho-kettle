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


package org.pentaho.di.job.entry.validator;

import org.apache.commons.validator.util.ValidatorUtils;

import java.util.List;

import org.apache.commons.validator.GenericTypeValidator;
import org.apache.commons.validator.GenericValidator;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;

/**
 * Fails if a field's value is not an integer.
 */
public class IntegerValidator implements JobEntryValidator {

  public static final IntegerValidator INSTANCE = new IntegerValidator();

  private String VALIDATOR_NAME = "integer";

  public boolean validate( CheckResultSourceInterface source, String propertyName,
    List<CheckResultInterface> remarks, ValidatorContext context ) {

    Object result = null;
    String value = null;

    value = ValidatorUtils.getValueAsString( source, propertyName );

    if ( GenericValidator.isBlankOrNull( value ) ) {
      return true;
    }

    result = GenericTypeValidator.formatInt( value );

    if ( result == null ) {
      JobEntryValidatorUtils.addFailureRemark( source, propertyName, VALIDATOR_NAME, remarks,
          JobEntryValidatorUtils.getLevelOnFail( context, VALIDATOR_NAME ) );
      return false;
    }
    return true;

  }

  public String getName() {
    return VALIDATOR_NAME;
  }

}
