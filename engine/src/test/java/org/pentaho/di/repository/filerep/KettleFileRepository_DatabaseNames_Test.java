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

package org.pentaho.di.repository.filerep;

import org.apache.commons.vfs2.FileObject;
import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
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
