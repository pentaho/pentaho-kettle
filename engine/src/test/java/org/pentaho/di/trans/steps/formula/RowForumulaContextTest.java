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

package org.pentaho.di.trans.steps.formula;

import org.junit.Test;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.reporting.libraries.formula.typing.Type;
import org.pentaho.reporting.libraries.formula.typing.coretypes.AnyType;
import org.pentaho.reporting.libraries.formula.typing.coretypes.NumberType;
import org.pentaho.reporting.libraries.formula.typing.coretypes.TextType;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RowForumulaContextTest {

  @Test
  public void testResolveReferenceTypeWithMetaTypeString() {
    RowMetaInterface row = mock( RowMetaInterface.class );
    ValueMetaInterface valueMeta = mock( ValueMetaInterface.class );
    RowForumulaContext context = new RowForumulaContext( row );

    String name = "name";
    when( row.searchValueMeta( name ) ).thenReturn( valueMeta );
    when( valueMeta.getType() ).thenReturn( ValueMetaInterface.TYPE_STRING );

    Type type = context.resolveReferenceType( name );
    assertTrue( type instanceof TextType );
  }

  @Test
  public void testResolveReferenceTypeWithMetaTypeInteger() {
    RowMetaInterface row = mock( RowMetaInterface.class );
    ValueMetaInterface valueMeta = mock( ValueMetaInterface.class );
    RowForumulaContext context = new RowForumulaContext( row );

    String name = "name";
    when( row.searchValueMeta( name ) ).thenReturn( valueMeta );
    when( valueMeta.getType() ).thenReturn( ValueMetaInterface.TYPE_INTEGER );

    Type type = context.resolveReferenceType( name );
    assertTrue( type instanceof NumberType );
  }

  @Test
  public void testResolveReferenceTypeWithMetaTypeNumber() {
    RowMetaInterface row = mock( RowMetaInterface.class );
    ValueMetaInterface valueMeta = mock( ValueMetaInterface.class );
    RowForumulaContext context = new RowForumulaContext( row );

    String name = "name";
    when( row.searchValueMeta( name ) ).thenReturn( valueMeta );
    when( valueMeta.getType() ).thenReturn( ValueMetaInterface.TYPE_NUMBER );

    Type type = context.resolveReferenceType( name );
    assertTrue( type instanceof NumberType );
  }

  @Test
  public void testResolveReferenceTypeWithMetaTypeBigDecimal() {
    RowMetaInterface row = mock( RowMetaInterface.class );
    ValueMetaInterface valueMeta = mock( ValueMetaInterface.class );
    RowForumulaContext context = new RowForumulaContext( row );

    String name = "name";
    when( row.searchValueMeta( name ) ).thenReturn( valueMeta );
    when( valueMeta.getType() ).thenReturn( ValueMetaInterface.TYPE_BIGNUMBER );

    Type type = context.resolveReferenceType( name );
    assertTrue( type instanceof NumberType );
  }

  @Test
  public void testResolveReferenceTypeWithMetaTypeNotStringAndNotNumeric() {
    RowMetaInterface row = mock( RowMetaInterface.class );
    ValueMetaInterface valueMeta = mock( ValueMetaInterface.class );
    RowForumulaContext context = new RowForumulaContext( row );

    String name = "name";
    when( row.searchValueMeta( name ) ).thenReturn( valueMeta );
    when( valueMeta.getType() ).thenReturn( ValueMetaInterface.TYPE_DATE );

    Type type = context.resolveReferenceType( name );
    assertTrue( type instanceof AnyType );
  }
}