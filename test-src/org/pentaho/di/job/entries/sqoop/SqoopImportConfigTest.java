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

package org.pentaho.di.job.entries.sqoop;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;
import org.pentaho.di.job.ArgumentWrapper;
import org.pentaho.ui.xul.util.AbstractModelList;

public class SqoopImportConfigTest {

  @Test
  public void getAdvancedArgumentsList() throws Exception {
    SqoopImportConfig c = new SqoopImportConfig();

    // Dummy Argument Wrappers that just need the proper name to match the .equals() check for looking up items in the list
    Method dummyGetter = getClass().getMethod("toString");
    Method dummySetter = SqoopImportConfig.class.getMethod("setHbaseZookeeperQuorum", String.class);
    ArgumentWrapper hbaseZookeeperQuorum = new ArgumentWrapper(SqoopImportConfig.HBASE_ZOOKEEPER_QUORUM, null, false,
        this, dummyGetter, dummySetter);
    ArgumentWrapper hbaseZookeeperClientPort = new ArgumentWrapper(SqoopImportConfig.HBASE_ZOOKEEPER_CLIENT_PORT, null,
        false, this, dummyGetter, dummySetter);

    AbstractModelList<ArgumentWrapper> items = c.getAdvancedArgumentsList();
    assertEquals(70, items.size());
    int indexOf = items.indexOf(hbaseZookeeperQuorum);
    assertEquals("Expected to find HBase Zookeeper Quorum property grouped with others", 35, indexOf);
    indexOf = items.indexOf(hbaseZookeeperClientPort);
    assertEquals("Expected to find HBase Zookeeper Client Port grouped with others", 36, indexOf);
    
  }

}
