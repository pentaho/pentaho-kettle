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

import java.io.IOException;
import java.util.List;

import org.apache.commons.validator.util.ValidatorUtils;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

/**
 * Fails if a field's value is a filename and the file does not exist.
 *
 * @author mlowery
 */
public class FileDoesNotExistValidator extends AbstractFileValidator
{

  private static final String KEY_FAIL_IF_EXISTS = "org.pentaho.di.job.entries.createfile.failIfExists"; //$NON-NLS-1$

  public static final FileDoesNotExistValidator INSTANCE = new FileDoesNotExistValidator();

  static final String VALIDATOR_NAME = "fileDoesNotExist"; //$NON-NLS-1$

  public boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context)
  {

    String filename = ValidatorUtils.getValueAsString(source, propertyName);
    VariableSpace variableSpace = getVariableSpace(source, propertyName, remarks, context);
    boolean failIfExists = getFailIfExists(source, propertyName, remarks, context);

    if (null == variableSpace)
    {
      return false;
    }

    String realFileName = variableSpace.environmentSubstitute(filename);
    FileObject fileObject = null;
    try
    {
      fileObject = KettleVFS.getFileObject(realFileName, variableSpace);

      if (fileObject.exists() && failIfExists)
      {
        JobEntryValidatorUtils.addFailureRemark(source, propertyName, VALIDATOR_NAME, remarks, JobEntryValidatorUtils
            .getLevelOnFail(context, VALIDATOR_NAME));
        return false;
      }
      try
      {
        fileObject.close(); // Just being paranoid
      } catch (IOException ignored)
      {
      }
    } catch (Exception e)
    {
      JobEntryValidatorUtils.addExceptionRemark(source, propertyName, VALIDATOR_NAME, remarks, e);
      return false;
    }
    return true;
  }

  public String getName()
  {
    return VALIDATOR_NAME;
  }

  public static ValidatorContext putFailIfExists(boolean failIfExists)
  {
    ValidatorContext context = new ValidatorContext();
    context.put(KEY_FAIL_IF_EXISTS, failIfExists);
    return context;
  }

  protected boolean getFailIfExists(CheckResultSourceInterface source, String propertyName,
      List<CheckResultInterface> remarks, ValidatorContext context)
  {
    Object obj = context.get(KEY_FAIL_IF_EXISTS);
    if (obj instanceof Boolean)
    {
      return (Boolean) obj;
    } else
    {
      // default is false
      return false;
    }
  }

  public static void putFailIfExists(ValidatorContext context, boolean failIfExists)
  {
    context.put(KEY_FAIL_IF_EXISTS, failIfExists);
  }

}
