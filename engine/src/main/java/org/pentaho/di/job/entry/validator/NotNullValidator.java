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

  private static final String VALIDATOR_NAME = "notNull";

  public boolean validate( CheckResultSourceInterface source, String propertyName,
    List<CheckResultInterface> remarks, ValidatorContext context ) {
    Object value = null;
    try {
      value = PropertyUtils.getProperty( source, propertyName );
      if ( null == value ) {
        JobEntryValidatorUtils.addFailureRemark(
          source, propertyName, VALIDATOR_NAME, remarks, JobEntryValidatorUtils.getLevelOnFail(
            context, VALIDATOR_NAME ) );
        return false;
      } else {
        return true;
      }
    } catch ( IllegalAccessException e ) {
      JobEntryValidatorUtils.addExceptionRemark( source, propertyName, VALIDATOR_NAME, remarks, e );
    } catch ( InvocationTargetException e ) {
      JobEntryValidatorUtils.addExceptionRemark( source, propertyName, VALIDATOR_NAME, remarks, e );
    } catch ( NoSuchMethodException e ) {
      JobEntryValidatorUtils.addExceptionRemark( source, propertyName, VALIDATOR_NAME, remarks, e );
    }
    return false;
  }

  public String getName() {
    return VALIDATOR_NAME;
  }

}
