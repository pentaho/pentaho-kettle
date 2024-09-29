/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.csvinput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * Regression test case for PDI JIRA-1317: a csv input step with less columns in certain rows than the number of columns
 * defined in the step.
 *
 * In the original problem (in v3.1-M2) this caused the filename column to be in the wrong places.
 *
 * @author Sven Boden Modified by Sean Flatley
 */
public class CsvInput1NoTrailingSpaceIT extends CsvInput1IT {

  /**
   * Write the file to be used as input (as a temporary file).
   *
   * @return Absolute file name/path of the created file.
   * @throws IOException
   *           UPON
   */
  public String writeInputFile() throws IOException {

    String rcode = null;

    File tempFile = File.createTempFile( "PDI_tmp", ".tmp" );
    tempFile.deleteOnExit();

    rcode = tempFile.getAbsolutePath();

    FileWriter fout = new FileWriter( tempFile );
    fout.write( "A;B;C;D;E\n" );
    fout.write( "1;'b0';'c0'\n" );
    fout.write( "2;'b1';'c1';'d1';'e1'\n" );
    fout.write( "3;'b2';'c2'\n" );

    fout.close();

    return rcode;
  }

  /**
   * Test case for Get XML Data step, very simple example.
   *
   * @throws Exception
   *           Upon any exception
   */
  @Test
  public void testCSVInput1() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "csvinput1" );

    PluginRegistry registry = PluginRegistry.getInstance();

    String fileName = writeInputFile();

    StepMeta injectorStep = createInjectorStep( transMeta, registry );
    StepMeta csvInputStep = createCsvInputStep( transMeta, registry, "\'", true );

    createAndTestTrans(
      registry, transMeta, injectorStep, csvInputStep, fileName, createTextFileInputFields().length );
  }
}
