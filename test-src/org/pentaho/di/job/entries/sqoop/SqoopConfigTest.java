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

import java.util.List;

import static org.junit.Assert.*;

public class SqoopConfigTest {

  @Test
  public void getCommandLineArgs_empty() {
    SqoopConfig config = new SqoopExportConfig();
    assertEquals(0, config.getCommandLineArgs().size());

    // Job Entry Name is not annotated so it shouldn't be added to the args list
    config.setJobEntryName("testing");
    assertEquals(0, config.getCommandLineArgs().size());
  }

  @Test
  public void getCommandLineArgs_boolean() {
    SqoopConfig config = new SqoopExportConfig();

    config.setArgumentValue("verbose", Boolean.toString(true));

    assertEquals(1, config.getCommandLineArgs().size());
    assertEquals("--verbose", config.getCommandLineArgs().get(0));
  }

  @Test
  public void getArgumentItem() {
    SqoopConfig config = new SqoopExportConfig();
    assertNull(config.getArgument("unknownArgument!!!"));
    assertNotNull(config.getArgument("connect"));
  }

  @Test
  public void blockingExecution() {
    SqoopConfig config = new SqoopExportConfig();

    // Default is true
    assertTrue(config.isBlockingExecution());
    config.setBlockingExecution(false);
    assertFalse(config.isBlockingExecution());
  }

  @Test
  public void blockingPollingInterval() {
    SqoopConfig config = new SqoopExportConfig();
    String pollingInterval = "100";

    // 300ms is the default polling interval
    assertEquals("300", config.getBlockingPollingInterval());
    config.setBlockingPollingInterval(pollingInterval);
    assertEquals(pollingInterval, config.getBlockingPollingInterval());
  }

  @Test
  public void namenodeHost() {
    SqoopConfig config = new SqoopImportConfig();
    String host = "host";
    assertNull(config.getNamenodeHost());
    config.setNamenodeHost(host);
    assertEquals(host, config.getNamenodeHost());
  }

  @Test
  public void namenodePort() {
    SqoopConfig config = new SqoopImportConfig();
    String port = "port";
    assertNull(config.getNamenodePort());
    config.setNamenodePort(port);
    assertEquals(port, config.getNamenodePort());
  }

  @Test
  public void jobtrackerHost() {
    SqoopConfig config = new SqoopImportConfig();
    String host = "host";
    assertNull(config.getJobtrackerHost());
    config.setJobtrackerHost(host);
    assertEquals(host, config.getJobtrackerHost());
  }

  @Test
  public void jobtrackerPort() {
    SqoopConfig config = new SqoopImportConfig();
    String port = "port";
    assertNull(config.getJobtrackerPort());
    config.setJobtrackerPort(port);
    assertEquals(port, config.getJobtrackerPort());
  }

  @Test
  public void copyArguments() {
    SqoopConfig config = new SqoopExportConfig();

    String property1 = "connect";
    String property2 = "export-dir";
    config.setArgumentValue(property1, "jdbc:mysql://localhost:3306/test");
    config.setArgumentValue(property2, "/folder");

    SqoopConfig config2 = new SqoopExportConfig();
    config2.copyArguments(config);

    assertEquals(config.getArgument(property1).getValue(), config2.getArgument(property1).getValue());
    assertEquals(config.getArgument(property2).getValue(), config2.getArgument(property2).getValue());
  }

  @Test
  public void clone_no_shared_objects() {
    SqoopConfig c = new SqoopExportConfig();
    SqoopConfig clone = c.clone();

    List<Argument> cArgs = c.getArguments();
    List<Argument> cloneArgs = clone.getArguments();

    // Make sure no arguments are shared
    for (Argument ai : cArgs) {
      for(Argument ai2 : cloneArgs) {
        assertFalse( ai == ai2);
      }
    }

    // Double check that setting any value does not bleed into the original
    clone.setArgumentValue("namenodeHost", "testing");
    assertEquals("testing", clone.getArgument("namenodeHost").getValue());
    clone.setArgumentValue("connect", "test-connect");
    assertEquals("test-connect", clone.getArgument("connect").getValue());
    assertNull(c.getJobEntryName());
    assertNull(c.getArgument("connect").getValue());
  }
}
