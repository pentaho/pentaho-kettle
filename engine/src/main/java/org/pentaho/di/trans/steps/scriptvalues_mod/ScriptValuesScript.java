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

package org.pentaho.di.trans.steps.scriptvalues_mod;

public class ScriptValuesScript {

  public static final int NORMAL_SCRIPT = -1;
  public static final int TRANSFORM_SCRIPT = 0;
  public static final int START_SCRIPT = 1;
  public static final int END_SCRIPT = 2;

  private int iScriptType;
  private boolean bScriptActive;
  private String sScriptName;
  private String sScript;

  // private Date dModDate;
  // private Date dFirstDate;

  public ScriptValuesScript( int iScriptType, String sScriptName, String sScript ) {
    super();
    this.iScriptType = iScriptType;
    this.sScriptName = sScriptName;
    this.sScript = sScript;
    bScriptActive = true;
    // dModDate = new Date();
    // dFirstDate = new Date();
  }

  public int getScriptType() {
    return iScriptType;
  }

  public void setScriptType( int iScriptType ) {
    this.iScriptType = iScriptType;
  }

  public String getScript() {
    return this.sScript;
  }

  public void setScript( String sScript ) {
    this.sScript = sScript;
  }

  public String getScriptName() {
    return sScriptName;
  }

  public void setScriptName( String sScriptName ) {
    this.sScriptName = sScriptName;
  }

  public boolean isTransformScript() {
    if ( this.bScriptActive && this.iScriptType == TRANSFORM_SCRIPT ) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isStartScript() {
    if ( this.bScriptActive && this.iScriptType == START_SCRIPT ) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isEndScript() {
    if ( this.bScriptActive && this.iScriptType == END_SCRIPT ) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isActive() {
    return bScriptActive;
  }

  public String toString() {
    return String.format( "ScriptValuesScript: (%d, %s, %s)", iScriptType, sScriptName, sScript );
  }

}
