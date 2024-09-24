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

package org.pentaho.di.imp.rule;

import java.util.ArrayList;
import java.util.List;

public class ImportValidationFeedback {
  private ImportRuleInterface importRule;
  private ImportValidationResultType resultType;
  private String comment;

  /**
   * @param resultType
   * @param comment
   */
  public ImportValidationFeedback( ImportRuleInterface importRule, ImportValidationResultType resultType,
    String comment ) {
    this.importRule = importRule;
    this.resultType = resultType;
    this.comment = comment;
  }

  public static List<ImportValidationFeedback> getErrors( List<ImportValidationFeedback> feedback ) {
    List<ImportValidationFeedback> errors = new ArrayList<ImportValidationFeedback>();

    for ( ImportValidationFeedback error : feedback ) {
      if ( error.isError() ) {
        errors.add( error );
      }
    }

    return errors;
  }

  @Override
  public String toString() {
    StringBuilder string = new StringBuilder();

    string.append( resultType.name() ).append( " : " );
    string.append( comment ).append( " - " );
    string.append( importRule.toString() );

    return string.toString();
  }

  /**
   * @return the resultType
   */
  public ImportValidationResultType getResultType() {
    return resultType;
  }

  public boolean isError() {
    return resultType == ImportValidationResultType.ERROR;
  }

  public boolean isApproval() {
    return resultType == ImportValidationResultType.APPROVAL;
  }

  /**
   * @param resultType
   *          the resultType to set
   */
  public void setResultType( ImportValidationResultType resultType ) {
    this.resultType = resultType;
  }

  /**
   * @return the comment
   */
  public String getComment() {
    return comment;
  }

  /**
   * @param comment
   *          the comment to set
   */
  public void setComment( String comment ) {
    this.comment = comment;
  }

  /**
   * @return the importRule
   */
  public ImportRuleInterface getImportRule() {
    return importRule;
  }

  /**
   * @param importRule
   *          the importRule to set
   */
  public void setImportRule( ImportRuleInterface importRule ) {
    this.importRule = importRule;
  }

}
