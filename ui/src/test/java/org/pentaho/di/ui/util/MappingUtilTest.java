/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.util;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MappingUtilTest {

  private List<String> sourceFields;
  private List<String> targetFields;
  private List<MappingValueRename> mappingValues;

  @Before
  public void setup() {
    sourceFields = new ArrayList<>( Arrays.asList( new String[] { "source1", "source2", "source3" } ) );
    targetFields = new ArrayList<>( Arrays.asList( new String[] { "target1", "target2" } ) );
    mappingValues = new ArrayList<>();

    mappingValues.add( new MappingValueRename( "source1", "target2" ) );
    mappingValues.add( new MappingValueRename( "source3", "target1" ) );
  }

  @Test
  public void getCurrentMapping() {
    List<SourceToTargetMapping> currentMapping = MappingUtil.getCurrentMappings( sourceFields, targetFields, mappingValues );

    assertEquals( 2, currentMapping.size() );
    assertEquals( currentMapping.get( 0 ).getSourcePosition(), sourceFields.indexOf( "source1" ) );
    assertEquals( currentMapping.get( 0 ).getTargetPosition(), targetFields.indexOf( "target2" ) );
    assertEquals( currentMapping.get( 1 ).getSourcePosition(), sourceFields.indexOf( "source3" ) );
    assertEquals( currentMapping.get( 1 ).getTargetPosition(), targetFields.indexOf( "target1" ) );
  }

  @Test
  public void getCurrentMappingSourceNull() {
    List<SourceToTargetMapping> currentMapping = MappingUtil.getCurrentMappings( null, targetFields, mappingValues );
    assertEquals( 0, currentMapping.size() );
  }

  @Test
  public void getCurrentMappingTargetNull() {
    List<SourceToTargetMapping> currentMapping = MappingUtil.getCurrentMappings( sourceFields, null, mappingValues );
    assertEquals( 0, currentMapping.size() );
  }

  @Test
  public void getCurrentMappingMappingNull() {
    List<SourceToTargetMapping> currentMapping = MappingUtil.getCurrentMappings( sourceFields, targetFields, null );
    assertEquals( 0, currentMapping.size() );
  }

  @Test
  public void getCurrentMappingEmptyMappings() {
    List<SourceToTargetMapping> currentMapping = MappingUtil.getCurrentMappings( sourceFields, targetFields, new ArrayList<>(  ) );
    assertEquals( 0, currentMapping.size() );
  }
}
