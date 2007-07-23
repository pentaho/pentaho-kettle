package org.pentaho.di.job.entry.validator;

import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;

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
public class FileExistsValidator implements JobEntryValidator {

  public static final FileExistsValidator INSTANCE = new FileExistsValidator();

  private static final String KEY_VARIABLE_SPACE = "variableSpace"; //$NON-NLS-1$

  private static final String VALIDATOR_NAME = "fileExists"; //$NON-NLS-1$

  public boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context) {

    String filename = ValidatorUtils.getValueAsString(source, propertyName);
    VariableSpace variableSpace = getVariableSpace(source, propertyName, remarks, context);

    if (null == variableSpace) {
      return false;
    }

    String realFileName = variableSpace.environmentSubstitute(filename);
    FileObject fileObject = null;
    try {
      fileObject = KettleVFS.getFileObject(realFileName);
      if (fileObject == null || (fileObject != null && !fileObject.exists())) {
        JobEntryValidatorUtils.addFailureRemark(source, propertyName, VALIDATOR_NAME, remarks, JobEntryValidatorUtils
            .getLevelOnFail(context, VALIDATOR_NAME));
        return false;
      }
    } catch (Exception e) {
      JobEntryValidatorUtils.addExceptionRemark(source, propertyName, VALIDATOR_NAME, remarks, e);
      return false;
    }
    return true;
  }

  private VariableSpace getVariableSpace(CheckResultSourceInterface source, String propertyName,
      List<CheckResultInterface> remarks, ValidatorContext context) {
    Object obj = context.get(KEY_VARIABLE_SPACE);
    if (obj instanceof VariableSpace) {
      return (VariableSpace) obj;
    } else {
      JobEntryValidatorUtils.addGeneralRemark(source, propertyName, VALIDATOR_NAME, remarks,
          "messages.failed.missingKey", CheckResultInterface.TYPE_RESULT_ERROR); //$NON-NLS-1$
      return null;
    }
  }

  public String getName() {
    return VALIDATOR_NAME;
  }

  public static ValidatorContext putVariableSpace(VariableSpace variableSpace) {
    ValidatorContext context = new ValidatorContext();
    context.put(KEY_VARIABLE_SPACE, variableSpace);
    return context;
  }

  public static void putVariableSpace(ValidatorContext context, VariableSpace variableSpace) {
    context.put(KEY_VARIABLE_SPACE, variableSpace);
  }

}
