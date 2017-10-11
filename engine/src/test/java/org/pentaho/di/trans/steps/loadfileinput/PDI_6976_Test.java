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

package org.pentaho.di.trans.steps.loadfileinput;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.tools.FileObject;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

/**
 * Tests for LoadFileInputMeta class
 *
 * @author Pavel Sakun
 * @see LoadFileInputMeta
 */
public class PDI_6976_Test {
  @Test
  public void testVerifyNoPreviousStep() {
    LoadFileInputMeta spy = spy( new LoadFileInputMeta() );

    FileInputList fileInputList = mock( FileInputList.class );
    List<FileObject> files = when( mock( List.class ).size() ).thenReturn( 1 ).getMock();
    doReturn( files ).when( fileInputList ).getFiles();
    doReturn( fileInputList ).when( spy ).getFiles( any( VariableSpace.class ) );

    @SuppressWarnings( "unchecked" )
    List<CheckResultInterface> validationResults = mock( List.class );

    // Check we do not get validation errors
    doAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        if ( ( (CheckResultInterface) invocation.getArguments()[0] ).getType() != CheckResultInterface.TYPE_RESULT_OK ) {
          TestCase.fail( "We've got validation error" );
        }

        return null;
      }
    } ).when( validationResults ).add( any( CheckResultInterface.class ) );

    spy.check( validationResults, mock( TransMeta.class ), mock( StepMeta.class ), mock( RowMetaInterface.class ),
      new String[] {}, new String[] { "File content", "File size" }, mock( RowMetaInterface.class ),
      mock( VariableSpace.class ), mock( Repository.class ), mock( IMetaStore.class ) );
  }
}
