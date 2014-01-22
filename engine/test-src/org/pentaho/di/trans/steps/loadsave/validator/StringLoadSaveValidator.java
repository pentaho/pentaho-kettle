package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.UUID;

public class StringLoadSaveValidator implements FieldLoadSaveValidator<String> {

  @Override
  public String getTestObject() {
    return UUID.randomUUID().toString();
  }

  @Override
  public boolean validateTestObject( String test, Object actual ) {
    return test.equals( actual );
  }
}
