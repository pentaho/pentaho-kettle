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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;

/**
 * Fails if a field's value is <code>null</code>.
 *
 * @author mlowery
 */
public class NotNullValidator implements JobEntryValidator {

  public static final NotNullValidator INSTANCE = new NotNullValidator();

  private static final String VALIDATOR_NAME = "notNull"; //$NON-NLS-1$

  public boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context) {
    Object value = null;
    try {
      value = PropertyUtils.getProperty(source, propertyName);
      if (null == value) {
        JobEntryValidatorUtils.addFailureRemark(source, propertyName, VALIDATOR_NAME, remarks, JobEntryValidatorUtils.getLevelOnFail(context, VALIDATOR_NAME));
        return false;
      } else {
        return true;
      }
    } catch (IllegalAccessException e) {
      JobEntryValidatorUtils.addExceptionRemark(source, propertyName, VALIDATOR_NAME, remarks, e);
    } catch (InvocationTargetException e) {
      JobEntryValidatorUtils.addExceptionRemark(source, propertyName, VALIDATOR_NAME, remarks, e);
    } catch (NoSuchMethodException e) {
      JobEntryValidatorUtils.addExceptionRemark(source, propertyName, VALIDATOR_NAME, remarks, e);
    }
    return false;
  }

  public String getName() {
    return VALIDATOR_NAME;
  }

}
