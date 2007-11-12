/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.job.entry.validator;

import static org.apache.commons.validator.util.ValidatorUtils.getValueAsString;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.addFailureRemark;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.getLevelOnFail;

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

  private String VALIDATOR_NAME = "integer"; //$NON-NLS-1$

  public boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context) {

    Object result = null;
    String value = null;

    value = getValueAsString(source, propertyName);

    if (GenericValidator.isBlankOrNull(value)) {
      return true;
    }

    result = GenericTypeValidator.formatInt(value);

    if (result == null) {
      addFailureRemark(source, propertyName, VALIDATOR_NAME, remarks, getLevelOnFail(context, VALIDATOR_NAME));
      return false;
    }
    return true;

  }

  public String getName() {
    return VALIDATOR_NAME;
  }

}
