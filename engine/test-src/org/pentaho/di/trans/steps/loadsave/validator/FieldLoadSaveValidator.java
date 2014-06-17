package org.pentaho.di.trans.steps.loadsave.validator;

public interface FieldLoadSaveValidator<T> {
  public T getTestObject();

  public boolean validateTestObject( T testObject, Object actual );
}
