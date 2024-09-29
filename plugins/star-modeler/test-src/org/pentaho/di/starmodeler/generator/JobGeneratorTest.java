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

package org.pentaho.di.starmodeler.generator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.starmodeler.DefaultIDs;
import org.pentaho.di.starmodeler.DimensionType;
import org.pentaho.di.starmodeler.StarDomain;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dimensionlookup.DimensionLookupMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.TableType;
import org.pentaho.pms.schema.concept.DefaultPropertyID;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobGeneratorTest {

  private JobGenerator jobGenerator;

  @Before
  public void setUp() throws Exception {
    final StarDomain starDomain = mock( StarDomain.class );

    final Domain domain = mock( Domain.class );
    when( domain.getProperty( eq( DefaultIDs.DOMAIN_TARGET_DATABASE ) ) ).thenReturn( "test_domain_target_db" );
    when( starDomain.getDomain() ).thenReturn( domain );

    final Repository repository = mock( Repository.class );
    final RepositoryDirectoryInterface targetDirectory = mock( RepositoryDirectoryInterface.class );

    final DatabaseMeta meta = Mockito.mock( DatabaseMeta.class );
    Mockito.when( meta.getName() ).thenReturn( "test_domain_target_db" );
    final LinkedList<DatabaseMeta> databases = new LinkedList<DatabaseMeta>() {
      {
        add( meta );
      }
    };

    final String locale = Locale.US.toString();

    jobGenerator = new JobGenerator( starDomain, repository, targetDirectory, databases, locale );
  }

  @Test
  public void testFindTargetDatabaseMeta() throws Exception {
    when( jobGenerator.starDomain.getDomain().getProperty( eq( DefaultIDs.DOMAIN_TARGET_DATABASE ) ) ).thenReturn(
        null );
    try {
      jobGenerator.findTargetDatabaseMeta();
      fail();
    } catch ( KettleException e ) {
      // expected
    }

    when( jobGenerator.starDomain.getDomain().getProperty( eq( DefaultIDs.DOMAIN_TARGET_DATABASE ) ) ).thenReturn( "test_domain_target_db_wrong" );
    try {
      jobGenerator.findTargetDatabaseMeta();
      fail();
    } catch ( KettleException e ) {
      // expected
    }

    when( jobGenerator.starDomain.getDomain().getProperty( eq( DefaultIDs.DOMAIN_TARGET_DATABASE ) ) ).thenReturn( "test_domain_target_db" );

    final DatabaseMeta targetDatabaseMeta = jobGenerator.findTargetDatabaseMeta();

    assertNotNull( targetDatabaseMeta );
  }

  @Test
  public void testFindSourceDatabaseMeta() throws Exception {
    try {
      jobGenerator.findSourceDatabaseMeta( "test_domain_target_db_wrong" );
      fail();
    } catch ( KettleException e ) {
      // expected
    }

    final DatabaseMeta sourceDatabaseMeta = jobGenerator.findSourceDatabaseMeta( "test_domain_target_db" );

    assertNotNull( sourceDatabaseMeta );
  }

  @Test
  public void testGetUniqueLogicalTables() throws Exception {
    final LogicalModel logicalModel = mock( LogicalModel.class );
    when( jobGenerator.domain.getLogicalModels() ).thenReturn( new LinkedList<LogicalModel>() { {
        add( logicalModel );
      } } );

    final LogicalTable logicalTable = mock( LogicalTable.class );
    when( logicalTable.getProperty( eq( DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME ) ) )
        .thenReturn( "test_table_name" );
    when( logicalModel.getLogicalTables() ).thenReturn( new LinkedList<LogicalTable>() { {
        add( logicalTable );
      } } );

    final List<LogicalTable> uniqueLogicalTables = jobGenerator.getUniqueLogicalTables();

    assertNotNull( uniqueLogicalTables );
    assertEquals( 1, uniqueLogicalTables.size() );
    assertEquals( logicalTable, uniqueLogicalTables.get( 0 ) );
  }

  @Test
  public void testGenerateDimensionTransformations() throws Exception {
    final LogicalModel logicalModel = mock( LogicalModel.class );
    when( jobGenerator.domain.getLogicalModels() ).thenReturn( new LinkedList<LogicalModel>() { {
        add( logicalModel );
      } } );

    final LogicalTable logicalTable = mock( LogicalTable.class );
    when( logicalTable.getProperty( eq( DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME ) ) )
        .thenReturn( "test_table_name" );
    when( logicalModel.getLogicalTables() ).thenReturn( new LinkedList<LogicalTable>() { {
        add( logicalTable );
      } } );

    when( logicalTable.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( TableType.DIMENSION );
    when( logicalTable.getProperty( eq( DefaultIDs.LOGICAL_TABLE_DIMENSION_TYPE ) ) ).thenReturn( DimensionType.JUNK_DIMENSION.name() );

    final List<TransMeta> transMetas = jobGenerator.generateDimensionTransformations();

    assertNotNull( transMetas );
    assertEquals( 1, transMetas.size() );
  }

  @Test
  public void testGenerateDimensionTransformation() throws Exception {
    final LogicalTable logicalTable = mock( LogicalTable.class );
    final DatabaseMeta databaseMeta = mock( DatabaseMeta.class );

    final TransMeta transMeta = jobGenerator.generateDimensionTransformation( databaseMeta, logicalTable );
    assertNotNull( transMeta );
    assertTrue( transMeta.getDatabases().contains( databaseMeta ) );
    assertEquals( 2, transMeta.getSteps().size() );
    assertEquals( 1, transMeta.nrTransHops() );
  }

  @Test
  public void testGenerateDimensionLookupStepFromLogicalTable() throws Exception {
    final LogicalTable logicalTable = mock( LogicalTable.class );
    final DatabaseMeta databaseMeta = mock( DatabaseMeta.class );

    final StepMeta stepMeta = jobGenerator.generateDimensionLookupStepFromLogicalTable( databaseMeta, logicalTable );
    assertNotNull( stepMeta );
    assertEquals( DimensionLookupMeta.class, stepMeta.getStepMetaInterface().getClass() );
    assertEquals( databaseMeta, ( (DimensionLookupMeta) stepMeta.getStepMetaInterface() ).getDatabaseMeta() );
  }

  @Test
  public void testGenerateCombinationLookupStepFromLogicalTable() throws Exception {
    final LogicalTable logicalTable = mock( LogicalTable.class );
    final DatabaseMeta databaseMeta = mock( DatabaseMeta.class );

    final StepMeta stepMeta = jobGenerator.generateCombinationLookupStepFromLogicalTable( databaseMeta, logicalTable );
    assertNotNull( stepMeta );
  }
}
