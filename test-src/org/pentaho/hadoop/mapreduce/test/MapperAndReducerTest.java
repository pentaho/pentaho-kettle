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
package org.pentaho.hadoop.mapreduce.test;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters.Counter;
import org.apache.hadoop.mapred.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.plugins.*;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.hadoopenter.HadoopEnterMeta;
import org.pentaho.di.trans.steps.hadoopexit.HadoopExitMeta;
import org.pentaho.hadoop.mapreduce.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@SuppressWarnings({ "nls", "unchecked", "deprecation", "rawtypes" })
public class MapperAndReducerTest {
  
  private boolean debug = false;
  
  @Before
  public void setup() {
  }
  
  @After
  public void shutdown() {
    
  }
  
  @Test
  public void testMapperBadInjectorFields() throws IOException, KettleException {
    try {
      GenericTransMap mapper = new GenericTransMap();
      MockOutputCollector outputCollector = new MockOutputCollector();
      MockReporter reporter = new MockReporter();
    
      mapper.configure(createJobConf("./test-res/bad-injector-fields.ktr", "./test-res/bad-injector-fields.ktr"));
      
      mapper.map(new Text("key"), new Text("value"), outputCollector, reporter);
      fail("Should have thrown an exception");
    } catch (IOException e) {
      assertTrue("Test for KettleException", e.getMessage().contains("key or value is not defined in transformation injector step"));
    }
  }
  
  @Test
  public void testMapperBadOutputFields() throws IOException, KettleException {
    try {
      GenericTransMap mapper = new GenericTransMap();
      MockOutputCollector outputCollector = new MockOutputCollector();
      MockReporter reporter = new MockReporter();
    
      mapper.configure(createJobConf("./test-res/bad-output-fields.ktr", "./test-res/bad-output-fields.ktr"));
      
      mapper.map(new Text("key"), new Text("value"), outputCollector, reporter);
      fail("Should have thrown an exception");
    } catch (IOException e) {
      assertTrue("Test for KettleException", e.getMessage().contains("outKey or outValue is not defined in transformation output stream"));
    }
  }
  
  @Test
  public void testMapperNoInjectorStep() throws IOException, KettleException {
    try {
      GenericTransMap mapper = new GenericTransMap();
      MockOutputCollector outputCollector = new MockOutputCollector();
      MockReporter reporter = new MockReporter();
    
      mapper.configure(createJobConf("./test-res/no-injector-step.ktr", "./test-res/no-injector-step.ktr"));
      
      mapper.map(new Text("key"), new Text("value"), outputCollector, reporter);
      fail("Should have thrown an exception");
    } catch (IOException e) {
      assertTrue("Test for KettleException", e.getMessage().contains("Unable to find thread with name Injector and copy number 0"));
    }
  }
  
  @Test
  public void testMapperNoOutputStep() throws IOException, KettleException {
    try {
      GenericTransMap mapper = new GenericTransMap();
      MockOutputCollector outputCollector = new MockOutputCollector();
      MockReporter reporter = new MockReporter();
    
      mapper.configure(createJobConf("./test-res/no-output-step.ktr", "./test-res/no-output-step.ktr"));
      
      mapper.map(new Text("key"), new Text("value"), outputCollector, reporter);
      fail("Should have thrown an exception");
    } catch (IOException e) {
      assertTrue("Test for KettleException", e.getMessage().contains("Output step not defined in transformation"));
    }
  }
  
  @Test
  public void testReducerBadInjectorFields() throws IOException, KettleException {
    try {
      GenericTransReduce reducer = new GenericTransReduce();
      MockOutputCollector outputCollector = new MockOutputCollector();
      MockReporter reporter = new MockReporter();
    
      reducer.configure(createJobConf("./test-res/bad-injector-fields.ktr", "./test-res/bad-injector-fields.ktr"));
      
      reducer.reduce(new Text("key"), Arrays.asList(new Text("value")).iterator(), outputCollector, reporter);
      fail("Should have thrown an exception");
    } catch (IOException e) {
      assertTrue("Test for KettleException", e.getMessage().contains("key or value is not defined in transformation injector step"));
    }
  }
  
  @Test
  public void testReducerBadOutputFields() throws IOException, KettleException {
    try {
      GenericTransReduce reducer = new GenericTransReduce();
      MockOutputCollector outputCollector = new MockOutputCollector();
      MockReporter reporter = new MockReporter();
    
      reducer.configure(createJobConf("./test-res/bad-output-fields.ktr", "./test-res/bad-output-fields.ktr"));
      
      reducer.reduce(new Text("key"), Arrays.asList(new Text("value")).iterator(), outputCollector, reporter);
      fail("Should have thrown an exception");
    } catch (IOException e) {
      assertTrue("Test for KettleException", e.getMessage().contains("outKey or outValue is not defined in transformation output stream"));
    }
  }
  
  @Test
  public void testReducerNoInjectorStep() throws IOException, KettleException {
    try {
      GenericTransReduce reducer = new GenericTransReduce();
      MockOutputCollector outputCollector = new MockOutputCollector();
      MockReporter reporter = new MockReporter();
    
      reducer.configure(createJobConf("./test-res/no-injector-step.ktr", "./test-res/no-injector-step.ktr"));
      
      reducer.reduce(new Text("key"), Arrays.asList(new Text("value")).iterator(), outputCollector, reporter);
      fail("Should have thrown an exception");
    } catch (IOException e) {
      assertTrue("Test for KettleException", e.getMessage().contains("Unable to find thread with name Injector and copy number 0"));
    }
  }
  
  @Test
  public void testReducerNoOutputStep() throws IOException, KettleException {
    try {
      GenericTransReduce reducer = new GenericTransReduce();
      MockOutputCollector outputCollector = new MockOutputCollector();
      MockReporter reporter = new MockReporter();
    
      reducer.configure(createJobConf("./test-res/no-output-step.ktr", "./test-res/no-output-step.ktr"));
      
      reducer.reduce(new Text("key"), Arrays.asList(new Text("value")).iterator(), outputCollector, reporter);
      fail("Should have thrown an exception");
    } catch (IOException e) {
      assertTrue("Test for KettleException", e.getMessage().contains("Output step not defined in transformation"));
    }
  }
  
  @Test
  @Ignore
  public void testWordCount() throws IOException, KettleException {
      GenericTransMap mapper = new GenericTransMap();
      MockOutputCollector outputCollector = new MockOutputCollector();
      MockReporter reporter = new MockReporter();
    
      mapper.configure(createJobConf("./test-res/wordcount-mapper.ktr", "./test-res/wordcount-reducer.ktr"));
      
//      mapper.map(new Text("key"), new Text("a quick brown fox jumped over the fence"), outputCollector, reporter);
//      mapper.map(new Text("key"), new Text("another quick brown fox jumped over the same fence"), outputCollector, reporter);
//      mapper.map(new Text("key"), new Text("but then a quicker brown fox jumped over the fence"), outputCollector, reporter);
//      mapper.map(new Text("key"), new Text("Hadoop pentaho hadoop"), outputCollector, reporter);
//      mapper.map(new Text("key"), new Text("Pentaho"), outputCollector, reporter);
//      mapper.map(new Text("key"), new Text("pentaho"), outputCollector, reporter);
//      mapper.map(new Text("key"), new Text("Only totally unique words go here"), outputCollector, reporter);

      int ROWS = 100000;
      
      long start = System.currentTimeMillis();
      for (int i=0;i<ROWS;i++) {
        mapper.map(new Text("key"), new Text("zebra giraffe hippo elephant tiger"), outputCollector, reporter);
      }
      mapper.close();
      outputCollector.close(); // Immediately stop collecting.  This reproduces the execution environment when running within Hadoop.
      long stop = System.currentTimeMillis();
      System.out.println("Executed " + ROWS + " in " + (stop-start) + "ms");
      System.out.println("Average: " + ((stop-start)/(float)ROWS) + "ms");
      System.out.println("Rows/Second: " + ((float)ROWS / ((stop-start)/1000f)));
      
      
      class CountValues {
        private Object workingKey;
        
        public CountValues setWorkingKey(Object k) {
          workingKey = k;
          return this;
        }
        
        public void countValues(String k, Object v, MockOutputCollector oc) {
          if(workingKey.equals(new Text(k))) {
            assertEquals(k.toString(), v, oc.getCollection().get(new Text(k)).size());
          }
        }
      }
      
      assertNotNull(outputCollector);
      assertNotNull(outputCollector.getCollection());
      assertNotNull(outputCollector.getCollection().keySet());
      assertEquals(5, outputCollector.getCollection().keySet().size());
      
      CountValues cv = new CountValues();
      for(Object key : outputCollector.getCollection().keySet()) {
        cv.setWorkingKey(key).countValues("zebra", ROWS, outputCollector);
        cv.setWorkingKey(key).countValues("giraffe", ROWS, outputCollector);
        cv.setWorkingKey(key).countValues("hippo", ROWS, outputCollector);
        cv.setWorkingKey(key).countValues("elephant", ROWS, outputCollector);
        cv.setWorkingKey(key).countValues("tiger", ROWS, outputCollector);
        //TODO: Add words that will not exist: unique words, same words - diff case
        
        if(debug) {
          for(Object value : outputCollector.getCollection().get(key)) {
            System.out.println(key + ": " + value);
          }
        }
      }

      GenericTransReduce reducer = new GenericTransReduce();
      MockOutputCollector inputCollector = outputCollector;
      outputCollector = new MockOutputCollector();
    
      reducer.configure(createJobConf("./test-res/wordcount-mapper.ktr", "./test-res/wordcount-reducer.ktr"));
      
      start = System.currentTimeMillis();
      for(Object key : inputCollector.getCollection().keySet()) {
        System.out.println("reducing: " + key);
        reducer.reduce((Text)key, (Iterator)new ArrayList(inputCollector.getCollection().get(key)).iterator(), outputCollector, reporter);
      }
      reducer.close();
      outputCollector.close(); // Immediately stop collecting.  This reproduces the execution environment when running within Hadoop.
      stop = System.currentTimeMillis();
      System.out.println("Executed " + ROWS + " in " + (stop-start) + "ms");
      System.out.println("Average: " + ((stop-start)/(float)ROWS) + "ms");
      System.out.println("Rows/Second: " + ((float)ROWS / ((stop-start)/1000f)));
      
      assertNotNull(outputCollector);
      assertNotNull(outputCollector.getCollection());
      assertNotNull(outputCollector.getCollection().keySet());
      assertEquals(5, outputCollector.getCollection().keySet().size());
      
      class CheckValues {
        private Object workingKey;
        
        public CheckValues setWorkingKey(Object k) {
          workingKey = k;
          return this;
        }
        
        public void checkValues(String k, Object v, MockOutputCollector oc) {
          if(workingKey.equals(new Text(k))) {
            assertEquals(k.toString(), v, ((IntWritable)oc.getCollection().get(new Text(k)).get(0)).get());
          }
        }
      }
      
      CheckValues cv2 = new CheckValues();
      for(Object key : outputCollector.getCollection().keySet()) {
        cv2.setWorkingKey(key).checkValues("zebra", ROWS, outputCollector);
        cv2.setWorkingKey(key).checkValues("giraffe", ROWS, outputCollector);
        cv2.setWorkingKey(key).checkValues("hippo", ROWS, outputCollector);
        cv2.setWorkingKey(key).checkValues("elephant", ROWS, outputCollector);
        cv2.setWorkingKey(key).checkValues("tiger", ROWS, outputCollector);
        
        if(debug) {
          for(Object value : outputCollector.getCollection().get(key)) {
            System.out.println(key + ": " + value);
          }
        }
      }
  }

  public static JobConf createJobConf(String mapperTransformationFile, String reducerTransformationFile) throws IOException, KettleException {
    return createJobConf(mapperTransformationFile, null, reducerTransformationFile, "localhost", "9000", "9001");
  }
  
  public static JobConf createJobConf(String mapperTransformationFile, String combinerTransformationFile, String reducerTransformationFile) throws IOException, KettleException {
    return createJobConf(mapperTransformationFile, combinerTransformationFile, reducerTransformationFile, "localhost", "9000", "9001");
  }
  
  public static JobConf createJobConf(String mapperTransformationFile, String combinerTransformationFile, String reducerTransformationFile, String hostname, String hdfsPort, String trackerPort) throws IOException, KettleException {
    
    JobConf conf = new JobConf();
    conf.setJobName("wordcount");

    KettleEnvironment.init();
    
    
    // Register Map/Reduce Input and Map/Reduce Output plugin steps
    PluginMainClassType mainClassTypesAnnotation = StepPluginType.class.getAnnotation(PluginMainClassType.class);
    
    Map<Class<?>, String> inputClassMap = new HashMap<Class<?>, String>();
    inputClassMap.put(mainClassTypesAnnotation.value(), HadoopEnterMeta.class.getName());
    PluginInterface inputStepPlugin = new Plugin(new String[]{"HadoopEnterPlugin"}, StepPluginType.class, mainClassTypesAnnotation.value(), "Hadoop", "Map/Reduce Input", "Enter a Hadoop Mapper or Reducer transformation", "MRI.png", false, false, inputClassMap, new ArrayList<String>(), null, null);
    PluginRegistry.getInstance().registerPlugin(StepPluginType.class, inputStepPlugin);
    
    Map<Class<?>, String> outputClassMap = new HashMap<Class<?>, String>();
    outputClassMap.put(mainClassTypesAnnotation.value(), HadoopExitMeta.class.getName());
    PluginInterface outputStepPlugin = new Plugin(new String[]{"HadoopExitPlugin"}, StepPluginType.class, mainClassTypesAnnotation.value(), "Hadoop", "Map/Reduce Output", "Exit a Hadoop Mapper or Reducer transformation", "MRO.png", false, false, outputClassMap, new ArrayList<String>(), null, null);
    PluginRegistry.getInstance().registerPlugin(StepPluginType.class, outputStepPlugin);
    
    TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
    
    TransMeta transMeta = null;
    TransConfiguration transConfig = null;
    
    if (mapperTransformationFile != null) {
      conf.setMapperClass((Class<? extends Mapper>) GenericTransMap.class);
      transMeta = new TransMeta(mapperTransformationFile);
      transConfig = new TransConfiguration(transMeta, transExecConfig);
      conf.set("transformation-map-xml", transConfig.getXML());
      conf.set("transformation-map-input-stepname", "Injector");
      conf.set("transformation-map-output-stepname", "Output");
    }

    if (combinerTransformationFile != null) {
      conf.setCombinerClass(GenericTransCombiner.class);
      transMeta = new TransMeta(combinerTransformationFile);
      transConfig = new TransConfiguration(transMeta, transExecConfig);
      conf.set("transformation-combiner-xml", transConfig.getXML());
      conf.set("transformation-combiner-input-stepname", "Injector");
      conf.set("transformation-combiner-output-stepname", "Output");
    }
    
    if (reducerTransformationFile != null) {
      conf.setReducerClass((Class<? extends Reducer>) GenericTransReduce.class);
      transMeta = new TransMeta(reducerTransformationFile);
      transConfig = new TransConfiguration(transMeta, transExecConfig);
      conf.set("transformation-reduce-xml", transConfig.getXML());
      conf.set("transformation-reduce-input-stepname", "Injector");
      conf.set("transformation-reduce-output-stepname", "Output");
    }
    
    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(IntWritable.class);

    File jar = new File("./dist/pentaho-big-data-plugin-TRUNK-SNAPSHOT.jar");


    conf.setInputFormat(TextInputFormat.class);
    conf.setOutputFormat(TextOutputFormat.class);

    FileInputFormat.setInputPaths(conf, new Path("/"));
    FileOutputFormat.setOutputPath(conf, new Path("/"));

    conf.set("fs.default.name", "hdfs://" + hostname + ":" + hdfsPort);
    conf.set("mapred.job.tracker", hostname + ":" + trackerPort);

    conf.setJar(jar.toURI().toURL().toExternalForm());
    conf.setWorkingDirectory(new Path("/tmp/wordcount"));
    
    return conf;
  }
  
  public static String getTransformationXml(String transFilename) throws IOException {
    StringBuilder sb = new StringBuilder();
    
    BufferedReader reader = new BufferedReader(new FileReader(transFilename));

    String line = null;
    while((line = reader.readLine()) != null) {
      sb.append(line + Const.CR);
    }
    
    return sb.toString();
  }
  
  public static class MockOutputCollector implements OutputCollector {

    private Map<Object, ArrayList<Object>> collection = new HashMap<Object, ArrayList<Object>>();
    private AtomicBoolean closed = new AtomicBoolean(false);
    
    public void close() {
      closed.set(true);
    }
    
    @Override
    public void collect(Object arg0, Object arg1) throws IOException {
      if (closed.get()) { return; }
      if(!collection.containsKey(arg0)) {
        collection.put((Object)arg0, new ArrayList<Object>());
      }

      collection.get(arg0).add(arg1);
    }
    
    public Map<Object, ArrayList<Object>> getCollection() {
      return collection;
    }
    
  }
  
  public static class MockReporter implements Reporter {

    @Override
    public void progress() {
      // TODO Auto-generated method stub
      
    }

    @Override
    public Counter getCounter(Enum<?> arg0) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Counter getCounter(String arg0, String arg1) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public InputSplit getInputSplit() throws UnsupportedOperationException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void incrCounter(Enum<?> arg0, long arg1) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void incrCounter(String arg0, String arg1, long arg2) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void setStatus(String arg0) {
      // TODO Auto-generated method stub
      
    }
    
  }

  class MockRecordReader implements RecordReader<Text, Text> {

    private Iterator<String> rowIter;

    int rowNum = -1;

    int totalRows;

    // Make them provide a pre-filled list so we don't confuse the overhead of generating strings 
    // with the time it takes to run the mapper
    public MockRecordReader(List<String> rows) {
      totalRows = rows.size();
      rowIter = rows.iterator();
    }

    @Override
    public boolean next(Text key, Text value) throws IOException {
      if (!rowIter.hasNext()) {
        return false;
      }
      rowNum++;
      key.set(String.valueOf(rowNum));
      value.set(rowIter.next());      
      return true;
    }

    @Override
    public Text createKey() {
      return new Text();
    }

    @Override
    public Text createValue() {
      return new Text();
    }

    @Override
    public long getPos() throws IOException {
      return rowNum;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public float getProgress() throws IOException {
      return (rowNum + 1) / totalRows;
    }

  }

  @Test
  public void testMapRunnable_wordCount() throws IOException, KettleException {
    PentahoMapRunnable mapRunnable = new PentahoMapRunnable();
    MockOutputCollector outputCollector = new MockOutputCollector();
    MockReporter reporter = new MockReporter();

    mapRunnable.configure(createJobConf("./test-res/wordcount-mapper.ktr", "./test-res/wordcount-reducer.ktr"));

    final int ROWS = 500000;

    List<String> strings = new ArrayList<String>();
    for (int i = 0; i < ROWS; i++) {
      strings.add("zebra giraffe hippo elephant tiger");
    }

    MockRecordReader reader = new MockRecordReader(strings);

    long start = System.currentTimeMillis();
    mapRunnable.run(reader, outputCollector, reporter);
    outputCollector.close();
    long stop = System.currentTimeMillis();
    System.out.println("Executed " + ROWS + " in " + (stop - start) + "ms");
    System.out.println("Average: " + ((stop - start) / (float) ROWS) + "ms");
    System.out.println("Rows/Second: " + ((float) ROWS / ((stop - start) / 1000f)));

    class CountValues {
      private Object workingKey;

      public CountValues setWorkingKey(Object k) {
        workingKey = k;
        return this;
      }

      public void countValues(String k, Object v, MockOutputCollector oc) {
        if (workingKey.equals(new Text(k))) {
          assertEquals(k.toString(), v, oc.getCollection().get(new Text(k)).size());
        }
      }
    }

    assertNotNull(outputCollector);
    assertNotNull(outputCollector.getCollection());
    assertNotNull(outputCollector.getCollection().keySet());
    assertEquals(5, outputCollector.getCollection().keySet().size());

    CountValues cv = new CountValues();
    for (Object key : outputCollector.getCollection().keySet()) {
      cv.setWorkingKey(key).countValues("zebra", ROWS, outputCollector);
      cv.setWorkingKey(key).countValues("giraffe", ROWS, outputCollector);
      cv.setWorkingKey(key).countValues("hippo", ROWS, outputCollector);
      cv.setWorkingKey(key).countValues("elephant", ROWS, outputCollector);
      cv.setWorkingKey(key).countValues("tiger", ROWS, outputCollector);
      //TODO: Add words that will not exist: unique words, same words - diff case

      if (debug) {
        for (Object value : outputCollector.getCollection().get(key)) {
          System.out.println(key + ": " + value);
        }
      }
    }

    GenericTransReduce reducer = new GenericTransReduce();
    MockOutputCollector inputCollector = outputCollector;
    outputCollector = new MockOutputCollector();

    reducer.configure(createJobConf("./test-res/wordcount-mapper.ktr", "./test-res/wordcount-reducer.ktr"));

    start = System.currentTimeMillis();
    for (Object key : inputCollector.getCollection().keySet()) {
      System.out.println("reducing: " + key);
      reducer.reduce((Text) key, (Iterator) new ArrayList(inputCollector.getCollection().get(key)).iterator(),
          outputCollector, reporter);
    }
    reducer.close();
    outputCollector.close();
    stop = System.currentTimeMillis();
    System.out.println("Executed " + ROWS + " in " + (stop - start) + "ms");
    System.out.println("Average: " + ((stop - start) / (float) ROWS) + "ms");
    System.out.println("Rows/Second: " + ((float) ROWS / ((stop - start) / 1000f)));

    assertNotNull(outputCollector);
    assertNotNull(outputCollector.getCollection());
    assertNotNull(outputCollector.getCollection().keySet());
    assertEquals(5, outputCollector.getCollection().keySet().size());

    class CheckValues {
      private Object workingKey;

      public CheckValues setWorkingKey(Object k) {
        workingKey = k;
        return this;
      }

      public void checkValues(String k, Object v, MockOutputCollector oc) {
        if (workingKey.equals(new Text(k))) {
          assertEquals(k.toString(), v, ((IntWritable) oc.getCollection().get(new Text(k)).get(0)).get());
        }
      }
    }

    CheckValues cv2 = new CheckValues();
    for (Object key : outputCollector.getCollection().keySet()) {
      cv2.setWorkingKey(key).checkValues("zebra", ROWS, outputCollector);
      cv2.setWorkingKey(key).checkValues("giraffe", ROWS, outputCollector);
      cv2.setWorkingKey(key).checkValues("hippo", ROWS, outputCollector);
      cv2.setWorkingKey(key).checkValues("elephant", ROWS, outputCollector);
      cv2.setWorkingKey(key).checkValues("tiger", ROWS, outputCollector);

      if (debug) {
        for (Object value : outputCollector.getCollection().get(key)) {
          System.out.println(key + ": " + value);
        }
      }
    }
  }
  
  @Test
  public void testMapper_null_output_value() throws Exception {
    PentahoMapRunnable mapper = new PentahoMapRunnable();
    MockOutputCollector outputCollector = new MockOutputCollector();
    MockReporter reporter = new MockReporter();

    mapper.configure(createJobConf("./test-res/null-test.ktr", "./test-res/null-test.ktr"));

    MockRecordReader reader = new MockRecordReader(Arrays.asList("test"));

    mapper.run(reader, outputCollector, reporter);
    outputCollector.close();

    Exception ex = mapper.getException();
    if (ex != null) {
      ex.printStackTrace();
    }
    assertNull("Exception thrown", ex);
    assertEquals("Received output when we didn't expect any.  <null>s aren't passed through.", 0, outputCollector.getCollection().size());
  }

  @Test
  public void testCombiner_null_output_value()  throws Exception {
    GenericTransCombiner combiner = new GenericTransCombiner();
    MockOutputCollector outputCollector = new MockOutputCollector();
    MockReporter reporter = new MockReporter();

    combiner.configure(createJobConf(null, "./test-res/null-test.ktr", null));

    combiner.reduce(new Text("0"), Arrays.asList(new Text("test")).iterator(), outputCollector, reporter);
    outputCollector.close();

    Exception ex = combiner.getException();
    if (ex != null) {
      ex.printStackTrace();
    }
    assertNull("Exception thrown", ex);
    assertEquals("Received output when we didn't expect any.  <null>s aren't passed through.", 0, outputCollector.getCollection().size());
  }
  
  @Test
  public void testReducer_null_output_value() throws Exception {
    GenericTransReduce reducer = new GenericTransReduce();
    MockOutputCollector outputCollector = new MockOutputCollector();
    MockReporter reporter = new MockReporter();

    reducer.configure(createJobConf("./test-res/null-test.ktr", "./test-res/null-test.ktr"));

    reducer.reduce(new Text("0"), Arrays.asList(new Text("test")).iterator(), outputCollector, reporter);
    outputCollector.close();

    Exception ex = reducer.getException();
    if (ex != null) {
      ex.printStackTrace();
    }
    assertNull("Exception thrown", ex);
    assertEquals("Received output when we didn't expect any.  <null>s aren't passed through.", 0, outputCollector.getCollection().size());
  }

  @Test
  public void testLogChannelLeaking_mapper() throws Exception {
    JobConf jobConf = createJobConf("./test-res/wordcount-mapper.ktr", "./test-res/wordcount-reducer.ktr",
        "./test-res/wordcount-reducer.ktr");
    PentahoMapRunnable mapper = new PentahoMapRunnable();
    mapper.configure(jobConf);
    MockReporter reporter = new MockReporter();

    // We expect 4 log channels per run.  The total should never grow past logChannelsBefore + 4.
    final int EXPECTED_CHANNELS_PER_RUN = 5;
    final int logChannels = LoggingRegistry.getInstance().getMap().size();
    // Run the reducer this many times
    final int RUNS = 10;

    for (int i = 0; i < RUNS; i++) {
      MockRecordReader reader = new MockRecordReader(Arrays.asList("test"));
      MockOutputCollector outputCollector = new MockOutputCollector();
      mapper.run(reader, outputCollector, reporter);
      outputCollector.close();
      Exception ex = mapper.getException();
      if (ex != null) {
        ex.printStackTrace();
      }
      assertNull("Exception thrown", ex);
      assertEquals("Incorrect output", 1, outputCollector.getCollection().size());

      assertEquals("LogChannels are not being cleaned up. On Run #" + i + " we have too many.", logChannels
          + EXPECTED_CHANNELS_PER_RUN, LoggingRegistry.getInstance().getMap().size());
    }
    assertEquals(logChannels + EXPECTED_CHANNELS_PER_RUN, LoggingRegistry.getInstance().getMap().size());
  }

  @Test
  public void testCombiner_single_threaded_wordcount() throws Exception {
    final GenericTransCombiner combiner = new GenericTransCombiner();
    final JobConf c = createJobConf(null, "./test-res/wordcount-reducer.ktr", null);
    c.set(PentahoMapReduceBase.STRING_COMBINE_SINGLE_THREADED, "true");

    combiner.configure(c);
    assertTrue(combiner.isSingleThreaded());

    final MockOutputCollector outputCollector = new MockOutputCollector();
    final MockReporter reporter = new MockReporter();

    combiner.reduce(new Text("pentaho"), Arrays.asList(new IntWritable(2), new IntWritable(8)).iterator(), outputCollector, reporter);
    outputCollector.close();

    Exception ex = combiner.getException();
    if (ex != null) {
      ex.printStackTrace();
    }
    assertNull("Exception thrown", ex);
    assertEquals("Expected 1 output row", 1, outputCollector.getCollection().size());
    assertEquals("Expected 1 result for word 'pentaho'", 1, outputCollector.getCollection().get(new Text("pentaho")).size());
    assertEquals("Expected 10 counts of 'pentaho'", new IntWritable(10), outputCollector.getCollection().get(new Text("pentaho")).get(0));
  }

  @Test
  public void testLogChannelLeaking_combiner() throws Exception {
    JobConf jobConf = createJobConf("./test-res/wordcount-mapper.ktr", "./test-res/wordcount-reducer.ktr",
        "./test-res/wordcount-reducer.ktr");
    List<IntWritable> input = Arrays.asList(new IntWritable(1));
    GenericTransCombiner combiner = new GenericTransCombiner();
    combiner.configure(jobConf);
    MockReporter reporter = new MockReporter();

    // We expect 4 log channels per run.  The total should never grow past logChannelsBefore + 4.
    final int EXPECTED_CHANNELS_PER_RUN = 4;
    final int logChannels = LoggingRegistry.getInstance().getMap().size();
    // Run the reducer this many times
    final int RUNS = 10;

    for (int i = 0; i < RUNS; i++) {
      MockOutputCollector outputCollector = new MockOutputCollector();
      combiner.reduce(new Text(String.valueOf(i)), input.iterator(), outputCollector, reporter);
      outputCollector.close();
      Exception ex = combiner.getException();
      if (ex != null) {
        ex.printStackTrace();
      }
      assertNull("Exception thrown", ex);
      assertEquals("Incorrect output", 1, outputCollector.getCollection().size());

      assertEquals("LogChannels are not being cleaned up. On Run #" + i + " we have too many.", logChannels
          + EXPECTED_CHANNELS_PER_RUN, LoggingRegistry.getInstance().getMap().size());
    }
    assertEquals(logChannels + EXPECTED_CHANNELS_PER_RUN, LoggingRegistry.getInstance().getMap().size());
  }

  @Test
  public void testLogChannelLeaking_reducer() throws Exception {
    JobConf jobConf = createJobConf("./test-res/wordcount-mapper.ktr", "./test-res/wordcount-reducer.ktr",
        "./test-res/wordcount-reducer.ktr");
    List<IntWritable> input = Arrays.asList(new IntWritable(1));
    GenericTransReduce reducer = new GenericTransReduce();
    reducer.configure(jobConf);
    MockReporter reporter = new MockReporter();

    // We expect 4 log channels per run.  The total should never grow past logChannelsBefore + 4.
    final int EXPECTED_CHANNELS_PER_RUN = 4;
    final int logChannels = LoggingRegistry.getInstance().getMap().size();
    // Run the reducer this many times
    final int RUNS = 10;

    for (int i = 0; i < RUNS; i++) {
      MockOutputCollector outputCollector = new MockOutputCollector();
      reducer.reduce(new Text(String.valueOf(i)), input.iterator(), outputCollector, reporter);
      outputCollector.close();
      Exception ex = reducer.getException();
      if (ex != null) {
        ex.printStackTrace();
      }
      assertNull("Exception thrown", ex);
      assertEquals("Incorrect output", 1, outputCollector.getCollection().size());

      assertEquals("LogChannels are not being cleaned up. On Run #" + i + " we have too many.", logChannels
          + EXPECTED_CHANNELS_PER_RUN, LoggingRegistry.getInstance().getMap().size());
    }
    assertEquals(logChannels + EXPECTED_CHANNELS_PER_RUN, LoggingRegistry.getInstance().getMap().size());
  }
  // TODO Create tests for exception propogation from RowListeners in Mapper, Combiner, and Reducer
  
  @Test
  public void testMapReduce_InputOutput() throws Exception {
    JobConf jobConf = createJobConf("./test-res/mr-input-output.ktr", "./test-res/mr-passthrough.ktr",
        "./test-res/mr-passthrough.ktr");
    
    PentahoMapRunnable mapper = new PentahoMapRunnable();
    mapper.configure(jobConf);
    
    MockReporter reporter = new MockReporter();
    MockOutputCollector outputCollector = new MockOutputCollector();
    MockRecordReader reader = new MockRecordReader(Arrays.asList("1", "2", "3"));

    mapper.run(reader, outputCollector, reporter);

    outputCollector.close();

    Exception ex = mapper.getException();
    if (ex != null) {
      ex.printStackTrace();
    }
    assertNull("Exception thrown", ex);
    assertEquals("Incorrect output", 3, outputCollector.getCollection().size());
    
    assertEquals("Validating output collector", new IntWritable(0), outputCollector.getCollection().get(new Text("1")).get(0));
    assertEquals("Validating output collector", new IntWritable(1), outputCollector.getCollection().get(new Text("2")).get(0));
    assertEquals("Validating output collector", new IntWritable(2), outputCollector.getCollection().get(new Text("3")).get(0));

  }
}
