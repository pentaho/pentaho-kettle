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
package org.pentaho.di.plugins.fileopensave.api.overwrite;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.plugins.fileopensave.api.progress.FileCopyProgressDialog;

public class OverwriteStatus {
  private Shell parentShell;
  private boolean applyToAll;
  private OverwriteMode overwriteMode;
  private FileCopyProgressDialog fileCopyProgressDialog;

  /**
   * This class keeps the current state of whether duplicate files should be renamed, skipped, or overwritten. It also
   * keep references to the display shell and the FileCopyProgressDialog for whoever needs it.  In this way, any file
   * copy/move operations can take advantage of this class to provide 1) a progress dialog to display what file is being
   * worked on at the moment and 2) A popup window to display any time a duplicate file it detected.  The user can click
   * on the "Apply to additional duplicate files/directories" checkbox to set the answer for all following files so that
   * the dialog will not show up again. <br>
   * <p>
   * When a copy starts the overwriteMode is set to NONE.  During the copy, when a duplicate file is detected, a call
   * must be made to the {@link #promptOverwriteIfNecessary(boolean, String)}. The boolean is whether the file in
   * question already exists and the string is the fully specified file path.  Since files can exist in may places, like
   * VFS, repository, NamedClusters as well as locally, the caller must let this class know what the duplicate status
   * is.<br> If the incoming boolean says the file does not exist, then it will reset the mode to NONE if the {@link
   * #applyToAll } is false. <br> If the incoming boolean says the file does exist, then if the applyToAll is set to
   * true, it will simple return control to the caller, but if the applyToAll is set to false, then it will pop up the
   * OverwriteStatus dialog and the user will be required to select one of the following choices and the caller should
   * operate on that choice accodingly.<br> Overwrite - The caller should overwrite the existing file<br> Rename - The
   * caller should rename the file to another name (and possibly remember that name if renaming a folder)<br> Skip - The
   * caller should skip this file, if the file is a folder it should skip the entire folder ) Cancel - The caller should
   * cancel the operation, whatever that entails.<br> The caller should check for the cancel mode on return and abort.
   *
   * @param parentShell
   */
  public OverwriteStatus( Shell parentShell ) {
    applyToAll = false;
    overwriteMode = OverwriteMode.NONE;
    this.parentShell = parentShell;
  }

  public OverwriteStatus( Shell parentShell, OverwriteMode overwriteMode ) {
    if ( overwriteMode.equals( OverwriteMode.NONE ) ) {
      throw new IllegalArgumentException( "Must set the override mode if using the non-interactive constructor" );
    }
    this.parentShell = parentShell;
    applyToAll = true;
    this.overwriteMode = overwriteMode;
  }

  public FileCopyProgressDialog getFileCopyProgressDialog() {
    return fileCopyProgressDialog;
  }

  public void setFileCopyProgressDialog(
    FileCopyProgressDialog fileCopyProgressDialog ) {
    this.fileCopyProgressDialog = fileCopyProgressDialog;
  }

  public boolean isApplyToAll() {
    return applyToAll;
  }

  public void setApplyToAll( boolean applyToAll ) {
    this.applyToAll = applyToAll;
  }

  public OverwriteMode getOverwriteMode() {
    return overwriteMode;
  }

  public boolean isOverwrite() {
    return overwriteMode.equals( OverwriteStatus.OverwriteMode.OVERWRITE );
  }

  public boolean isSkip() {
    return overwriteMode.equals( OverwriteStatus.OverwriteMode.SKIP );
  }

  public boolean isRename() {
    return overwriteMode.equals( OverwriteStatus.OverwriteMode.RENAME );
  }

  public boolean isCancel() {
    return overwriteMode.equals( OverwriteStatus.OverwriteMode.CANCEL );
  }

  public boolean isNone() {
    return overwriteMode.equals( OverwriteStatus.OverwriteMode.NONE );
  }

  public void setOverwriteMode( OverwriteMode overwriteMode ) {
    this.overwriteMode = overwriteMode;
  }

  public Shell getParentShell() {
    return parentShell;
  }

  public void promptOverwriteIfNecessary( boolean fileExists, String pathToDupFile, String type ) {
    promptOverwriteIfNecessary( fileExists, pathToDupFile, type, null, "" );
  }

  public void promptOverwriteIfNecessary( boolean fileExists, String pathToDupFile, String type,
                                          OverwriteMode[] disableModes, String notes ) {
    if ( fileExists ) {
      promptOverwriteIfNecessary( pathToDupFile, type, disableModes, notes );
    } else {
      reset();
    }
  }

  public void promptOverwriteIfNecessary( String pathToDupFile, String type ) {
    promptOverwriteIfNecessary( pathToDupFile, type, new OverwriteMode[] {} );
  }

  // Use this method to prompt on duplicates if not already answered.  May modify overwriteStatus
  public void promptOverwriteIfNecessary( String pathToDupFile, String type, OverwriteMode[] disableModes ) {
    promptOverwriteIfNecessary( pathToDupFile, type, disableModes, "" );
  }

  // Use this method to prompt on duplicates and supply a note as well as the file in play. May modify overwriteStatus
  public void promptOverwriteIfNecessary( String pathToDupFile, String type, OverwriteMode[] disableModes, String notes ) {
    if ( !applyToAll && parentShell != null ) {
      OverwriteDialog overwriteDialog = new OverwriteDialog( parentShell, 600, 300, disableModes );
      //Change the original object so it automatically propogates the answer
      OverwriteStatus newOverwriteStatus = overwriteDialog.open( StringUtils.capitalize( type ) + " already exists",
        pathToDupFile, type, notes );
      copyData( newOverwriteStatus, this );
    }
  }

  public void activateProgressDialog( String title ) {
    if ( parentShell != null ) {
      FileCopyProgressDialog progressDialog = new FileCopyProgressDialog( parentShell );
      progressDialog.open( title );
      fileCopyProgressDialog = progressDialog;
      readAndDispatch();
    } else {
      fileCopyProgressDialog = null; //If there is no shell we can't have a progress dialog
    }
  }

  public void setCurrentFileInProgressDialog( String currentFile ) {
    if ( fileCopyProgressDialog != null ) {
      fileCopyProgressDialog.setFile( currentFile );
      readAndDispatch();
    }
  }

  private void readAndDispatch() {
    if ( fileCopyProgressDialog != null ) {
      Shell shell = fileCopyProgressDialog.getShell();
      while ( !shell.isDisposed() ) {
        if ( !shell.getDisplay().readAndDispatch() ) {
          break; //All events processed, exit and give control back to the copy instead of sleeping
        }
      }
      if ( shell.isDisposed() ) {
        //The shell may be disposed because the user hit the cancel button.  Set the ovewrite mode
        // to cancel on the next file.
        applyToAll = true;
        overwriteMode = OverwriteMode.CANCEL;
      }
    }
  }

  public void dispose() {
    if ( fileCopyProgressDialog != null ) {
      fileCopyProgressDialog.dispose();
      fileCopyProgressDialog = null;
    }

  }

  public void reset() {
    if ( !applyToAll ) {
      overwriteMode = OverwriteMode.NONE;
    }
  }

  private static void copyData( OverwriteStatus source, OverwriteStatus destination ) {
    //Don't ever send back a new object.  Always modify the input
    destination.setOverwriteMode( source.getOverwriteMode() );
    destination.setApplyToAll( source.isApplyToAll() );
  }

  public enum OverwriteMode {
    NONE, SKIP, OVERWRITE, RENAME, CANCEL
  }
}
