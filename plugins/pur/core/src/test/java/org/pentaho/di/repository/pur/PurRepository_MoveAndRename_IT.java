/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.VersionSummary;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PurRepository_MoveAndRename_IT extends PurRepositoryTestBase {

  private final JobAssistant jobAssistant = new JobAssistant();
  private final TransAssistant transAssistant = new TransAssistant();

  public PurRepository_MoveAndRename_IT( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  @Test
  public void renameJob_Successfully() throws Exception {
    rename_Successfully( jobAssistant );
  }

  @Test
  public void renameTrans_Successfully() throws Exception {
    rename_Successfully( transAssistant );
  }

  private void rename_Successfully( Assistant assistant ) throws Exception {
    final String initial = "rename_Successfully";
    final String renamed = initial + "_renamed";
    AbstractMeta meta = assistant.createNew();

    RepositoryDirectoryInterface directory = getPublicDir();
    assistant.save( meta, initial, directory );

    assistant.rename( meta, renamed );
  }

  @Test
  public void renameJob_CreatesNewRevision() throws Exception {
    rename_CreatesNewRevision( jobAssistant );
  }

  @Test
  public void renameTrans_CreatesNewRevision() throws Exception {
    rename_CreatesNewRevision( transAssistant );
  }

  private void rename_CreatesNewRevision( Assistant assistant ) throws Exception {
    final String initial = "rename_CreatesNewRevision";
    final String renamed = initial + "_renamed";
    AbstractMeta meta = assistant.createNew();

    assistant.save( meta, initial, getPublicDir() );
    List<VersionSummary> historyBefore = unifiedRepository.getVersionSummaries( meta.getObjectId().getId() );

    long before = System.currentTimeMillis();
    assistant.rename( meta, renamed );
    long after = System.currentTimeMillis();

    List<VersionSummary> historyAfter = unifiedRepository.getVersionSummaries( meta.getObjectId().getId() );
    assertEquals( historyBefore.size() + 1, historyAfter.size() );

    long newRevisionTs = historyAfter.get( historyAfter.size() - 1 ).getDate().getTime();
    assertTrue( String.format( "%d <= %d <= %d", before, newRevisionTs, after ), ( before <= newRevisionTs )
        && ( newRevisionTs <= after ) );
  }

  @Test( expected = KettleException.class )
  public void renameJob_FailsIfANameConflictOccurs() throws Exception {
    rename_FailsIfANameConflictOccurs( jobAssistant );
  }

  @Test( expected = KettleException.class )
  public void renameTrans_FailsIfANameConflictOccurs() throws Exception {
    rename_FailsIfANameConflictOccurs( transAssistant );
  }

  private void rename_FailsIfANameConflictOccurs( Assistant assistant ) throws Exception {
    final String name = "rename_FailsIfANameConflictOccurs";
    AbstractMeta meta = assistant.createNew();

    assistant.save( meta, name, getPublicDir() );
    assistant.rename( meta, name );
  }

  @Test
  public void moveJob_Successfully() throws Exception {
    move_Successfully( jobAssistant );
  }

  @Test
  public void moveTrans_Successfully() throws Exception {
    move_Successfully( transAssistant );
  }

  private void move_Successfully( Assistant assistant ) throws Exception {
    final String filename = "move_Successfully";
    AbstractMeta meta = assistant.createNew();

    assistant.save( meta, filename, getPublicDir() );

    RepositoryDirectoryInterface destFolder = getDirInsidePublic( filename );
    assertNotNull( destFolder );

    assistant.move( meta, destFolder );
  }

  @Test
  public void moveJob_DoesNotCreateRevision() throws Exception {
    move_DoesNotCreateRevision( jobAssistant );
  }

  @Test
  public void moveTrans_DoesNotCreateRevision() throws Exception {
    move_DoesNotCreateRevision( transAssistant );
  }

  private void move_DoesNotCreateRevision( Assistant assistant ) throws Exception {
    final String fileName = "move_DoesNotCreateRevision";
    AbstractMeta meta = assistant.createNew();

    assistant.save( meta, fileName, getPublicDir() );

    List<VersionSummary> historyBefore = unifiedRepository.getVersionSummaries( meta.getObjectId().getId() );
    purRepository.renameTransformation( meta.getObjectId(), getDirInsidePublic( fileName ), null );
    List<VersionSummary> historyAfter = unifiedRepository.getVersionSummaries( meta.getObjectId().getId() );

    assertEquals( historyBefore.size(), historyAfter.size() );
  }

  @Test( expected = KettleException.class )
  public void moveJob_FailsIfANameConflictOccurs() throws Exception {
    move_FailsIfANameConflictOccurs( jobAssistant );
  }

  @Test( expected = KettleException.class )
  public void moveTrans_FailsIfANameConflictOccurs() throws Exception {
    move_FailsIfANameConflictOccurs( transAssistant );
  }

  private void move_FailsIfANameConflictOccurs( Assistant assistant ) throws Exception {
    final String fileName = "move_FailsIfANameConflictOccurs";
    AbstractMeta meta = assistant.createNew();
    AbstractMeta anotherMeta = assistant.createNew();

    RepositoryDirectoryInterface directory = getPublicDir();
    assistant.save( meta, fileName, directory );

    RepositoryDirectoryInterface destFolder = getDirInsidePublic( fileName );
    assistant.save( anotherMeta, fileName, destFolder );

    assistant.move( meta, destFolder );
  }

  @Test
  public void moveAndRenameJob_Successfully() throws Exception {
    moveAndRename_Successfully( jobAssistant );
  }

  @Test
  public void moveAndRenameTrans_Successfully() throws Exception {
    moveAndRename_Successfully( transAssistant );
  }

  private void moveAndRename_Successfully( Assistant assistant ) throws Exception {
    final String fileName = "moveAndRename_Successfully";
    final String renamed = fileName + "_renamed";
    AbstractMeta meta = assistant.createNew();

    assistant.save( meta, fileName, getPublicDir() );

    RepositoryDirectoryInterface destFolder = getDirInsidePublic( fileName );
    assertNotNull( destFolder );

    assistant.rename( meta, destFolder, renamed );
  }

  @Test( expected = KettleException.class )
  public void moveAndRenameTrans_FailsIfANameConflictOccurs() throws Exception {
    moveAndRename_FailsIfANameConflictOccurs( transAssistant );
  }

  @Test( expected = KettleException.class )
  public void moveAndRenameJob_FailsIfANameConflictOccurs() throws Exception {
    moveAndRename_FailsIfANameConflictOccurs( jobAssistant );
  }

  private void moveAndRename_FailsIfANameConflictOccurs( Assistant assistant ) throws Exception {
    final String fileName = "moveAndRename_FailsIfANameConflictOccurs";
    final String renamed = fileName + "_renamed";
    AbstractMeta meta = assistant.createNew();
    AbstractMeta anotherMeta = assistant.createNew();

    RepositoryDirectoryInterface directory = getPublicDir();
    assistant.save( meta, fileName, directory );

    RepositoryDirectoryInterface destFolder = getDirInsidePublic( fileName );
    assistant.save( anotherMeta, renamed, destFolder );

    assistant.rename( meta, destFolder, renamed );
  }

  private RepositoryDirectoryInterface getPublicDir() throws Exception {
    return purRepository.findDirectory( "public" );
  }

  private RepositoryDirectoryInterface getDirInsidePublic( String dirName ) throws Exception {
    RepositoryDirectoryInterface child = getPublicDir().findChild( dirName );
    return ( child == null ) ? purRepository.createRepositoryDirectory( getPublicDir(), dirName ) : child;
  }

  private abstract class Assistant {
    public abstract AbstractMeta createNew();

    abstract String getType();

    public void save( AbstractMeta meta, String name, RepositoryDirectoryInterface directory ) throws Exception {
      assertNotNull( directory );

      meta.setName( name );
      meta.setRepositoryDirectory( directory );
      purRepository.save( meta, null, null );
      assertExistsIn( directory, name, getType() + " was not saved" );
    }

    void assertExistsIn( RepositoryDirectoryInterface dir, String name, String message ) throws Exception {
      List<String> existing = getNames( dir );
      assertThat( message, existing, hasItem( name ) );
    }

    abstract List<String> getNames( RepositoryDirectoryInterface dir ) throws Exception;

    public void rename( AbstractMeta meta, String newName ) throws Exception {
      rename( meta, meta.getRepositoryDirectory(), newName );
    }

    public void move( AbstractMeta meta, RepositoryDirectoryInterface destFolder ) throws Exception {
      rename( meta, destFolder, null );
    }

    public void rename( AbstractMeta meta, RepositoryDirectoryInterface destFolder, String newName ) throws Exception {
      doRename( meta, destFolder, newName );

      String checkedName = ( newName == null ) ? meta.getName() : newName;
      assertExistsIn( destFolder, checkedName, getType() + " was not renamed" );
    }

    abstract void doRename( AbstractMeta meta, RepositoryDirectoryInterface destFolder, String newName )
      throws Exception;

  }

  private class JobAssistant extends Assistant {

    @Override
    public JobMeta createNew() {
      return new JobMeta();
    }

    @Override
    String getType() {
      return "Job";
    }

    @Override
    void doRename( AbstractMeta meta, RepositoryDirectoryInterface destFolder, String newName ) throws Exception {
      purRepository.renameJob( meta.getObjectId(), destFolder, newName );
    }

    @Override
    List<String> getNames( RepositoryDirectoryInterface dir ) throws Exception {
      return Arrays.asList( purRepository.getJobNames( dir.getObjectId(), false ) );
    }

  }

  private class TransAssistant extends Assistant {

    @Override
    public TransMeta createNew() {
      return new TransMeta();
    }

    @Override
    public String getType() {
      return "Trans";
    }

    @Override
    void doRename( AbstractMeta meta, RepositoryDirectoryInterface destFolder, String newName ) throws Exception {
      purRepository.renameTransformation( meta.getObjectId(), destFolder, newName );
    }

    @Override
    List<String> getNames( RepositoryDirectoryInterface dir ) throws Exception {
      return Arrays.asList( purRepository.getTransformationNames( dir.getObjectId(), false ) );
    }
  }
}
