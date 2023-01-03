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

package org.pentaho.di.trans.steps.synchronizeaftermerge;

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SynchronizeAfterMergeTest {

  private static final String STEP_NAME = "Sync";

  @Test
  public void initWithCommitSizeVariable() throws KettleDatabaseException {
    StepMeta stepMeta = mock( StepMeta.class );
    doReturn( STEP_NAME ).when( stepMeta ).getName();
    doReturn( 1 ).when( stepMeta ).getCopies();

    SynchronizeAfterMergeMeta smi = mock( SynchronizeAfterMergeMeta.class );
    SynchronizeAfterMergeData sdi = mock( SynchronizeAfterMergeData.class );

    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    doReturn( mock( MySQLDatabaseMeta.class ) ).when( dbMeta ).getDatabaseInterface();

    doReturn( dbMeta ).when( smi ).getDatabaseMeta();
    doReturn( "${commit.size}" ).when( smi ).getCommitSize();

    TransMeta transMeta = mock( TransMeta.class );
    doReturn( "1" ).when( transMeta ).getVariable( Const.INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER );
    doReturn( "2" ).when( transMeta ).getVariable( Const.INTERNAL_VARIABLE_CLUSTER_SIZE );
    doReturn( "Y" ).when( transMeta ).getVariable( Const.INTERNAL_VARIABLE_CLUSTER_MASTER );
    doReturn( stepMeta ).when( transMeta ).findStep( STEP_NAME );

    SynchronizeAfterMerge step = mock( SynchronizeAfterMerge.class );
    doCallRealMethod().when( step ).setTransMeta( any( TransMeta.class ) );
    doCallRealMethod().when( step ).setStepMeta( any( StepMeta.class ) );
    doCallRealMethod().when( step ).init( any( StepMetaInterface.class ), any( StepDataInterface.class ) );
    doCallRealMethod().when( step ).connectToDatabaseOrInitDataSource(  any(), any()  );
    doReturn( stepMeta ).when( step ).getStepMeta();
    doReturn( transMeta ).when( step ).getTransMeta();
    doReturn( "1" ).when( step ).getStepExecutionId();
    doReturn( "120" ).when( step ).environmentSubstitute( "${commit.size}" );

    step.setTransMeta( transMeta );
    step.setStepMeta( stepMeta );
    step.init( smi, sdi );

    assertEquals( 120, sdi.commitSize );
  }
}
