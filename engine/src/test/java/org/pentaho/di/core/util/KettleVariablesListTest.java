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


package org.pentaho.di.core.util;

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleVariablesList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * Created by Yury_Bakhmutski on 11/4/2015.
 */
public class KettleVariablesListTest {

  @Test
  public void testInit() throws Exception {
    KettleVariablesList variablesList = KettleVariablesList.getInstance();
    variablesList.init();
    //See PDI-14522
    boolean actual = Boolean.valueOf( variablesList.getDefaultValueMap().get( Const.VFS_USER_DIR_IS_ROOT ) );
    assertEquals( false, actual );

    String vfsUserDirIsRootDefaultMessage =
        "Set this variable to true if VFS should treat the user directory"
            + " as the root directory when connecting via ftp. Defaults to false.";
    assertEquals( variablesList.getDescriptionMap().get( Const.VFS_USER_DIR_IS_ROOT ), vfsUserDirIsRootDefaultMessage );
  }

  @Test
  public void testInit_closeInputStream() throws Exception {
    KettleVariablesList.init();
    RandomAccessFile fos = null;
    try {
      File file = new File( Const.KETTLE_VARIABLES_FILE );
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
