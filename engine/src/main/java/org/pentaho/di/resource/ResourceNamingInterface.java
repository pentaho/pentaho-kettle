/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.resource;

import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.variables.VariableSpace;

public interface ResourceNamingInterface {

  public static enum FileNamingType {
    TRANSFORMATION, JOB, DATA_FILE, SHELL_SCRIPT,
  }

  /**
   * Create a (file) name for a resource based on a prefix and an extension.
   *
   * @param prefix
   *          The prefix, usually the name of the object that is being exported
   * @param originalFilePath
   *          The original path to the file. This will be used in the naming of the resource to ensure that the same
   *          GUID will be returned for the same file.
   * @param extension
   *          The extension of the filename to be created. For now this also gives a clue as to what kind of data is
   *          being exported and named..
   * @param namingType
   *          the file naming type to use, in case of DATA_FILE for example, the return value might not be the complete
   *          file, but rather
   * @return The filename, typically including a GUID, but always the same when given the same prefix and extension as
   *         input.
   */
  public String nameResource( String prefix, String originalFilePath, String extension, FileNamingType namingType );

  /**
   * Create a (file) name based on the passed FileObject
   *
   * @param FileObject
   *          fileObject The file in which the name ....
   * @param VariableSpace
   *          variable(space) of the transformation or job.
   * @param pathOnly
   *          Set to true to just return the path, false to return file name and path
   * @return String The file name with the path set as a variable. If pathOnly is set to true then the file name will be
   *         left out.
   */
  public String nameResource( FileObject fileObject, VariableSpace space, boolean pathOnly ) throws FileSystemException;

  /**
   * @return the map of folders mapped to created parameters during the resource naming.
   */
  public Map<String, String> getDirectoryMap();
}
