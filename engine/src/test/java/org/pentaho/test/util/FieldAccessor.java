/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import junit.framework.Assert;

import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.value.ValueMetaBase;

public class FieldAccessor {

  private static final boolean ValueMetaBase_EMPTY_STRING_AND_NULL_ARE_DIFFERENT =
      ValueMetaBase.EMPTY_STRING_AND_NULL_ARE_DIFFERENT;
  private static final boolean ValueMeta_EMPTY_STRING_AND_NULL_ARE_DIFFERENT =
      ValueMeta.EMPTY_STRING_AND_NULL_ARE_DIFFERENT;

  private static final String EMPTY_AND_NULL_ARE_DIFF_FIELD = "EMPTY_STRING_AND_NULL_ARE_DIFFERENT";

  public static void ensureBooleanStaticFieldVal( Field f, boolean newValue ) throws NoSuchFieldException,
    SecurityException, IllegalArgumentException, IllegalAccessException {
    boolean value = f.getBoolean( null );
    if ( value != newValue ) {
      final boolean fieldAccessibleBak = f.isAccessible();
      Field.setAccessible( new Field[] { f }, true );

      Field modifiersField = Field.class.getDeclaredField( "modifiers" );
      final int modifiersBak = f.getModifiers();
      final int modifiersNew = modifiersBak & ~Modifier.FINAL;
      final boolean modifAccessibleBak = modifiersField.isAccessible();
      if ( modifiersBak != modifiersNew ) {
        if ( !modifAccessibleBak ) {
          modifiersField.setAccessible( true );
        }
        modifiersField.setInt( f, modifiersNew );
      }

      f.setBoolean( null, newValue );

      if ( modifiersBak != modifiersNew ) {
        modifiersField.setInt( f, modifiersBak );
        if ( !modifAccessibleBak ) {
          modifiersField.setAccessible( false );
        }
      }
      if ( !fieldAccessibleBak ) {
        Field.setAccessible( new Field[] { f }, false );
      }
    }
  }

  public static void ensureEmptyStringIsNotNull( boolean newValue ) {
    try {
      ensureBooleanStaticFieldVal( ValueMetaBase.class.getField( EMPTY_AND_NULL_ARE_DIFF_FIELD ), newValue );
      ensureBooleanStaticFieldVal( ValueMeta.class.getField( EMPTY_AND_NULL_ARE_DIFF_FIELD ), newValue );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }

    Assert.assertEquals( "ValueMetaBase", newValue, ValueMetaBase.EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
    Assert.assertEquals( "ValueMeta", newValue, ValueMeta.EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
  }

  public static void resetEmptyStringIsNotNull() throws NoSuchFieldException, IllegalAccessException {
    ensureBooleanStaticFieldVal( ValueMetaBase.class.getField( EMPTY_AND_NULL_ARE_DIFF_FIELD ),
        ValueMetaBase_EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
    ensureBooleanStaticFieldVal( ValueMeta.class.getField( EMPTY_AND_NULL_ARE_DIFF_FIELD ),
        ValueMeta_EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
  }
}
