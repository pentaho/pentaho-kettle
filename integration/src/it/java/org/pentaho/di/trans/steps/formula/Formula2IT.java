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

package org.pentaho.di.trans.steps.formula;

import static org.pentaho.di.core.row.ValueMetaInterface.TRIM_TYPE_BOTH;
import static org.pentaho.di.core.row.ValueMetaInterface.TRIM_TYPE_LEFT;
import static org.pentaho.di.core.row.ValueMetaInterface.TRIM_TYPE_NONE;
import static org.pentaho.di.core.row.ValueMetaInterface.TRIM_TYPE_RIGHT;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.test.util.FieldAccessorUtl;

public class Formula2IT {

  public static final String stepName = "Formula";
  static final String KEY_ARG = "arg_value";
  static final String KEY_ARG2 = "arg_value2";
  static final String KEY_RESULT = "result_value";

  @BeforeClass
  public static void before() throws KettleException {
    KettleEnvironment.init();
  }

  @After
  public void after() throws KettleException, NoSuchFieldException, IllegalAccessException {
    FieldAccessorUtl.resetEmptyStringIsNotNull();
  }

  @Test
  public void testIsBlank_EmptyStringIsNotNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( true );

    final String fIsBlank = "ISBLANK([" + KEY_ARG + "])";

    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "1" }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "a" }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { null }, true );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "" }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "   " }, false );

    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 1 }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 0 }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { -1.0 }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { null }, true );
  }

  @Test
  public void testIsBlank_EmptyStringIsNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( false );

    final String fIsBlank = "ISBLANK([" + KEY_ARG + "])";
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "1" }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "a" }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { null }, true );
    // assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "" }, true ); //
    // TODO: Is it correct?
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "" }, false ); //
    // TODO: Is it correct?
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "   " }, false );

    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 1 }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 0 }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { -1.0 }, false );
    assertBooleanFormula( fIsBlank, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { null }, true );
  }

  @Test
  public void testIsText_EmptyStringIsNotNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( true );

    final String fIsText = "ISTEXT([" + KEY_ARG + "])";

    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "1" }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "a" }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { null }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "" }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "   " }, true );

    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 1 }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 0 }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { -1.0 }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { null }, false );
  }

  @Test
  public void testIsText_EmptyStringIsNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( false );

    final String fIsText = "ISTEXT([" + KEY_ARG + "])";

    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "1" }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "a" }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { null }, false );
    // assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "" }, false ); //
    // TODO: Is it correct?
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "" }, true ); //
    // TODO: Is it correct?
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "   " }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 1 }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 0 }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { -1.0 }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { null }, false );
  }

  @Test
  public void testIsNonText_EmptyStringIsNotNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( true );

    final String fIsText = "ISNONTEXT([" + KEY_ARG + "])";

    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "1" }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "a" }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { null }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "" }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "   " }, false );

    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 1 }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 0 }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { -1.0 }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { null }, true );
  }

  @Test
  public void testIsNonText_EmptyStringIsNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( false );

    final String fIsText = "ISNONTEXT([" + KEY_ARG + "])";

    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "1" }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "a" }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { null }, true );
    // assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "" }, true ); //
    // TODO: Is it correct?
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "" }, false ); //
    // TODO: Is it correct?
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaString( KEY_ARG ) ), new Object[] { "   " }, false );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 1 }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { 0 }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { -1.0 }, true );
    assertBooleanFormula( fIsText, buildRowMeta( new ValueMetaNumber( KEY_ARG ) ), new Object[] { null }, true );
  }

  @Test
  public void testConcat_EmptyStringIsNotNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( true );

    final String fConcat = "[" + KEY_ARG + "] & [" + KEY_ARG2 + "]";

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "a", "2" }, "a2" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "a", " " }, "a " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "a", "" }, "a" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "a", null }, null );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { " ", "2" }, " 2" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { " ", " " }, "  " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { " ", "" }, " " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { " ", null }, null );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "", "2" }, "2" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "", " " }, " " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "", "" }, "" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "", null }, null );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { null, "2" }, null );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { null, " " }, null );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { null, "" }, null );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { null, null }, null );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_NONE ) ), new Object[] { " a  ", "    2        " }, " a      2        " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_LEFT ) ), new Object[] { " a  ", "    2        " }, " a  2        " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), buildVMString( KEY_ARG2,
        TRIM_TYPE_RIGHT ) ), new Object[] { " a  ", "    2        " }, " a      2" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_BOTH ) ), new Object[] { " a  ", "    2        " }, " a  2" );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_BOTH ) ), new Object[] { " a  ", " " }, " a  " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_BOTH ) ), new Object[] { " a  ", "" }, " a  " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_BOTH ) ), new Object[] { " a  ", null }, null );

  }

  @Test
  public void testConcat_EmptyStringIsNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( false );

    // TODO: this is the same as testConcat_EmptyStringIsNotNull(). Is it correct?

    final String fConcat = "[" + KEY_ARG + "] & [" + KEY_ARG2 + "]";

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "a", "2" }, "a2" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "a", " " }, "a " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "a", "" }, "a" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "a", null }, null );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { " ", "2" }, " 2" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { " ", " " }, "  " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { " ", "" }, " " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { " ", null }, null );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "", "2" }, "2" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "", " " }, " " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "", "" }, "" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { "", null }, null );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { null, "2" }, null );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { null, " " }, null );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { null, "" }, null );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { null, null }, null );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_NONE ) ), new Object[] { " a  ", "    2        " }, " a      2        " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_LEFT ) ), new Object[] { " a  ", "    2        " }, " a  2        " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ), buildVMString( KEY_ARG2,
        TRIM_TYPE_RIGHT ) ), new Object[] { " a  ", "    2        " }, " a      2" );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_BOTH ) ), new Object[] { " a  ", "    2        " }, " a  2" );

    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_BOTH ) ), new Object[] { " a  ", " " }, " a  " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_BOTH ) ), new Object[] { " a  ", "" }, " a  " );
    assertStringFormula( fConcat, buildRowMeta( new ValueMetaString( KEY_ARG ),
        buildVMString( KEY_ARG2, TRIM_TYPE_BOTH ) ), new Object[] { " a  ", null }, null );

  }

  @Test
  public void testCompare_EmptyStringIsNotNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( true );

    assertBooleanOperator( "a", "=", "a", true );
    assertBooleanOperator( "a", "=", "2", false );
    assertBooleanOperator( "a", "=", " ", false );
    assertBooleanOperator( "a", "=", "", false );
    assertBooleanOperator( "a", "=", null, null );

    assertBooleanOperator( "2", "=", "a", false );
    assertBooleanOperator( "2", "=", "2", true );
    assertBooleanOperator( "2", "=", " ", false );
    assertBooleanOperator( "2", "=", "", false );
    assertBooleanOperator( "2", "=", null, null );

    assertBooleanOperator( " ", "=", "a", false );
    assertBooleanOperator( " ", "=", "2", false );
    assertBooleanOperator( " ", "=", " ", true );
    assertBooleanOperator( " ", "=", "", false );
    assertBooleanOperator( " ", "=", null, null );

    assertBooleanOperator( "", "=", "a", false );
    assertBooleanOperator( "", "=", "2", false );
    assertBooleanOperator( "", "=", " ", false );
    assertBooleanOperator( "", "=", "", true );
    assertBooleanOperator( "", "=", null, null );

    assertBooleanOperator( null, "=", "a", null );
    assertBooleanOperator( null, "=", "2", null );
    assertBooleanOperator( null, "=", " ", null );
    assertBooleanOperator( null, "=", "", null );
    assertBooleanOperator( null, "=", null, null );

    final String fEq = "[" + KEY_ARG + "] = [" + KEY_ARG2 + "]";
    assertBooleanFormula( fEq , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_RIGHT ) ), new Object[] { "  a", "a        " }, true );
    assertBooleanFormula( fEq , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_NONE ) ), new Object[] { "  ", "" }, true );

    assertBooleanOperator( "a", "<>", "a", false );
    assertBooleanOperator( "a", "<>", "2", true );
    assertBooleanOperator( "a", "<>", " ", true );
    assertBooleanOperator( "a", "<>", "", true );
    assertBooleanOperator( "a", "<>", null, null );

    assertBooleanOperator( "2", "<>", "a", true );
    assertBooleanOperator( "2", "<>", "2", false );
    assertBooleanOperator( "2", "<>", " ", true );
    assertBooleanOperator( "2", "<>", "", true );
    assertBooleanOperator( "2", "<>", null, null );

    assertBooleanOperator( " ", "<>", "a", true );
    assertBooleanOperator( " ", "<>", "2", true );
    assertBooleanOperator( " ", "<>", " ", false );
    assertBooleanOperator( " ", "<>", "", true );
    assertBooleanOperator( " ", "<>", null, null );

    assertBooleanOperator( "", "<>", "a", true );
    assertBooleanOperator( "", "<>", "2", true );
    assertBooleanOperator( "", "<>", " ", true );
    assertBooleanOperator( "", "<>", "", false );
    assertBooleanOperator( "", "<>", null, null );

    assertBooleanOperator( null, "<>", "a", null );
    assertBooleanOperator( null, "<>", "2", null );
    assertBooleanOperator( null, "<>", " ", null );
    assertBooleanOperator( null, "<>", "", null );
    assertBooleanOperator( null, "<>", null, null );

    final String fNeq = "[" + KEY_ARG + "] <> [" + KEY_ARG2 + "]";
    assertBooleanFormula( fNeq , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_RIGHT ) ), new Object[] { "  a", "a        " }, false );
    assertBooleanFormula( fNeq , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_NONE ) ), new Object[] { "  ", "" }, false );

    assertBooleanOperator( "a", "<", "a", false );
    assertBooleanOperator( "a", "<", "2", false );
    assertBooleanOperator( "a", "<", " ", false );
    assertBooleanOperator( "a", "<", "", false );
    assertBooleanOperator( "a", "<", null, null );

    assertBooleanOperator( "2", "<", "a", true );
    assertBooleanOperator( "2", "<", "2", false );
    assertBooleanOperator( "2", "<", " ", false );
    assertBooleanOperator( "2", "<", "", false );
    assertBooleanOperator( "2", "<", null, null );

    assertBooleanOperator( " ", "<", "a", true );
    assertBooleanOperator( " ", "<", "2", true );
    assertBooleanOperator( " ", "<", " ", false );
    assertBooleanOperator( " ", "<", "", false );
    assertBooleanOperator( " ", "<", null, null );

    assertBooleanOperator( "", "<", "a", true );
    assertBooleanOperator( "", "<", "2", true );
    assertBooleanOperator( "", "<", " ", true );
    assertBooleanOperator( "", "<", "", false );
    assertBooleanOperator( "", "<", null, null );

    assertBooleanOperator( null, "<", "a", null );
    assertBooleanOperator( null, "<", "2", null );
    assertBooleanOperator( null, "<", " ", null );
    assertBooleanOperator( null, "<", "", null );
    assertBooleanOperator( null, "<", null, null );

    final String fLt = "[" + KEY_ARG + "] <> [" + KEY_ARG2 + "]";
    assertBooleanFormula( fLt , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_RIGHT ) ), new Object[] { "  a", "a        " }, false );
    assertBooleanFormula( fLt , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_NONE ) ), new Object[] { "  ", "" }, false );
    assertBooleanFormula( fLt , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_NONE ) ), new Object[] { "  ", "  " }, true );
  }

  @Test
  public void testCompare_EmptyStringIsNull() throws KettleException {
    FieldAccessorUtl.ensureEmptyStringIsNotNull( false );

    // TODO: How to compare empty string and null? The flag doesn't matter now.

    assertBooleanOperator( "a", "=", "a", true );
    assertBooleanOperator( "a", "=", "2", false );
    assertBooleanOperator( "a", "=", " ", false );
    assertBooleanOperator( "a", "=", "", false );
    assertBooleanOperator( "a", "=", null, null );

    assertBooleanOperator( "2", "=", "a", false );
    assertBooleanOperator( "2", "=", "2", true );
    assertBooleanOperator( "2", "=", " ", false );
    assertBooleanOperator( "2", "=", "", false );
    assertBooleanOperator( "2", "=", null, null );

    assertBooleanOperator( " ", "=", "a", false );
    assertBooleanOperator( " ", "=", "2", false );
    assertBooleanOperator( " ", "=", " ", true );
    assertBooleanOperator( " ", "=", "", false );
    assertBooleanOperator( " ", "=", null, null );

    assertBooleanOperator( "", "=", "a", false );
    assertBooleanOperator( "", "=", "2", false );
    assertBooleanOperator( "", "=", " ", false );
    assertBooleanOperator( "", "=", "", true );
    assertBooleanOperator( "", "=", null, null );

    assertBooleanOperator( null, "=", "a", null );
    assertBooleanOperator( null, "=", "2", null );
    assertBooleanOperator( null, "=", " ", null );
    assertBooleanOperator( null, "=", "", null );
    assertBooleanOperator( null, "=", null, null );

    final String fEq = "[" + KEY_ARG + "] = [" + KEY_ARG2 + "]";
    assertBooleanFormula( fEq , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_RIGHT ) ), new Object[] { "  a", "a        " }, true );
    assertBooleanFormula( fEq , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_NONE ) ), new Object[] { "  ", "" }, true );

    assertBooleanOperator( "a", "<>", "a", false );
    assertBooleanOperator( "a", "<>", "2", true );
    assertBooleanOperator( "a", "<>", " ", true );
    assertBooleanOperator( "a", "<>", "", true );
    assertBooleanOperator( "a", "<>", null, null );

    assertBooleanOperator( "2", "<>", "a", true );
    assertBooleanOperator( "2", "<>", "2", false );
    assertBooleanOperator( "2", "<>", " ", true );
    assertBooleanOperator( "2", "<>", "", true );
    assertBooleanOperator( "2", "<>", null, null );

    assertBooleanOperator( " ", "<>", "a", true );
    assertBooleanOperator( " ", "<>", "2", true );
    assertBooleanOperator( " ", "<>", " ", false );
    assertBooleanOperator( " ", "<>", "", true );
    assertBooleanOperator( " ", "<>", null, null );

    assertBooleanOperator( "", "<>", "a", true );
    assertBooleanOperator( "", "<>", "2", true );
    assertBooleanOperator( "", "<>", " ", true );
    assertBooleanOperator( "", "<>", "", false );
    assertBooleanOperator( "", "<>", null, null );

    assertBooleanOperator( null, "<>", "a", null );
    assertBooleanOperator( null, "<>", "2", null );
    assertBooleanOperator( null, "<>", " ", null );
    assertBooleanOperator( null, "<>", "", null );
    assertBooleanOperator( null, "<>", null, null );

    final String fNeq = "[" + KEY_ARG + "] <> [" + KEY_ARG2 + "]";
    assertBooleanFormula( fNeq , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_RIGHT ) ), new Object[] { "  a", "a        " }, false );
    assertBooleanFormula( fNeq , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_NONE ) ), new Object[] { "  ", "" }, false );

    assertBooleanOperator( "a", "<", "a", false );
    assertBooleanOperator( "a", "<", "2", false );
    assertBooleanOperator( "a", "<", " ", false );
    assertBooleanOperator( "a", "<", "", false );
    assertBooleanOperator( "a", "<", null, null );

    assertBooleanOperator( "2", "<", "a", true );
    assertBooleanOperator( "2", "<", "2", false );
    assertBooleanOperator( "2", "<", " ", false );
    assertBooleanOperator( "2", "<", "", false );
    assertBooleanOperator( "2", "<", null, null );

    assertBooleanOperator( " ", "<", "a", true );
    assertBooleanOperator( " ", "<", "2", true );
    assertBooleanOperator( " ", "<", " ", false );
    assertBooleanOperator( " ", "<", "", false );
    assertBooleanOperator( " ", "<", null, null );

    assertBooleanOperator( "", "<", "a", true );
    assertBooleanOperator( "", "<", "2", true );
    assertBooleanOperator( "", "<", " ", true );
    assertBooleanOperator( "", "<", "", false );
    assertBooleanOperator( "", "<", null, null );

    assertBooleanOperator( null, "<", "a", null );
    assertBooleanOperator( null, "<", "2", null );
    assertBooleanOperator( null, "<", " ", null );
    assertBooleanOperator( null, "<", "", null );
    assertBooleanOperator( null, "<", null, null );

    final String fLt = "[" + KEY_ARG + "] <> [" + KEY_ARG2 + "]";
    assertBooleanFormula( fLt , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_RIGHT ) ), new Object[] { "  a", "a        " }, false );
    assertBooleanFormula( fLt , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_NONE ) ), new Object[] { "  ", "" }, false );
    assertBooleanFormula( fLt , buildRowMeta( buildVMString( KEY_ARG, TRIM_TYPE_LEFT ), buildVMString( KEY_ARG2,
        TRIM_TYPE_NONE ) ), new Object[] { "  ", "  " }, true );

}

  /**
   * 
   * @param strValue1
   * @param operatorTxt
   * @param strValue2
   * @param expectedResult
   * @throws KettleException
   */
  private void assertBooleanOperator( String strValue1, String operatorTxt, String strValue2, Boolean expectedResult )
    throws KettleException {
    final String formula = "[" + KEY_ARG + "] " + operatorTxt + " [" + KEY_ARG2 + "]";
    assertBooleanFormula( formula, buildRowMeta( new ValueMetaString( KEY_ARG ), new ValueMetaString( KEY_ARG2 ) ),
        new Object[] { strValue1, strValue2 }, expectedResult );
  }

  /**
   * 
   * @param argName
   * @param trimType
   * @return
   */
  private ValueMetaString buildVMString( final String argName, final int trimType ) {
    ValueMetaString r = new ValueMetaString( argName );
    r.setTrimType( trimType );
    return r;
  }

  /**
   * 
   * @param valueMetaInterfaces
   * @return
   */
  private static RowMetaInterface buildRowMeta( ValueMetaInterface... valueMetaInterfaces ) {
    RowMetaInterface rm = new RowMeta();
    for ( int i = 0; i < valueMetaInterfaces.length; i++ ) {
      rm.addValueMeta( valueMetaInterfaces[i] );
    }
    return rm;
  }

  /**
   * @param inputRowData
   * @return
   */
  private static String getMsg( final Object[] inputRowData ) {
    StringBuilder sb = new StringBuilder( "<" );
    for ( int i = 0; i < inputRowData.length; i++ ) {
      if ( i > 0 ) {
        sb.append( ", " );
      }
      sb.append( String.valueOf( inputRowData[i] ) );
    }
    sb.append( ">" );
    return sb.toString();
  }

  /**
   * 
   * @param formula
   * @param inputRowMeta
   * @param inputRowData
   * @param expectedResult
   * @throws KettleException
   */
  private void assertBooleanFormula( //
      final String formula, final RowMetaInterface inputRowMeta, final Object[] inputRowData, //
      final Boolean expectedResult //
    ) throws KettleException {
    assertBooleanFormula( getMsg( inputRowData ), formula, inputRowMeta, inputRowData, expectedResult );
  }

  /**
   * 
   * @param msg
   * @param formula
   * @param inputRowMeta
   * @param inputRowData
   * @param expectedResult
   * @throws KettleException
   */
  private void assertBooleanFormula( final String msg, //
      final String formula, final RowMetaInterface inputRowMeta, final Object[] inputRowData, //
      final Boolean expectedResult //
    ) throws KettleException {
    final int formulaType = ValueMetaInterface.TYPE_BOOLEAN;

    final FormulaMetaFunction function = new FormulaMetaFunction( KEY_RESULT, formula, formulaType, -1, -1, null );

    Assert.assertEquals( msg + ". check input data size", inputRowMeta.size(), inputRowData.length ); // Just to be sure
    final int inputSize = inputRowMeta.size();

    final FormulaMeta meta = new FormulaMeta();
    meta.setFormula( new FormulaMetaFunction[] { function } );
    final TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );
    final List<RowMetaAndData> inputList =
        java.util.Collections.singletonList( new RowMetaAndData( inputRowMeta, inputRowData ) );
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
            TransTestFactory.DUMMY_STEPNAME, inputList );

    Assert.assertNotNull( msg + ". So we have some results", ret );
    Assert.assertEquals( msg + ". We have one result row", 1, ret.size() );

    int resultIndex = inputSize;
    RowMetaAndData rmd = ret.get( 0 );

    Assert.assertEquals( msg + ". Result row includes input plus result columns", inputSize + 1, rmd.size() );

    ValueMetaInterface resultValueMeta = rmd.getValueMeta( resultIndex );

    Assert.assertNotNull( resultValueMeta );
    Assert.assertEquals( msg + ". resultType", formulaType, resultValueMeta.getType() );
    Assert.assertEquals( msg + ". resultName", KEY_RESULT, resultValueMeta.getName() );

    if ( expectedResult != null ) {
      Assert.assertEquals( msg + ". expectedResult.00", expectedResult, rmd.getBoolean( resultIndex, false ) );
      Assert.assertEquals( msg + ". expectedResult.01", expectedResult, rmd.getBoolean( resultIndex, true ) );
      Assert.assertEquals( msg + ". expectedResult.02", expectedResult, rmd.getBoolean( KEY_RESULT, false ) );
      Assert.assertEquals( msg + ". expectedResult.03", expectedResult, rmd.getBoolean( KEY_RESULT, true ) );
    } else {
      Assert.assertEquals( msg + ". expectedResult.10", false, rmd.getBoolean( resultIndex, false ) );
      Assert.assertEquals( msg + ". expectedResult.11", true, rmd.getBoolean( resultIndex, true ) );
      Assert.assertEquals( msg + ". expectedResult.12", false, rmd.getBoolean( KEY_RESULT, false ) );
      Assert.assertEquals( msg + ". expectedResult.13", true, rmd.getBoolean( KEY_RESULT, true ) );
    }
  }

  /**
   * 
   * @param formula
   * @param inputRowMeta
   * @param inputRowData
   * @param expectedResult
   * @throws KettleException
   */
  private void assertStringFormula( //
      final String formula, final RowMetaInterface inputRowMeta, final Object[] inputRowData, //
      final String expectedResult //
    ) throws KettleException {
    assertStringFormula( getMsg( inputRowData ), formula, inputRowMeta, inputRowData, expectedResult );
  }

  /**
   * 
   * @param msg
   * @param formula
   * @param inputRowMeta
   * @param inputRowData
   * @param expectedResult
   * @throws KettleException
   */
  private void assertStringFormula( final String msg, //
      final String formula, final RowMetaInterface inputRowMeta, final Object[] inputRowData, //
      final String expectedResult //
    ) throws KettleException {
    final int formulaType = ValueMetaInterface.TYPE_STRING;

    final FormulaMetaFunction function = new FormulaMetaFunction( KEY_RESULT, formula, formulaType, -1, -1, null );

    Assert.assertEquals( msg + ". check input data size", inputRowMeta.size(), inputRowData.length ); // Just to be sure
    final int inputSize = inputRowMeta.size();

    final FormulaMeta meta = new FormulaMeta();
    meta.setFormula( new FormulaMetaFunction[] { function } );
    final TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );
    final List<RowMetaAndData> inputList =
        java.util.Collections.singletonList( new RowMetaAndData( inputRowMeta, inputRowData ) );
    List<RowMetaAndData> ret =
        TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
            TransTestFactory.DUMMY_STEPNAME, inputList );

    Assert.assertNotNull( msg + ". So we have some results", ret );
    Assert.assertEquals( msg + ". We have one result row", 1, ret.size() );

    int resultIndex = inputSize;
    RowMetaAndData rmd = ret.get( 0 );

    Assert.assertEquals( msg + ". Result row includes input plus result columns", inputSize + 1, rmd.size() );

    ValueMetaInterface resultValueMeta = rmd.getValueMeta( resultIndex );

    Assert.assertNotNull( resultValueMeta );
    Assert.assertEquals( msg + ". resultType", formulaType, resultValueMeta.getType() );
    Assert.assertEquals( msg + ". resultName", KEY_RESULT, resultValueMeta.getName() );

    Assert.assertEquals( msg + ". expectedResult.0", expectedResult, rmd.getString( resultIndex, null ) );
    Assert.assertEquals( msg + ". expectedResult.1", expectedResult, rmd.getString( KEY_RESULT, null ) );

  }

}
