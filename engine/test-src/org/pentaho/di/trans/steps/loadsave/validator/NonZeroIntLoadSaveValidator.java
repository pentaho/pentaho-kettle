package org.pentaho.di.trans.steps.loadsave.validator;

public class NonZeroIntLoadSaveValidator extends IntLoadSaveValidator {

  public NonZeroIntLoadSaveValidator() {
    super();
  }

  public NonZeroIntLoadSaveValidator( Integer maxValue ) {
    super( maxValue );
  }

  @Override
  public Integer getTestObject() {
    Integer rtn = super.getTestObject();
    if ( rtn.intValue() == 0 ) {
      return 1;
    } else {
      return rtn;
    }
  }

}
