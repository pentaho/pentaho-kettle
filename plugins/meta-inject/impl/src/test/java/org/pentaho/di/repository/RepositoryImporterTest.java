/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.metainject.MetaInjectMeta;
import org.pentaho.di.trans.steps.metainject.RepositoryImporterExtension;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepositoryImporterTest {

  private static final String ROOT_PATH = "/test_root";

  @Mock
  private RepositoryImportFeedbackInterface feedback;

  private RepositoryDirectoryInterface baseDirectory;

  private Node entityNode;

  @Before
  public void beforeTest() {
    NodeList nodeList = mock( NodeList.class );

    entityNode = mock( Node.class );
    when( entityNode.getChildNodes() ).thenReturn( nodeList );

    baseDirectory = mock( RepositoryDirectoryInterface.class );
    when( baseDirectory.getPath() ).thenReturn( ROOT_PATH );
  }

  @Test
  public void testPatchTransSteps_with_meta_inject_step() throws Exception {
    Repository repository = mock( Repository.class );
    LogChannelInterface log = mock( LogChannelInterface.class );
    RepositoryImporter importer = spy( new RepositoryImporter( repository, log ) );
    importer.setBaseDirectory( mock( RepositoryDirectoryInterface.class ) );
    doReturn( "TEST_PATH" ).when( importer ).resolvePath( anyString(), anyString() );

    MetaInjectMeta metaInjectMeta = mock( MetaInjectMeta.class );
    doReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME ).when( metaInjectMeta ).getSpecificationMethod();
    StepMeta stepMeta = mock( StepMeta.class );
    doReturn( metaInjectMeta ).when( stepMeta ).getStepMetaInterface();
    doReturn( true ).when( stepMeta ).isEtlMetaInject();
    TransMeta transMeta = mock( TransMeta.class );
    doReturn( Collections.singletonList( stepMeta ) ).when( transMeta ).getSteps();

    Object[] object = new Object[4];
    object[0] = "TEST_PATH";
    object[1] = mock( RepositoryDirectoryInterface.class );
    object[2] = stepMeta;
    object[3] = true;

    RepositoryImporterExtension repositoryImporterExtension = new RepositoryImporterExtension();
    repositoryImporterExtension.callExtensionPoint( log, object );

    verify( metaInjectMeta ).setDirectoryPath( "TEST_PATH" );
  }
}
