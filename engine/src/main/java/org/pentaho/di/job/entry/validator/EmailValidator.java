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


package org.pentaho.di.job.entry.validator;

import org.apache.commons.validator.util.ValidatorUtils;

import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;

public class EmailValidator implements JobEntryValidator {

  public static final EmailValidator INSTANCE = new EmailValidator();

  private static final String VALIDATOR_NAME = "email";

  public String getName() {
    return VALIDATOR_NAME;
  }

  public boolean validate( CheckResultSourceInterface source, String propertyName,
    List<CheckResultInterface> remarks, ValidatorContext context ) {
    String value = null;

    value = ValidatorUtils.getValueAsString( source, propertyName );

    if ( !GenericValidator.isBlankOrNull( value ) && !GenericValidator.isEmail( value ) ) {
      JobEntryValidatorUtils.addFailureRemark(
        source, propertyName, VALIDATOR_NAME, remarks, JobEntryValidatorUtils.getLevelOnFail(
          context, VALIDATOR_NAME ) );
      return false;
    } else {
      return true;
    }
  }

}
