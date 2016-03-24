/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.sql;

import junit.framework.TestCase;
import org.junit.Test;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.RowMetaInterface;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.pentaho.di.core.sql.SQLTest.mockRowMeta;

public class SQLConditionTest extends TestCase {

  public void testCondition01() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "A = 'FOO'";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "A", condition.getLeftValuename() );
    assertEquals( "=", condition.getFunctionDesc() );
    assertEquals( "FOO", condition.getRightExactString() );
  }

  public void testCondition02() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "B > 123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">", condition.getFunctionDesc() );
    assertEquals( "123", condition.getRightExactString() );
  }

  public void testCondition03() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "B < 123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( "<", condition.getFunctionDesc() );
    assertEquals( "123", condition.getRightExactString() );
  }

  public void testCondition04() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "B >= 123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">=", condition.getFunctionDesc() );
    assertEquals( "123", condition.getRightExactString() );
  }

  public void testCondition05() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "B => 123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">=", condition.getFunctionDesc() );
    assertEquals( "123", condition.getRightExactString() );
  }

  public void testCondition06() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "B <= 123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( "<=", condition.getFunctionDesc() );
    assertEquals( "123", condition.getRightExactString() );
  }

  public void testCondition07() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "B >= 123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">=", condition.getFunctionDesc() );
    assertEquals( "123", condition.getRightExactString() );
  }

  public void testCondition08() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "B => 123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">=", condition.getFunctionDesc() );
    assertEquals( "123", condition.getRightExactString() );
  }

  public void testCondition09() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "B <> 123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( "<>", condition.getFunctionDesc() );
    assertEquals( "123", condition.getRightExactString() );
  }

  public void testCondition10() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "B IN (1, 2, 3, 4)";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( "IN LIST", condition.getFunctionDesc() );
    assertEquals( "1;2;3;4", condition.getRightExactString() );
  }

  public void testCondition11() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "A IN ( 'foo' , 'bar' )";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "A", condition.getLeftValuename() );
    assertEquals( "IN LIST", condition.getFunctionDesc() );
    assertEquals( "foo;bar", condition.getRightExactString() );
  }

  public void testCondition12() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "A REGEX 'foo.*bar'";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "A", condition.getLeftValuename() );
    assertEquals( "REGEXP", condition.getFunctionDesc() );
    assertEquals( "foo.*bar", condition.getRightExactString() );
  }

  public void testCondition13() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "A LIKE 'foo%'";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "A", condition.getLeftValuename() );
    assertEquals( "LIKE", condition.getFunctionDesc() );
    assertEquals( "foo%", condition.getRightExactString() );
  }

  public void testCondition14() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "A LIKE 'foo??bar'";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "A", condition.getLeftValuename() );
    assertEquals( "LIKE", condition.getFunctionDesc() );
    assertEquals( "foo??bar", condition.getRightExactString() );
  }

  // Now the more complex AND/OR/NOT situations...
  //
  public void testCondition15() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "A='Foo' AND B>5";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 2, condition.nrConditions() );

    Condition one = condition.getCondition( 0 );
    assertEquals( "A", one.getLeftValuename() );
    assertEquals( "=", one.getFunctionDesc() );
    assertEquals( "Foo", one.getRightExactString() );

    Condition two = condition.getCondition( 1 );
    assertEquals( "B", two.getLeftValuename() );
    assertEquals( ">", two.getFunctionDesc() );
    assertEquals( "5", two.getRightExactString() );

    assertEquals( Condition.OPERATOR_AND, two.getOperator() );
  }

  public void testCondition16() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "( A='Foo' ) AND ( B>5 )";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 2, condition.nrConditions() );

    Condition one = condition.getCondition( 0 );
    assertEquals( "A", one.getLeftValuename() );
    assertEquals( "=", one.getFunctionDesc() );
    assertEquals( "Foo", one.getRightExactString() );

    Condition two = condition.getCondition( 1 );
    assertEquals( "B", two.getLeftValuename() );
    assertEquals( ">", two.getFunctionDesc() );
    assertEquals( "5", two.getRightExactString() );

    assertEquals( Condition.OPERATOR_AND, two.getOperator() );
  }

  public void testCondition17() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B, C, D";
    String conditionClause = "A='Foo' AND B>5 AND C='foo' AND D=123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 4, condition.nrConditions() );

    Condition one = condition.getCondition( 0 );
    assertEquals( "A", one.getLeftValuename() );
    assertEquals( "=", one.getFunctionDesc() );
    assertEquals( "Foo", one.getRightExactString() );

    Condition two = condition.getCondition( 1 );
    assertEquals( "B", two.getLeftValuename() );
    assertEquals( ">", two.getFunctionDesc() );
    assertEquals( "5", two.getRightExactString() );
    assertEquals( Condition.OPERATOR_AND, two.getOperator() );

    Condition three = condition.getCondition( 2 );
    assertEquals( "C", three.getLeftValuename() );
    assertEquals( "=", three.getFunctionDesc() );
    assertEquals( "foo", three.getRightExactString() );
    assertEquals( Condition.OPERATOR_AND, three.getOperator() );

    Condition four = condition.getCondition( 3 );
    assertEquals( "D", four.getLeftValuename() );
    assertEquals( "=", four.getFunctionDesc() );
    assertEquals( "123", four.getRightExactString() );
    assertEquals( Condition.OPERATOR_AND, four.getOperator() );

  }

  /**
   * Test precedence.
   *
   * @throws KettleSQLException
   */
  public void testCondition18() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B, C, D";
    String conditionClause = "A='Foo' OR B>5 AND C='foo' OR D=123";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 3, condition.nrConditions() );

    Condition leftOr = condition.getCondition( 0 );

    assertTrue( leftOr.isAtomic() );
    assertEquals( "A", leftOr.getLeftValuename() );
    assertEquals( "=", leftOr.getFunctionDesc() );
    assertEquals( "Foo", leftOr.getRightExactString() );

    Condition middleOr = condition.getCondition( 1 );
    assertEquals( 2, middleOr.nrConditions() );

    Condition leftAnd = middleOr.getCondition( 0 );
    assertTrue( leftAnd.isAtomic() );
    assertEquals( "B", leftAnd.getLeftValuename() );
    assertEquals( ">", leftAnd.getFunctionDesc() );
    assertEquals( "5", leftAnd.getRightExactString() );
    assertEquals( Condition.OPERATOR_NONE, leftAnd.getOperator() );

    Condition rightAnd = middleOr.getCondition( 1 );
    assertEquals( Condition.OPERATOR_AND, rightAnd.getOperator() );
    assertEquals( "C", rightAnd.getLeftValuename() );
    assertEquals( "=", rightAnd.getFunctionDesc() );
    assertEquals( "foo", rightAnd.getRightExactString() );

    Condition rightOr = condition.getCondition( 2 );
    assertTrue( rightOr.isAtomic() );
    assertEquals( "D", rightOr.getLeftValuename() );
    assertEquals( "=", rightOr.getFunctionDesc() );
    assertEquals( "123", rightOr.getRightExactString() );
    assertEquals( Condition.OPERATOR_OR, rightOr.getOperator() );
  }

  public void testCondition19() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "A LIKE '%AL%' AND ( B LIKE '15%' OR C IS NULL )";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 2, condition.nrConditions() );

    Condition leftAnd = condition.getCondition( 0 );

    assertTrue( leftAnd.isAtomic() );
    assertEquals( "A", leftAnd.getLeftValuename() );
    assertEquals( "LIKE", leftAnd.getFunctionDesc() );
    assertEquals( "%AL%", leftAnd.getRightExactString() );

    Condition rightBracket = condition.getCondition( 1 );
    assertEquals( 1, rightBracket.nrConditions() );
    Condition rightAnd = rightBracket.getCondition( 0 );
    assertEquals( 2, rightAnd.nrConditions() );

    Condition leftOr = rightAnd.getCondition( 0 );
    assertTrue( leftOr.isAtomic() );
    assertEquals( "B", leftOr.getLeftValuename() );
    assertEquals( "LIKE", leftOr.getFunctionDesc() );
    assertEquals( "15%", leftOr.getRightExactString() );
    assertEquals( Condition.OPERATOR_NONE, leftOr.getOperator() );

    Condition rightOr = rightAnd.getCondition( 1 );
    assertEquals( Condition.OPERATOR_OR, rightOr.getOperator() );
    assertEquals( "C", rightOr.getLeftValuename() );
    assertEquals( "IS NULL", rightOr.getFunctionDesc() );
    assertNull( rightOr.getRightExactString() );

  }

  public void testCondition20() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "NOT ( A = 'FOO' )";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertTrue( condition.isNegated() );
    assertEquals( "A", condition.getLeftValuename() );
    assertEquals( "=", condition.getFunctionDesc() );
    assertEquals( "FOO", condition.getRightExactString() );
  }

  public void testCondition21() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "A='Foo' AND NOT ( B>5 )";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 2, condition.nrConditions() );

    Condition one = condition.getCondition( 0 );
    assertEquals( "A", one.getLeftValuename() );
    assertEquals( "=", one.getFunctionDesc() );
    assertEquals( "Foo", one.getRightExactString() );

    Condition two = condition.getCondition( 1 );
    assertTrue( two.isNegated() );
    assertEquals( "B", two.getLeftValuename() );
    assertEquals( ">", two.getFunctionDesc() );
    assertEquals( "5", two.getRightExactString() );

    assertEquals( Condition.OPERATOR_AND, two.getOperator() );
  }

  public void testCondition22() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B, C";
    String conditionClause = "A='Foo' AND NOT ( B>5 OR C='AAA' ) ";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 2, condition.nrConditions() );

    Condition leftAnd = condition.getCondition( 0 );
    assertEquals( "A", leftAnd.getLeftValuename() );
    assertEquals( "=", leftAnd.getFunctionDesc() );
    assertEquals( "Foo", leftAnd.getRightExactString() );

    Condition rightAnd = condition.getCondition( 1 );
    assertEquals( 1, rightAnd.nrConditions() );

    Condition notBlock = rightAnd.getCondition( 0 );
    assertTrue( notBlock.isNegated() );
    assertEquals( 2, notBlock.nrConditions() );

    Condition leftOr = notBlock.getCondition( 0 );
    assertTrue( leftOr.isAtomic() );
    assertEquals( "B", leftOr.getLeftValuename() );
    assertEquals( ">", leftOr.getFunctionDesc() );
    assertEquals( "5", leftOr.getRightExactString() );
    assertEquals( Condition.OPERATOR_NONE, leftOr.getOperator() );

    Condition rightOr = notBlock.getCondition( 1 );
    assertEquals( Condition.OPERATOR_OR, rightOr.getOperator() );
    assertEquals( "C", rightOr.getLeftValuename() );
    assertEquals( "=", rightOr.getFunctionDesc() );
    assertEquals( "AAA", rightOr.getRightExactString() );
  }

  // Brackets, quotes...
  //
  public void testCondition23() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "( A LIKE '(AND' ) AND ( B>5 )";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 2, condition.nrConditions() );

    Condition one = condition.getCondition( 0 );
    assertEquals( "A", one.getLeftValuename() );
    assertEquals( "LIKE", one.getFunctionDesc() );
    assertEquals( "(AND", one.getRightExactString() );

    Condition two = condition.getCondition( 1 );
    assertEquals( "B", two.getLeftValuename() );
    assertEquals( ">", two.getFunctionDesc() );
    assertEquals( "5", two.getRightExactString() );

    assertEquals( Condition.OPERATOR_AND, two.getOperator() );
  }

  // Brackets, quotes...
  //
  public void testCondition24() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "( A LIKE '(AND' ) AND ( ( B>5 ) OR ( B=3 ) )";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 2, condition.nrConditions() );

    Condition one = condition.getCondition( 0 );
    assertEquals( "A", one.getLeftValuename() );
    assertEquals( "LIKE", one.getFunctionDesc() );
    assertEquals( "(AND", one.getRightExactString() );

    Condition two = condition.getCondition( 1 );
    assertEquals( 1, two.nrConditions() );

    Condition brackets = condition.getCondition( 1 );
    assertEquals( 1, brackets.nrConditions() );

    Condition right = brackets.getCondition( 0 );
    assertEquals( 2, right.nrConditions() );

    Condition leftOr = right.getCondition( 0 );
    assertTrue( leftOr.isAtomic() );
    assertEquals( "B", leftOr.getLeftValuename() );
    assertEquals( ">", leftOr.getFunctionDesc() );
    assertEquals( "5", leftOr.getRightExactString() );
    assertEquals( Condition.OPERATOR_NONE, leftOr.getOperator() );

    Condition rightOr = right.getCondition( 1 );
    assertEquals( Condition.OPERATOR_OR, rightOr.getOperator() );
    assertEquals( "B", rightOr.getLeftValuename() );
    assertEquals( "=", rightOr.getFunctionDesc() );
    assertEquals( "3", rightOr.getRightExactString() );

  }

  // Parameters...
  //

  public void testCondition25() throws KettleSQLException {
    runParamTest( "PARAMETER('param')='FOO'",
        "param",
        "FOO" );
  }

  public void testLowerCaseParamInConditionClause() throws KettleSQLException {
    runParamTest( "parameter('param')='FOO'",
        "param",
        "FOO" );
  }

  public void testMixedCaseParamInConditionClause() throws KettleSQLException {
    runParamTest( "Parameter('param')='FOO'",
        "param",
        "FOO" );
  }

  public void testSpaceInParamNameAndValueInConditionClause() throws KettleSQLException {
    runParamTest( "Parameter('My Parameter')='Foo Bar Baz'",
        "My Parameter",
        "Foo Bar Baz" );
  }

  public void testUnquotedNumericParameterValueInConditionClause() throws KettleSQLException {
    runParamTest( "Parameter('My Parameter') = 123",
        "My Parameter",
        "123" );
  }

  public void testExtraneousWhitespaceInParameterConditionClause() throws KettleSQLException {
    runParamTest( "Parameter   ( \t     'My Parameter'  \t   )  \t= \t    'My value'",
        "My Parameter",
        "My value" );
  }

  public void testParameterNameMissingThrows() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();
    SQLFields fields = new SQLFields( "Service", rowMeta, "A, B" );
    try {
      new SQLCondition( "Service", "Parameter('') =  'My value'", rowMeta, fields );
      fail();
    } catch ( KettleSQLException kse ) {
      assertTrue( kse.getMessage().contains( "A parameter name cannot be empty" ) );
    }
  }

  public void testParameterValueMissingThrows() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();
    SQLFields fields = new SQLFields( "Service", rowMeta, "A, B" );
    try {
      new SQLCondition( "Service", "Parameter('Foo') =  ''", rowMeta, fields );
      fail();
    } catch ( KettleSQLException kse ) {
      assertTrue( kse.getMessage().contains( "A parameter value cannot be empty" ) );
    }
  }

  private void runParamTest( String conditionClause, String paramName, String paramValue )
      throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();
    SQLFields fields = new SQLFields( "Service", rowMeta, "A, B" );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals(
        String.format(
            "Expected condition to be of type FUNC_TRUE, was (%s)",
            Condition.functions[condition.getFunction()] ),
        Condition.FUNC_TRUE, condition.getFunction() );

    assertEquals( paramName, condition.getLeftValuename() );
    assertEquals( paramValue, condition.getRightExactString() );
  }

  public void testCondition26() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest4RowMeta();

    String fieldsClause = "A, B";
    String conditionClause = "A='Foo' AND B>5 OR PARAMETER('par')='foo'";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertFalse( "Non-atomic condition expected", condition.isAtomic() );
    assertEquals( 2, condition.nrConditions() );

    Condition one = condition.getCondition( 0 );
    assertEquals( 2, one.nrConditions() );

    Condition leftAnd = one.getCondition( 0 );
    assertEquals( "A", leftAnd.getLeftValuename() );
    assertEquals( "=", leftAnd.getFunctionDesc() );
    assertEquals( "Foo", leftAnd.getRightExactString() );
    Condition rightAnd = one.getCondition( 1 );
    assertEquals( "B", rightAnd.getLeftValuename() );
    assertEquals( ">", rightAnd.getFunctionDesc() );
    assertEquals( "5", rightAnd.getRightExactString() );
    assertEquals( Condition.OPERATOR_AND, rightAnd.getOperator() );

    Condition param = condition.getCondition( 1 );
    assertTrue( param.isAtomic() );
    assertEquals( Condition.OPERATOR_OR, param.getOperator() );
    assertEquals( Condition.FUNC_TRUE, param.getFunction() );
    assertEquals( "par", param.getLeftValuename() );
    assertEquals( "foo", param.getRightExactString() );
  }

  public void testCondition27() throws Exception {

    RowMetaInterface rowMeta = SQLTest.generateServiceRowMeta();
    String fieldsClause = "\"Service\".\"Category\" as \"c0\", \"Service\".\"Country\" as \"c1\"";
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    String
        conditionClause =
        "(NOT((sum(\"Service\".\"sales_amount\") is null)) OR NOT((sum(\"Service\".\"products_sold\") is null)) )";
    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();
    assertNotNull( condition );
  }

  public void testCondition28() throws Exception {

    RowMetaInterface rowMeta = SQLTest.generateServiceRowMeta();
    String
        fieldsClause =
        "\"Service\".\"Category\" as \"c0\", \"Service\".\"Country\" as \"c1\" from \"Service\" as \"Service\"";
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    String conditionClause = "((not (\"Service\".\"Country\" = 'Belgium') or (\"Service\".\"Country\" is null)))";
    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();
    assertNotNull( condition );
  }

  public void testCondition29() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateGettingStartedRowMeta();

    String fieldsClause = "CUSTOMERNAME";
    String
        conditionClause =
        "\"GETTING_STARTED\".\"CUSTOMERNAME\" IN ('ANNA''S DECORATIONS, LTD', 'MEN ''R'' US RETAILERS, Ltd.' )";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( Condition.FUNC_IN_LIST, condition.getFunction() );

    assertEquals( "\"GETTING_STARTED\".\"CUSTOMERNAME\"", condition.getLeftValuename() );
    assertEquals( "ANNA'S DECORATIONS, LTD;MEN 'R' US RETAILERS, Ltd.", condition.getRightExactString() );
  }

  /**
   * Test IN-clause with escaped quoting and semi-colons in them.
   *
   * @throws KettleSQLException
   */
  public void testCondition30() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateGettingStartedRowMeta();

    String fieldsClause = "CUSTOMERNAME";
    String conditionClause = "CUSTOMERNAME IN (''';''', 'Toys ''R'' us' )";

    // Correctness of the next statement is tested in SQLFieldsTest
    //
    SQLFields fields = new SQLFields( "Service", rowMeta, fieldsClause );

    SQLCondition sqlCondition = new SQLCondition( "Service", conditionClause, rowMeta, fields );
    Condition condition = sqlCondition.getCondition();

    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( Condition.FUNC_IN_LIST, condition.getFunction() );

    assertEquals( "CUSTOMERNAME", condition.getLeftValuename() );
    assertEquals( "'\\;';Toys 'R' us", condition.getRightExactString() );
  }

  @Test
  public void testLeftFieldWithTableQualifier() throws KettleSQLException {
    RowMetaInterface rowMeta = mockRowMeta( "noSpaceField" );

    SQLCondition sqlCondition = new SQLCondition(
      "table", "\"table\".\"noSpaceField\" IS NULL", rowMeta );

    Condition condition = sqlCondition.getCondition();
    assertThat( condition.getFunctionDesc(), is( "IS NULL" ) );
    assertThat( condition.getLeftValuename(), is( "noSpaceField" ) );
    assertTrue( condition.isAtomic() );
  }

  @Test
  public void testLeftFieldWithSpaceAndTableQualifier() throws KettleSQLException {
    RowMetaInterface rowMeta = mockRowMeta( "Space Field" );

    SQLCondition sqlCondition = new SQLCondition(
      "table", "\"table\".\"Space Field\" IS NULL", rowMeta );
    Condition condition = sqlCondition.getCondition();
    assertThat( condition.getFunctionDesc(), is( "IS NULL" ) );
    assertThat( condition.getLeftValuename(), is( "Space Field" ) );
    assertTrue( condition.isAtomic() );
  }

  @Test
  public void testLeftAndRightFieldWithSpaceAndTableQualifier() throws KettleSQLException {
    RowMetaInterface rowMeta = mockRowMeta( "Left Field", "Right Field" );

    SQLCondition sqlCondition = new SQLCondition(
      "table", "\"table\".\"Left Field\" = \"table\".\"Right Field\"", rowMeta );
    Condition condition = sqlCondition.getCondition();
    assertThat( condition.getFunctionDesc(), is( "=" ) );
    assertThat( condition.getLeftValuename(), is( "Left Field" ) );
    assertThat( condition.getRightValuename(), is( "Right Field" ) );
    assertTrue( condition.isAtomic() );
  }

  @Test
  public void testCompoundConditionLeftFieldWithSpaceAndTableQualifier() throws KettleSQLException {
    RowMetaInterface rowMeta = mockRowMeta( "Space Field" );

    SQLCondition sqlCondition = new SQLCondition(
      "table", "\"table\".\"Space Field\" IS NULL AND \"table\".\"Space Field\" > 1", rowMeta );
    Condition condition = sqlCondition.getCondition();
    assertThat( condition.getChildren().size(), is( 2 ) );

    List<Condition> children = condition.getChildren();
    assertThat( children.get( 0 ).getFunctionDesc(), is( "IS NULL" ) );
    assertThat( children.get( 0 ).getLeftValuename(), is( "Space Field" ) );
    assertThat( children.get( 1 ).getOperator(), is( Condition.OPERATOR_AND ) );
    assertThat( children.get( 1 ).getFunctionDesc(), is( ">" ) );
    assertThat( children.get( 1 ).getLeftValuename(), is( "Space Field" ) );
    assertTrue( condition.isComposite() );
  }
}
