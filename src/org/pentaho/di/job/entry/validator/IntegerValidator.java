package org.pentaho.di.job.entry.validator;

import static org.apache.commons.validator.util.ValidatorUtils.getValueAsString;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.addFailureRemark;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.getLevelOnFail;

import java.util.List;

import org.apache.commons.validator.GenericTypeValidator;
import org.apache.commons.validator.GenericValidator;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;

/**
 * Fails if a field's value is not an integer.
 */
public class IntegerValidator implements JobEntryValidator {

  public static final IntegerValidator INSTANCE = new IntegerValidator();

  private String VALIDATOR_NAME = "integer"; //$NON-NLS-1$

  public boolean validate(CheckResultSourceInterface source, String propertyName, List<CheckResultInterface> remarks,
      ValidatorContext context) {

    Object result = null;
    String value = null;

    value = getValueAsString(source, propertyName);

    if (GenericValidator.isBlankOrNull(value)) {
      return true;
    }

    result = GenericTypeValidator.formatInt(value);

    if (result == null) {
      addFailureRemark(source, propertyName, VALIDATOR_NAME, remarks, getLevelOnFail(context, VALIDATOR_NAME));
      return false;
    }
    return true;

  }

  public String getName() {
    return VALIDATOR_NAME;
  }

}
