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

package org.pentaho.di.trans.steps.file;

import java.util.List;

import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Base meta for file-based input steps.
 *
 * @author Alexander Buloichik
 */
public abstract class BaseFileInputMeta<A extends BaseFileInputAdditionalField, I extends BaseFileInputFiles, F extends BaseFileField>
    extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = BaseFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };

  public static final String NO = "N";

  public static final String YES = "Y";

  public static final String[] RequiredFilesDesc =
      new String[] { BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG,
          "System.Combo.Yes" ) };

  @InjectionDeep
  public I inputFiles;

  /** The fields to import... */
  @InjectionDeep
  public F[] inputFields;

  @InjectionDeep
  public BaseFileErrorHandling errorHandling = new BaseFileErrorHandling();
  @InjectionDeep
  public A additionalOutputFields;

  public Object clone() {
    BaseFileInputMeta<BaseFileInputAdditionalField, BaseFileInputFiles, BaseFileField> retval = (BaseFileInputMeta<BaseFileInputAdditionalField, BaseFileInputFiles, BaseFileField>) super.clone();

    retval.inputFiles = (BaseFileInputFiles) inputFiles.clone();
    retval.errorHandling = (BaseFileErrorHandling) errorHandling.clone();
    retval.additionalOutputFields = (BaseFileInputAdditionalField) additionalOutputFields.clone();

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
    inputFiles.normalizeAllocation( inputFiles.fileName.length );
    return FileInputList.createFileList( space, inputFiles.fileName, inputFiles.fileMask, inputFiles.excludeFileMask,
        inputFiles.fileRequired, inputFiles.includeSubFolderBoolean() );
  }

  @Override
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    return inputFiles.getResourceDependencies( transMeta, stepInfo );
  }

  public abstract String getEncoding();
}
