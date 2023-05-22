/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.metainject;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

public class MetaInjectMigrationTest {

  @Test
  public void testMigration() {

    //Migrate
    Class<?> clazz = TableOutputMeta.class;
    String key1 = "SCHENAMENAMEFIELD";
    String key1MigratedExpected = "SCHEMANAMEFIELD";
    String key1Migrated = MetaInjectMigration.migrate( clazz, key1 );
    assertEquals(key1MigratedExpected, key1Migrated);

    String key2 = "DATABASE_FIELDNAME";
    String key2MigratedExpected = "DATABASE_FIELD_NAME";
    String key2Migrated = MetaInjectMigration.migrate( clazz, key2 );
    assertEquals(key2MigratedExpected, key2Migrated);

    String key3 = "STREAM_FIELDNAME";
    String key3MigratedExpected = "DATABASE_STREAM_NAME";
    String key3Migrated = MetaInjectMigration.migrate( clazz, key3 );
    assertEquals(key3MigratedExpected, key3Migrated);

    String key4 = UUID.randomUUID().toString();
    String key4MigratedExpected = key4;
    String key4Migrated = MetaInjectMigration.migrate( clazz, key4 );
    assertEquals(key4MigratedExpected, key4Migrated);


  }
}
