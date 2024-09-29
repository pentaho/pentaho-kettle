/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Random;

/**
 * @author Andrey Khayrutdinov
 */
public class EnumLoadSaveValidator<E extends Enum<E>> implements FieldLoadSaveValidator<E> {

  private final Enum<E>[] values;

  public EnumLoadSaveValidator( E defaultValue ) {
    this( defaultValue.getClass() );
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  public EnumLoadSaveValidator( Class<? extends Enum> clazz ) {
    this.values = clazz.getEnumConstants();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public E getTestObject() {
    return (E) values[new Random().nextInt( values.length )];
  }

  @Override
  public boolean validateTestObject( E testObject, Object actual ) {
    return testObject == actual;
  }
}
