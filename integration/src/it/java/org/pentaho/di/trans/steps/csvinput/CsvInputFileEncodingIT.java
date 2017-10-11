/*! ****************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * Regression test case for Jira PDI-10242: a csv input step does not recognize parameter in encoding
 *
 * In the original problem this caused every other row to be skipped.
 *
 * @author Kanstantsin Karneliuk
 */
public class CsvInputFileEncodingIT extends CsvInputBase {

  private CsvInputMeta csvInpMeta;
  private CsvInput csvInput;

  @Before
  protected void setUp() throws Exception {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "csvinput1" );

    Map<String, String> vars = new HashMap<String, String>();
    vars.put( "${P_ENCODING}", "UTF-8" );
    vars.put( "P_ENCODING", "UTF-8" );
    transMeta.injectVariables( vars );

    StepMeta csvStepMeta = createCsvInputStep( transMeta, PluginRegistry.getInstance(), "\"", false );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    csvInput = new CsvInput( csvStepMeta, new CsvInputData(), 1, transMeta, trans );
    csvInput.copyVariablesFrom( trans );

    csvInpMeta = (CsvInputMeta) csvStepMeta.getStepMetaInterface();
    csvInpMeta.setFilename( "temp_file" );
    super.setUp();
  }

  /**
   * testing the fix
   *
   * @throws Exception
   */

  @Test
  public void testCSVVariableEncodingInit() throws Exception {

    csvInpMeta.setEncoding( "${P_ENCODING}" );
    assertTrue( csvInput.init( csvInpMeta, new CsvInputData() ) );
  }

  /**
   * testing the fix
   *
   * @throws Exception
   */

  @Test
  public void testCSVVariableEncodingSpecSybmbolsInit() throws Exception {

    csvInpMeta.setEncoding( "%%${P_ENCODING}%%" );
    assertTrue( csvInput.init( csvInpMeta, new CsvInputData() ) );
  }

  /**
   * testing the fix
   *
   * @throws Exception
   */

  @Test
  public void testCSVVariableEncodingFail() throws Exception {
    csvInpMeta.setEncoding( "${P_ENCODING_MISSED}" );
    KettleLogStore.init();
    assertFalse( csvInput.init( csvInpMeta, new CsvInputData() ) );
  }

  /**
   * testing possible regressions
   *
   * @throws Exception
   */
  @Test
  public void testCSVFixedEncodingInit() throws Exception {
    csvInpMeta.setEncoding( "UTF-8" );
    assertTrue( csvInput.init( csvInpMeta, new CsvInputData() ) );
  }

  /**
   * testing possible regressions
   *
   * @throws Exception
   */
  @Test
  public void testCSVNullEncodingInit() throws Exception {
    csvInpMeta.setEncoding( null );
    assertTrue( csvInput.init( csvInpMeta, new CsvInputData() ) );
  }

  @Override
  public List<RowMetaAndData> createResultData1() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected TextFileInputField[] createTextFileInputFields() {
    // TODO Auto-generated method stub
    return null;
  }

}
