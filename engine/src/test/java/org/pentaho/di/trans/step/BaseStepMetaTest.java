/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.step;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import org.junit.Test;

public class BaseStepMetaTest {
  
  @Test
  public void testLoadStepAttributes_closeInputStream() throws Exception {
    BaseStepMeta meta = new BaseStepMeta();
    meta.loadStepAttributes();
    RandomAccessFile fos = null;
    try {
      File file = new File( getClass().getResource( BaseStepMeta.STEP_ATTRIBUTES_FILE ).getPath() );
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
