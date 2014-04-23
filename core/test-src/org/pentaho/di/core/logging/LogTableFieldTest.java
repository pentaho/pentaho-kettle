/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.logging;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class LogTableFieldTest {
  List<LogTableField> collection = new ArrayList<LogTableField>();
  private static final String FIELD_ID1 = "FIELD_ID";

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
