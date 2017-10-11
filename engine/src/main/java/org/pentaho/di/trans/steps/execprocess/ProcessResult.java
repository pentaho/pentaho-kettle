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

package org.pentaho.di.trans.steps.execprocess;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class ProcessResult {
  private String outputStream;
  private String errorStream;
  private long exitValue;

  public ProcessResult() {
    super();
    this.outputStream = null;
    this.errorStream = null;
    this.exitValue = 1;
  }

  public String getOutputStream() {
    return this.outputStream;
  }

  public void setOutputStream( String string ) {
    this.outputStream = string;
  }

  public String getErrorStream() {
    return this.errorStream;
  }

  public void setErrorStream( String string ) {
    this.errorStream = string;
  }

  public long getExistStatus() {
    return this.exitValue;
  }

  public void setExistStatus( long value ) {
    this.exitValue = value;
  }
}
