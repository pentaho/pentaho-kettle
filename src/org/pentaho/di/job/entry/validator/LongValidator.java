package org.pentaho.di.job.entry.validator;

import static org.apache.commons.validator.util.ValidatorUtils.getValueAsString;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.addFailureRemark;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.getLevelOnFail;

import java.util.List;

import org.apache.commons.validator.GenericTypeValidator;
import org.apache.commons.validator.GenericValidator;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;

public class LongValidator implements JobEntryValidator
{

  public static final LongValidator INSTANCE = new LongValidator();

  private String VALIDATOR_NAME = "long"; //$NON-NLS-1$

  public String getName()
  {
    return VALIDATOR_NAME;
  }

  public boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context)
  {
    Object result = null;
    String value = null;

    value = getValueAsString(source, propertyName);

    if (GenericValidator.isBlankOrNull(value))
    {
      return Boolean.TRUE;
    }

    result = GenericTypeValidator.formatLong(value);

    if (result == null)
    {
      addFailureRemark(source, propertyName, VALIDATOR_NAME, remarks, getLevelOnFail(context, VALIDATOR_NAME));
      return false;
    }
    return true;
  }

}
