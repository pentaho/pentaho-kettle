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
package org.pentaho.di.trans;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Yury_Bakhmutski on 2/8/2017.
 */
public class StepWithMappingMetaTest {

  @Test
  public void loadMappingMeta() throws Exception {
    String variablePath = "Internal.Entry.Current.Directory";
    String virtualDir = "/testFolder/CDA-91";
    String fileName = "testTrans.ktr";

    VariableSpace variables = new Variables();
    variables.setVariable( variablePath, virtualDir );

    StepWithMappingMeta mappingMetaMock = mock( StepWithMappingMeta.class );
    when( mappingMetaMock.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    when( mappingMetaMock.getFileName() ).thenReturn( "${" + variablePath + "}/" + fileName );

    // mock repo and answers
    Repository rep = mock( Repository.class );

    Mockito.doAnswer( new Answer<TransMeta>() {
      @Override
      public TransMeta answer( final InvocationOnMock invocation ) throws Throwable {
        final String originalArgument = (String) ( invocation.getArguments() )[ 0 ];
        // be sure that the variable was replaced by real path
        assertEquals( originalArgument, virtualDir );
        return null;
      }
    } ).when( rep ).findDirectory( anyString() );

    Mockito.doAnswer( new Answer<TransMeta>() {
      @Override
      public TransMeta answer( final InvocationOnMock invocation ) throws Throwable {
        final String originalArgument = (String) ( invocation.getArguments() )[ 0 ];
        // be sure that transformation name was resolved correctly
        assertEquals( originalArgument, fileName );
        return mock( TransMeta.class );
      }
    } ).when( rep ).loadTransformation( anyString(), any( RepositoryDirectoryInterface.class ),
      any( ProgressMonitorListener.class ), anyBoolean(), anyString() );

    StepWithMappingMeta.loadMappingMeta( mappingMetaMock, rep, null, variables, true );
  }

}
