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

package org.pentaho.di.job.entries.deletefiles;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobEntryDeleteFilesTest {
  private final String PATH_TO_FILE = "path/to/file";
  private final String STRING_SPACES_ONLY = "   ";

  private JobEntryDeleteFiles jobEntry;
  private NamedClusterEmbedManager mockNamedClusterEmbedManager;

  @Before
  public void setUp() throws Exception {
    jobEntry = new JobEntryDeleteFiles();
    Job parentJob = mock( Job.class );
    doReturn( false ).when( parentJob ).isStopped();

    jobEntry.setParentJob( parentJob );
    JobMeta mockJobMeta = mock( JobMeta.class );
    mockNamedClusterEmbedManager = mock( NamedClusterEmbedManager.class );
    when( mockJobMeta.getNamedClusterEmbedManager() ).thenReturn( mockNamedClusterEmbedManager );
    jobEntry.setParentJobMeta( mockJobMeta );
    jobEntry = spy( jobEntry );
    doReturn( true ).when( jobEntry ).processFile( anyString(), anyString(), eq( parentJob ) );
  }

  @Test
  public void filesWithNoPath_AreNotProcessed_ArgsOfCurrentJob() throws Exception {
    jobEntry.setArguments( new String[] { Const.EMPTY_STRING, STRING_SPACES_ONLY } );
    jobEntry.setFilemasks( new String[] { null, null } );
    jobEntry.setArgFromPrevious( false );

    jobEntry.execute( new Result(), 0 );
    verify( jobEntry, never() ).processFile( anyString(), anyString(), any( Job.class ) );
  }


  @Test
  public void filesWithPath_AreProcessed_ArgsOfCurrentJob() throws Exception {
    String[] args = new String[] { PATH_TO_FILE };
    jobEntry.setArguments( args );
    jobEntry.setFilemasks( new String[] { null, null } );
    jobEntry.setArgFromPrevious( false );

    jobEntry.execute( new Result(), 0 );
    verify( jobEntry, times( args.length ) ).processFile( anyString(), anyString(), any( Job.class ) );
    verify( mockNamedClusterEmbedManager ).passEmbeddedMetastoreKey( anyObject(), anyString() );
  }


  @Test
  public void filesWithNoPath_AreNotProcessed_ArgsOfPreviousMeta() throws Exception {
    jobEntry.setArgFromPrevious( true );

    Result prevMetaResult = new Result();
    List<RowMetaAndData> metaAndDataList = new ArrayList<>();

    metaAndDataList.add( constructRowMetaAndData( Const.EMPTY_STRING, null ) );
    metaAndDataList.add( constructRowMetaAndData( STRING_SPACES_ONLY, null ) );

    prevMetaResult.setRows( metaAndDataList );

    jobEntry.execute( prevMetaResult, 0 );
    verify( jobEntry, never() ).processFile( anyString(), anyString(), any( Job.class ) );
  }

  @Test
  public void filesPath_AreProcessed_ArgsOfPreviousMeta() throws Exception {
    jobEntry.setArgFromPrevious( true );

    Result prevMetaResult = new Result();
    List<RowMetaAndData> metaAndDataList = new ArrayList<>();

    metaAndDataList.add( constructRowMetaAndData( PATH_TO_FILE, null ) );
    prevMetaResult.setRows( metaAndDataList );

    jobEntry.execute( prevMetaResult, 0 );
    verify( jobEntry, times( metaAndDataList.size() ) ).processFile( anyString(), anyString(), any( Job.class ) );
  }

  @Test
  public void filesPathVariables_AreProcessed_OnlyIfValueIsNotBlank() throws Exception {
    final String pathToFileBlankValue = "pathToFileBlankValue";
    final String pathToFileValidValue = "pathToFileValidValue";

    jobEntry.setVariable( pathToFileBlankValue, Const.EMPTY_STRING );
    jobEntry.setVariable( pathToFileValidValue, PATH_TO_FILE );

    jobEntry.setArguments( new String[] { asVariable( pathToFileBlankValue ), asVariable( pathToFileValidValue ) } );
    jobEntry.setFilemasks( new String[] { null, null } );
    jobEntry.setArgFromPrevious( false );

    jobEntry.execute( new Result(), 0 );

    verify( jobEntry ).processFile( eq( PATH_TO_FILE ), anyString(), any( Job.class ) );
  }

  @Test
  public void specifyingTheSamePath_WithDifferentWildcards() throws Exception {
    final String fileExtensionTxt = ".txt";
    final String fileExtensionXml = ".xml";

    String[] args = new String[] { PATH_TO_FILE, PATH_TO_FILE };
    jobEntry.setArguments( args );
    jobEntry.setFilemasks( new String[] { fileExtensionTxt, fileExtensionXml } );
    jobEntry.setArgFromPrevious( false );

    jobEntry.execute( new Result(), 0 );

    verify( jobEntry ).processFile( eq( PATH_TO_FILE ), eq( fileExtensionTxt ), any( Job.class ) );
    verify( jobEntry ).processFile( eq( PATH_TO_FILE ), eq( fileExtensionXml ), any( Job.class ) );
  }

  private RowMetaAndData constructRowMetaAndData( Object... data ) {
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "filePath" ) );
    meta.addValueMeta( new ValueMetaString( "wildcard" ) );

    return new RowMetaAndData( meta, data );
  }

  private String asVariable( String variable ) {
    return "${" + variable + "}";
  }
}
