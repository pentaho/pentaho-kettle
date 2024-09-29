/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
