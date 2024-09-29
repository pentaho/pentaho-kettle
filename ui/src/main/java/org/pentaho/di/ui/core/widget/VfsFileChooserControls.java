/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.core.widget;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.UriParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.vfs.ui.CustomVfsUiPanel;
import org.pentaho.vfs.ui.VfsFileChooserDialog;
import org.pentaho.vfs.util.VFSScheme;

import java.util.ArrayList;
import java.util.List;

public class VfsFileChooserControls extends Composite {
  private static final Class<?> PKG = VfsFileChooserControls.class;
  protected static final String[] FILES_FILTERS = { "*.*" };
  protected static final String[] fileFilterNames = new String[] { BaseMessages.getString( "System.FileType.AllFiles" ) };
  private static final String DEFAULT_LOCAL_PATH = "file:///C:/";
  private static final String HDFS_SCHEME = "hdfs";
  public static final int MARGIN = 15;
  public static final int FIELDS_SEP = 10;
  public static final int FIELD_LABEL_SEP = 5;
  public static final int FIELD_SMALL = 150;
  public static final int FIELD_LARGE = 350;
  protected static final int VAR_EXTRA_WIDTH = GUIResource.getInstance().getImageVariable().getBounds().width;

  public TextVar wPath;
  public VariableSpace space;
  protected VFSScheme selectedVFSScheme;
  public CCombo wLocation;
  protected LogChannel log;
  public Button wbBrowse;
  private ModifyListener lsMod;

  public VfsFileChooserControls( VariableSpace space, Composite composite, int i, ModifyListener lsMod ) {
    super( composite, i );
    this.space = space;
    this.lsMod = lsMod;
    FormLayout noMarginLayout = new FormLayout();
    noMarginLayout.marginWidth = 0;
    noMarginLayout.marginHeight = 0;
    noMarginLayout.marginTop = 0;
    noMarginLayout.marginBottom = 0;
    this.setLayout( noMarginLayout );
    addFileWidgets();
  }

  protected FileObject getInitialFile( String filePath ) throws KettleFileException {
    FileObject initialFile = null;
    if ( filePath != null && !filePath.isEmpty() ) {
      String fileName = space.environmentSubstitute( filePath );
      if ( fileName != null && !fileName.isEmpty() ) {
        initialFile = KettleVFS.getFileObject( fileName );
      }
    }
    if ( initialFile == null ) {
      initialFile = KettleVFS.getFileObject( Spoon.getInstance().getLastFileOpened() );
    }
    return initialFile;
  }

  protected void addFileWidgets() {
    Label wlLocation = new Label( this, SWT.RIGHT );
    wlLocation.setText( BaseMessages.getString( PKG, "VfsFileChooserControls.Location.Label" ) );
    wlLocation.setLayoutData( new FormDataBuilder( ).left( 0, 0 ).top( 0, 0 ).result() );
    wLocation = new CCombo( this, SWT.BORDER | SWT.READ_ONLY );
    List<VFSScheme> availableVFSSchemes = getAvailableVFSSchemes();
    availableVFSSchemes.forEach( scheme -> wLocation.add( scheme.schemeName ) );
    wLocation.addListener( SWT.Selection, event -> {
      this.selectedVFSScheme = availableVFSSchemes.get( wLocation.getSelectionIndex() );
      this.wPath.setText( "" );
    } );
    if ( !availableVFSSchemes.isEmpty() ) {
      wLocation.select( 0 );
      this.selectedVFSScheme = availableVFSSchemes.get( wLocation.getSelectionIndex() );
    }
    wLocation.addModifyListener( lsMod );
    wLocation.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( wlLocation, FIELD_LABEL_SEP ).width( FIELD_SMALL ).result() );

    Label wlPath = new Label( this, SWT.RIGHT );
    wlPath.setText( BaseMessages.getString( PKG, "VfsFileChooserControls.Filename.Label" ) );
    wlPath.setLayoutData( new FormDataBuilder().left( 0, 0 ).top( wLocation, FIELDS_SEP ).result() );
    wPath = new TextVar( space, this, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wPath.addModifyListener( lsMod );
    wPath.setLayoutData( new FormDataBuilder().left( 0, 0 ).top( wlPath, FIELD_LABEL_SEP ).width( FIELD_LARGE + VAR_EXTRA_WIDTH ).result() );

    wbBrowse = new Button( this, SWT.PUSH );
    wbBrowse.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbBrowse.addListener( SWT.Selection, event -> browseForFileInputPath() );
    int bOffset = ( wbBrowse.computeSize( SWT.DEFAULT, SWT.DEFAULT, false ).y
      - wPath.computeSize( SWT.DEFAULT, SWT.DEFAULT, false ).y ) / 2;
    wbBrowse.setLayoutData( new FormDataBuilder().left( wPath, FIELD_LABEL_SEP ).top( wlPath, FIELD_LABEL_SEP - bOffset ).result() );
  }

  protected void browseForFileInputPath() {
    try {
      String path = space.environmentSubstitute( wPath.getText() );
      VfsFileChooserDialog fileChooserDialog;
      String fileName;
      if ( path == null || path.length() == 0 ) {
        fileChooserDialog = getVfsFileChooserDialog( null, null );
        fileName = selectedVFSScheme.scheme + "://";
      } else {
        FileObject initialFile = getInitialFile( wPath.getText() );
        FileObject rootFile = initialFile.getFileSystem().getRoot();
        fileChooserDialog = getVfsFileChooserDialog( rootFile, initialFile );
        fileName = null;
      }

      FileObject selectedFile =
        fileChooserDialog.open( getParent().getShell(), null, selectedVFSScheme.scheme, true, fileName, FILES_FILTERS,
          fileFilterNames, true, VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE_OR_DIRECTORY, true, true );
      if ( selectedFile != null ) {
        String filePath = selectedFile.getURL().toString();
        if ( !DEFAULT_LOCAL_PATH.equals( filePath ) ) {
          wPath.setText( filePath );
          updateLocation();
        }
      }
    } catch ( KettleFileException | FileSystemException ex ) {
      log.logError( ex.getMessage() );
    }
  }

  protected List<VFSScheme> getAvailableVFSSchemes() {
    VfsFileChooserDialog fileChooserDialog = getVfsFileChooserDialog( null, null );
    List<CustomVfsUiPanel> customVfsUiPanels = fileChooserDialog.getCustomVfsUiPanels();
    List<VFSScheme> vfsSchemes = new ArrayList<>();
    customVfsUiPanels.forEach( vfsPanel -> {
      VFSScheme scheme = new VFSScheme( vfsPanel.getVfsScheme(), vfsPanel.getVfsSchemeDisplayText() );
      vfsSchemes.add( scheme );
    } );
    return vfsSchemes;
  }

  private void updateLocation() {
    String pathText = wPath.getText();
    String scheme = pathText.isEmpty() ? HDFS_SCHEME : UriParser.extractScheme( pathText );
    if ( scheme != null ) {
      List<VFSScheme> availableVFSSchemes = getAvailableVFSSchemes();
      for ( int i = 0; i < availableVFSSchemes.size(); i++ ) {
        VFSScheme s = availableVFSSchemes.get( i );
        if ( scheme.equals( s.scheme ) ) {
          wLocation.select( i );
          selectedVFSScheme = s;
        }
      }
    }
  }

  protected VfsFileChooserDialog getVfsFileChooserDialog( FileObject rootFile, FileObject initialFile ) {
    return getSpoon().getVfsFileChooserDialog( rootFile, initialFile );
  }

  private Spoon getSpoon() {
    return Spoon.getInstance();
  }
}
