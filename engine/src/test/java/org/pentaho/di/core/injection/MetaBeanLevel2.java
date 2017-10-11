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

package org.pentaho.di.core.injection;

public class MetaBeanLevel2 extends MetaBeanLevel2Base {

  @Injection( name = "SEPARATOR" )
  private String separator;

  @InjectionDeep
  MetaBeanLevel3[] files;

  @InjectionDeep( prefix = "SECOND" )
  private MetaBeanLevel4 filesSecond;
  @InjectionDeep( prefix = "THIRD" )
  private MetaBeanLevel4 filesThird;

  @Injection( name = "FILENAME_ARRAY" )
  String[] filenames;

  public String[] getFilenames() {
    return filenames;
  }

  public String getSeparator() {
    return separator;
  }

  public MetaBeanLevel3[] getFiles() {
    return files;
  }
}
