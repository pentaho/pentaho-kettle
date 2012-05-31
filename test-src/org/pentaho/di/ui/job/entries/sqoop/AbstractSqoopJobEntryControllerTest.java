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

package org.pentaho.di.ui.job.entries.sqoop;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sqoop.AbstractSqoopJobEntry;
import org.pentaho.di.job.entries.sqoop.SqoopConfig;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.impl.XulFragmentContainer;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AbstractSqoopJobEntryControllerTest {

  private class TestSqoopJobEntryController extends AbstractSqoopJobEntryController<SqoopConfig> {

    private TestSqoopJobEntryController() {
      super(new JobMeta(), new XulFragmentContainer(null), new AbstractSqoopJobEntry() {
        @Override
        protected SqoopConfig buildSqoopConfig() {
          return new SqoopConfig() {
          };
        }

        @Override
        protected String getToolName() {
          return null;
        }
      }, new DefaultBindingFactory());
    }

    @Override
    public String getDialogElementId() {
      return null;
    }

    @Override
    protected void editDatabaseMeta(DatabaseMeta database, boolean isNew) {
      // do nothing in tests
    }

    @Override
    protected void setDatabaseInteractionButtonsDisabled(boolean b) {
      // do nothing in tests
    }
  }

  @BeforeClass
  public static void setup() throws KettleException {
    KettleEnvironment.init();
  }

  @Test
  public void populateDatabases() throws XulException, InvocationTargetException {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();

    controller.getJobMeta().addDatabase(new DatabaseMeta("test", "MYSQL", null, null, null, null, null, null));
    controller.populateDatabases();

    assertEquals(2, controller.databaseConnections.size());
    assertEquals(controller.NO_DATABASE, controller.databaseConnections.get(0));
    assertEquals("test", controller.databaseConnections.get(1));
  }

  @Test
  public void getSelectedDatabaseConnection() throws InvocationTargetException {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();

    String database = "testing";
    controller.getConfig().setDatabase(database);

    assertEquals(database, controller.getSelectedDatabaseConnection());
  }

  @Test
  public void setSelectedDatabaseConnection() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();

    String database = "testing";
    controller.setSelectedDatabaseConnection(database);

    assertNull("Setting a database to one that doesn't exist in the JobMeta should not work", controller.getConfig().getDatabase());

    controller.getJobMeta().addDatabase(new DatabaseMeta(database, "MYSQL", null, null, null, null, null, null));
    controller.setSelectedDatabaseConnection(database);

    assertEquals(database, controller.getConfig().getDatabase());
  }


  @Test
  public void setConnectChanged() {
    AbstractSqoopJobEntryController controller = new TestSqoopJobEntryController();

    String database = "testing";
    controller.getConfig().setDatabase(database);

    String connect = "jdbc:bogus://bogus";
    controller.setConnectChanged(connect);

    assertNull(controller.getConfig().getDatabase());
  }
}
