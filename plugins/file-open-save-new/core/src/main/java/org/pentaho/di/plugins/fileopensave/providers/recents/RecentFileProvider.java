/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.recents;

import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentFile;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentTree;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryFile;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryTree;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RecentFileProvider extends BaseFileProvider<RecentFile> {

  public static final String TYPE = "recents";
  public static final String NAME = "Recents";

  private Supplier<PropsUI> propsUISupplier = PropsUI::getInstance;

  @Override public Class<RecentFile> getFileClass() {
    return RecentFile.class;
  }

  @Override public String getName() {
    return null;
  }

  @Override public String getType() {
    return TYPE;
  }

  @Override public boolean isAvailable() {
    return true;
  }

  @Override public Tree getTree() {
    Tree recentTree;

    PropsUI propsUI = getPropsUI();
    Date dateThreshold = getDateThreshold();
    List<LastUsedFile> lastUsedFiles;
    final Spoon spoonInstance = Spoon.getInstance();
    if ( spoonInstance.rep == null ) {
      lastUsedFiles = propsUI.getLastUsedFiles().stream()
        .filter(
          lastUsedFile -> !lastUsedFile.isSourceRepository() && !lastUsedFile.getLastOpened().before( dateThreshold ) )
        .collect( Collectors.toList() );
      recentTree = new RecentTree( NAME );
      for ( LastUsedFile lastUsedFile : lastUsedFiles ) {
        recentTree.addChild( RecentFile.create( lastUsedFile ) );
      }
    } else {
      IUser userInfo = spoonInstance.rep.getUserInfo();
      String repoAndUser = spoonInstance.rep.getName() + ":" + ( userInfo != null ? userInfo.getLogin() : "" );
      lastUsedFiles = propsUI.getLastUsedRepoFiles().getOrDefault( repoAndUser, Collections.emptyList() ).stream()
        .filter( lastUsedFile -> !lastUsedFile.getLastOpened().before( dateThreshold ) ).collect( Collectors.toList() );
      recentTree = new RepositoryTree( NAME );
      getLastUsedFile( lastUsedFiles, spoonInstance, recentTree );
    }

    return recentTree;
  }

  @Override public List<RecentFile> getFiles( RecentFile file, String filters, VariableSpace space ) throws FileException {
    return Collections.emptyList();
  }

  @Override public List<RecentFile> delete( List<RecentFile> files, VariableSpace space ) throws FileException {
    return Collections.emptyList();
  }

  @Override public RecentFile add( RecentFile folder, VariableSpace space ) throws FileException {
    return null;
  }

  @Override public RecentFile getFile( RecentFile file, VariableSpace space ) {
    return null;
  }

  @Override public boolean fileExists( RecentFile dir, String path, VariableSpace space ) throws FileException {
    return false;
  }

  @Override public String getNewName( RecentFile destDir, String newPath, VariableSpace space ) throws FileException {
    return null;
  }

  @Override public boolean isSame( File file1, File file2 ) {
    return false;
  }

  @Override public RecentFile rename( RecentFile file, String newPath, OverwriteStatus overwriteStatus, VariableSpace space ) throws FileException {
    return null;
  }

  @Override public RecentFile copy( RecentFile file, String toPath, OverwriteStatus overwriteStatus, VariableSpace space ) throws FileException {
    return null;
  }

  @Override public RecentFile move( RecentFile file, String toPath, OverwriteStatus overwriteStatus, VariableSpace space ) throws FileException {
    return null;
  }

  @Override public InputStream readFile( RecentFile file, VariableSpace space ) throws FileException {
    return null;
  }

  @Override public RecentFile writeFile( InputStream inputStream, RecentFile destDir, String path,
                                         OverwriteStatus overwriteStatus, VariableSpace space )
    throws FileException {
    return null;
  }

  @Override public RecentFile getParent( RecentFile file ) {
    return null;
  }

  @Override public void clearProviderCache() {
    // Not cached
  }

  @Override public RecentFile createDirectory( String parentPath, RecentFile file, String newDirectoryName ) {
    return null;
  }

  private PropsUI getPropsUI() {
    return propsUISupplier.get();
  }

  private Date getDateThreshold() {
    Calendar calendar = Calendar.getInstance();
    calendar.add( Calendar.DATE, -30 );
    return calendar.getTime();
  }

  private void getLastUsedFile( List<LastUsedFile> lastUsedFiles, Spoon spoonInstance, Tree recentTree ) {
    for ( LastUsedFile lastUsedFile : lastUsedFiles ) {
      ObjectId objectID;
      try {
        if ( lastUsedFile.isTransformation() ) {
          objectID = spoonInstance.rep.getTransformationID( lastUsedFile.getFilename(),
            spoonInstance.rep.findDirectory( lastUsedFile.getDirectory() ) );
        } else {
          objectID = spoonInstance.rep.getJobId( lastUsedFile.getFilename(),
            spoonInstance.rep.findDirectory( lastUsedFile.getDirectory() ) );
        }
      } catch ( KettleException e ) {
        objectID = null;
      }
      if ( objectID != null ) {
        recentTree.addChild( RepositoryFile.create( lastUsedFile, objectID ) );
      }
    }
  }
}
