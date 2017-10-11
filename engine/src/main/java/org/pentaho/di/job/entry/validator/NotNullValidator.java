/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
