/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

public class SQLFieldTest extends TestCase {

  public void testSqlField01() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "A as foo";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField01Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "Service.A as foo";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField01QuotedAlias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "\"Service\".A as foo";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField02() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "A as \"foo\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField02Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "Service.A as \"foo\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField03() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "\"A\" as \"foo\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField03Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "Service.\"A\" as \"foo\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField04() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "\"A\" \"foo\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField04Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "Service.\"A\" \"foo\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField05() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "A   as   foo";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField05Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "Service.A   as   foo";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "foo", field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField06() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "SUM(B) as total";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "B", field.getName() );
    assertEquals( "total", field.getAlias() );
    assertEquals( SQLAggregation.SUM, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField06Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "SUM(Service.B) as total";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "B", field.getName() );
    assertEquals( "total", field.getAlias() );
    assertEquals( SQLAggregation.SUM, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField07() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "SUM( B ) as total";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "B", field.getName() );
    assertEquals( "total", field.getAlias() );
    assertEquals( SQLAggregation.SUM, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField07Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "SUM( Service.B ) as total";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "B", field.getName() );
    assertEquals( "total", field.getAlias() );
    assertEquals( SQLAggregation.SUM, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField08() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "SUM( \"B\" ) as total";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "B", field.getName() );
    assertEquals( "total", field.getAlias() );
    assertEquals( SQLAggregation.SUM, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField08Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "SUM( Service.\"B\" ) as total";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "B", field.getName() );
    assertEquals( "total", field.getAlias() );
    assertEquals( SQLAggregation.SUM, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField09() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "SUM(\"B\") as   \"total\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "B", field.getName() );
    assertEquals( "total", field.getAlias() );
    assertEquals( SQLAggregation.SUM, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField09Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "SUM(Service.\"B\") as   \"total\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "B", field.getName() );
    assertEquals( "total", field.getAlias() );
    assertEquals( SQLAggregation.SUM, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
  }

  public void testSqlField10() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "COUNT(*) as   \"Number of lines\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "*", field.getName() );
    assertEquals( "Number of lines", field.getAlias() );
    assertEquals( SQLAggregation.COUNT, field.getAggregation() );
    assertNull( field.getValueMeta() );
    assertTrue( field.isCountStar() );
  }

  public void testSqlField10NoAlias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "COUNT(*)";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "*", field.getName() );
    assertEquals( "COUNT(*)", field.getAlias() );
    assertEquals( SQLAggregation.COUNT, field.getAggregation() );
    assertNull( field.getValueMeta() );
    assertTrue( field.isCountStar() );
  }

  public void testSqlField10Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "COUNT(Service.*) as   \"Number of lines\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "*", field.getName() );
    assertEquals( "Number of lines", field.getAlias() );
    assertEquals( SQLAggregation.COUNT, field.getAggregation() );
    assertNull( field.getValueMeta() );
    assertTrue( field.isCountStar() );
  }

  public void testSqlField11() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "COUNT(DISTINCT A) as   \"Number of customers\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "Number of customers", field.getAlias() );
    assertEquals( SQLAggregation.COUNT, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
    assertTrue( field.isCountDistinct() );
  }

  public void testSqlField11Alias() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "COUNT(DISTINCT Service.A) as   \"Number of customers\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "A", field.getName() );
    assertEquals( "Number of customers", field.getAlias() );
    assertEquals( SQLAggregation.COUNT, field.getAggregation() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
    assertTrue( field.isCountDistinct() );
  }

  public void testSqlField12_Function() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "IIF( B>5000, 'Big', 'Small' ) as \"Sales size\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "IIF( B>5000, 'Big', 'Small' )", field.getName() );
    assertEquals( "Sales size", field.getAlias() );
    assertNull( "The service data type was discovered", field.getValueMeta() );

    assertNotNull( field.getIif() );
    Condition condition = field.getIif().getSqlCondition().getCondition();
    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">", condition.getFunctionDesc() );
    assertEquals( "5000", condition.getRightExactString() );
  }

  public void testSqlField12Alias_Function() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "IIF( Service.B>5000, 'Big', 'Small' ) as \"Sales size\"";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "IIF( Service.B>5000, 'Big', 'Small' )", field.getName() );
    assertEquals( "Sales size", field.getAlias() );
    assertNull( "The service data type was discovered", field.getValueMeta() );

    assertNotNull( field.getIif() );
    Condition condition = field.getIif().getSqlCondition().getCondition();
    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">", condition.getFunctionDesc() );
    assertEquals( "5000", condition.getRightExactString() );
  }

  public void testSqlField13_Function() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "IIF( B>50, 'high', 'low' ) as nrSize";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "IIF( B>50, 'high', 'low' )", field.getName() );
    assertEquals( "nrSize", field.getAlias() );
    assertNull( "The service data type was discovered", field.getValueMeta() );

    assertNotNull( field.getIif() );
    Condition condition = field.getIif().getSqlCondition().getCondition();
    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">", condition.getFunctionDesc() );
    assertEquals( "50", condition.getRightExactString() );
  }

  public void testSqlField13Alias_Function() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "IIF( Service.B>50, 'high', 'low' ) as nrSize";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "IIF( Service.B>50, 'high', 'low' )", field.getName() );
    assertEquals( "nrSize", field.getAlias() );
    assertNull( "The service data type was discovered", field.getValueMeta() );

    assertNotNull( field.getIif() );
    Condition condition = field.getIif().getSqlCondition().getCondition();
    assertNotNull( condition );
    assertFalse( condition.isEmpty() );
    assertTrue( condition.isAtomic() );
    assertEquals( "B", condition.getLeftValuename() );
    assertEquals( ">", condition.getFunctionDesc() );
    assertEquals( "50", condition.getRightExactString() );
  }

  public void testSqlFieldConstants01() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "1";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "1", field.getName() );
    assertNull( field.getAlias() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
    assertEquals( field.getValueMeta().getType(), ValueMetaInterface.TYPE_INTEGER );
    assertEquals( 1L, field.getValueData() );
  }

  public void testSqlFieldConstants02() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateTest2RowMeta();

    String fieldClause = "COUNT(1)";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "1", field.getName() );
    assertEquals( "COUNT(1)", field.getAlias() );
    assertEquals( SQLAggregation.COUNT, field.getAggregation() );
    assertEquals( 1L, field.getValueData() );
    assertNotNull( "The service data type was not discovered", field.getValueMeta() );
    assertEquals( field.getValueMeta().getType(), ValueMetaInterface.TYPE_INTEGER );
    assertEquals( 1L, field.getValueData() );
  }

  /**
   * Mondrian generated CASE WHEN <condition> THEN true-value ELSE false-value END
   *
   * @throws KettleSQLException
   */
  public void testSqlFieldCaseWhen01() throws KettleSQLException {
    RowMetaInterface rowMeta = SQLTest.generateServiceRowMeta();

    String fieldClause = "CASE WHEN \"Service\".\"Category\" IS NULL THEN 1 ELSE 0 END";

    SQLField field = new SQLField( "Service", fieldClause, rowMeta );
    assertEquals( "CASE WHEN \"Service\".\"Category\" IS NULL THEN 1 ELSE 0 END", field.getName() );
    assertNull( field.getAlias() );
    assertNotNull( field.getIif() );
    assertEquals( "\"Service\".\"Category\" IS NULL", field.getIif().getConditionClause() );
    assertEquals( 1L, field.getIif().getTrueValue().getValueData() );
    assertEquals( 0L, field.getIif().getFalseValue().getValueData() );
  }

}
