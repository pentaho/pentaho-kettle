/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TestHiveDatabaseMeta {

  @Before
  public void setup() {
  }

  @Test
  public void testColumnAlias_060_And_Later() throws Throwable {
    HiveDatabaseMeta dbm = new HiveDatabaseMeta(0, 6);
    
    String alias = dbm.generateColumnAlias(0, "alias");
    assertEquals("alias", alias);
    
    alias = dbm.generateColumnAlias(1, "alias1");
    assertEquals("alias1", alias);
    
    alias = dbm.generateColumnAlias(2, "alias2");
    assertEquals("alias2", alias);
  }
  
  @Test
  public void testColumnAlias_050() throws Throwable {
    HiveDatabaseMeta dbm = new HiveDatabaseMeta(0, 5);
    
    String alias = dbm.generateColumnAlias(0, "alias");
    assertEquals("_col0", alias);
    
    alias = dbm.generateColumnAlias(1, "alias1");
    assertEquals("_col1", alias);
    
    alias = dbm.generateColumnAlias(2, "alias2");
    assertEquals("_col2", alias);
  }
}
