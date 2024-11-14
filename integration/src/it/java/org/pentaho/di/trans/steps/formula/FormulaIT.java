/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.formula;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

public class FormulaIT {

  public static final String stepName = "Formula";
  static final String KEY1 = "int_value";
  static final String KEY2 = "KEY2";
  static final String KEY3 = "KEY3";
  static final String[] keys = { KEY1, KEY2 };

  @BeforeClass
  public static void before() throws KettleException {
    KettleEnvironment.init();
  }

  List<RowMetaAndData> getTestRowMetaAndData( int[] value ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    Object[] row = new Object[ value.length ];
    RowMetaInterface rm = new RowMeta();
    for ( int i = 0; i < value.length; i++ ) {
      rm.addValueMeta( new ValueMetaInteger( keys[ i ] ) );
      row[ i ] = new Long( value[ i ] );
    }
    list.add( new RowMetaAndData( rm, row ) );
    return list;
  }

  List<RowMetaAndData> getTestRowMetaAndData( BigDecimal[] value ) {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    Object[] row = new Object[ value.length ];
    RowMetaInterface rm = new RowMeta();
    for ( int i = 0; i < value.length; i++ ) {
      rm.addValueMeta( new ValueMetaBigNumber( keys[ i ] ) );
      row[ i ] = value[ i ];
    }
    list.add( new RowMetaAndData( rm, row ) );
    return list;
  }

  List<RowMetaAndData> getTestRowMetaAndData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    Object[] row = new Object[] { null };
    RowMetaInterface rm = new RowMeta();
    rm.addValueMeta( new ValueMetaNumber( "n" ) );
    list.add( new RowMetaAndData( rm, row ) );
    return list;
  }


  /**
   * PDI-7923 - Formula step requires Number value type when could be used Integer. see transf_formula_error.ktr
   *
   * @throws KettleException
   */
  @Test
  public void testValueMetaIntegerConversion() throws KettleException {
    FormulaMetaFunction function =
      new FormulaMetaFunction( KEY2, "[int_value]", ValueMetaInterface.TYPE_NUMBER, -1, -1, null );

    FormulaMeta meta = new FormulaMeta();
    meta.setFormula( new FormulaMetaFunction[] { function } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );
    List<RowMetaAndData> inputList = getTestRowMetaAndData( new int[] { 13, 14 } );
    List<RowMetaAndData> ret =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
        TransTestFactory.DUMMY_STEPNAME, inputList );
    Assert.assertNotNull( "So we have some results", ret );
    Assert.assertEquals( "We have one result row", 1, ret.size() );
    RowMetaAndData rmd = ret.get( 0 );
    ValueMetaInterface resValueMeta = rmd.getValueMeta( 2 );
    Assert.assertNotNull( resValueMeta );

    Assert.assertEquals( "It was Integer value meta and now it is Number", ValueMetaInterface.TYPE_NUMBER, resValueMeta
      .getType() );
  }

  /**
   * PDI-7923 - Formula step requires Number value type when could be used Integer. see
   * sample-datagrid-truncating-numbers.ktr
   *
   * @throws KettleException
   */
  @Test
  public void testValueMetaTypeNotErased() throws KettleException {
    FormulaMetaFunction function =
      new FormulaMetaFunction( KEY2, "max([" + KEY1 + "];[" + KEY2 + "])", ValueMetaInterface.TYPE_BIGNUMBER, -1, -1,
        null );
    // Hope kettle is also uses MathContext correctly everywhere
    BigDecimal great = new BigDecimal( 999.00002, new MathContext( 7 ) );
    BigDecimal less = new BigDecimal( 999.00001, new MathContext( 7 ) );

    FormulaMeta meta = new FormulaMeta();
    meta.setFormula( new FormulaMetaFunction[] { function } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );

    List<RowMetaAndData> inputList = getTestRowMetaAndData( new BigDecimal[] { less, great } );
    List<RowMetaAndData> ret =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
        TransTestFactory.DUMMY_STEPNAME, inputList );
    Assert.assertNotNull( "So we have some results", ret );
    Assert.assertEquals( "We have one result row", 1, ret.size() );
    RowMetaAndData rmd = ret.get( 0 );
    ValueMetaInterface resValueMeta = rmd.getValueMeta( 2 );
    Assert.assertNotNull( resValueMeta );

    Assert.assertEquals( "It is still BinDecimal", ValueMetaInterface.TYPE_BIGNUMBER, resValueMeta.getType() );
    Assert.assertTrue( "So we have an a row with at least 3 not null objects", rmd.getData().length >= 3 );
    Assert.assertEquals( "Grater value is choosen correctly", great, rmd.getData()[ 2 ] );
  }

  @Test
  public void testNullReturnValueConversion() throws Exception {
    FormulaMetaFunction function =
      new FormulaMetaFunction( "if", "IF(ISNA([n]);[n];[n])", ValueMetaInterface.TYPE_NUMBER, -1, -1,
        null );

    FormulaMeta meta = new FormulaMeta();
    meta.setFormula( new FormulaMetaFunction[] { function } );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );
    List<RowMetaAndData> inputList = getTestRowMetaAndData();
    try {
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
        TransTestFactory.DUMMY_STEPNAME, inputList );
    } catch ( KettleException e ) {
      Assert.fail( "Null is not handled correctly" );
    }
  }

}
