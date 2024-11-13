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


package org.pentaho.di.metastore;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.metastore.stores.xml.XmlUtil;

import com.google.common.io.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MetaStoreConstTest {

  @Test
  public void testOpenLocalPentahoMetaStore() throws Exception {
    MetaStoreConst.disableMetaStore = false;
    File tempDir = Files.createTempDir();
    String tempPath = tempDir.getAbsolutePath();
    System.setProperty( Const.PENTAHO_METASTORE_FOLDER, tempPath );
    String metaFolder = tempPath + File.separator + XmlUtil.META_FOLDER_NAME;

    // Create a metastore
    assertNotNull( MetaStoreConst.openLocalPentahoMetaStore() );
    assertTrue( ( new File( metaFolder ) ).exists() );

    // Check existing while disabling the metastore ( used for tests )
    MetaStoreConst.disableMetaStore = true;
    assertNull( MetaStoreConst.openLocalPentahoMetaStore() );

    // Check existing metastore
    MetaStoreConst.disableMetaStore = false;
    assertNotNull( MetaStoreConst.openLocalPentahoMetaStore( false ) );

    // Try to read a metastore that does not exist with allowCreate = false
    FileUtils.deleteDirectory( new File( metaFolder ) );
    assertNull( MetaStoreConst.openLocalPentahoMetaStore( false ) );
    assertFalse( ( new File( metaFolder ) ).exists() );
  }

}
