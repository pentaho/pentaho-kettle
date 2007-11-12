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
import org.pentaho.di.core.variables.VariableSpace;

public abstract class AbstractFileValidator implements JobEntryValidator
{

  private static final String KEY_VARIABLE_SPACE = "org.pentaho.di.job.entries.file.variableSpace"; //$NON-NLS-1$

  public static ValidatorContext putVariableSpace(VariableSpace variableSpace)
  {
    ValidatorContext context = new ValidatorContext();
    context.put(KEY_VARIABLE_SPACE, variableSpace);
    return context;
  }

  protected VariableSpace getVariableSpace(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks, ValidatorContext context)
  {
    Object obj = context.get(KEY_VARIABLE_SPACE);
    if (obj instanceof VariableSpace)
    {
      return (VariableSpace) obj;
    } else
    {
      JobEntryValidatorUtils.addGeneralRemark(source, propertyName, getName(), remarks,
          "messages.failed.missingKey", CheckResultInterface.TYPE_RESULT_ERROR); //$NON-NLS-1$
      return null;
    }
  }

  public static void putVariableSpace(ValidatorContext context, VariableSpace variableSpace)
  {
    context.put(KEY_VARIABLE_SPACE, variableSpace);
  }

  public AbstractFileValidator()
  {
    super();
  }

}