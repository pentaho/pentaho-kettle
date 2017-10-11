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

package org.pentaho.di.trans.steps.excelinput;

public enum SpreadSheetType {
  JXL( "Excel 97-2003 XLS (JXL)" ), POI( "Excel 2007 XLSX (Apache POI)" ),
    SAX_POI( "Excel 2007 XLSX (Apache POI Streaming)" ),
    ODS( "Open Office ODS (ODFDOM)" );

  private String description;

  /**
   * @param description
   */
  private SpreadSheetType( String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public static SpreadSheetType getStpreadSheetTypeByDescription( String description ) {
    for ( SpreadSheetType type : values() ) {
      if ( type.getDescription().equalsIgnoreCase( description ) ) {
        return type;
      }
    }
    return null;
  }
}
