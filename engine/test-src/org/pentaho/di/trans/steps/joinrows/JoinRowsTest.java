/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.joinrows;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.File;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 * @author Denis Mashukov
 */
public class JoinRowsTest {

  private StepMetaInterface meta;
  private JoinRowsData data;

  @Before
  public void setUp() throws Exception {
    meta = new JoinRowsMeta();
    data = new JoinRowsData();
  }

  @After
  public void tearDown() {
    meta = null;
    data = null;
  }

  /**
   * BACKLOG-8520 Check that method call does't throw an error NullPointerException.
   */
  @Test
  public void checkThatMethodPerformedWithoutError() throws Exception {
    getJoinRows().dispose( meta, data );
  }

  @Test
  public void disposeDataFiles() throws Exception {
    File mockFile1 = Mockito.mock( File.class );
    File mockFile2 = Mockito.mock( File.class );
    data.file = new File[]{ null, mockFile1, mockFile2 };
    getJoinRows().dispose( meta, data );
    verify( mockFile1, times( 1 ) ).delete();
    verify( mockFile2, times( 1 ) ).delete();
  }

  private JoinRows getJoinRows() throws Exception {
    StepMeta stepMeta = new StepMeta();
    TransMeta transMeta = new TransMeta();
    Trans trans = new Trans( transMeta );

    transMeta.clear();
    transMeta.addStep( stepMeta );
    transMeta.setStep( 0, stepMeta );
    stepMeta.setName( "test" );
    trans.setLog( Mockito.mock( LogChannelInterface.class ) );
    trans.prepareExecution( null );
    trans.startThreads();

    return new JoinRows( stepMeta, null, 0, transMeta, trans );
  }
}
