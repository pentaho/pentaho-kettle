/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
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
