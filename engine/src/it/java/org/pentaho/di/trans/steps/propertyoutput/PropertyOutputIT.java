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

package org.pentaho.di.trans.steps.propertyoutput;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class PropertyOutputIT {

  @Before
  public void setUp() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testExecute() throws KettleException, IOException {
    TransMeta meta = new TransMeta( getClass().getResource( "propertyOutput.ktr" ).getPath() );
    Trans trans = new Trans( meta );
    trans.execute( new String[] {} );
    trans.waitUntilFinished();

    //check that trans is finished
    assertTrue( trans.isFinished() );

    PropertyOutputData dataStep = (PropertyOutputData) trans.getSteps().get( 1 ).data;

    RandomAccessFile fos = null;
    try {
      File file = new File( URI.create( dataStep.filename.replace( "\\", "/" ) ).getPath() );
      if ( file.exists() ) {
        fos = new RandomAccessFile( file, "rw" );
      }
    } catch ( FileNotFoundException | SecurityException e ) {
      fail( "the file with properties should be unallocated" );
    } finally {
      if ( fos != null ) {
        fos.close();
      }
    }
  }

}
