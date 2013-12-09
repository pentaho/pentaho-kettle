package org.pentaho.di.core.sql;

import junit.framework.TestCase;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class IifFunctionTest extends TestCase {
  public void testIifFunction01() throws Exception {
    
    RowMetaInterface serviceFields = generateTestRowMeta();
    
    String conditionClause = "B>5000";
    String trueValueString= "'Big'";
    String falseValueString= "'Small'";

    IifFunction function = new IifFunction("Service", conditionClause, trueValueString, falseValueString, serviceFields);
    assertNotNull(function.getSqlCondition());
    Condition condition = function.getSqlCondition().getCondition();
    assertNotNull(condition);
    assertTrue(condition.isAtomic());
    assertEquals("B", condition.getLeftValuename());
    assertEquals(">", condition.getFunctionDesc());
    assertEquals("5000", condition.getRightExactString());
    
    // test the value data type determination
    //
    assertNotNull(function.getTrueValue());
    assertEquals(ValueMetaInterface.TYPE_STRING, function.getTrueValue().getValueMeta().getType());
    assertEquals("Big", function.getTrueValue().getValueData());
    assertNotNull(function.getFalseValue());
    assertEquals(ValueMetaInterface.TYPE_STRING, function.getFalseValue().getValueMeta().getType());
    assertEquals("Small", function.getFalseValue().getValueData());
  }
  
  
  
  
  private RowMetaInterface generateTestRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMeta("A", ValueMetaInterface.TYPE_STRING, 50));
    rowMeta.addValueMeta(new ValueMeta("B", ValueMetaInterface.TYPE_INTEGER, 7));
    return rowMeta;
  }
}
