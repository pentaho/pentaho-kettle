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

package org.pentaho.di.lineage;

import java.util.List;

/**
 * This describes how a field gets renamed in a certain step.<br>
 * It helps us to do the complete lineage from source to target and back.<br>
 *
 * @author matt
 *
 */
public class FieldnameLineage {
  private String inputFieldname;
  private String outputFieldname;

  /**
   * Create a new field lineage object
   *
   * @param inputFieldname
   *          The input field name
   * @param outputFieldname
   *          The output field name
   */
  public FieldnameLineage( String inputFieldname, String outputFieldname ) {
    super();
    this.inputFieldname = inputFieldname;
    this.outputFieldname = outputFieldname;
  }

  /**
   * @return the input Field name
   */
  public String getInputFieldname() {
    return inputFieldname;
  }

  /**
   * @param inputFieldname
   *          the input Field name to set
   */
  public void setInputFieldname( String inputFieldname ) {
    this.inputFieldname = inputFieldname;
  }

  /**
   * @return the output Field name
   */
  public String getOutputFieldname() {
    return outputFieldname;
  }

  /**
   * @param outputFieldname
   *          the output Field name to set
   */
  public void setOutputFieldname( String outputFieldname ) {
    this.outputFieldname = outputFieldname;
  }

  /**
   * Search for a field name lineage object in a list.
   *
   * @param lineages
   *          The list
   * @param input
   *          the input field name to look for
   * @return The first encountered field name lineage object where the input field name matches. If nothing is found
   *         null is returned.
   */
  public static final FieldnameLineage findFieldnameLineageWithInput( List<FieldnameLineage> lineages, String input ) {
    for ( FieldnameLineage lineage : lineages ) {
      if ( lineage.getInputFieldname().equalsIgnoreCase( input ) ) {
        return lineage;
      }
    }
    return null;
  }
}
