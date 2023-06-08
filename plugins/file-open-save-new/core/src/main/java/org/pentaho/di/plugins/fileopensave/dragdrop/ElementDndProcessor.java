/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.dragdrop;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.Result;
import org.pentaho.di.plugins.fileopensave.controllers.FileController;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.service.FileCacheService;
import org.pentaho.di.plugins.fileopensave.service.ProviderServiceService;

import java.util.Arrays;
import java.util.Objects;

public class ElementDndProcessor {
  private static final FileController
    FILE_CONTROLLER = new FileController( FileCacheService.INSTANCE.get(), ProviderServiceService.get() );
  private static final EntityType[] DESTINATION_BLACKLIST =
    new EntityType[] { EntityType.TREE, EntityType.RECENT_FILE, EntityType.REPOSITORY_OBJECT, EntityType.LOCAL_FILE,
      EntityType.NAMED_CLUSTER_FILE, EntityType.REPOSITORY_FILE, EntityType.TEST_FILE };
  public static boolean isLive = true;  //Change to false to test DnD logic without actually copying the files

  /**
   * Do some copy/move/delete operation on the source.  If the operation is copy or move than a destination is required.
   * If the operation is delete, only the source is used and the destination is not processed.
   *
   * @parem shell            The shell of the calling window.  Leave null if headless
   * @param source           an array of Elements that are to be moved/copied/deleted
   * @param destination      A single element to serve as the destination.  Must be a Directory of some type
   * @param elementOperation Copy or Move
   * @param variables        Variable space for substitution
   * @param overwriteStatus  Overwrite state to work under. No prompts will be made if overwriteStatus.isApplyToAll==true
   * @param log              The log channel for logging
   */
  public static void process( Element[] source, Element destination, ElementOperation elementOperation,
                              VariableSpace variables, OverwriteStatus overwriteStatus, LogChannelInterface log )
    throws KettleException {

    if ( elementOperation == ElementOperation.COPY || elementOperation == ElementOperation.MOVE ) {
      File destinationFolder = determineDestinationFolder( destination, variables );
      File[] sourceObjects = determineSourceObjects( source, variables );
      checkIfDestinationIsChildOfSource( sourceObjects, destinationFolder );

      //perform copy
      for ( File sourceObject : sourceObjects ) {
        String separator = destinationFolder.getEntityType().isLocalType() ? java.io.File.separator : "/";
        String toPath = destinationFolder.getPath() + separator + sourceObject.getName();
        log.logBasic( ( isLive ? "" : "NOT " ) +
          "Copying \"" + sourceObject.getPath() + "\" to \"" + destinationFolder.getPath() + "\" to create \""
          + toPath );
        if ( isLive ) {
          try {
            overwriteStatus.activateProgressDialog( "Copy File Progress" );
            Result result = FILE_CONTROLLER.copyFile( sourceObject, destinationFolder,
              toPath, overwriteStatus );
            if ( !result.getStatus().equals( Result.Status.SUCCESS ) ) {
              String onObject = result.getData() instanceof File ? ( (File) result.getData() ).getPath() : "";
              throw new KettleException( result.getStatus() + ": " + result.getMessage() + onObject );
            }
          } finally {
            overwriteStatus.dispose();
          }
        }
      }
    }
    if ( elementOperation == ElementOperation.MOVE || elementOperation == ElementOperation.DELETE ) {
      //perform delete here if we decide to implement move
    }
  }

  private static File determineDestinationFolder( Element destination, VariableSpace variableSpace ) {
    if ( checkArray( destination.getEntityType(), DESTINATION_BLACKLIST ) ) {
      processErrorMessage( "Invalid destination.  The destination cannot be a " + destination.getEntityType() );
    }
    return destination.convertToFile( variableSpace );
  }

  private static File[] determineSourceObjects( Element[] source, VariableSpace variables ) {
    return Arrays.stream( source ).map( x -> x.convertToFile( variables ) ).filter( Objects::nonNull )
      .toArray( File[]::new );
  }

  private static boolean checkArray( EntityType entityType, EntityType[] array ) {
    return Arrays.stream( array ).filter( x -> entityType.equals( x ) ).findFirst().isPresent();
  }

  private static void processErrorMessage( String errorMessage ) {
    throw new IllegalArgumentException( errorMessage );
  }

  private static void checkIfDestinationIsChildOfSource( File[] sourceObjects, File destinationFolder ) {
    if ( !destinationFolder.getEntityType().isDirectory() ) {
      processErrorMessage( "Destination \"" + destinationFolder.getPath() + "\" must be a folder." );
    }
    for ( File source : sourceObjects ) {
      if ( destinationFolder.getPath().indexOf( source.getPath() ) == 0 ) {
        processErrorMessage(
          "Destination folder \"" + destinationFolder.getPath() + "\" cannot be a sub-folder of source \""
            + source.getPath() + "\".  This can cause infinite looping." );
      }
    }
  }

  public enum ElementOperation {
    COPY( 0 ),
    MOVE( 1 ),
    DELETE( 2 );

    private int val;

    ElementOperation( int val ) {
      this.val = val;
    }

  }


}
