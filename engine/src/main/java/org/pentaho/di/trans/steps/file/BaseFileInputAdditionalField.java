/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.injection.Injection;

/**
 * Additional fields settings.
 */
public class BaseFileInputAdditionalField implements Cloneable {

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

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch ( CloneNotSupportedException ex ) {
      throw new IllegalArgumentException( "Clone not supported for " + this.getClass().getName() );
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
