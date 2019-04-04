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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MetaInjectMigrationTest {
  @Test
  public void test70() {
    Map<TargetStepAttribute, SourceStepField> targetSourceMapping = new HashMap<>();
    TargetStepAttribute target = new TargetStepAttribute( "step", "SCHENAMENAMEFIELD", true );
    SourceStepField source = new SourceStepField( "step", "field" );
    targetSourceMapping.put( target, source );

    MetaInjectMigration.migrateFrom70( targetSourceMapping );

    assertEquals( 1, targetSourceMapping.size() );
    TargetStepAttribute target2 = targetSourceMapping.keySet().iterator().next();
    assertEquals( "SCHEMANAMEFIELD", target2.getAttributeKey() );
    assertEquals( target.getStepname(), target2.getStepname() );
    assertEquals( target.isDetail(), target2.isDetail() );

    assertEquals( source, targetSourceMapping.get( target2 ) );
  }
}
