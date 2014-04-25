/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.market;

/**
 * This is an indicator for the support level of a certain software component (plugin)
 *
 * @author matt
 */
public enum SupportLevel {
  // Supported by ...
  //
    PROFESSIONALLY_SUPPORTED( "Professionally supported" ),

    // Supported by the community
    //
    COMMUNITY_SUPPORTED( "Community Supported" ),

    // Unsupported by anyone: you're on your own.
    //
    NOT_SUPPORTED( "Not supported" );

  private String description;

  private SupportLevel( String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Get the SupportLevel for a given support level code
   * @param code The code to search for
   * @return the corresponding SupportLevel or NOT_SUPPORTED if not found.
   */
  public static SupportLevel getSupportLevel( String code ) {
    for ( SupportLevel level : values() ) {
      if ( level.name().equalsIgnoreCase( code ) ) {
        return level;
      }
    }
    return NOT_SUPPORTED;
  }
}
