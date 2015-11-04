/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.repository.kdr;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryJobDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryTransDelegate;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_DATE;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_INTEGER;
import static org.pentaho.di.core.row.ValueMetaInterface.TYPE_STRING;
import static org.pentaho.di.repository.RepositoryObjectType.JOB;
import static org.pentaho.di.repository.RepositoryObjectType.TRANSFORMATION;
import static org.pentaho.di.repository.kdr.KettleDatabaseRepositoryBase.*;

/**
 * @author Andrey Khayrutdinov
 */
public class KettleDatabaseRepository_GetObjectInformation_Test {
  private static final String ABSENT_ID = "non-existing object";
  private static final String EXISTING_ID = "existing object";

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }


  private KettleDatabaseRepository repository;
  private RepositoryDirectoryInterface directoryInterface;

  @Before
  public void setUp() throws Exception {
    directoryInterface = mock( RepositoryDirectoryInterface.class );

    repository = spy( new KettleDatabaseRepository() );
    doReturn( directoryInterface ).when( repository ).loadRepositoryDirectoryTree();
    doReturn( directoryInterface )
      .when( repository )
      .loadRepositoryDirectoryTree( any( RepositoryDirectoryInterface.class ) );
  }


  @Test
  public void getObjectInformation_AbsentJob_IsDeletedFlagSet() throws Exception {
    KettleDatabaseRepositoryJobDelegate jobDelegate =
      spy( new KettleDatabaseRepositoryJobDelegate( repository ) );

    RowMeta meta = createMetaForJob();
    doReturn( new RowMetaAndData( meta, new Object[ meta.size() ] ) )
      .when( jobDelegate )
      .getJob( new StringObjectId( ABSENT_ID ) );

    assertIsDeletedSet_ForAbsentObject( null, jobDelegate, JOB );
  }

  @Test
  public void getObjectInformation_AbsentTrans_IsDeletedFlagSet() throws Exception {
    KettleDatabaseRepositoryTransDelegate transDelegate =
      spy( new KettleDatabaseRepositoryTransDelegate( repository ) );

    RowMeta meta = createMetaForTrans();
    doReturn( new RowMetaAndData( meta, new Object[ meta.size() ] ) )
      .when( transDelegate )
      .getTransformation( new StringObjectId( ABSENT_ID ) );

    assertIsDeletedSet_ForAbsentObject( transDelegate, null, TRANSFORMATION );
  }

  private void assertIsDeletedSet_ForAbsentObject( KettleDatabaseRepositoryTransDelegate transDelegate,
                                                   KettleDatabaseRepositoryJobDelegate jobDelegate,
                                                   RepositoryObjectType objectType )
    throws Exception {
    repository.transDelegate = transDelegate;
    repository.jobDelegate = jobDelegate;

    when( directoryInterface.findDirectory( any( ObjectId.class ) ) ).thenReturn( null );

    RepositoryObject object = repository.getObjectInformation( new StringObjectId( ABSENT_ID ), objectType );
    assertTrue( object.isDeleted() );
  }


  @Test
  public void getObjectInformation_ExistingJob_IsDeletedFlagNotSet() throws Exception {
    KettleDatabaseRepositoryJobDelegate jobDelegate =
      spy( new KettleDatabaseRepositoryJobDelegate( repository ) );

    RowMeta meta = createMetaForJob();
    Object[] values = new Object[ meta.size() ];
    values[ asList( meta.getFieldNames() ).indexOf( FIELD_JOB_NAME ) ] = EXISTING_ID;
    doReturn( new RowMetaAndData( meta, values ) )
      .when( jobDelegate )
      .getJob( new StringObjectId( EXISTING_ID ) );

    assertIsDeletedNotSet_ForExistingObject( null, jobDelegate, JOB );
  }

  @Test
  public void getObjectInformation_ExistingTrans_IsDeletedFlagNotSet() throws Exception {
    KettleDatabaseRepositoryTransDelegate transDelegate =
      spy( new KettleDatabaseRepositoryTransDelegate( repository ) );

    RowMeta meta = createMetaForJob();
    Object[] values = new Object[ meta.size() ];
    values[ asList( meta.getFieldNames() ).indexOf( FIELD_TRANSFORMATION_NAME ) ] = EXISTING_ID;
    doReturn( new RowMetaAndData( meta, values ) )
      .when( transDelegate )
      .getTransformation( new StringObjectId( EXISTING_ID ) );

    assertIsDeletedNotSet_ForExistingObject( transDelegate, null, TRANSFORMATION );
  }

  private void assertIsDeletedNotSet_ForExistingObject( KettleDatabaseRepositoryTransDelegate transDelegate,
                                                        KettleDatabaseRepositoryJobDelegate jobDelegate,
                                                        RepositoryObjectType objectType )
    throws Exception {
    repository.transDelegate = transDelegate;
    repository.jobDelegate = jobDelegate;

    when( directoryInterface.findDirectory( any( ObjectId.class ) ) ).thenReturn( null );

    RepositoryObject object = repository.getObjectInformation( new StringObjectId( EXISTING_ID ), objectType );
    assertFalse( object.isDeleted() );
  }


  private static RowMeta createMetaForJob() throws Exception {
    LinkedHashMap<String, Integer> fields = new LinkedHashMap<String, Integer>();
    fields.put( FIELD_JOB_NAME, TYPE_STRING );
    fields.put( FIELD_JOB_DESCRIPTION, TYPE_STRING );
    fields.put( FIELD_JOB_MODIFIED_USER, TYPE_STRING );
    fields.put( FIELD_JOB_MODIFIED_DATE, TYPE_DATE );
    fields.put( FIELD_JOB_ID_DIRECTORY, TYPE_INTEGER );
    return createMeta( fields );
  }

  private static RowMeta createMetaForTrans() throws Exception {
    LinkedHashMap<String, Integer> fields = new LinkedHashMap<String, Integer>();
    fields.put( FIELD_TRANSFORMATION_NAME, TYPE_STRING );
    fields.put( FIELD_TRANSFORMATION_DESCRIPTION, TYPE_STRING );
    fields.put( FIELD_TRANSFORMATION_MODIFIED_USER, TYPE_STRING );
    fields.put( FIELD_TRANSFORMATION_MODIFIED_DATE, TYPE_DATE );
    fields.put( FIELD_TRANSFORMATION_ID_DIRECTORY, TYPE_INTEGER );
    return createMeta( fields );
  }

  private static RowMeta createMeta( LinkedHashMap<String, Integer> fields ) throws Exception {
    RowMeta meta = new RowMeta();
    for ( Map.Entry<String, Integer> entry : fields.entrySet() ) {
      meta.addValueMeta( ValueMetaFactory.createValueMeta( entry.getKey(), entry.getValue() ) );
    }
    return meta;
  }
}
