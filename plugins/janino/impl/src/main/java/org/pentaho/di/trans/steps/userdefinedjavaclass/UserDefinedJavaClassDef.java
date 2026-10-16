/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.apache.commons.codec.binary.Hex;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.injection.Injection;

import java.security.MessageDigest;
import java.util.Objects;

public class UserDefinedJavaClassDef implements Cloneable {
  public enum ClassType {
    NORMAL_CLASS, TRANSFORM_CLASS
  }

  private ClassType classType;
  private boolean classActive;

  @Injection( name = "CLASS_NAME", group = "JAVA_CLASSES" )
  private String className;

  @Injection( name = "CLASS_SOURCE", group = "JAVA_CLASSES" )
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
