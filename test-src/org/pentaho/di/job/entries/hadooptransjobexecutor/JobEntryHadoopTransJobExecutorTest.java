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

package org.pentaho.di.job.entries.hadooptransjobexecutor;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.plugins.*;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.steps.hadoopenter.HadoopEnterMeta;
import org.pentaho.di.trans.steps.hadoopexit.HadoopExitMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

// TODO Refactor JobEntryHadoopTransJobExecutor so it can be tested better than this pseudo-integration test
public class JobEntryHadoopTransJobExecutorTest {

  @BeforeClass
  public static final void setup() throws Exception {
    KettleEnvironment.init();

    // Register Map/Reduce Input and Map/Reduce Output plugin steps
    PluginMainClassType mainClassTypesAnnotation = StepPluginType.class.getAnnotation(PluginMainClassType.class);

    Map<Class<?>, String> inputClassMap = new HashMap<Class<?>, String>();
    inputClassMap.put(mainClassTypesAnnotation.value(), HadoopEnterMeta.class.getName());
    PluginInterface inputStepPlugin = new Plugin(new String[]{"HadoopEnterPlugin"}, StepPluginType.class, mainClassTypesAnnotation.value(), "Hadoop", "MapReduce Input", "Enter a Hadoop Mapper or Reducer transformation", "MRI.png", false, false, inputClassMap, new ArrayList<String>(), null, null);
    PluginRegistry.getInstance().registerPlugin(StepPluginType.class, inputStepPlugin);

    Map<Class<?>, String> outputClassMap = new HashMap<Class<?>, String>();
    outputClassMap.put(mainClassTypesAnnotation.value(), HadoopExitMeta.class.getName());
    PluginInterface outputStepPlugin = new Plugin(new String[]{"HadoopExitPlugin"}, StepPluginType.class, mainClassTypesAnnotation.value(), "Hadoop", "MapReduce Output", "Exit a Hadoop Mapper or Reducer transformation", "MRO.png", false, false, outputClassMap, new ArrayList<String>(), null, null);
    PluginRegistry.getInstance().registerPlugin(StepPluginType.class, outputStepPlugin);

  }

  // TODO Remove throws Throwable when those contructors are fixed!
  @Test
  public void invalidMapperStepNames() throws Throwable {
    Job job = new Job();
    JobEntryHadoopTransJobExecutor executor = new JobEntryHadoopTransJobExecutor();
    executor.setParentJob(job);
    executor.setHadoopJobName("hadoop job name");

    executor.setMapTrans("test-res/mr-passthrough.ktr");

    Result result = new Result();

    // No input step name should fail
    executor.execute(result, 0);
    assertEquals(1, result.getNrErrors());

    // Invalid input step name should fail
    result.clear();
    executor.setMapInputStepName("Testing");
    executor.execute(result, 0);
    assertEquals(1, result.getNrErrors());

    // No output step name should fail
    result.clear();
    executor.setMapInputStepName("Injector");
    executor.execute(result, 0);
    assertEquals(1, result.getNrErrors());

    // Invalid output step name should fail
    result.clear();
    executor.setMapInputStepName("Injector");
    executor.setMapOutputStepName("Testing");
    executor.execute(result, 0);
    assertEquals(1, result.getNrErrors());
  }
}
