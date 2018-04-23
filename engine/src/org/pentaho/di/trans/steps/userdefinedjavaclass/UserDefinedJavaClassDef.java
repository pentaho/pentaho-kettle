/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.apache.commons.codec.binary.Hex;
import org.pentaho.di.core.exception.KettleStepException;

import java.security.MessageDigest;
import java.util.Objects;

public class UserDefinedJavaClassDef implements Cloneable {
  public enum ClassType {
    NORMAL_CLASS, TRANSFORM_CLASS
  }

  private ClassType classType;
  private boolean classActive;
  private String className;
  private String source;

  public UserDefinedJavaClassDef( ClassType classType, String className, String source ) {
    super();
    this.classType = classType;
    this.className = className;
    this.source = source;
    classActive = true;
  }

  public int hashCode() {
    return Objects.hash( className, source );
  }

  public String getChecksum() throws KettleStepException {
    String ck = this.className + this.source;
    try {
      byte[] b = MessageDigest.getInstance( "MD5" ).digest( ck.getBytes() );
      return Hex.encodeHexString( b );
    } catch ( Exception ex ) {
      // Can't get MD5 hashcode ?
      throw new KettleStepException( "Unable to obtain checksum of UDJC - " + this.className );
    }
  }

  public ClassType getClassType() {
    return classType;
  }

  public void setClassType( ClassType classType ) {
    this.classType = classType;
  }

  public String getSource() {
    return this.source;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public String getTransformedSource() throws KettleStepException {
    StringBuilder sb = new StringBuilder( getSource() );
    appendConstructor( sb );
    return sb.toString();
  }

  public void setSource( String source ) {
    this.source = source;
  }

  private static final String CONSTRUCTOR =
    "\n\npublic %s(UserDefinedJavaClass parent, UserDefinedJavaClassMeta meta, UserDefinedJavaClassData data) "
      + "throws KettleStepException { super(parent,meta,data);}";

  private void appendConstructor( StringBuilder sb ) {
    sb.append( String.format( CONSTRUCTOR, className ) );
  }

  public String getClassName() {
    return className;
  }

  public void setClassName( String className ) {
    this.className = className;
  }

  public boolean isTransformClass() {
    return ( this.classActive && this.classType == ClassType.TRANSFORM_CLASS );
  }

  public void setActive( boolean classActive ) {
    this.classActive = classActive;
  }

  public boolean isActive() {
    return classActive;
  }
}
