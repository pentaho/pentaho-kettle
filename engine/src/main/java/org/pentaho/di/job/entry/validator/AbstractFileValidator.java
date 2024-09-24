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

import java.util.List;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.variables.VariableSpace;

public abstract class AbstractFileValidator implements JobEntryValidator {

  private static final String KEY_VARIABLE_SPACE = "org.pentaho.di.job.entries.file.variableSpace";

  public static ValidatorContext putVariableSpace( VariableSpace variableSpace ) {
    ValidatorContext context = new ValidatorContext();
    context.put( KEY_VARIABLE_SPACE, variableSpace );
    return context;
  }

  protected VariableSpace getVariableSpace( CheckResultSourceInterface source, String propertyName,
    List<CheckResultInterface> remarks, ValidatorContext context ) {
    Object obj = context.get( KEY_VARIABLE_SPACE );
    if ( obj instanceof VariableSpace ) {
      return (VariableSpace) obj;
    } else {
      JobEntryValidatorUtils.addGeneralRemark(
        source, propertyName, getName(), remarks, "messages.failed.missingKey",
        CheckResultInterface.TYPE_RESULT_ERROR );
      return null;
    }
  }

  public static void putVariableSpace( ValidatorContext context, VariableSpace variableSpace ) {
    context.put( KEY_VARIABLE_SPACE, variableSpace );
  }

  public AbstractFileValidator() {
    super();
  }

}
