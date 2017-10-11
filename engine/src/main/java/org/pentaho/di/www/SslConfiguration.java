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
package org.pentaho.di.www;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

/**
 * @author Tatsiana_Kasiankova
 *
 */
public class SslConfiguration {
  private static Class<?> PKG = SslConfiguration.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "sslConfig";

  private static final String XML_TAG_KEY_STORE = "keyStore";

  private static final String XML_TAG_KEY_STORE_TYPE = "keyStoreType";

  private static final String XML_TAG_KEY_PASSWORD = "keyPassword";

  private static final String XML_TAG_KEY_STORE_PASSWORD = "keyStorePassword";

  private static final String EMPTY = "empty";

  private static final String NULL = "null";

  private String keyStoreType = "JKS";

  private String keyStore;

  private String keyStorePassword;

  private String keyPassword;

  public SslConfiguration( Node sslConfigNode ) {
    super();
    setKeyStore( XMLHandler.getTagValue( sslConfigNode, XML_TAG_KEY_STORE ) );
    setKeyStorePassword( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( sslConfigNode,
        XML_TAG_KEY_STORE_PASSWORD ) ) );
    setKeyPassword( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( sslConfigNode,
        XML_TAG_KEY_PASSWORD ) ) );
    setKeyStoreType( XMLHandler.getTagValue( sslConfigNode, XML_TAG_KEY_STORE_TYPE ) );
  }

  /**
   * @return the keyStoreType
   */
  public String getKeyStoreType() {
    return keyStoreType;
  }

  /**
   * @param keyStoreType
   *          the keyStoreType to set
   */
  public void setKeyStoreType( String keyStoreType ) {
    if ( keyStoreType != null ) {
      this.keyStoreType = keyStoreType;
    }
  }

  /**
   * @return the keyStorePath
   */
  public String getKeyStore() {
    return keyStore;
  }

  /**
   * @param keyStorePath
   *          the keyStorePath to set
   */
  public void setKeyStore( String keyStore ) {
    Validate.notNull( keyStore, BaseMessages.getString( PKG, "WebServer.Error.IllegalSslParameter", XML_TAG_KEY_STORE,
        NULL ) );
    Validate.notEmpty( keyStore, BaseMessages.getString( PKG, "WebServer.Error.IllegalSslParameter", XML_TAG_KEY_STORE,
        EMPTY ) );
    this.keyStore = keyStore;
  }

  /**
   * @return the keyStorePassword
   */
  public String getKeyStorePassword() {
    return keyStorePassword;
  }

  /**
   * @param keyStorePassword
   *          the keyStorePassword to set
   */
  public void setKeyStorePassword( String keyStorePassword ) {
    Validate.notNull( keyStorePassword, BaseMessages.getString( PKG, "WebServer.Error.IllegalSslParameter",
        XML_TAG_KEY_STORE_PASSWORD, NULL ) );
    Validate.notEmpty( keyStorePassword, BaseMessages.getString( PKG, "WebServer.Error.IllegalSslParameter",
        XML_TAG_KEY_STORE_PASSWORD, EMPTY ) );
    this.keyStorePassword = keyStorePassword;
  }

  /**
   * @return the keyPassword
   */
  public String getKeyPassword() {
    return ( this.keyPassword != null ) ? this.keyPassword : getKeyStorePassword();
  }

  /**
   * @param keyPassword
   *          the keyPassword to set
   */
  public void setKeyPassword( String keyPassword ) {
    this.keyPassword = keyPassword;
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder();
    xml.append( "        " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    addXmlValue( xml, XML_TAG_KEY_STORE, keyStore );
    addXmlValue( xml, XML_TAG_KEY_STORE_PASSWORD, encrypt( keyStorePassword ) );
    addXmlValue( xml, XML_TAG_KEY_PASSWORD, encrypt( keyPassword ) );
    xml.append( "        " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );
    return xml.toString();
  }

  private static void addXmlValue( StringBuilder xml, String key, String value ) {
    if ( !StringUtils.isBlank( value ) ) {
      xml.append( "          " ).append( XMLHandler.addTagValue( key, value, false ) );
    }
  }

  private static String encrypt( String value ) {
    if ( !StringUtils.isBlank( value ) ) {
      return Encr.encryptPasswordIfNotUsingVariables( value );
    }
    return null;
  }
}
