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
