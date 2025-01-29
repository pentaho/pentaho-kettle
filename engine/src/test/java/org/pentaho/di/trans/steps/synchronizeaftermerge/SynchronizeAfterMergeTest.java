/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
