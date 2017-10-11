/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;

public class ResourceUtil {

  private static Class<?> PKG = ResourceUtil.class; // for i18n purposes, needed by Translator2!!

  /**
   * Serializes the referenced resource export interface (Job, Transformation, Mapping, Step, Job Entry, etc) to a ZIP
   * file.
   *
   * @param zipFilename
   *          The ZIP file to put the content in
   * @param resourceExportInterface
   *          the interface to serialize
   * @param space
   *          the space to use for variable replacement
   * @param repository
   *          the repository to load objects from (or null if not used)
   * @param metaStore
   *          the metaStore to load from
   * @return The full VFS filename reference to the serialized export interface XML file in the ZIP archive.
   * @throws KettleException
   *           in case anything goes wrong during serialization
   */
  public static final TopLevelResource serializeResourceExportInterface( String zipFilename,
    ResourceExportInterface resourceExportInterface, VariableSpace space, Repository repository,
    IMetaStore metaStore ) throws KettleException {
    return serializeResourceExportInterface(
      zipFilename, resourceExportInterface, space, repository, metaStore, null, null );
  }

  /**
   * Serializes the referenced resource export interface (Job, Transformation, Mapping, Step, Job Entry, etc) to a ZIP
   * file.
   *
   * @param zipFilename
   *          The ZIP file to put the content in
   * @param resourceExportInterface
   *          the interface to serialize
   * @param space
   *          the space to use for variable replacement
   * @param repository
   *          the repository to load objects from (or null if not used)
   * @param injectXML
   *          The XML to inject into the resulting ZIP archive (optional, can be null)
   * @param injectFilename
   *          The name of the file for the XML to inject in the ZIP archive (optional, can be null)
   * @return The full VFS filename reference to the serialized export interface XML file in the ZIP archive.
   * @throws KettleException
   *           in case anything goes wrong during serialization
   */
  public static final TopLevelResource serializeResourceExportInterface( String zipFilename,
    ResourceExportInterface resourceExportInterface, VariableSpace space, Repository repository,
    IMetaStore metaStore, String injectXML, String injectFilename ) throws KettleException {

    ZipOutputStream out = null;

    try {
      Map<String, ResourceDefinition> definitions = new HashMap<String, ResourceDefinition>();

      // In case we want to add an extra pay-load to the exported ZIP file...
      //
      if ( injectXML != null ) {
        ResourceDefinition resourceDefinition = new ResourceDefinition( injectFilename, injectXML );
        definitions.put( injectFilename, resourceDefinition );
      }

      ResourceNamingInterface namingInterface = new SequenceResourceNaming();

      String topLevelResource =
        resourceExportInterface.exportResources( space, definitions, namingInterface, repository, metaStore );

      if ( topLevelResource != null && !definitions.isEmpty() ) {

        // Create the ZIP file...
        //
        FileObject fileObject = KettleVFS.getFileObject( zipFilename, space );

        // Store the XML in the definitions in a ZIP file...
        //
        out = new ZipOutputStream( KettleVFS.getOutputStream( fileObject, false ) );

        for ( String filename : definitions.keySet() ) {
          ResourceDefinition resourceDefinition = definitions.get( filename );

          ZipEntry zipEntry = new ZipEntry( resourceDefinition.getFilename() );

          String comment =
            BaseMessages.getString(
              PKG, "ResourceUtil.SerializeResourceExportInterface.ZipEntryComment.OriginatingFile", filename,
              Const.NVL( resourceDefinition.getOrigin(), "-" ) );
          zipEntry.setComment( comment );
          out.putNextEntry( zipEntry );

          out.write( resourceDefinition.getContent().getBytes() );
          out.closeEntry();
        }
        String zipURL = fileObject.getName().toString();
        return new TopLevelResource( topLevelResource, zipURL, "zip:" + zipURL + "!" + topLevelResource );
      } else {
        throw new KettleException( BaseMessages.getString( PKG, "ResourceUtil.Exception.NoResourcesFoundToExport" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ResourceUtil.Exception.ErrorSerializingExportInterface", resourceExportInterface.toString() ), e );
    } finally {
      if ( out != null ) {
        try {
          out.close();
        } catch ( IOException e ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "ResourceUtil.Exception.ErrorClosingZipStream", zipFilename ) );
        }
      }
    }
  }

  public static String getExplanation( String zipFilename, String launchFile,
    ResourceExportInterface resourceExportInterface ) {

    String commandString = "";
    if ( Const.isWindows() ) {
      if ( resourceExportInterface instanceof TransMeta ) {
        commandString += "Pan.bat /file:\"";
      } else {
        commandString += "Kitchen.bat /file:\"";
      }
    } else {
      if ( resourceExportInterface instanceof TransMeta ) {
        commandString += "sh pan.sh -file='";
      } else {
        commandString += "sh kitchen.sh -file='";
      }
    }
    commandString += launchFile;
    if ( Const.isWindows() ) {
      commandString += "\"";
    } else {
      commandString += "'";
    }

    String message =
      BaseMessages.getString(
        PKG, "ResourceUtil.ExportResourcesExplanation", zipFilename, commandString, launchFile, Const.CR );
    return message;
  }
}
