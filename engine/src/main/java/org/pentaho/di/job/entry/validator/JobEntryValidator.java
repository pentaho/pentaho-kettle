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

import java.util.List;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;

/**
 * The interface of a job entry validator.
 *
 * <p>
 * Job entry validators can provide convenience methods for adding information to the validator context. Those methods
 * should following a naming convention: putX where X is the name of the object being adding to the context. An example:
 * <ul>
 * <li>ValidatorContext putSomeObject(Object someObject)</li>
 * <li>void putSomeObject(ValidatorContext context, Object someObject)</li>
 * </ul>
 * </p>
 *
 * @author mlowery
 */
public interface JobEntryValidator {

  String KEY_LEVEL_ON_FAIL = "levelOnFail";

  /**
   * Using reflection, the validator fetches the field named <code>propertyName</code> from the bean <code>source</code>
   * and runs the validation putting any messages into <code>remarks</code>. The return value is <code>true</code> if
   * the validation passes.
   *
   * @param source
   *          bean to validate
   * @param propertyName
   *          property to validate
   * @param remarks
   *          list to which to add messages
   * @param context
   *          any other information needed to perform the validation
   * @return validation result
   */
  boolean validate( CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
    ValidatorContext context );

  /**
   * Returns the name of this validator, unique among all validators.
   *
   * @return name
   */
  String getName();
}
