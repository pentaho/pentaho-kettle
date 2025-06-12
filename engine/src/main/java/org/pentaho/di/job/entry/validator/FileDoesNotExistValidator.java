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

import java.io.IOException;
import java.util.List;

import org.apache.commons.validator.util.ValidatorUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

/**
 * Fails if a field's value is a filename and the file does not exist.
 *
 * @author mlowery
 */
public class FileDoesNotExistValidator extends AbstractFileValidator {

  private static final String KEY_FAIL_IF_EXISTS = "org.pentaho.di.job.entries.createfile.failIfExists";

  public static final FileDoesNotExistValidator INSTANCE = new FileDoesNotExistValidator();

  static final String VALIDATOR_NAME = "fileDoesNotExist";

  public boolean validate( Bowl bowl, CheckResultSourceInterface source, String propertyName,
    List<CheckResultInterface> remarks, ValidatorContext context ) {

    String filename = ValidatorUtils.getValueAsString( source, propertyName );
    VariableSpace variableSpace = getVariableSpace( source, propertyName, remarks, context );
    boolean failIfExists = getFailIfExists( source, propertyName, remarks, context );

    if ( null == variableSpace ) {
      return false;
    }

    String realFileName = variableSpace.environmentSubstitute( filename );
    FileObject fileObject = null;
    try {
      fileObject = KettleVFS.getInstance( bowl ).getFileObject( realFileName, variableSpace );

      if ( fileObject.exists() && failIfExists ) {
        JobEntryValidatorUtils.addFailureRemark(
          source, propertyName, VALIDATOR_NAME, remarks, JobEntryValidatorUtils.getLevelOnFail(
            context, VALIDATOR_NAME ) );
        return false;
      }
      try {
        fileObject.close(); // Just being paranoid
      } catch ( IOException ignored ) {
        // Ignore close errors
      }
    } catch ( Exception e ) {
      JobEntryValidatorUtils.addExceptionRemark( source, propertyName, VALIDATOR_NAME, remarks, e );
      return false;
    }
    return true;
  }

  public String getName() {
    return VALIDATOR_NAME;
  }

  public static ValidatorContext putFailIfExists( boolean failIfExists ) {
    ValidatorContext context = new ValidatorContext();
    context.put( KEY_FAIL_IF_EXISTS, failIfExists );
    return context;
  }

  protected boolean getFailIfExists( CheckResultSourceInterface source, String propertyName,
    List<CheckResultInterface> remarks, ValidatorContext context ) {
    Object obj = context.get( KEY_FAIL_IF_EXISTS );
    if ( obj instanceof Boolean ) {
      return (Boolean) obj;
    } else {
      // default is false
      return false;
    }
  }

  public static void putFailIfExists( ValidatorContext context, boolean failIfExists ) {
    context.put( KEY_FAIL_IF_EXISTS, failIfExists );
  }

}
