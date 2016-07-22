/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.fileinput;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * Base meta for file-based input steps.
 *
 * @author Alexander Buloichik
 */
public abstract class BaseFileInputStepMeta<A extends BaseFileInputStepMeta.AdditionalOutputFields, I extends BaseFileInputStepMeta.InputFiles<? extends BaseFileInputField>>
    extends BaseStepMeta {
  private static Class<?> PKG = BaseFileInputStepMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };

  public static final String NO = "N";

  public static final String YES = "Y";

  public static final String[] RequiredFilesDesc =
      new String[] { BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG,
          "System.Combo.Yes" ) };

  @InjectionDeep
  public I inputFiles;
  @InjectionDeep
  public ErrorHandling errorHandling = new ErrorHandling();
  @InjectionDeep
  public A additionalOutputFields;

  /**
   * Input files settings.
   */
  public static class InputFiles<F extends BaseFileInputField> implements Cloneable {

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

    /** The fields to import... */
    @InjectionDeep
    public F[] inputFields;

    /** The add filenames to result filenames flag */
    @Injection( name = "ADD_FILES_TO_RESULT" )
    public boolean isaddresult;

    public Object clone() {
      try {
        return super.clone();
      } catch ( CloneNotSupportedException e ) {
        return null;
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
  }

  /**
   * Error handling settings.
   */
  public static class ErrorHandling implements Cloneable {

    /** Ignore error : turn into warnings */
    @Injection( name = "IGNORE_ERRORS" )
    public boolean errorIgnored;

    /** File error field name. */
    @Injection( name = "FILE_ERROR_FIELD" )
    public String fileErrorField;

    /** File error text field name. */
    @Injection( name = "FILE_ERROR_MESSAGE_FIELD" )
    public String fileErrorMessageField;

    @Injection( name = "SKIP_BAD_FILES" )
    public boolean skipBadFiles;

    /** The directory that will contain warning files */
    @Injection( name = "WARNING_FILES_TARGET_DIR" )
    public String warningFilesDestinationDirectory;

    /** The extension of warning files */
    @Injection( name = "WARNING_FILES_EXTENTION" )
    public String warningFilesExtension;

    /** The directory that will contain error files */
    @Injection( name = "ERROR_FILES_TARGET_DIR" )
    public String errorFilesDestinationDirectory;

    /** The extension of error files */
    @Injection( name = "ERROR_FILES_EXTENTION" )
    public String errorFilesExtension;

    /** The directory that will contain line number files */
    @Injection( name = "LINE_NR_FILES_TARGET_DIR" )
    public String lineNumberFilesDestinationDirectory;

    /** The extension of line number files */
    @Injection( name = "LINE_NR_FILES_EXTENTION" )
    public String lineNumberFilesExtension;

    public Object clone() {
      try {
        return super.clone();
      } catch ( CloneNotSupportedException e ) {
        return null;
      }
    }
  }

  /**
   * Additional fields settings.
   */
  public static class AdditionalOutputFields implements Cloneable {

    /** Additional fields **/
    @Injection( name = "FILE_SHORT_FILE_FIELDNAME" )
    public String shortFilenameField;
    @Injection( name = "FILE_EXTENSION_FIELDNAME" )
    public String extensionField;
    @Injection( name = "FILE_PATH_FIELDNAME" )
    public String pathField;
    @Injection( name = "FILE_SIZE_FIELDNAME" )
    public String sizeField;
    @Injection( name = "FILE_HIDDEN_FIELDNAME" )
    public String hiddenField;
    @Injection( name = "FILE_LAST_MODIFICATION_FIELDNAME" )
    public String lastModificationField;
    @Injection( name = "FILE_URI_FIELDNAME" )
    public String uriField;
    @Injection( name = "FILE_ROOT_URI_FIELDNAME" )
    public String rootUriField;

    public Object clone() {
      try {
        return super.clone();
      } catch ( CloneNotSupportedException e ) {
        return null;
      }
    }

    /**
     * Set null for all empty field values to be able to fast check during step processing. Need to be executed once
     * before processing.
     */
    public void normalize() {
      if ( StringUtils.isBlank( shortFilenameField ) ) {
        shortFilenameField = null;
      }
      if ( StringUtils.isBlank( extensionField ) ) {
        extensionField = null;
      }
      if ( StringUtils.isBlank( pathField ) ) {
        pathField = null;
      }
      if ( StringUtils.isBlank( sizeField ) ) {
        sizeField = null;
      }
      if ( StringUtils.isBlank( hiddenField ) ) {
        hiddenField = null;
      }
      if ( StringUtils.isBlank( lastModificationField ) ) {
        lastModificationField = null;
      }
      if ( StringUtils.isBlank( uriField ) ) {
        uriField = null;
      }
      if ( StringUtils.isBlank( rootUriField ) ) {
        rootUriField = null;
      }
    }
  }

  public Object clone() {
    BaseFileInputStepMeta retval = (BaseFileInputStepMeta) super.clone();

    retval.inputFiles = (InputFiles) inputFiles.clone();
    retval.errorHandling = (ErrorHandling) errorHandling.clone();
    retval.additionalOutputFields = (AdditionalOutputFields) additionalOutputFields.clone();

    return retval;
  }

  /**
   * @param fileRequired
   *          The fileRequired to set.
   */
  public void inputFiles_fileRequired( String[] fileRequiredin ) {
    for ( int i = 0; i < fileRequiredin.length; i++ ) {
      inputFiles.fileRequired[i] = getRequiredFilesCode( fileRequiredin[i] );
    }
  }

  public String[] inputFiles_includeSubFolders() {
    return inputFiles.includeSubFolders;
  }

  public void inputFiles_includeSubFolders( String[] includeSubFoldersin ) {
    for ( int i = 0; i < includeSubFoldersin.length; i++ ) {
      inputFiles.includeSubFolders[i] = getRequiredFilesCode( includeSubFoldersin[i] );
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

  public FileInputList getFileInputList( VariableSpace space ) {
    inputFiles.fileMask = normalizeAllocation( inputFiles.fileMask, inputFiles.fileName.length );
    inputFiles.excludeFileMask = normalizeAllocation( inputFiles.excludeFileMask, inputFiles.fileName.length );
    inputFiles.fileRequired = normalizeAllocation( inputFiles.fileRequired, inputFiles.fileName.length );
    inputFiles.includeSubFolders = normalizeAllocation( inputFiles.includeSubFolders, inputFiles.fileName.length );
    return FileInputList.createFileList( space, inputFiles.fileName, inputFiles.fileMask, inputFiles.excludeFileMask,
        inputFiles.fileRequired, includeSubFolderBoolean() );
  }

  public boolean[] includeSubFolderBoolean() {
    int len = inputFiles.fileName.length;
    boolean[] includeSubFolderBoolean = new boolean[len];
    for ( int i = 0; i < len; i++ ) {
      includeSubFolderBoolean[i] = YES.equalsIgnoreCase( inputFiles.includeSubFolders[i] );
    }
    return includeSubFolderBoolean;
  }

  private String[] normalizeAllocation( String[] oldAllocation, int length ) {
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

  @Override
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    List<ResourceReference> references = new ArrayList<ResourceReference>( 5 );
    ResourceReference reference = new ResourceReference( stepInfo );
    references.add( reference );

    String[] textFiles =
        FileInputList.createFilePathList( transMeta, inputFiles.fileName, inputFiles.fileMask,
            inputFiles.excludeFileMask, inputFiles.fileRequired, includeSubFolderBoolean() );
    if ( textFiles != null ) {
      for ( int i = 0; i < textFiles.length; i++ ) {
        reference.getEntries().add( new ResourceEntry( textFiles[i], ResourceType.FILE ) );
      }
    }
    return references;
  }

  public abstract String getEncoding();
}
