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
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.repo.dialog.RepositoryOpenSaveDialog;

import java.util.Collections;
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

  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;
  private Supplier<PropsUI> propsUISupplier = PropsUI::getInstance;

  @Override public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {

    PropsUI propsUI = propsUISupplier.get();
    LastUsedFile lastUsedFile = null;
    List<LastUsedFile>
      lastUsedFileList = propsUI.getLastUsedRepoFiles().getOrDefault( getRepository().getName(), Collections.emptyList() );
    if ( lastUsedFileList.size() > 0 ) {
      lastUsedFile = lastUsedFileList.get( 0 );
    }

    RepositoryOpenSaveDialog repositoryOpenSaveDialog =
      new RepositoryOpenSaveDialog( spoonSupplier.get().getShell(), 950, 615 );
    repositoryOpenSaveDialog
      .open( lastUsedFile != null ? lastUsedFile.getDirectory() : null,
        RepositoryOpenSaveDialog.STATE_SAVE.equals( o ) ? RepositoryOpenSaveDialog.STATE_SAVE : RepositoryOpenSaveDialog.STATE_OPEN );
  }

  private Repository getRepository() {
    return spoonSupplier.get().getRepository();
  }
}
