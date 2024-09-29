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

import java.lang.reflect.Type;

import org.pentaho.di.trans.steps.loadsave.getter.Getter;

public interface FieldLoadSaveValidatorFactory {
  public <T> FieldLoadSaveValidator<T> createValidator( Getter<T> getterMethod );

  public void registerValidator( String typeString, FieldLoadSaveValidator<?> validator );

  public String getName( Class<?> type, Class<?>... parameters );

  public String getName( Type type );
}
