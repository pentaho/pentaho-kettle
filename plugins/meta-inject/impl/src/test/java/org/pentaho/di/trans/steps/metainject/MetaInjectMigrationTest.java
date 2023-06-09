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

import org.junit.Test;

public class MetaInjectMigrationTest {

  @Test
  public void testMigration() {
    Map<TargetStepAttribute, SourceStepField> targetSourceMapping = new LinkedHashMap<>();

    //First 3 old mappings are the mappings that need migration. The last one is an example of a mapping that does not need migration
    String[] oldMappingNames = { "SCHENAMENAMEFIELD", "DATABASE_FIELDNAME", "STREAM_FIELDNAME", "DATE_RANGE_START_FIELD" };
    //The expected new mappings. Since the migration process removes and adds again the new  mapping, the one that does
    // not need change will become the first one.
    String[] newMappingNames = { "DATE_RANGE_START_FIELD", "SCHEMANAMEFIELD" ,"DATABASE_FIELD_NAME", "DATABASE_STREAM_NAME" };
    //Expected Step Names. After the migration the Step names should not change.
    String[] expectedStepName = { "step4", "step1", "step2", "step3" };

    //Initiate target mapping with the old mapping names
    int idStep = 1;
    for ( String oldMappingName : oldMappingNames ) {
      TargetStepAttribute target = new TargetStepAttribute( "step" + idStep, oldMappingName, true );
      SourceStepField source = new SourceStepField( "step" + idStep, "field" );
      targetSourceMapping.put( target, source );
      ++idStep;
    }

    //Migrate
    MetaInjectMigration.migrate( targetSourceMapping );

    /* Assert that after the migration the same number of mappings exist and that names that need migration are changed
    and all the others are kept the same */
    assertEquals( "After the migration the same number of mapping should exist", 4, targetSourceMapping.size() );
    int newMappingIndex = 0;
    Set<Map.Entry<TargetStepAttribute, SourceStepField>> entrySet = targetSourceMapping.entrySet();
    for ( Map.Entry<TargetStepAttribute, SourceStepField> entry : entrySet ) {
      assertEquals( "after the migration names that need migration should change to the new ones and all other should be kept the same",
        newMappingNames[newMappingIndex], entry.getKey().getAttributeKey() );
      assertEquals( "after the migration the step names should not have changed", expectedStepName[newMappingIndex], entry.getKey().getStepname() );
      assertEquals( "after the migration the detail option should not have changed", true, entry.getKey().isDetail() );
      ++newMappingIndex;
    }
  }
}
