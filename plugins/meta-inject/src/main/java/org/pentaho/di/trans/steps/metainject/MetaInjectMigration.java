/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.Map;

/**
 * This class converts old mapping info into new one.
 * 
 * @author Alexander Buloichik
 */
public class MetaInjectMigration {
  /**
   * Migrate mapping from 7.0 version.
   */
  public static void migrateFrom70( Map<TargetStepAttribute, SourceStepField> targetSourceMapping ) {
    /*
     * Need to convert GetTableNamesMeta.SCHENAMENAMEFIELD to the GetTableNamesMeta.SCHEMANAMEFIELD
     */
    for ( TargetStepAttribute target : new ArrayList<>( targetSourceMapping.keySet() ) ) {
      if ( "SCHENAMENAMEFIELD".equals( target.getAttributeKey() ) ) {
        SourceStepField so = targetSourceMapping.remove( target );
        TargetStepAttribute target2 =
            new TargetStepAttribute( target.getStepname(), "SCHEMANAMEFIELD", target.isDetail() );
        targetSourceMapping.put( target2, so );
      }
    }
  }
}
