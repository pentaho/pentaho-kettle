/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.trans.steps.tableoutput.TableOutputData;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class converts old mapping info into new one.
 * 
 * @author Alexander Buloichik
 */
public class MetaInjectMigration {

  /* migration mapping of keys.
     key is the old version of the mapping
     value is the new version of the mapping */
  private static Map<String, String> migrationMappingsTableOutputMeta = new HashMap<>();
  private static Map<Class<?>, Map<String, String> > migrationMappings = new HashMap<>();

  static {
    migrationMappingsTableOutputMeta.put( "SCHENAMENAMEFIELD", "SCHEMANAMEFIELD" );
    migrationMappingsTableOutputMeta.put( "DATABASE_FIELDNAME", "DATABASE_FIELD_NAME" );
    migrationMappingsTableOutputMeta.put( "STREAM_FIELDNAME", "DATABASE_STREAM_NAME" );

    migrationMappings.put( TableOutputMeta.class, migrationMappingsTableOutputMeta );
  }

  /**
   * Migrate mapping from previous versions.
   */
  public static String migrate( Class<?> clazz, String attributeKey) {
    return Optional.ofNullable( migrationMappings.get( clazz ) )
      .map( mappings -> mappings.getOrDefault( attributeKey, attributeKey ) )
      .orElse( attributeKey );
  }
}
