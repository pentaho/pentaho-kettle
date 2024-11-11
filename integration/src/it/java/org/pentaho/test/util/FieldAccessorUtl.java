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


package org.pentaho.test.util;

import junit.framework.Assert;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.value.ValueMetaBase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldAccessorUtl {

  private static final boolean ValueMetaBase_EMPTY_STRING_AND_NULL_ARE_DIFFERENT = ValueMetaBase.convertStringToBoolean(
    Const.NVL( System.getProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" ), "N" ) );

  private static final boolean ValueMeta_EMPTY_STRING_AND_NULL_ARE_DIFFERENT = ValueMeta.EMPTY_STRING_AND_NULL_ARE_DIFFERENT;

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
          modifiersField.setAccessible( modifAccessibleBak );
        }
      }
      if ( !fieldAccessibleBak ) {
        Field.setAccessible( new Field[] { f }, fieldAccessibleBak );
      }
    }
  }

  public static void ensureEmptyStringIsNotNull( boolean newValue ) {
    try {
      ensureBooleanStaticFieldVal( ValueMetaBase.class.getField( "emptyStringAndNullAreDifferent" ), newValue );
      ensureBooleanStaticFieldVal( ValueMeta.class.getField( "EMPTY_STRING_AND_NULL_ARE_DIFFERENT" ), newValue );
    } catch ( NoSuchFieldException e ) {
      throw new RuntimeException( e );
    } catch ( SecurityException e ) {
      throw new RuntimeException( e );
    } catch ( IllegalArgumentException e ) {
      throw new RuntimeException( e );
    } catch ( IllegalAccessException e ) {
      throw new RuntimeException( e );
    }
//    Assert.assertEquals( "ValueMetaBase.EMPTY_STRING_AND_NULL_ARE_DIFFERENT", newValue,
//        ValueMetaBase.emptyStringAndNullAreDifferent.booleanValue() );
    Assert.assertEquals( "ValueMeta.EMPTY_STRING_AND_NULL_ARE_DIFFERENT", newValue,
        ValueMeta.EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
  }

  public static void resetEmptyStringIsNotNull() throws NoSuchFieldException, IllegalAccessException {
    ensureBooleanStaticFieldVal( ValueMetaBase.class.getField( "emptyStringAndNullAreDifferent" ),
        ValueMetaBase_EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
    ensureBooleanStaticFieldVal( ValueMeta.class.getField( "EMPTY_STRING_AND_NULL_ARE_DIFFERENT" ),
        ValueMeta_EMPTY_STRING_AND_NULL_ARE_DIFFERENT );
  }
}
