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
