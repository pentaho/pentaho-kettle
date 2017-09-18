/*
 * Copyright 2017 Pentaho Corporation. All rights reserved.
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
 */

package org.pentaho.repo.extension;

import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.repo.dialog.RepositoryOpenSaveDialog;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 5/23/17.
 */

@ExtensionPoint(
  id = "RepositoryOpenSaveExtensionPoint",
  extensionPointId = "SpoonOpenSaveRepository",
  description = "Open the repository browser"
)
public class RepositoryOpenSaveExtensionPoint implements ExtensionPointInterface {

  public static final String TRANSFORMATION = "transformation";
  public static final int WIDTH = 947;
  public static final int HEIGHT = 626;
  public static final int DAYS = -30;
  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;
  private Supplier<PropsUI> propsUISupplier = PropsUI::getInstance;

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
    FileDialogOperation fileDialogOperation = (FileDialogOperation) o;

    PropsUI propsUI = propsUISupplier.get();

    String username = getRepository().getUserInfo() != null ? getRepository().getUserInfo().getLogin() : "";
    String repoAndUser = getRepository().getName() + ":" + username;
    List<LastUsedFile>
      lastUsedFileList = propsUI.getLastUsedRepoFiles().getOrDefault( repoAndUser, Collections.emptyList() );

    String startingDir = getStartingDir( fileDialogOperation, lastUsedFileList );

    RepositoryOpenSaveDialog repositoryOpenSaveDialog =
      new RepositoryOpenSaveDialog( spoonSupplier.get().getShell(), WIDTH, HEIGHT );
    repositoryOpenSaveDialog
      .open( startingDir,
        RepositoryOpenSaveDialog.STATE_SAVE.equals( fileDialogOperation.getCommand() ) ?
          RepositoryOpenSaveDialog.STATE_SAVE :
          RepositoryOpenSaveDialog.STATE_OPEN, fileDialogOperation.getFilter(),
        fileDialogOperation.getOrigin() );

    if ( !Utils.isEmpty( repositoryOpenSaveDialog.getObjectName() ) ) {
      RepositoryObject repositoryObject = new RepositoryObject();
      repositoryObject.setObjectId( repositoryOpenSaveDialog::getObjectId );
      repositoryObject.setName( repositoryOpenSaveDialog.getObjectName() );
      repositoryObject
        .setRepositoryDirectory( getRepository().findDirectory( repositoryOpenSaveDialog.getObjectDirectory() ) );
      repositoryObject.setObjectType(
        repositoryOpenSaveDialog.getObjectType().equals( TRANSFORMATION ) ? RepositoryObjectType.TRANSFORMATION :
          RepositoryObjectType.JOB );
      fileDialogOperation.setRepositoryObject( repositoryObject );
    }
  }

  private String getStartingDir( FileDialogOperation fileDialogOperation, List<LastUsedFile> lastUsedFileList ) {
    String startingDir = fileDialogOperation.getStartDir();
    if ( !Utils.isEmpty( startingDir ) ) {
      return startingDir;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.add( Calendar.DATE, DAYS );
    Date dateBefore = calendar.getTime();

    LastUsedFile lastUsedFile = null;
    if ( lastUsedFileList.size() > 0 && lastUsedFileList.get( 0 ).getLastOpened().after( dateBefore ) ) {
      lastUsedFile = lastUsedFileList.get( 0 );
    }
    return lastUsedFile != null ? lastUsedFile.getDirectory() : null;
  }

  private Repository getRepository() {
    return spoonSupplier.get().getRepository();
  }
}
