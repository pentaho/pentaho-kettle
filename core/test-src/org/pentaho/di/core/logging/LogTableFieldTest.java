package org.pentaho.di.core.logging;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class LogTableFieldTest {
  List<LogTableField> collection = new ArrayList<LogTableField>();
  private final static String FIELD_ID1 = "FIELD_ID"; 
  
  @Test
  public void testEqualsObject() {
    LogTableField f1 = new LogTableField( FIELD_ID1, "", "" );
    LogTableField f2 = new LogTableField( FIELD_ID1, "", "" );
    
    assertEquals( "Fields with the same id are assumed to be equal", f1, f2 );
    
    collection.add( f1 );
    int index = collection.indexOf( f2 );
    assertTrue( "The object should be found", index != -1 );
  }

}
