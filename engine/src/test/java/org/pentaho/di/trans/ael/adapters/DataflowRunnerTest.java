/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.adapters;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * TODO Create a better integration test for this
 *
 * Created by ccaspanello on 6/26/18.
 */
public class DataflowRunnerTest {

  @Ignore
  @Test
  public void test(){
    String runner = "DirectRunner";
    File applicationJar = new File("/Users/ccaspanello/Desktop/kettle-flow/dataflow-engine/target","dataflow-engine-bundled-1.0-SNAPSHOT.jar");
    DataflowRunner dataflowRunner = new DataflowRunner(new TestLogger(), runner, applicationJar);
      //runner.setDebug();
      dataflowRunner.run("/Users/ccaspanello/Desktop/Dataflow.ktr");
  }
}
