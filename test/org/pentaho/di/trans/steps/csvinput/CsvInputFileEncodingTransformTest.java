/*! ****************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.csvinput;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * Regression test case for Jira PDI-10242: a csv input step does not recognize parameter in encoding
 *
 * In the original problem this caused every other row to be skipped.
 *
 * @author Kanstantsin Karneliuk
 */
public class CsvInputFileEncodingTransformTest extends CsvInput3Test {
  private StepMeta csvInputStep;
  private PluginRegistry registry;
  private String fileName;
  private StepMeta injectorStep;
  private TransMeta transMeta;

  @Before
  protected void setUp() throws Exception {
    super.setUp();
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    transMeta = new TransMeta();
    transMeta.setName( "csvinput1" );

    registry = PluginRegistry.getInstance();

    fileName = writeInputFile();

    injectorStep = createInjectorStep( transMeta, registry );
    csvInputStep = createCsvInputStep( transMeta, registry, "\"", false );
  }

  /**
   * Test case for PDI 10242, that test the whole transformation run -- not just init step
   *
   * @throws Exception
   *           Upon any exception
   */
  @Test
  public void testCSVInput1() throws Exception {
    Map<String, String> vars = new HashMap<String, String>();
    vars.put( "P_ENCODING", "UTF-8" );
    transMeta.injectVariables( vars );

    ( (CsvInputMeta) csvInputStep.getStepMetaInterface() ).setEncoding( "${P_ENCODING}" );

    createAndTestTrans(
      registry, transMeta, injectorStep, csvInputStep, fileName, createTextFileInputFields().length );
  }

  /**
   * Test case for PDI 10242, that test the whole transformation run -- not just init step
   *
   * @throws Exception
   *           Upon any exception
   */
  @Test
  public void testCSVSpecSymbolInput() throws Exception {
    Map<String, String> vars = new HashMap<String, String>();
    vars.put( "${P_ENCODING}", "UTF-8" );
    transMeta.injectVariables( vars );

    ( (CsvInputMeta) csvInputStep.getStepMetaInterface() ).setEncoding( "%%${P_ENCODING}%%" );

    createAndTestTrans(
      registry, transMeta, injectorStep, csvInputStep, fileName, createTextFileInputFields().length );
  }

  /**
   * Test case for PDI 10242, that test the whole transformation run -- not just init step
   *
   * @throws Exception
   *           Upon any exception
   */
  @Test
  public void testCSVException() throws Exception {
    try {
      ( (CsvInputMeta) csvInputStep.getStepMetaInterface() ).setEncoding( "${P_ENCODING}" );
      createAndTestTrans(
        registry, transMeta, injectorStep, csvInputStep, fileName, createTextFileInputFields().length );
      fail();
    } catch ( Exception ex ) {
      System.out.println( "Expected exception" );
    }
  }

  /**
   * Test case for PDI 10242, that test the whole transformation run -- regression check
   *
   * @throws Exception
   *           Upon any exception
   */
  @Test
  public void testCSVFixedEncoding() throws Exception {

    ( (CsvInputMeta) csvInputStep.getStepMetaInterface() ).setEncoding( "UTF-8" );

    createAndTestTrans(
      registry, transMeta, injectorStep, csvInputStep, fileName, createTextFileInputFields().length );
  }

  /**
   * Test case for PDI 10242, that test the whole transformation run -- regression check
   *
   * @throws Exception
   *           Upon any exception
   */
  @Test
  public void testCSVNullEncoding() throws Exception {

    ( (CsvInputMeta) csvInputStep.getStepMetaInterface() ).setEncoding( null );

    createAndTestTrans(
      registry, transMeta, injectorStep, csvInputStep, fileName, createTextFileInputFields().length );
  }
}
