/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.plugins.fileopensave.api.providers.BaseFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentFile;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentTree;
import org.pentaho.di.ui.core.PropsUI;

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
    RecentTree recentTree = new RecentTree( NAME );

    PropsUI propsUI = getPropsUI();
    Date dateThreshold = getDateThreshold();
    List<LastUsedFile> lastUsedFiles = propsUI.getLastUsedFiles().stream().filter(
      lastUsedFile -> !lastUsedFile.getLastOpened().before( dateThreshold ) ).collect( Collectors.toList() );

    for ( LastUsedFile lastUsedFile : lastUsedFiles ) {
      recentTree.addChild( RecentFile.create( lastUsedFile ) );
    }

    return recentTree;
  }

  @Override public List<RecentFile> getFiles( RecentFile file, String filters ) throws FileException {
    return Collections.emptyList();
  }

  @Override public List<RecentFile> delete( List<RecentFile> files ) throws FileException {
    return Collections.emptyList();
  }

  @Override public RecentFile add( RecentFile folder ) throws FileException {
    return null;
  }

  @Override public RecentFile getFile( RecentFile file ) {
    return null;
  }

  @Override public boolean fileExists( RecentFile dir, String path ) throws FileException {
    return false;
  }

  @Override public String getNewName( RecentFile destDir, String newPath ) throws FileException {
    return null;
  }

  @Override public boolean isSame( File file1, File file2 ) {
    return false;
  }

  @Override public RecentFile rename( RecentFile file, String newPath, boolean overwrite ) throws FileException {
    return null;
  }

  @Override public RecentFile copy( RecentFile file, String toPath, boolean overwrite ) throws FileException {
    return null;
  }

  @Override public RecentFile move( RecentFile file, String toPath, boolean overwrite ) throws FileException {
    return null;
  }

  @Override public InputStream readFile( RecentFile file ) throws FileException {
    return null;
  }

  @Override public RecentFile writeFile( InputStream inputStream, RecentFile destDir, String path, boolean overwrite )
    throws FileException {
    return null;
  }

  @Override public RecentFile getParent( RecentFile file ) {
    return null;
  }

  @Override public void clearProviderCache() {
    // Not cached
  }

  private PropsUI getPropsUI() {
    return propsUISupplier.get();
  }

  private Date getDateThreshold() {
    Calendar calendar = Calendar.getInstance();
    calendar.add( Calendar.DATE, -30 );
    return calendar.getTime();
  }
}
