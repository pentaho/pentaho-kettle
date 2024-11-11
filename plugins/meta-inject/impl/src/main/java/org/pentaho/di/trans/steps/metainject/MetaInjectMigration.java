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


package org.pentaho.di.trans.steps.metainject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class converts old mapping info into new one.
 * 
 * @author Alexander Buloichik
 */
public class MetaInjectMigration {

  /* migration mapping of keys.
     key is the old version of the mapping
     value is the new version of the mapping */
  private static Map<String, String> migrationMappings = new HashMap<>();
  static {
    migrationMappings.put( "SCHENAMENAMEFIELD", "SCHEMANAMEFIELD" );
    migrationMappings.put( "DATABASE_FIELDNAME", "DATABASE_FIELD_NAME" );
    migrationMappings.put( "STREAM_FIELDNAME", "DATABASE_STREAM_NAME" );
  }

  /**
   * Migrate mapping from previous versions.
   */
  public static void migrate( Map<TargetStepAttribute, SourceStepField> targetSourceMapping ) {
    for ( TargetStepAttribute oldTarget : new ArrayList<>( targetSourceMapping.keySet() ) ) {
      for ( Map.Entry<String, String> mapping : migrationMappings.entrySet() ) {
        if ( mapping.getKey().equals( oldTarget.getAttributeKey() ) ) {
          SourceStepField so = targetSourceMapping.remove( oldTarget );
          TargetStepAttribute newTarget = new TargetStepAttribute( oldTarget.getStepname(), mapping.getValue(), oldTarget.isDetail() );
          targetSourceMapping.put( newTarget, so );
        }
      }
    }
  }
}
