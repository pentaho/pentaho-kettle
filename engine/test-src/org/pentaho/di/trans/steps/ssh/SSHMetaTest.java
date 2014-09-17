package org.pentaho.di.trans.steps.ssh;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.w3c.dom.Node;

public class SSHMetaTest {

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Test
  public void testEncryptedPasswords() throws KettleXMLException {
    String plaintextPassword = "MyEncryptedPassword";
    String plaintextPassphrase = "MyEncryptedPassPhrase";
    String plaintextProxyPassword = "MyEncryptedProxyPassword";

    SSHMeta sshMeta = new SSHMeta();
    sshMeta.setpassword( plaintextPassword );
    sshMeta.setPassphrase( plaintextPassphrase );
    sshMeta.setProxyPassword( plaintextProxyPassword );

    StringBuffer xmlString = new StringBuffer();
    xmlString.append( XMLHandler.getXMLHeader() ).append( Const.CR );
    xmlString.append( XMLHandler.openTag( "step" ) ).append( Const.CR );
    xmlString.append( sshMeta.getXML() );
    xmlString.append( XMLHandler.closeTag( "step" ) ).append( Const.CR );
    Node sshXMLNode = XMLHandler.loadXMLString( xmlString.toString(), "step" );

    assertEquals( Encr.encryptPasswordIfNotUsingVariables( plaintextPassword ),
      XMLHandler.getTagValue( sshXMLNode, "password" ) );
    assertEquals( Encr.encryptPasswordIfNotUsingVariables( plaintextPassphrase ),
      XMLHandler.getTagValue( sshXMLNode, "passPhrase" ) );
    assertEquals( Encr.encryptPasswordIfNotUsingVariables( plaintextProxyPassword ),
      XMLHandler.getTagValue( sshXMLNode, "proxyPassword" ) );
  }

  @Test
  public void testRoundTrips() throws KettleException {
    List<String> commonFields = Arrays.<String>asList( "dynamicCommandField", "command", "commandfieldname", "port",
      "servername", "userName", "password", "usePrivateKey", "keyFileName", "passPhrase", "stdOutFieldName",
      "stdErrFieldName", "timeOut", "proxyHost", "proxyPort", "proxyUsername", "proxyPassword" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "dynamicCommandField", "isDynamicCommand" );
    getterMap.put( "command", "getCommand" );
    getterMap.put( "commandfieldname", "getcommandfieldname" );
    getterMap.put( "port", "getPort" );
    getterMap.put( "servername", "getServerName" );
    getterMap.put( "userName", "getuserName" );
    getterMap.put( "password", "getpassword" );
    getterMap.put( "usePrivateKey", "isusePrivateKey" );
    getterMap.put( "keyFileName", "getKeyFileName" );
    getterMap.put( "passPhrase", "getPassphrase" );
    getterMap.put( "stdOutFieldName", "getStdOutFieldName" );
    getterMap.put( "stdErrFieldName", "getStdErrFieldName" );
    getterMap.put( "timeOut", "getTimeOut" );
    getterMap.put( "proxyHost", "getProxyHost" );
    getterMap.put( "proxyPort", "getProxyPort" );
    getterMap.put( "proxyUsername", "getProxyUsername" );
    getterMap.put( "proxyPassword", "getProxyPassword" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "dynamicCommandField", "setDynamicCommand" );
    setterMap.put( "command", "setCommand" );
    setterMap.put( "commandfieldname", "setcommandfieldname" );
    setterMap.put( "port", "setPort" );
    setterMap.put( "servername", "setServerName" );
    setterMap.put( "userName", "setuserName" );
    setterMap.put( "password", "setpassword" );
    setterMap.put( "usePrivateKey", "usePrivateKey" );
    setterMap.put( "keyFileName", "setKeyFileName" );
    setterMap.put( "passPhrase", "setPassphrase" );
    setterMap.put( "stdOutFieldName", "setstdOutFieldName" );
    setterMap.put( "stdErrFieldName", "setStdErrFieldName" );
    setterMap.put( "timeOut", "setTimeOut" );
    setterMap.put( "proxyHost", "setProxyHost" );
    setterMap.put( "proxyPort", "setProxyPort" );
    setterMap.put( "proxyUsername", "setProxyUsername" );
    setterMap.put( "proxyPassword", "setProxyPassword" );

    LoadSaveTester tester = new LoadSaveTester( SSHMeta.class, commonFields , getterMap, setterMap );

    tester.testXmlRoundTrip();
    tester.testRepoRoundTrip();
  }

}
