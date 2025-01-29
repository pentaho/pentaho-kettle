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

package org.pentaho.di.core;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaPluginType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DBCacheTest {
  @BeforeClass
  public static void setUpClass() throws KettleException {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init();
  }

  @Before
  public void setUp() throws Exception {
    DBCache.dbCache = null;
  }

  @After
  public void tearDown() throws Exception {
    DBCache.dbCache = null;
    DBCache.fileNameSupplier = DBCache::getFilename;
  }

  @Test
  public void readsDbCacheFile() {
    DBCache.fileNameSupplier = () -> getClass().getResource( "db.cache-Test" ).getPath();
    DBCache dbCache = DBCache.getInstance();
    RowMetaInterface fields = dbCache.get( new DBCacheEntry( "foodmart", "select * from sales" ) );
    assertEquals( 1, fields.size() );
    assertEquals( "field1", fields.getFieldNames()[0] );
  }

  @Test
  public void savesCacheToDisk() throws KettleFileException, IOException {
    Path tempFile = Files.createTempFile( "dbcache", "test" );
    DBCache.fileNameSupplier = tempFile::toString;
    DBCache dbCache = DBCache.getInstance();
    RowMeta fields = new RowMeta();
    fields.addValueMeta( new ValueMetaInteger( "int1" ) );
    String select = "select name from warehouse";
    dbCache.put( new DBCacheEntry( "warehouse", select ), fields );
    dbCache.saveCache();
    assertTrue( FileUtils.readFileToString( tempFile.toFile() ).contains( select ) );
  }
}
