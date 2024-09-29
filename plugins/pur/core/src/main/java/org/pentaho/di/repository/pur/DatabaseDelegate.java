/*!
 * Copyright 2010 - 2020 Hitachi Vantara.  All rights reserved.
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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository.RepositoryFilenameUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DatabaseDelegate extends AbstractDelegate implements ITransformer, SharedObjectAssembler<DatabaseMeta>,
    java.io.Serializable {

  private static final long serialVersionUID = 1512547938350522165L; /* EESOURCE: UPDATE SERIALVERUID */

  // ~ Static fields/initializers ======================================================================================

  private static final String PROP_INDEX_TBS = "INDEX_TBS";
  private static final String PROP_DATA_TBS = "DATA_TBS";
  private static final String PROP_SERVERNAME = "SERVERNAME";
  private static final String PROP_PASSWORD = "PASSWORD";
  private static final String PROP_USERNAME = "USERNAME";
  private static final String PROP_PORT = "PORT";
  private static final String PROP_DATABASE_NAME = "DATABASE_NAME";
  private static final String PROP_HOST_NAME = "HOST_NAME";
  private static final String PROP_CONTYPE = "CONTYPE";
  private static final String PROP_TYPE = "TYPE";
  private static final String NODE_ROOT = "databaseMeta";
  private static final String NODE_ATTRIBUTES = "attributes";
  private static final String NODE_POOLING_PROPS = "poolProps";
  private static final String NODE_EXTRA_OPTIONS = "extraOptions";
  private static final String PROP_CONNECT_SQL = "connectionSQL";
  private static final String PROP_INITIAL_POOL_SIZE = "initialPoolSize";
  private static final String PROP_MAX_POOL_SIZE = "maxPoolSize";
  private static final String PROP_IS_POOLING = "isPooling";
  private static final String PROP_IS_FORCING_TO_LOWER = "isForcingLower";
  private static final String PROP_IS_FORCING_TO_UPPER = "isForcingUpper";
  private static final String PROP_IS_QUOTE_FIELDS = "isQuoteFields";
  private static final String PROP_IS_DECIMAL_SEPERATOR = "isUsingDecimalSeperator";
  private static final String PROP_SUPPORT_BOOLEAN_DATA_TYPE = "isSupportBooleanDataType";
  private static final String PROP_SUPPORT_TIMESTAMP_DATA_TYPE = "isSupportTimestampDataType";


  // ~ Instance fields =================================================================================================

  private PurRepository repo;

  // ~ Constructors ====================================================================================================

  public DatabaseDelegate( final PurRepository repo ) {
    super();
    this.repo = repo;
  }

  // ~ Methods =========================================================================================================

  public DataNode elementToDataNode( final RepositoryElementInterface element ) throws KettleException {
    DatabaseMeta databaseMeta = (DatabaseMeta) element;
    DataNode rootNode = new DataNode( NODE_ROOT );

    // Then the basic db information
    //
    rootNode.setProperty( PROP_TYPE, databaseMeta.getPluginId() );
    rootNode.setProperty( PROP_CONTYPE, DatabaseMeta.getAccessTypeDesc( databaseMeta.getAccessType() ) );
    rootNode.setProperty( PROP_HOST_NAME, databaseMeta.getHostname() );
    rootNode.setProperty( PROP_DATABASE_NAME, databaseMeta.getDatabaseName() );
    rootNode.setProperty( PROP_PORT, Const.toInt( databaseMeta.getDatabasePortNumberString(), -1 ) );
    rootNode.setProperty( PROP_USERNAME, databaseMeta.getUsername() );
    rootNode.setProperty( PROP_PASSWORD, Encr.encryptPasswordIfNotUsingVariables( databaseMeta.getPassword() ) );
    rootNode.setProperty( PROP_SERVERNAME, databaseMeta.getServername() );
    rootNode.setProperty( PROP_DATA_TBS, databaseMeta.getDataTablespace() );
    rootNode.setProperty( PROP_INDEX_TBS, databaseMeta.getIndexTablespace() );

    rootNode.setProperty( PROP_CONNECT_SQL, setNull( databaseMeta.getConnectSQL() ) );
    rootNode.setProperty( PROP_INITIAL_POOL_SIZE, databaseMeta.getInitialPoolSize() );
    rootNode.setProperty( PROP_MAX_POOL_SIZE, databaseMeta.getMaximumPoolSize() );
    rootNode.setProperty( PROP_IS_POOLING, databaseMeta.isUsingConnectionPool() );
    rootNode.setProperty( PROP_IS_FORCING_TO_LOWER, databaseMeta.isForcingIdentifiersToLowerCase() );
    rootNode.setProperty( PROP_IS_FORCING_TO_UPPER, databaseMeta.isForcingIdentifiersToUpperCase() );
    rootNode.setProperty( PROP_IS_QUOTE_FIELDS, databaseMeta.isQuoteAllFields() );
    rootNode.setProperty( PROP_IS_DECIMAL_SEPERATOR, databaseMeta.isUsingDoubleDecimalAsSchemaTableSeparator() );
    rootNode.setProperty( PROP_SUPPORT_BOOLEAN_DATA_TYPE, databaseMeta.supportsBooleanDataType() );
    rootNode.setProperty( PROP_SUPPORT_TIMESTAMP_DATA_TYPE, databaseMeta.supportsTimestampDataType() );

    if ( databaseMeta.getAttributes() != null && !databaseMeta.getAttributes().isEmpty() ) {
      addNodeToElement( NODE_ATTRIBUTES, rootNode, databaseMeta.getAttributes().entrySet() );
    }

    if ( databaseMeta.getConnectionPoolingProperties() != null && !databaseMeta.getConnectionPoolingProperties().isEmpty() ) {
      addNodeToElement( NODE_POOLING_PROPS, rootNode, databaseMeta.getConnectionPoolingProperties().entrySet() );
    }

    if ( databaseMeta.getExtraOptions() != null && !databaseMeta.getExtraOptions().isEmpty() ) {
      addNodeToElement( NODE_EXTRA_OPTIONS, rootNode, new HashMap<Object, Object>( databaseMeta.getExtraOptions() ).entrySet() );
    }

    return rootNode;
  }

  private void addNodeToElement( String nodeName, DataNode rootNode, Set<Map.Entry<Object, Object>> attributes ) {
    if ( attributes == null ) {
      return;
    }

    DataNode attrNode = rootNode.addNode( nodeName );
    Iterator<Map.Entry<Object, Object>> keys = attributes.iterator();
    while ( keys.hasNext() ) {
      Map.Entry<Object, Object> entry = keys.next();
      String code = (String) entry.getKey();
      String attribute = (String) entry.getValue();
      // Save this attribute
      //
      // Escape the code as it might contain invalid JCR characters like '/' as in AS/400
      String escapedCode = RepositoryFilenameUtils.escape( code, repo.getUnderlyingRepository().getReservedChars() );
      attrNode.setProperty( escapedCode, attribute );
    }
  }

  public RepositoryElementInterface dataNodeToElement( final DataNode rootNode ) throws KettleException {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    dataNodeToElement( rootNode, databaseMeta );
    return databaseMeta;
  }

  public void dataNodeToElement( final DataNode rootNode, final RepositoryElementInterface element )
    throws KettleException {
    DatabaseMeta databaseMeta = (DatabaseMeta) element;
    databaseMeta.setDatabaseType( getString( rootNode, PROP_TYPE ) );
    databaseMeta.setAccessType( DatabaseMeta.getAccessType( getString( rootNode, PROP_CONTYPE ) ) );
    databaseMeta.setHostname( getString( rootNode, PROP_HOST_NAME ) );
    databaseMeta.setDBName( getString( rootNode, PROP_DATABASE_NAME ) );
    databaseMeta.setDBPort( getString( rootNode, PROP_PORT ) );
    databaseMeta.setUsername( getString( rootNode, PROP_USERNAME ) );
    databaseMeta.setPassword( Encr.decryptPasswordOptionallyEncrypted( getString( rootNode, PROP_PASSWORD ) ) );
    databaseMeta.setServername( getString( rootNode, PROP_SERVERNAME ) );
    databaseMeta.setDataTablespace( getString( rootNode, PROP_DATA_TBS ) );
    databaseMeta.setIndexTablespace( getString( rootNode, PROP_INDEX_TBS ) );

    databaseMeta.setConnectSQL( getString( rootNode, PROP_CONNECT_SQL ) );
    databaseMeta.setInitialPoolSize( getInt( rootNode, PROP_INITIAL_POOL_SIZE ) );
    databaseMeta.setMaximumPoolSize( getInt( rootNode, PROP_MAX_POOL_SIZE ) );
    databaseMeta.setUsingConnectionPool( getBoolean( rootNode, PROP_IS_POOLING ) );
    databaseMeta.setForcingIdentifiersToLowerCase( getBoolean( rootNode, PROP_IS_FORCING_TO_LOWER ) );
    databaseMeta.setForcingIdentifiersToUpperCase( getBoolean( rootNode, PROP_IS_FORCING_TO_UPPER ) );
    databaseMeta.setQuoteAllFields( getBoolean( rootNode, PROP_IS_QUOTE_FIELDS ) );
    databaseMeta.setUsingDoubleDecimalAsSchemaTableSeparator( getBoolean( rootNode, PROP_IS_DECIMAL_SEPERATOR ) );
    databaseMeta.setSupportsBooleanDataType( getBoolean( rootNode, PROP_SUPPORT_BOOLEAN_DATA_TYPE ) );
    databaseMeta.setSupportsTimestampDataType( getBoolean( rootNode, PROP_SUPPORT_TIMESTAMP_DATA_TYPE ) );

    // Also, load all the properties we can find...

    DataNode attrNode = rootNode.getNode( NODE_ATTRIBUTES );
    for ( DataProperty property : attrNode.getProperties() ) {
      String code = property.getName();
      String attribute = property.getString();

      // We need to unescape the code as it was escaped to handle characters that JCR does not handle
      String unescapeCode = RepositoryFilenameUtils.unescape( code );
      databaseMeta.getAttributes().put( unescapeCode, Const.NVL( attribute, "" ) );
    }

    // Also, load any pooling params
    attrNode = rootNode.getNode( NODE_POOLING_PROPS );
    if ( attrNode != null ) {
      Properties properties = new Properties();
      for ( DataProperty property : attrNode.getProperties() ) {
        String code = property.getName();
        String attribute = property.getString();
        properties.put( code, ( attribute == null || attribute.length() == 0 ) ? "" : attribute );
      }
      databaseMeta.setConnectionPoolingProperties( properties );
    }

    // Load extra options
    attrNode = rootNode.getNode( NODE_EXTRA_OPTIONS );
    if ( attrNode != null ) {
      for ( DataProperty property : attrNode.getProperties() ) {
        String databaseTypeCode = property.getName().substring( 0, property.getName().indexOf( '.' ) );
        String code = property.getName().replace( databaseTypeCode + ".", "" );
        String attribute = property.getString();
        databaseMeta.addExtraOption( databaseTypeCode, code,
                ( attribute == null || attribute.length() == 0 ) ? "" : attribute );
      }
    }
  }

  public Repository getRepository() {
    return repo;
  }

  public DatabaseMeta assemble( RepositoryFile file, NodeRepositoryFileData data, VersionSummary version )
    throws KettleException {
    DatabaseMeta databaseMeta = (DatabaseMeta) dataNodeToElement( data.getNode() );
    String fileName = file.getName();
    if ( fileName.endsWith( ".kdb" ) ) {
      fileName = fileName.substring( 0, fileName.length() - 4 );
    }
    databaseMeta.setChangedDate( file.getLastModifiedDate() );
    databaseMeta.setName( fileName );
    databaseMeta.setDisplayName( file.getTitle() );
    databaseMeta.setObjectId( new StringObjectId( file.getId().toString() ) );
    databaseMeta.setObjectRevision( repo.createObjectRevision( version ) );
    databaseMeta.clearChanged();
    return databaseMeta;
  }
}
