package org.pentaho.di.trans.steps.loadsave.validator;

/**
 * @author Andrey Khayrutdinov
 */
public class EnumLoadSaveValidator<E extends Enum<E>> implements FieldLoadSaveValidator<E> {

  private final E defaultValue;

  public EnumLoadSaveValidator( E defaultValue ) {
    this.defaultValue = defaultValue;
  }

  @Override public E getTestObject() {
    return defaultValue;
  }

  @Override public boolean validateTestObject( E testObject, Object actual ) {
    return testObject == actual;
  }
}
