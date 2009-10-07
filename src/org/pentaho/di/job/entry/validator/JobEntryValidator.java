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
 * The interface of a job entry validator.
 *
 * <p>Job entry validators can provide convenience methods for adding information to the validator context.  Those
 * methods should following a naming convention: putX where X is the name of the object being adding to the context.
 * An example:
 * <ul>
 * <li>ValidatorContext putSomeObject(Object someObject)</li>
 * <li>void putSomeObject(ValidatorContext context, Object someObject)</li>
 * </ul>
 * </p>
 *
 * @author mlowery
 */
public interface JobEntryValidator {

  String KEY_LEVEL_ON_FAIL = "levelOnFail"; //$NON-NLS-1$

  /**
   * Using reflection, the validator fetches the field named <code>propertyName</code> from the bean
   * <code>source</code> and runs the validation putting any messages into <code>remarks</code>. The return value is
   * <code>true</code> if the validation passes.
   * @param source bean to validate
   * @param propertyName property to validate
   * @param remarks list to which to add messages
   * @param context any other information needed to perform the validation
   * @return validation result
   */
  boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks, ValidatorContext context);

  /**
   * Returns the name of this validator, unique among all validators.
   * @return name
   */
  String getName();
}
