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

package org.pentaho.di.trans.steps.script;

public class ScriptValuesScript {

  public static final int NORMAL_SCRIPT = -1;
  public static final int TRANSFORM_SCRIPT = 0;
  public static final int START_SCRIPT = 1;
  public static final int END_SCRIPT = 2;

  private int iScriptType;
  private boolean bScriptActive;
  private String sScriptName;
  private String sScript;

  public ScriptValuesScript( int iScriptType, String sScriptName, String sScript ) {
    super();
    this.iScriptType = iScriptType;
    this.sScriptName = sScriptName;
    this.sScript = sScript;
    bScriptActive = true;
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

  @Override
  public String toString() {
    return String.format( "ScriptValuesScript: (%d, %s, %s, %b)", getScriptType(), getScriptName(), getScript(), isActive() );
  }
}
