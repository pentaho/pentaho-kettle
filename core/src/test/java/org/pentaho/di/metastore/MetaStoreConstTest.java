/*! ******************************************************************************
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
