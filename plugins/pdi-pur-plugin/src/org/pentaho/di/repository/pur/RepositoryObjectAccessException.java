/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.pentaho.di.repository.pur;

public class RepositoryObjectAccessException extends Exception implements java.io.Serializable {

  private static final long serialVersionUID = -3339087102211752867L; /* EESOURCE: UPDATE SERIALVERUID */

  public enum AccessExceptionType {
    USER_HOME_DIR
  }
  
  private AccessExceptionType type;
  
  public RepositoryObjectAccessException(String message, AccessExceptionType type) {
    this.type = type;
  }
  
  public AccessExceptionType getObjectAccessType() {
    return type;
  }
  
  public void setObjectAccessType(AccessExceptionType type) {
    this.type = type;
  }

}
