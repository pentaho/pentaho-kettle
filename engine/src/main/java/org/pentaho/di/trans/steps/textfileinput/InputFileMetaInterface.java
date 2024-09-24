/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.textfileinput;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * @deprecated replaced by implementation in the ...steps.fileinput.text package
 */
@Deprecated
public interface InputFileMetaInterface extends StepMetaInterface {

  public TextFileInputField[] getInputFields();

  public int getFileFormatTypeNr();

  public boolean hasHeader();

  public int getNrHeaderLines();

  /**
   * @deprecated replaced by getFilePaths( Bowl bowl, VariableSpace space )
   */
  @Deprecated
  default String[] getFilePaths( VariableSpace space ) {
    throw new UnsupportedOperationException( "deprecated" );
  }

  default String[] getFilePaths( Bowl bowl, VariableSpace space ) {
    return getFilePaths( space );
  }

  public boolean isErrorIgnored();

  public String getErrorCountField();

  public String getErrorFieldsField();

  public String getErrorTextField();

  public String getFileType();

  public String getEnclosure();

  public String getEscapeCharacter();

  public String getSeparator();

  public boolean isErrorLineSkipped();

  public boolean includeFilename();

  public boolean includeRowNumber();
}
