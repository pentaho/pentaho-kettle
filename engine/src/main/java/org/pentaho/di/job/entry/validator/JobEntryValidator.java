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

import java.util.List;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
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
   * @deprecated use the version with the Bowl
   */
  @Deprecated
  default boolean validate( CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context ) {
    return validate( DefaultBowl.getInstance(), source, propertyName, remarks, context );
  }

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
  default boolean validate( Bowl bowl, CheckResultSourceInterface source, String propertyName,
      List<CheckResultInterface> remarks, ValidatorContext context ) {
    return validate( source, propertyName, remarks, context );
  }

  /**
   * Returns the name of this validator, unique among all validators.
   *
   * @return name
   */
  String getName();
}
