package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Random;

/**
 * @author Andrey Khayrutdinov
 */
public class IntLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
  private final Integer sample = new Random().nextInt();

  @Override
  public Integer getTestObject() {
    return sample;
  }

  @Override
  public boolean validateTestObject( Integer original, Object actual ) {
    return original.equals( actual );
  }
}
