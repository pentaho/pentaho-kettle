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

package org.pentaho.di.ui.spoon.partition;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.ui.spoon.PartitionSchemasProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Evgeniy_Lyakhov@epam.com
 */
public class PartitionSettingsTest {
  private TransMeta transMeta;
  private StepMeta stepMeta;
  private PartitionSchemasProvider partitionSchemasProvider;

  private int length;
  private PartitionSettings settings;

  private PluginInterface plugin;

  @Before
  public void setUp() throws KettleException {
    transMeta = mock( TransMeta.class );
    stepMeta = mock( StepMeta.class );
    partitionSchemasProvider = mock( PartitionSchemasProvider.class );

    length = StepPartitioningMeta.methodCodes.length + 1;
    settings = new PartitionSettings( length, transMeta, stepMeta, partitionSchemasProvider );

    plugin = mock( PluginInterface.class );
    when( plugin.getIds() ).thenReturn( new String[] { "qwerty" } );
    when( plugin.getName() ).thenReturn( "qwerty" );
    when( plugin.getDescription() ).thenReturn( "asdfg" );
  }

  @Test
  public void codesArePickedUpFromPlugins() {
    PartitionSettings settings = new PartitionSettings( StepPartitioningMeta.methodCodes.length,
      transMeta, stepMeta, partitionSchemasProvider );

    assertTrue( Arrays.equals( StepPartitioningMeta.methodCodes, settings.getCodes() ) );
  }

  @Test
  public void pluginsCodesAreGathered() {
    settings.fillOptionsAndCodesByPlugins( Collections.singletonList( plugin ) );

    assertEquals( "qwerty", settings.getCodes()[ length - 1 ] );
  }

  @Test
  public void codeIsFoundByDescription() {
    settings.fillOptionsAndCodesByPlugins( Collections.singletonList( plugin ) );

    assertEquals( "qwerty", settings.getMethodByMethodDescription( "asdfg" ) );
  }

  @Test
  public void codeOfNoneIsReturnedWhenNotFoundByDescription() {
    settings.fillOptionsAndCodesByPlugins( Collections.singletonList( plugin ) );

    assertEquals( StepPartitioningMeta.methodCodes[ StepPartitioningMeta.PARTITIONING_METHOD_NONE ],
      settings.getMethodByMethodDescription( "qwerty" ) );
  }

  @Test
  public void defaultSelectedSchemaIndexIsFoundBySchemaName() throws Exception {
    PartitionSchema schema = new PartitionSchema( "qwerty", Collections.<String>emptyList() );
    StepPartitioningMeta meta = mock( StepPartitioningMeta.class );
    when( meta.getPartitionSchema() ).thenReturn( schema );
    when( stepMeta.getStepPartitioningMeta() ).thenReturn( meta );

    List<String> schemas = Arrays.asList( "1", plugin.getName(), "2" );
    when( partitionSchemasProvider.getPartitionSchemasNames( any( TransMeta.class ) ) ).thenReturn( schemas );

    assertEquals( 1, settings.getDefaultSelectedSchemaIndex() );
  }

  @Test
  public void defaultSelectedSchemaIndexWhenSchemaNameIsNotDefined() throws Exception {
    PartitionSchema schema = new PartitionSchema( );
    StepPartitioningMeta meta = mock( StepPartitioningMeta.class );
    when( meta.getPartitionSchema() ).thenReturn( schema );
    when( stepMeta.getStepPartitioningMeta() ).thenReturn( meta );

    List<String> schemas = Arrays.asList( "test" );
    when( partitionSchemasProvider.getPartitionSchemasNames( any( TransMeta.class ) ) ).thenReturn( schemas );

    assertEquals( 0, settings.getDefaultSelectedSchemaIndex() );
  }

  @Test
  public void defaultSelectedSchemaIndexIsNilWhenNotFoundBySchemaName() throws Exception {
    PartitionSchema schema = new PartitionSchema( "asdfg", Collections.<String>emptyList() );
    StepPartitioningMeta meta = mock( StepPartitioningMeta.class );
    when( meta.getPartitionSchema() ).thenReturn( schema );
    when( stepMeta.getStepPartitioningMeta() ).thenReturn( meta );

    List<String> schemas = Arrays.asList( "1", plugin.getName(), "2" );
    when( partitionSchemasProvider.getPartitionSchemasNames( any( TransMeta.class ) ) ).thenReturn( schemas );

    assertEquals( 0, settings.getDefaultSelectedSchemaIndex() );
  }

  @Test
  public void metaIsUpdated() {
    PartitionSchema schema = new PartitionSchema( "1", Collections.<String>emptyList() );

    StepPartitioningMeta meta = mock( StepPartitioningMeta.class );
    when( stepMeta.getStepPartitioningMeta() ).thenReturn( meta );

    settings.updateSchema( schema );
    verify( meta ).setPartitionSchema( schema );
  }

  @Test
  public void metaIsNotUpdatedWithNull() {
    StepPartitioningMeta meta = mock( StepPartitioningMeta.class );
    when( stepMeta.getStepPartitioningMeta() ).thenReturn( meta );

    settings.updateSchema( null );
    verify( meta, never() ).setPartitionSchema( any( PartitionSchema.class ) );
  }

  @Test
  public void metaIsNotUpdatedWithNameless() {
    PartitionSchema schema = new PartitionSchema( null, Collections.<String>emptyList() );

    StepPartitioningMeta meta = mock( StepPartitioningMeta.class );
    when( stepMeta.getStepPartitioningMeta() ).thenReturn( meta );

    settings.updateSchema( null );
    verify( meta, never() ).setPartitionSchema( any( PartitionSchema.class ) );
  }
}
