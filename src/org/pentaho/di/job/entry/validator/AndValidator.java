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

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;

/**
 * Boolean ANDs the results of all validators. If one validator fails, <code>false</code> is immediately returned. The
 * validators list (a <code>List&lt;JobEntryValidator></code>) should be stored under the <code>KEY_VALIDATORS</code>
 * key.
 *
 * @author mlowery
 */
public class AndValidator implements JobEntryValidator {

  public static final AndValidator INSTANCE = new AndValidator();

  private static final String KEY_VALIDATORS = "validators"; //$NON-NLS-1$

  private static final String VALIDATOR_NAME = "and"; //$NON-NLS-1$

  public boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context) {
    //    Object o = context.get(KEY_VALIDATORS);

    Object[] validators = (Object[]) context.get(KEY_VALIDATORS);
    for (Object validator : validators) {
      if (!((JobEntryValidator) validator).validate(source, propertyName, remarks, context)) {
        // failure remarks have already been saved
        return false;
      }
    }
    JobEntryValidatorUtils.addOkRemark(source, propertyName, remarks);
    return true;
  }

  public String getName() {
    return VALIDATOR_NAME;
  }

  public String getKeyValidators() {
    return KEY_VALIDATORS;
  }

  /**
   * Uses varargs to conveniently add validators to the list of validators consumed by <code>AndValidator</code>. This
   * method creates and returns a new context.
   * @see #putValidators(ValidatorContext, JobEntryValidator[])
   */
  public static ValidatorContext putValidators(JobEntryValidator... validators) {
    ValidatorContext context = new ValidatorContext();
    context.put(AndValidator.KEY_VALIDATORS, validators);
    return context;
  }

  /**
   * Uses varargs to conveniently add validators to the list of validators consumed by <code>AndValidator</code>. This
   * method adds to an existing map.
   * @see #putValidators(JobEntryValidator[])
   */
  public static void putValidators(ValidatorContext context, JobEntryValidator... validators) {
    context.put(AndValidator.KEY_VALIDATORS, validators);
  }

}
