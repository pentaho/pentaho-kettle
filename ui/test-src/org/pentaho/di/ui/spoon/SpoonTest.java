/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;

/**
 * Spoon tests
 *
 * @author Pavel Sakun
 * @see Spoon
 */
public class SpoonTest {
  @Test
  public void testCopyPasteStepErrorHandling() throws KettleException {
    final Spoon spoon = mock( Spoon.class );
    doCallRealMethod().when( spoon ).copySelected( any( TransMeta.class ), anyListOf( StepMeta.class ),
        anyListOf( NotePadMeta.class ) );
    doCallRealMethod().when( spoon ).pasteXML( any( TransMeta.class ), anyString(), any( Point.class ) );

    LogChannelInterface log = mock( LogChannelInterface.class );
    when( spoon.getLog() ).thenReturn( log );

    doAnswer( new Answer() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        TransMeta transMeta = new TransMeta();
        spoon.pasteXML( transMeta, (String) invocation.getArguments()[0], mock( Point.class ) );
        Assert.assertNotNull( "No steps found in transformation", transMeta.getStep( 0 ) );
        Assert.assertNotNull( "Error handling information was not copied", transMeta.getStep( 0 ).getStepErrorMeta() );

        return null;
      }
    } ).when( spoon ).toClipboard( anyString() );

    StepMeta stepMeta = new StepMeta( "CsvInput", "Step1", new CsvInputMeta() );
    TransMeta transMeta = mock( TransMeta.class );

    StepErrorMeta errorMeta = new StepErrorMeta( transMeta, stepMeta, stepMeta );
    stepMeta.setStepErrorMeta( errorMeta );

    ArrayList<StepMeta> steps = new ArrayList<StepMeta>();
    steps.add( stepMeta );

    KettleEnvironment.init();

    spoon.copySelected( transMeta, steps, Collections.<NotePadMeta>emptyList() );
  }
}
