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

package org.pentaho.di.job;

import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobExecutionConfigurationTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testConnectRepository() throws KettleException {
    JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
    final RepositoriesMeta repositoriesMeta = mock( RepositoriesMeta.class );
    final RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    final Repository repository = mock( Repository.class );
    final String mockRepo = "mockRepo";
    final boolean[] connectionSuccess = {false};

    Repository initialRepo = mock( Repository.class );
    jobExecutionConfiguration.setRepository( initialRepo );

    KettleLogStore.init();

    //Create mock repository plugin
    MockRepositoryPlugin mockRepositoryPlugin = mock( MockRepositoryPlugin.class );
    when( mockRepositoryPlugin.getIds() ).thenReturn( new String[]{"mockRepo"} );
    when( mockRepositoryPlugin.matches( "mockRepo" ) ).thenReturn( true );
    when( mockRepositoryPlugin.getName() ).thenReturn( "mock-repository" );
    when( mockRepositoryPlugin.getClassMap() ).thenAnswer( (Answer<Map<Class<?>, String>>) invocation -> {
      Map<Class<?>, String> dbMap = new HashMap<>();
      dbMap.put( Repository.class, repositoryMeta.getClass().getName() );
      return dbMap;
    } );
    PluginRegistry.getInstance().registerPlugin( RepositoryPluginType.class, mockRepositoryPlugin );

    // Define valid connection criteria
    when( repositoriesMeta.findRepository( anyString() ) ).thenAnswer(
      (Answer<RepositoryMeta>) invocation -> mockRepo.equals( invocation.getArguments()[0] ) ? repositoryMeta : null );
    when( mockRepositoryPlugin.loadClass( Repository.class ) ).thenReturn( repository );
    doAnswer( invocation -> {
      if ( "username".equals( invocation.getArguments()[0] ) &&  "password".equals( invocation.getArguments()[1] ) ) {
        connectionSuccess[0] = true;
      } else {
        connectionSuccess[0] = false;
        throw new KettleException( "Mock Repository connection failed" );
      }
      return null;
    } ).when( repository ).connect( anyString(), anyString() );

    //Ignore repository not found in RepositoriesMeta
    jobExecutionConfiguration.connectRepository( repositoriesMeta, "notFound", "username", "password" );
    assertEquals( "Repository Changed", initialRepo, jobExecutionConfiguration.getRepository() );

    //Ignore failed attempt to connect
    jobExecutionConfiguration.connectRepository( repositoriesMeta, mockRepo, "username", "" );
    assertEquals( "Repository Changed", initialRepo, jobExecutionConfiguration.getRepository() );

    //Save repository if connection passes
    jobExecutionConfiguration.connectRepository( repositoriesMeta, mockRepo, "username", "password" );
    assertEquals( "Repository didn't change", repository, jobExecutionConfiguration.getRepository() );
    assertTrue( "Repository not connected", connectionSuccess[0] );
  }
  public interface MockRepositoryPlugin extends PluginInterface, ClassLoadingPluginInterface { }

  @Test
  public void testDefaultPassedBatchId() {
    JobExecutionConfiguration jec = new JobExecutionConfiguration();
    assertNull( "default passedBatchId value must be null", jec.getPassedBatchId() );
  }

  @Test
  public void testCopy() {
    JobExecutionConfiguration jec = new JobExecutionConfiguration();
    final Long passedBatchId0 = null;
    final long passedBatchId1 = 0L;
    final long passedBatchId2 = 5L;

    jec.setPassedBatchId( passedBatchId0 );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      JobExecutionConfiguration jecCopy = (JobExecutionConfiguration) jec.clone();
      assertEquals( "clone-copy", jec.getPassedBatchId(), jecCopy.getPassedBatchId() );
    }
    jec.setPassedBatchId( passedBatchId1 );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      JobExecutionConfiguration jecCopy = (JobExecutionConfiguration) jec.clone();
      assertEquals( "clone-copy", jec.getPassedBatchId(), jecCopy.getPassedBatchId() );
    }
    jec.setPassedBatchId( passedBatchId2 );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      JobExecutionConfiguration jecCopy = (JobExecutionConfiguration) jec.clone();
      assertEquals( "clone-copy", jec.getPassedBatchId(), jecCopy.getPassedBatchId() );
    }
  }

  @Test
  public void testCopyXml() throws Exception {
    JobExecutionConfiguration jec = new JobExecutionConfiguration();
    final Long passedBatchId0 = null;
    final long passedBatchId1 = 0L;
    final long passedBatchId2 = 5L;

    jec.setPassedBatchId( passedBatchId0 );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      String xml = jec.getXML();
      Document doc = XMLHandler.loadXMLString( xml );
      Node node = XMLHandler.getSubNode( doc, JobExecutionConfiguration.XML_TAG );
      JobExecutionConfiguration jecCopy = new JobExecutionConfiguration( node );
      assertEquals( "xml-copy", jec.getPassedBatchId(), jecCopy.getPassedBatchId() );
    }
    jec.setPassedBatchId( passedBatchId1 );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      String xml = jec.getXML();
      Document doc = XMLHandler.loadXMLString( xml );
      Node node = XMLHandler.getSubNode( doc, JobExecutionConfiguration.XML_TAG );
      JobExecutionConfiguration jecCopy = new JobExecutionConfiguration( node );
      assertEquals( "xml-copy", jec.getPassedBatchId(), jecCopy.getPassedBatchId() );
    }
    jec.setPassedBatchId( passedBatchId2 );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      String xml = jec.getXML();
      Document doc = XMLHandler.loadXMLString( xml );
      Node node = XMLHandler.getSubNode( doc, JobExecutionConfiguration.XML_TAG );
      JobExecutionConfiguration jecCopy = new JobExecutionConfiguration( node );
      assertEquals( "xml-copy", jec.getPassedBatchId(), jecCopy.getPassedBatchId() );
    }
  }

  @Test
  public void testGetUsedArguments() throws KettleException {
    JobExecutionConfiguration executionConfiguration = new JobExecutionConfiguration();
    JobMeta jobMeta = new JobMeta(  );
    jobMeta.jobcopies = new ArrayList<>(  );
    String[] commandLineArguments = new String[ 0 ];
    IMetaStore metaStore = mock( IMetaStore.class );

    JobEntryCopy jobEntryCopy0 = new JobEntryCopy(  );

    TransMeta transMeta0 = mock( TransMeta.class );
    Map<String, String> map0 = new HashMap<>(  );
    map0.put( "arg0", "argument0" );
    when( transMeta0.getUsedArguments( commandLineArguments ) ).thenReturn( map0 );

    JobEntryInterface jobEntryInterface0 = mock( JobEntryInterface.class );
    when( jobEntryInterface0.isTransformation() ).thenReturn( false );

    jobEntryCopy0.setEntry( jobEntryInterface0 );
    jobMeta.jobcopies.add( jobEntryCopy0 );


    JobEntryCopy jobEntryCopy1 = new JobEntryCopy(  );

    TransMeta transMeta1 = mock( TransMeta.class );
    Map<String, String> map1 = new HashMap<>(  );
    map1.put( "arg1", "argument1" );
    when( transMeta1.getUsedArguments( commandLineArguments ) ).thenReturn( map1 );

    JobEntryTrans jobEntryTrans1 = mock( JobEntryTrans.class );
    when( jobEntryTrans1.isTransformation() ).thenReturn( true );
    when( jobEntryTrans1.getTransMeta( executionConfiguration.getRepository(), metaStore, jobMeta ) ).thenReturn( transMeta1 );

    jobEntryCopy1.setEntry( jobEntryTrans1 );
    jobMeta.jobcopies.add( jobEntryCopy1 );


    JobEntryCopy jobEntryCopy2 = new JobEntryCopy(  );

    TransMeta transMeta2 = mock( TransMeta.class );
    Map<String, String> map2 = new HashMap<>(  );
    map2.put( "arg1", "argument1" );
    map2.put( "arg2", "argument2" );
    when( transMeta2.getUsedArguments( commandLineArguments ) ).thenReturn( map2 );

    JobEntryTrans jobEntryTrans2 = mock( JobEntryTrans.class );
    when( jobEntryTrans2.isTransformation() ).thenReturn( true );
    when( jobEntryTrans2.getTransMeta( executionConfiguration.getRepository(), metaStore, jobMeta ) ).thenReturn( transMeta2 );

    jobEntryCopy2.setEntry( jobEntryTrans2 );
    jobMeta.jobcopies.add( jobEntryCopy2 );


    executionConfiguration.getUsedArguments( jobMeta, commandLineArguments, metaStore );
    assertEquals( 2, executionConfiguration.getArguments().size() );
  }

  @Test
  public void testGetUsedVariablesWithNoPreviousExecutionConfigurationVariables() {
    JobExecutionConfiguration executionConfiguration = new JobExecutionConfiguration();
    Map<String, String> variables0 =  new HashMap<>();

    executionConfiguration.setVariables( variables0 );

    JobMeta jobMeta0 = mock( JobMeta.class );
    List<String> list0 = new ArrayList<>();
    list0.add( "var1" );
    when( jobMeta0.getUsedVariables(  ) ).thenReturn( list0 );
    // Const.INTERNAL_VARIABLE_PREFIX values
    when( jobMeta0.getVariable( anyString() ) ).thenReturn( "internalDummyValue" );

    executionConfiguration.getUsedVariables( jobMeta0 );

    // 8 = 7 internalDummyValues + var1 from JobMeta with default value
    assertEquals( 8, executionConfiguration.getVariables().size() );
    assertEquals( "", executionConfiguration.getVariables().get( "var1" ) );

  }

  @Test
  public void testGetUsedVariablesWithSamePreviousExecutionConfigurationVariables() {
    JobExecutionConfiguration executionConfiguration = new JobExecutionConfiguration();
    Map<String, String> variables0 =  new HashMap<>();
    variables0.put( "var1", "valueVar1" );
    executionConfiguration.setVariables( variables0 );

    JobMeta jobMeta0 = mock( JobMeta.class );
    List<String> list0 = new ArrayList<>();
    list0.add( "var1" );
    when( jobMeta0.getUsedVariables(  ) ).thenReturn( list0 );
    when( jobMeta0.getVariable( anyString() ) ).thenReturn( "internalDummyValue" );

    executionConfiguration.getUsedVariables( jobMeta0 );

    // 8 = 7 internalDummyValues + var1 from JobMeta ( with variables0 value )
    assertEquals( 8, executionConfiguration.getVariables().size() );
    assertEquals( "valueVar1", executionConfiguration.getVariables().get( "var1" ) );

  }

  @Test
  public void testGetUsedVariablesWithDifferentPreviousExecutionConfigurationVariables() {
    JobExecutionConfiguration executionConfiguration = new JobExecutionConfiguration();
    Map<String, String> variables0 =  new HashMap<>();
    variables0.put( "var2", "valueVar2" );
    executionConfiguration.setVariables( variables0 );

    JobMeta jobMeta0 = mock( JobMeta.class );
    List<String> list0 = new ArrayList<>();
    list0.add( "var1" );
    when( jobMeta0.getUsedVariables(  ) ).thenReturn( list0 );
    when( jobMeta0.getVariable( anyString() ) ).thenReturn( "internalDummyValue" );

    executionConfiguration.getUsedVariables( jobMeta0 );

    // 9 = 7 internalDummyValues + var1 from JobMeta ( with the default value ) + var2 from variables0
    assertEquals( 9, executionConfiguration.getVariables().size() );
    assertEquals( "", executionConfiguration.getVariables().get( "var1" ) );
    assertEquals( "valueVar2", executionConfiguration.getVariables().get( "var2" ) );

  }

}
