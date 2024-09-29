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
