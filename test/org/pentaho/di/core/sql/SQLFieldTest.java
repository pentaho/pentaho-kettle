package org.pentaho.di.core.sql;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import junit.framework.TestCase;

public class SQLFieldTest extends TestCase {
  
  
  public void testSqlField01() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "A as foo";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("A", field.getName());
    assertEquals("foo", field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
  }

  public void testSqlField02() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "A as \"foo\"";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("A", field.getName());
    assertEquals("foo", field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
  }

  public void testSqlField03() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "\"A\" as \"foo\"";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("A", field.getName());
    assertEquals("foo", field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
  }

  public void testSqlField04() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "\"A\" \"foo\"";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("A", field.getName());
    assertEquals("foo", field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
  }

  public void testSqlField05() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "A   as   foo";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("A", field.getName());
    assertEquals("foo", field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
  }
  
  public void testSqlField06() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "SUM(B) as total";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("B", field.getName());
    assertEquals("total", field.getAlias());
    assertEquals(SQLAggregation.SUM, field.getAggregation());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
  }
  
  public void testSqlField07() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "SUM( B ) as total";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("B", field.getName());
    assertEquals("total", field.getAlias());
    assertEquals(SQLAggregation.SUM, field.getAggregation());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
  }
  
  public void testSqlField08() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "SUM( \"B\" ) as total";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("B", field.getName());
    assertEquals("total", field.getAlias());
    assertEquals(SQLAggregation.SUM, field.getAggregation());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
  }

  public void testSqlField09() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "SUM(\"B\") as   \"total\"";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("B", field.getName());
    assertEquals("total", field.getAlias());
    assertEquals(SQLAggregation.SUM, field.getAggregation());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
  }
  
  public void testSqlField10() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "COUNT(*) as   \"Number of lines\"";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("COUNT(*)", field.getName());
    assertEquals("Number of lines", field.getAlias());
    assertEquals(SQLAggregation.COUNT, field.getAggregation());
    assertNull(field.getValueMeta());
    assertTrue(field.isCountStar());
  }

  public void testSqlField11() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "COUNT(DISTINCT A) as   \"Number of customers\"";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("A", field.getName());
    assertEquals("Number of customers", field.getAlias());
    assertEquals(SQLAggregation.COUNT, field.getAggregation());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
    assertTrue(field.isCountDistinct());
  }
  
  public void testSqlField12_Function() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "IIF( B>5000, 'Big', 'Small' ) as \"Sales size\"";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("IIF( B>5000, 'Big', 'Small' )", field.getName());
    assertEquals("Sales size", field.getAlias());
    assertNull("The service data type was discovered", field.getValueMeta());
    
    assertNotNull(field.getIif());
    Condition condition = field.getIif().getSqlCondition().getCondition();
    assertNotNull(condition);
    assertFalse(condition.isEmpty());
    assertTrue(condition.isAtomic());
    assertEquals("B", condition.getLeftValuename());
    assertEquals(">", condition.getFunctionDesc());
    assertEquals("5000", condition.getRightExactString());
  }
  
  public void testSqlField13_Function() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldClause= "IIF( B>50, 'high', 'low' ) as nrSize";
    
    SQLField field = new SQLField(fieldClause, rowMeta);
    assertEquals("IIF( B>50, 'high', 'low' )", field.getName());
    assertEquals("nrSize", field.getAlias());
    assertNull("The service data type was discovered", field.getValueMeta());
    
    assertNotNull(field.getIif());
    Condition condition = field.getIif().getSqlCondition().getCondition();
    assertNotNull(condition);
    assertFalse(condition.isEmpty());
    assertTrue(condition.isAtomic());
    assertEquals("B", condition.getLeftValuename());
    assertEquals(">", condition.getFunctionDesc());
    assertEquals("50", condition.getRightExactString());
  }
  
  
  
  private RowMetaInterface generateTestRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("A", ValueMetaInterface.TYPE_STRING, 50));
    rowMeta.addValueMeta(new ValueMeta("B", ValueMetaInterface.TYPE_INTEGER, 7));
    return rowMeta;
  }
}
