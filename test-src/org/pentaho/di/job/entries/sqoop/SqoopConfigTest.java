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

import org.junit.Test;
import org.pentaho.ui.xul.util.AbstractModelList;

import static org.junit.Assert.assertEquals;

/**
 * Test the SqoopConfig functionality not exercised by {@link PropertyFiringObjectTests}
 */
public class SqoopConfigTest {

  @Test
  public void addRemovePropertyChangeListener() {
    SqoopConfig config = new SqoopConfig() {
    };

    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();

    config.addPropertyChangeListener(l);
    config.setJobEntryName("test");
    assertEquals(1, l.getReceivedEvents().size());
    config.removePropertyChangeListener(l);
    config.setJobEntryName("test1");
    assertEquals(1, l.getReceivedEvents().size());
  }

  @Test
  public void addRemovePropertyChangeListener_propertyName() {
    SqoopConfig config = new SqoopConfig() {
    };

    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();

    config.addPropertyChangeListener("test", l);
    config.setJobEntryName("test");
    assertEquals(0, l.getReceivedEvents().size());
    config.removePropertyChangeListener("test", l);

    config.addPropertyChangeListener(SqoopConfig.JOB_ENTRY_NAME, l);
    config.setJobEntryName("test1");
    assertEquals(1, l.getReceivedEvents().size());
    config.removePropertyChangeListener(SqoopConfig.JOB_ENTRY_NAME, l);
    config.setJobEntryName("test2");
    assertEquals(1, l.getReceivedEvents().size());
  }

  @Test
  public void getAdvancedArgumentsList() {
    SqoopConfig config = new SqoopConfig() {
    };

    AbstractModelList<ArgumentWrapper> args = config.getAdvancedArgumentsList();
    assertEquals(33, args.size());

    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();
    config.addPropertyChangeListener(l);

    // Make sure we can get and set the value for all arguments returned
    String value = String.valueOf(System.currentTimeMillis());
    for (ArgumentWrapper arg : args) {
      arg.setValue(value);
      assertEquals(value, arg.getValue());
    }

    // We should have received one event for every property changed
    assertEquals(33, l.getReceivedEvents().size());
  }

  @Test
  public void testClone() {
    SqoopConfig config = new SqoopConfig() {
    };

    config.setConnect(SqoopConfig.CONNECT);
    config.setJobEntryName(SqoopConfig.JOB_ENTRY_NAME);

    SqoopConfig clone = config.clone();

    assertEquals(config.getConnect(), clone.getConnect());
    assertEquals(config.getJobEntryName(), clone.getJobEntryName());
  }

  @Test
  public void setDatabaseConnectionInformation() {
    SqoopConfig config = new SqoopConfig() {
    };

    PersistentPropertyChangeListener l = new PersistentPropertyChangeListener();
    config.addPropertyChangeListener(l);

    String database = "bogus";
    String connect = "jdbc:bogus://bogus";
    String username = "bob";
    String password = "uncle";


    config.setDatabaseConnectionInformation(database, connect, username, password);

    assertEquals(0, l.getReceivedEvents().size());
    assertEquals(database, config.getDatabase());
    assertEquals(connect, config.getConnect());
    assertEquals(username, config.getUsername());
    assertEquals(password, config.getPassword());
  }
}