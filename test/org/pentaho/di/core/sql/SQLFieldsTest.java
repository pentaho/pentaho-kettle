package org.pentaho.di.core.sql;

import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import junit.framework.TestCase;

public class SQLFieldsTest extends TestCase {
  
  public void testSqlFromFields01() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldsClause= "A, B";
    
    SQLFields fromFields = new SQLFields(rowMeta, fieldsClause);
    assertFalse(fromFields.isDistinct());
    
    assertEquals(2, fromFields.getFields().size());
    
    SQLField field = fromFields.getFields().get(0);
    assertEquals("A", field.getName());
    assertNull(field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
    assertEquals("A", field.getValueMeta().getName().toUpperCase());

    field = fromFields.getFields().get(1);
    assertEquals("B", field.getName());
    assertNull(field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
    assertEquals("B", field.getValueMeta().getName().toUpperCase());
  }
  
  public void testSqlFromFields02() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldsClause= "A as foo, B";
    
    SQLFields fromFields = new SQLFields(rowMeta, fieldsClause);
    assertFalse(fromFields.isDistinct());
    
    assertEquals(2, fromFields.getFields().size());
    
    SQLField field = fromFields.getFields().get(0);
    assertEquals("A", field.getName());
    assertEquals("foo", field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
    assertEquals("A", field.getValueMeta().getName().toUpperCase());

    field = fromFields.getFields().get(1);
    assertEquals("B", field.getName());
    assertNull(field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
    assertEquals("B", field.getValueMeta().getName().toUpperCase());
  }

  public void testSqlFromFields03() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldsClause= "A, sum( B ) as \"Total sales\"";
    
    SQLFields fromFields = new SQLFields(rowMeta, fieldsClause);
    assertFalse(fromFields.isDistinct());

    assertEquals(2, fromFields.getFields().size());
    
    SQLField field = fromFields.getFields().get(0);
    assertEquals("A", field.getName());
    assertNull(field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
    assertEquals("A", field.getValueMeta().getName().toUpperCase());

    field = fromFields.getFields().get(1);
    assertEquals("B", field.getName());
    assertEquals("Total sales", field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
    assertEquals("B", field.getValueMeta().getName().toUpperCase());
  }
  
  public void testSqlFromFields04() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String fieldsClause= "DISTINCT A as foo, B as bar";
    
    SQLFields fromFields = new SQLFields(rowMeta, fieldsClause);
    assertTrue(fromFields.isDistinct());
    
    assertEquals(2, fromFields.getFields().size());
    
    SQLField field = fromFields.getFields().get(0);
    assertEquals("A", field.getName());
    assertEquals("foo", field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
    assertEquals("A", field.getValueMeta().getName().toUpperCase());

    field = fromFields.getFields().get(1);
    assertEquals("B", field.getName());
    assertEquals("bar", field.getAlias());
    assertNotNull("The service data type was not discovered", field.getValueMeta());
    assertEquals("B", field.getValueMeta().getName().toUpperCase());
  }

  
  private RowMetaInterface generateTestRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("A", ValueMetaInterface.TYPE_STRING, 50));
    rowMeta.addValueMeta(new ValueMeta("B", ValueMetaInterface.TYPE_INTEGER, 7));
    return rowMeta;
  }
}
