package org.pentaho.di.core.sql;

import java.util.List;

import org.pentaho.di.core.exception.KettleSQLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

import junit.framework.TestCase;

public class SQLTest extends TestCase {
  
  
  public void testSql01() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String sqlString = "SELECT A, B, C\nFROM Service\nWHERE B > 5\nORDER BY B DESC";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertEquals("A, B, C", sql.getSelectClause());
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals(3, selectFields.size());
    
    assertEquals("B > 5", sql.getWhereClause());
    SQLCondition whereCondition = sql.getWhereCondition();
    assertNotNull(whereCondition.getCondition());
    
    assertNull(sql.getGroupClause());
    assertNull(sql.getHavingClause());
    assertEquals("B DESC", sql.getOrderClause());
    List<SQLField> orderFields = sql.getOrderFields().getFields();
    assertEquals(1, orderFields.size());
    SQLField orderField = orderFields.get(0);
    assertTrue(orderField.isOrderField());
    assertFalse(orderField.isAscending());
    assertNull(orderField.getAlias());
    assertEquals("B", orderField.getValueMeta().getName().toUpperCase());
  }
  
  public void testSql02() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String sqlString = "SELECT A as \"FROM\", B as \"TO\", C\nFROM Service\nWHERE B > 5\nORDER BY B DESC";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertEquals("A as \"FROM\", B as \"TO\", C", sql.getSelectClause());
    assertEquals("B > 5", sql.getWhereClause());
    assertNull(sql.getGroupClause());
    assertNull(sql.getHavingClause());
    assertEquals("B DESC", sql.getOrderClause());
  }
  
  public void testSql03() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String sqlString = "SELECT A as \"FROM\", B as \"TO\", C, COUNT(*)\nFROM Service\nWHERE B > 5\nGROUP BY A,B,C\nHAVING COUNT(*) > 100\nORDER BY A,B,C";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);

    assertEquals("A as \"FROM\", B as \"TO\", C, COUNT(*)", sql.getSelectClause());
    assertEquals("B > 5", sql.getWhereClause());
    assertEquals("A,B,C", sql.getGroupClause());
    assertEquals("COUNT(*) > 100", sql.getHavingClause());
    assertEquals("A,B,C", sql.getOrderClause());
  }

  public void testSql04() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String sqlString = "SELECT *\nFROM Service\nWHERE B > 5\nORDER BY B DESC";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertEquals("*", sql.getSelectClause());
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals(3, selectFields.size());
    
    assertEquals("B > 5", sql.getWhereClause());
    SQLCondition whereCondition = sql.getWhereCondition();
    assertNotNull(whereCondition.getCondition());
    
    assertNull(sql.getGroupClause());
    assertNull(sql.getHavingClause());
    assertEquals("B DESC", sql.getOrderClause());
    List<SQLField> orderFields = sql.getOrderFields().getFields();
    assertEquals(1, orderFields.size());
    SQLField orderField = orderFields.get(0);
    assertTrue(orderField.isOrderField());
    assertFalse(orderField.isAscending());
    assertNull(orderField.getAlias());
    assertEquals("B", orderField.getValueMeta().getName().toUpperCase());
  }
  
  public void testSql05() throws KettleSQLException {
    RowMetaInterface rowMeta = generateTestRowMeta();
    
    String sqlString = "SELECT count(*) as NrOfRows FROM Service";
    
    SQL sql = new SQL(sqlString);
    
    assertEquals("Service", sql.getServiceName());
    sql.parse(rowMeta);
    
    assertEquals("count(*) as NrOfRows", sql.getSelectClause());
    List<SQLField> selectFields = sql.getSelectFields().getFields();
    assertEquals(1, selectFields.size());
    SQLField countField = selectFields.get(0);
    assertTrue(countField.isCountStar());
    assertEquals("count(*)", countField.getField());
    
    assertNull(sql.getGroupClause());
    assertNotNull(sql.getGroupFields());
    assertNull(sql.getHavingClause());
    assertNull(sql.getOrderClause());
  }
  
  
  private RowMetaInterface generateTestRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("A", ValueMetaInterface.TYPE_STRING, 50));
    rowMeta.addValueMeta(new ValueMeta("B", ValueMetaInterface.TYPE_INTEGER, 7));
    rowMeta.addValueMeta(new ValueMeta("C", ValueMetaInterface.TYPE_INTEGER, 7));
    return rowMeta;
  }
}
