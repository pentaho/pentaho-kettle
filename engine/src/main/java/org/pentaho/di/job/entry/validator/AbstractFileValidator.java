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
