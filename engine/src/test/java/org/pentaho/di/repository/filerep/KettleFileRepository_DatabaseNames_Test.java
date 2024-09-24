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

package org.pentaho.di.repository.filerep;

import org.apache.commons.vfs2.FileObject;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;

/**
 * @author Andrey Khayrutdinov
 */
public class KettleFileRepository_DatabaseNames_Test extends KettleFileRepositoryTestBase {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void getDatabaseId_ExactMatch() throws Exception {
    final String name = UUID.randomUUID().toString();
    DatabaseMeta db = saveDatabase( name );

    ObjectId id = repository.getDatabaseID( name );
    assertEquals( db.getObjectId(), id );
  }

  @Test
  public void getDatabaseId_InsensitiveMatch() throws Exception {
    final String name = "databaseWithCamelCase";
    final String lookupName = name.toLowerCase();
    assertNotSame( lookupName, name );

    DatabaseMeta db = saveDatabase( name );

    ObjectId id = repository.getDatabaseID( lookupName );
    assertEquals( db.getObjectId(), id );
  }

  @Test
  public void getDatabaseId_ReturnsExactMatch_PriorToCaseInsensitiveMatch() throws Exception {
    final String exact = "databaseExactMatch";
    final String similar = exact.toLowerCase();
    assertNotSame( similar, exact );

    DatabaseMeta db = saveDatabase( exact );

    // simulate legacy repository - store a DB with a name different only in case
    DatabaseMeta another = new DatabaseMeta();
    another.setName( similar );
    FileObject fileObject = repository.getFileObject( another );
    assertFalse( fileObject.exists() );
    // just create it - enough for this case
    fileObject.createFile();
    assertTrue( fileObject.exists() );

    ObjectId id = this.repository.getDatabaseID( exact );
    assertEquals( db.getObjectId(), id );
  }

  private DatabaseMeta saveDatabase( String name ) throws Exception {
    DatabaseMeta db = new DatabaseMeta();
    db.setName( name );
    repository.save( db, null, null );
    assertNotNull( db.getObjectId() );
    return db;
  }
}
