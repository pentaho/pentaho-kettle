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

import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.util.ValidatorUtils;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;

/**
 * Fails if the field's value is either <code>null</code>, an empty string, or a string containing only whitespace.
 *
 * @author mlowery
 */
public class NotBlankValidator implements JobEntryValidator {

  public static final NotBlankValidator INSTANCE = new NotBlankValidator();

  private static final String VALIDATOR_NAME = "notBlank"; //$NON-NLS-1$

  public boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context) {
    String value = ValidatorUtils.getValueAsString(source, propertyName);
    if (GenericValidator.isBlankOrNull(value)) {
      JobEntryValidatorUtils.addFailureRemark(source, propertyName, VALIDATOR_NAME, remarks, JobEntryValidatorUtils
          .getLevelOnFail(context, VALIDATOR_NAME));
      return false;
    } else {
      return true;
    }
  }

  public String getName() {
    return VALIDATOR_NAME;
  }

}
