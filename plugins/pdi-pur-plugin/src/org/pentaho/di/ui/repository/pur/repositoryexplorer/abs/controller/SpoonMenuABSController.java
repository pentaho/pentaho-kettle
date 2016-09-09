/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.pur.PurRepository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.repository.EESpoonPlugin;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider;
import org.pentaho.di.ui.spoon.ISpoonMenuController;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulMenu;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.dom.Document;

public class SpoonMenuABSController implements ISpoonMenuController, java.io.Serializable {

  private static final long serialVersionUID = -5878581743406400314L; /* EESOURCE: UPDATE SERIALVERUID */

  protected LogChannelInterface log;
  protected LogLevel logLevel = DefaultLogLevel.getLogLevel();

  public SpoonMenuABSController() {
    this.log = new LogChannel( this );
  }

  public String getName() {
    return "SpoonMenuABSController"; //$NON-NLS-1$
  }

  public void updateMenu( Document doc ) {
    try {
      Spoon spoon = Spoon.getInstance();
      boolean createPermitted = true;
      boolean executePermitted = true;

      // If we are working with an Enterprise Repository
      if ( ( spoon != null ) && ( spoon.getRepository() != null ) && ( spoon.getRepository() instanceof PurRepository ) ) {
        Repository repo = spoon.getRepository();

        // Check for ABS Security
        if ( repo.hasService( IAbsSecurityProvider.class ) ) {
          IAbsSecurityProvider securityProvider = (IAbsSecurityProvider) repo.getService( IAbsSecurityProvider.class );

          // Get create & execute permission
          createPermitted = securityProvider.isAllowed( IAbsSecurityProvider.CREATE_CONTENT_ACTION );
          executePermitted = securityProvider.isAllowed( IAbsSecurityProvider.EXECUTE_CONTENT_ACTION );

          EngineMetaInterface meta = spoon.getActiveMeta();

          // If (meta is not null) and (meta is either a Transformation or Job)
          if ( ( meta != null ) && ( ( meta instanceof JobMeta ) || ( meta instanceof TransMeta ) ) ) {

            // Main spoon toolbar
            ( (XulToolbarbutton) doc.getElementById( "toolbar-file-new" ) ).setDisabled( !createPermitted ); //$NON-NLS-1$
            ( (XulToolbarbutton) doc.getElementById( "toolbar-file-save" ) ).setDisabled( !createPermitted ); //$NON-NLS-1$
            ( (XulToolbarbutton) doc.getElementById( "toolbar-file-save-as" ) ).setDisabled( !createPermitted ); //$NON-NLS-1$

            // Popup menus
            ( (XulMenuitem) doc.getElementById( "trans-class-new" ) ).setDisabled( !createPermitted ); //$NON-NLS-1$
            ( (XulMenuitem) doc.getElementById( "job-class-new" ) ).setDisabled( !createPermitted ); //$NON-NLS-1$

            // Main spoon menu
            ( (XulMenu) doc.getElementById( "file-new" ) ).setDisabled( !createPermitted ); //$NON-NLS-1$
            ( (XulMenuitem) doc.getElementById( "file-save" ) ).setDisabled( !createPermitted ); //$NON-NLS-1$
            ( (XulMenuitem) doc.getElementById( "file-save-as" ) ).setDisabled( !createPermitted ); //$NON-NLS-1$
            ( (XulMenuitem) doc.getElementById( "file-close" ) ).setDisabled( !createPermitted ); //$NON-NLS-1$
          }

          // Handle Execute permissions
          ( (XulMenuitem) doc.getElementById( "process-run" ) ).setDisabled( !executePermitted ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "trans-preview" ) ).setDisabled( !executePermitted ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "trans-debug" ) ).setDisabled( !executePermitted ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "trans-replay" ) ).setDisabled( !executePermitted ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "trans-verify" ) ).setDisabled( !executePermitted ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "trans-impact" ) ).setDisabled( !executePermitted ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "trans-get-sql" ) ).setDisabled( !executePermitted ); //$NON-NLS-1$

          // Disable Show Last menu under the Action menu. Disable without execute permissions.
          ( (XulMenu) doc.getElementById( "trans-last" ) ).setDisabled( !executePermitted ); //$NON-NLS-1$

          boolean exportAllowed = createPermitted && executePermitted;
          ( (XulMenu) doc.getElementById( "file-export" ) ).setDisabled( !exportAllowed ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "repository-export-all" ) ).setDisabled( !exportAllowed ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "file-save-as-vfs" ) ).setDisabled( !exportAllowed ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "edit-cut-steps" ) ).setDisabled( !exportAllowed ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "edit-copy-steps" ) ).setDisabled( !exportAllowed ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "edit.copy-file" ) ).setDisabled( !exportAllowed ); //$NON-NLS-1$
          ( (XulMenuitem) doc.getElementById( "edit-paste-steps" ) ).setDisabled( !exportAllowed ); //$NON-NLS-1$

          // Schedule is a plugin
          if ( doc.getElementById( "trans-schedule" ) != null ) {
            ( (XulMenuitem) doc.getElementById( "trans-schedule" ) ).setDisabled( !executePermitted ); //$NON-NLS-1$
          }

          TransGraph transGraph = Spoon.getInstance().getActiveTransGraph();
          if ( transGraph != null ) {
            XulToolbar toolbar = transGraph.getToolbar();
            XulToolbarbutton runButton = (XulToolbarbutton) toolbar.getElementById( "trans-run" );
            XulToolbarbutton debugButton = (XulToolbarbutton) toolbar.getElementById( "trans-debug" );
            XulToolbarbutton previewButton = (XulToolbarbutton) toolbar.getElementById( "trans-preview" );
            XulToolbarbutton replayButton = (XulToolbarbutton) toolbar.getElementById( "trans-replay" );
            XulToolbarbutton verifyButton = (XulToolbarbutton) toolbar.getElementById( "trans-verify" );
            XulToolbarbutton impactButton = (XulToolbarbutton) toolbar.getElementById( "trans-impact" );
            XulToolbarbutton generateSqlButton = (XulToolbarbutton) toolbar.getElementById( "trans-get-sql" );

            if ( ( runButton != null ) && ( runButton.isDisabled() ^ !executePermitted ) ) {
              runButton.setDisabled( !executePermitted );
            }

            if ( ( debugButton != null ) && ( debugButton.isDisabled() ^ !executePermitted ) ) {
              debugButton.setDisabled( !executePermitted );
            }

            if ( ( previewButton != null ) && ( previewButton.isDisabled() ^ !executePermitted ) ) {
              previewButton.setDisabled( !executePermitted );
            }

            if ( ( replayButton != null ) && ( replayButton.isDisabled() ^ !executePermitted ) ) {
              replayButton.setDisabled( !executePermitted );
            }

            if ( ( verifyButton != null ) && ( verifyButton.isDisabled() ^ !executePermitted ) ) {
              verifyButton.setDisabled( !executePermitted );
            }

            if ( ( impactButton != null ) && ( impactButton.isDisabled() ^ !executePermitted ) ) {
              impactButton.setDisabled( !executePermitted );
            }

            if ( ( generateSqlButton != null ) && ( generateSqlButton.isDisabled() ^ !executePermitted ) ) {
              generateSqlButton.setDisabled( !executePermitted );
            }
          }

          JobGraph jobGraph = Spoon.getInstance().getActiveJobGraph();
          if ( jobGraph != null ) {
            XulToolbar toolbar = jobGraph.getToolbar();
            XulToolbarbutton runButton = (XulToolbarbutton) toolbar.getElementById( "job-run" );
            XulToolbarbutton generateSqlButton = (XulToolbarbutton) toolbar.getElementById( "job-get-sql" );

            if ( ( runButton != null ) && ( runButton.isDisabled() ^ !executePermitted ) ) {
              runButton.setDisabled( !executePermitted );
            }

            if ( ( generateSqlButton != null ) && ( generateSqlButton.isDisabled() ^ !executePermitted ) ) {
              generateSqlButton.setDisabled( !executePermitted );
            }
          }
        }
      }

      EESpoonPlugin.updateChangedWarningDialog( createPermitted );

    } catch ( Exception e ) {
      // don't let this bomb all the way out, otherwise we'll get stuck: PDI-4670
      log.logError( e.getMessage(), e );
    }
  }

}
