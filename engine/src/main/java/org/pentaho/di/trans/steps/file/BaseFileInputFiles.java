/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * Input files settings.
 */
public class BaseFileInputFiles implements Cloneable {
  private static Class<?> PKG = BaseFileInputFiles.class; // for i18n purposes, needed by Translator2!!

  public static final String NO = "N";

  public static final String YES = "Y";

  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };
  public static final String[] RequiredFilesDesc =
      new String[] { BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG,
          "System.Combo.Yes" ) };

  /** Array of filenames */
  @Injection( name = "FILENAME", group = "FILENAME_LINES" )
  public String[] fileName = {};

  /** Wildcard or filemask (regular expression) */
  @Injection( name = "FILEMASK", group = "FILENAME_LINES" )
  public String[] fileMask = {};

  /** Wildcard or filemask to exclude (regular expression) */
  @Injection( name = "EXCLUDE_FILEMASK", group = "FILENAME_LINES" )
  public String[] excludeFileMask = {};

  /** Array of boolean values as string, indicating if a file is required. */
  @Injection( name = "FILE_REQUIRED", group = "FILENAME_LINES" )
  public String[] fileRequired = {};

  /** Array of boolean values as string, indicating if we need to fetch sub folders. */
  @Injection( name = "INCLUDE_SUBFOLDERS", group = "FILENAME_LINES" )
  public String[] includeSubFolders = {};

  /** Are we accepting filenames in input rows? */
  @Injection( name = "ACCEPT_FILE_NAMES" )
  public boolean acceptingFilenames;

  /** The stepname to accept filenames from */
  @Injection( name = "ACCEPT_FILE_STEP" )
  public String acceptingStepName;

  /** If receiving input rows, should we pass through existing fields? */
  @Injection( name = "PASS_THROUGH_FIELDS" )
  public boolean passingThruFields;

  /** The field in which the filename is placed */
  @Injection( name = "ACCEPT_FILE_FIELD" )
  public String acceptingField;

  /** The add filenames to result filenames flag */
  @Injection( name = "ADD_FILES_TO_RESULT" )
  public boolean isaddresult;

  @Override
  public Object clone() {
    try {
      BaseFileInputFiles cloned = (BaseFileInputFiles) super.clone();
      cloned.fileName = Arrays.copyOf( fileName, fileName.length );
      cloned.fileMask = Arrays.copyOf( fileMask, fileMask.length );
      cloned.excludeFileMask = Arrays.copyOf( excludeFileMask, excludeFileMask.length );
      cloned.fileRequired = Arrays.copyOf( fileRequired, fileRequired.length );
      cloned.includeSubFolders = Arrays.copyOf( includeSubFolders, includeSubFolders.length );
      return cloned;
    } catch ( CloneNotSupportedException ex ) {
      throw new IllegalArgumentException( "Clone not supported for " + this.getClass().getName() );
    }
  }

  public void setFileRequired( String[] fileRequiredin ) {
    for ( int i = 0; i < fileRequiredin.length; i++ ) {
      this.fileRequired[i] = getRequiredFilesCode( fileRequiredin[i] );
    }
  }

  public void setIncludeSubFolders( String[] includeSubFoldersin ) {
    for ( int i = 0; i < includeSubFoldersin.length; i++ ) {
      this.includeSubFolders[i] = getRequiredFilesCode( includeSubFoldersin[i] );
    }
  }

  public static String getRequiredFilesCode( String tt ) {
    if ( tt == null ) {
      return RequiredFilesCode[0];
    }
    if ( tt.equals( RequiredFilesDesc[1] ) ) {
      return RequiredFilesCode[1];
    } else {
      return RequiredFilesCode[0];
    }
  }

  public void normalizeAllocation( int length ) {
    fileMask = normalizeAllocation( fileMask, length );
    excludeFileMask = normalizeAllocation( excludeFileMask, length );
    fileRequired = normalizeAllocation( fileRequired, length );
    includeSubFolders = normalizeAllocation( includeSubFolders, length );
  }

  protected static String[] normalizeAllocation( String[] oldAllocation, int length ) {
    String[] newAllocation = null;
    if ( oldAllocation.length < length ) {
      newAllocation = new String[length];
      for ( int i = 0; i < oldAllocation.length; i++ ) {
        newAllocation[i] = oldAllocation[i];
      }
    } else {
      newAllocation = oldAllocation;
    }
    return newAllocation;
  }

  public boolean[] includeSubFolderBoolean() {
    int len = fileName.length;
    boolean[] includeSubFolderBoolean = new boolean[len];
    for ( int i = 0; i < len; i++ ) {
      includeSubFolderBoolean[i] = YES.equalsIgnoreCase( includeSubFolders[i] );
    }
    return includeSubFolderBoolean;
  }

  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    List<ResourceReference> references = new ArrayList<ResourceReference>( 5 );
    ResourceReference reference = new ResourceReference( stepInfo );
    references.add( reference );

    String[] textFiles =
        FileInputList.createFilePathList( transMeta.getBowl(), transMeta, fileName, fileMask,
            excludeFileMask, fileRequired, includeSubFolderBoolean() );
    if ( textFiles != null ) {
      for ( int i = 0; i < textFiles.length; i++ ) {
        reference.getEntries().add( new ResourceEntry( textFiles[i], ResourceType.FILE ) );
      }
    }
    return references;
  }
}
