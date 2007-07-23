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