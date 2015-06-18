package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Random;

public class BooleanLoadSaveValidator implements FieldLoadSaveValidator<Boolean> {
  private final Boolean value = new Random().nextBoolean();

  @Override
  public Boolean getTestObject() {
    return value;
  }

  @Override
  public boolean validateTestObject( Boolean original, Object actual ) {
    return original.equals( actual );
  }
}
