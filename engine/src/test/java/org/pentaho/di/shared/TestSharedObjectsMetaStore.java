package org.pentaho.di.shared;
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


import junit.framework.TestCase;
import org.junit.Ignore;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.util.PentahoDefaults;

import java.io.File;
import java.util.List;

@Ignore( "Ignored, not running with ant build. Investigate." )
public class TestSharedObjectsMetaStore extends TestCase {

  private static String databaseMetaXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    + "<connection>" + "<name>db</name>" + "<server>127.0.0.1</server>" + "<type>H2</type>"
    + "<access>Native</access>" + "<database>mem:db</database>" + "<port>${PORT_NUMBER_STRING}</port>"
    + "<username>sa</username>" + "<password></password>" + "</connection>";

  private static String namespace = PentahoDefaults.NAMESPACE;

  public void testSharedObjectsMetaStore() throws Exception {
    KettleEnvironment.init();

    File sharedObjectsFile = File.createTempFile( "shared-objects", ".xml" );
    String sharedObjectsFilename = sharedObjectsFile.toString();
    sharedObjectsFile.delete();

    SharedObjects sharedObjects = new SharedObjects( sharedObjectsFilename );
    SharedObjectsMetaStore metaStore = new SharedObjectsMetaStore( sharedObjects );

    List<IMetaStoreElementType> elementTypes = metaStore.getElementTypes( namespace );
    assertEquals( 1, elementTypes.size() );

    IMetaStoreElementType databaseElementType =
      metaStore.getElementType( namespace, PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME );
    assertNotNull( databaseElementType );

    List<IMetaStoreElement> elements = metaStore.getElements( namespace, databaseElementType );
    assertEquals( 0, elements.size() );

    DatabaseMeta databaseMeta = new DatabaseMeta( databaseMetaXml );

    // add it to shared objects, see if we find the element (SharedObjects --> MetaStore)
    //
    sharedObjects.storeObject( databaseMeta );

    elements = metaStore.getElements( namespace, databaseElementType );
    assertEquals( 1, elements.size() );
    IMetaStoreElement databaseElement = elements.get( 0 );

    // Remove it again...
    sharedObjects.removeObject( databaseMeta );
    elements = metaStore.getElements( namespace, databaseElementType );
    assertEquals( 0, elements.size() );

    // Add it to the meta store, see if it shows in the shared objects (MetaStore --> SharedObjects)
    //
    metaStore.createElement( namespace, databaseElementType, databaseElement );
    elements = metaStore.getElements( namespace, databaseElementType );
    assertEquals( 1, elements.size() );

    assertNotNull( sharedObjects.getSharedDatabase( databaseMeta.getName() ) );
  }
}
