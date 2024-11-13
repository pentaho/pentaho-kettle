/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.metainject;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.StepMeta;

@ExtensionPoint(
    id = "RepositoryImporterPatchTransStep",
    description = "Patch the step in a transformation during repository import",
    extensionPointId = "RepositoryImporterPatchTransStep" )

public class RepositoryImporterExtension implements ExtensionPointInterface {

  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {

    Object[] metaInjectObjectArray = (Object[]) object;
    String transDirOverride = (String) metaInjectObjectArray[0];
    RepositoryDirectoryInterface baseDirectory = (RepositoryDirectoryInterface) metaInjectObjectArray[1];
    StepMeta stepMeta = (StepMeta) metaInjectObjectArray[2];
    boolean needToCheckPathForVariables = (boolean) metaInjectObjectArray[3];

    if ( stepMeta.isEtlMetaInject() ) {
      MetaInjectMeta metaInjectMeta = (MetaInjectMeta) stepMeta.getStepMetaInterface();
      if ( metaInjectMeta.getSpecificationMethod() == ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME ) {
        if ( transDirOverride != null ) {
          metaInjectMeta.setDirectoryPath( transDirOverride );
        } else {
          String mappingMetaPath = resolvePath( baseDirectory.getPath(), metaInjectMeta.getDirectoryPath(), needToCheckPathForVariables );
          metaInjectMeta.setDirectoryPath( mappingMetaPath );
        }
      }
    }
  }

  String resolvePath( String rootPath, String entryPath, boolean check ) {
    boolean needToCheckPathForVariables = check;
    String extraPath = Const.NVL( entryPath, "/" );
    if ( needToCheckPathForVariables ) {
      if ( containsVariables( entryPath ) ) {
        return extraPath;
      }
    }
    String newPath = Const.NVL( rootPath, "/" );
    if ( newPath.endsWith( "/" ) && extraPath.startsWith( "/" ) ) {
      newPath = newPath.substring( 0, newPath.length() - 1 );
    } else if ( !newPath.endsWith( "/" ) && !extraPath.startsWith( "/" ) ) {
      newPath += "/";
    } else if ( extraPath.equals( "/" ) ) {
      extraPath = "";
    }
    return newPath + extraPath;
  }

  private static boolean containsVariables( String entryPath ) {
    List<String> variablesList = new ArrayList<String>();
    StringUtil.getUsedVariables( entryPath, variablesList, true );
    return !variablesList.isEmpty();
  }
}
