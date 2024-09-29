/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.ui.database.event;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseConnectionPoolParameter;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseTestResults;
import org.pentaho.di.core.database.GenericDatabaseMeta;
import org.pentaho.di.core.database.MSSQLServerNativeDatabaseMeta;
import org.pentaho.di.core.database.OracleDatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeListener;
import org.pentaho.di.core.util.Utils;
import org.pentaho.ui.database.Messages;
import org.pentaho.ui.util.Launch;
import org.pentaho.ui.util.Launch.Status;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCell;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulRoot;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeItem;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.containers.XulWindow;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.CLIENT_SECRET_KEY;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.CLIENT_ID;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.IS_ALWAYS_ENCRYPTION_ENABLED;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.SQL_AUTHENTICATION;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_ACCESS_KEY_ID;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_CREDENTIALS;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_PROFILE_NAME;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_SECRET_ACCESS_KEY;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_SESSION_TOKEN;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.JDBC_AUTH_METHOD;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.PROFILE_CREDENTIALS;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.STANDARD_CREDENTIALS;
import static org.pentaho.di.core.database.SnowflakeHVDatabaseMeta.WAREHOUSE;

/**
 * Handles all manipulation of the DatabaseMeta, data retrieval from XUL DOM and rudimentary validation.
 * <p/>
 * TODO: 2. Needs to be abstracted away from the DatabaseMeta object, so other tools in the platform can use the dialog
 * and their preferred database object. 3. Needs exception handling, string resourcing and logging
 *
 * @author gmoran
 * @created Mar 19, 2008
 */
public class DataHandler extends AbstractXulEventHandler {

  public static final SortedMap<String, DatabaseInterface> connectionMap = new TreeMap<>();
  public static final Map<String, String> connectionNametoID = new HashMap<>();

  // Kettle thin related
  private static final String EXTRA_OPTION_WEB_APPLICATION_NAME = BaseDatabaseMeta.ATTRIBUTE_PREFIX_EXTRA_OPTION
    + "KettleThin.webappname";
  private static final String DEFAULT_WEB_APPLICATION_NAME = "pentaho";


  private List<String> databaseDialects;

  /** A database-specific handler */
  private Optional<DbInfoHandler> extraHandler = Optional.empty();

  // The connectionMap allows us to keep track of the connection
  // type we are working with and the correlating database interface

  static {
    PluginRegistry registry = PluginRegistry.getInstance();

    List<PluginInterface> plugins = registry.getPlugins( DatabasePluginType.class );

    PluginTypeListener databaseTypeListener = new DatabaseTypeListener( registry ) {
      public void databaseTypeAdded( String pluginName, DatabaseInterface databaseInterface ) {
        connectionMap.put( pluginName, databaseInterface );
        connectionNametoID.put( pluginName, databaseInterface.getPluginId() );
      }

      public void databaseTypeRemoved( String pluginName ) {
        connectionMap.remove( pluginName );
        connectionNametoID.remove( pluginName );
      }
    };

    registry.addPluginListener( DatabasePluginType.class, databaseTypeListener );
    for ( PluginInterface plugin : plugins ) {
      databaseTypeListener.pluginAdded( plugin );
    }

  }

  protected DatabaseMeta databaseMeta = null;

  protected DatabaseMeta cache = new DatabaseMeta();

  protected Launch launch = new Launch();

  private XulDeck dialogDeck;

  private XulListbox deckOptionsBox;

  private XulListbox connectionBox;

  private XulListbox accessBox;

  private XulTextbox connectionNameBox;

  protected XulTextbox hostNameBox;

  protected XulTextbox databaseNameBox;

  protected XulTextbox portNumberBox;

  protected XulTextbox userNameBox;

  protected XulTextbox passwordBox;

  // Generic database specific
  protected XulTextbox customDriverClassBox;

  // Generic database specific
  protected XulTextbox customUrlBox;

  // Oracle specific
  protected XulTextbox dataTablespaceBox;

  // Oracle specific
  protected XulTextbox indexTablespaceBox;

  // MS SQL Server specific
  protected XulTextbox serverInstanceBox;

  // Informix specific
  private XulTextbox serverNameBox;

  // SAP R/3 specific
  protected XulTextbox languageBox;

  // SAP R/3 specific
  protected XulTextbox systemNumberBox;

  // SAP R/3 specific
  protected XulTextbox clientBox;

  // Snowflake specific
  protected XulTextbox warehouseBox;

  // MS SQL Server specific
  private XulCheckbox doubleDecimalSeparatorCheck;

  // MySQL specific
  private XulCheckbox resultStreamingCursorCheck;

  // Hitachi Vantara data services specific
  private XulTextbox webAppName;

  // ==== Options Panel ==== //

  protected XulTree optionsParameterTree;

  // ==== Clustering Panel ==== //

  private XulCheckbox clusteringCheck;

  protected XulTree clusterParameterTree;

  private XulLabel clusterParameterDescriptionLabel;

  // ==== Advanced Panel ==== //

  XulCheckbox supportBooleanDataType;

  XulCheckbox supportTimestampDataType;

  XulCheckbox quoteIdentifiersCheck;

  XulCheckbox lowerCaseIdentifiersCheck;

  XulCheckbox upperCaseIdentifiersCheck;

  XulCheckbox preserveReservedCaseCheck;

  XulCheckbox strictBigNumberInterpretaion;

  XulCheckbox useIntegratedSecurityCheck;

  XulTextbox preferredSchemaName;

  XulTextbox sqlBox;

  // ==== Pooling Panel ==== //

  private XulLabel poolSizeLabel;

  private XulLabel maxPoolSizeLabel;

  private XulCheckbox poolingCheck;

  protected XulTextbox poolSizeBox;

  protected XulTextbox maxPoolSizeBox;

  private XulTextbox poolingDescription;

  private XulLabel poolingParameterDescriptionLabel;

  private XulLabel poolingDescriptionLabel;

  protected XulTree poolParameterTree;

  protected XulMenuList<String> databaseDialectList;

  protected XulButton acceptButton;
  private XulButton cancelButton;
  private XulButton testButton;
  private XulLabel noticeLabel;

  private XulMenuList<?> jdbcAuthMethod;
  private XulTextbox iamAccessKeyId;
  private XulTextbox iamSecretKeyId;
  private XulTextbox iamSessionToken;
  private XulTextbox iamProfileName;
  protected XulMenuList<String> namedClusterList;

  //Azure SQL DB Variables
  private XulMenuList<?> azureSqlDBJdbcAuthMethod;
  private XulCheckbox azureSqlDBAlwaysEncryptionEnabled;
  private XulTextbox azureSqlDBClientSecretId;
  private XulTextbox azureSqlDBClientSecretKey;

  public DataHandler() {
    databaseDialects = new ArrayList<String>();
    DatabaseInterface[] dialectMetas = DatabaseMeta.getDatabaseInterfaces();
    for ( DatabaseInterface dialect : dialectMetas ) {
      databaseDialects.add( dialect.getPluginName() );
    }
    Collections.sort( databaseDialects );

  }

  /** Set a handler to deal with database-specific save/load. */
  public void setExtraHandler( DbInfoHandler handler ) {
    this.extraHandler = Optional.of( handler );
  }

  /** @see #setExtraHandler(DbInfoHandler) */
  public void clearExtraHandler() {
    this.extraHandler = Optional.empty();
  }

  public void loadConnectionData() {

    // HACK: need to check if onload event was already fired.
    // It is called from XulDatabaseDialog from dcDialog.getSwtInstance(shell); AND dialog.show();
    // Multiple calls lead to multiple numbers of database types.
    // Therefore we check if the connectionBox was already filled.
    if ( connectionBox != null ) {
      return;
    }

    getControls();

    // Add sorted types to the listbox now.

    final SortedSet<String> keys = new TreeSet<String>( connectionMap.keySet() );
    for ( String key : keys ) {
      connectionBox.addItem( key );
    }
    PluginRegistry registry = PluginRegistry.getInstance();
    registry.addPluginListener( DatabasePluginType.class, new DatabaseTypeListener( registry ) {
      @Override
      public void databaseTypeAdded( String pluginName, DatabaseInterface databaseInterface ) {
        if ( keys.add( pluginName ) ) {
          update();
        }
      }

      @Override
      public void databaseTypeRemoved( String pluginName ) {
        if ( keys.remove( pluginName ) ) {
          update();
        }
      }

      private void update() {
        Display.getDefault().syncExec( new Runnable() {
          @Override
          public void run() {
            connectionBox.removeItems();
            for ( String key : keys ) {
              connectionBox.addItem( key );
            }
          }
        } );
      }
    } );

    // HACK: Need to force height of list control, as it does not behave
    // well when using relative layouting

    connectionBox.setRows( connectionBox.getRows() );

    Object key = connectionBox.getSelectedItem();

    // Nothing selected yet...select first item.

    // TODO Implement a connection type preference,
    // and use that type as the default for
    // new databases.

    if ( key == null ) {
      key = connectionMap.firstKey();
      connectionBox.setSelectedItem( key );
    }

    // HACK: Need to force selection of first panel

    if ( dialogDeck != null ) {
      setDeckChildIndex();
    }

    setDefaultPoolParameters();
    // HACK: reDim the pooling table
    if ( poolParameterTree != null ) {
      poolParameterTree.setRows( poolParameterTree.getRows() );
    }
  }

  // On Database type change
  public void loadAccessData() {

    getControls();

    pushCache();

    Object key = connectionBox.getSelectedItem();

    // Nothing selected yet...
    if ( key == null ) {
      key = connectionMap.firstKey();
      connectionBox.setSelectedItem( key );
      return;
    }

    DatabaseInterface database = connectionMap.get( key );

    int[] acc = database.getAccessTypeList();
    Object accessKey = accessBox.getSelectedItem();
    accessBox.removeItems();

    // Add those access types applicable to this conneciton type

    for ( int value : acc ) {
      accessBox.addItem( DatabaseMeta.getAccessTypeDescLong( value ) );
    }

    // HACK: Need to force height of list control, as it does not behave
    // well when using relative layouting

    accessBox.setRows( accessBox.getRows() );

    // May not exist for this connection type.
    if ( accessKey != null ) { // This check keeps the SwtListbox from complaining about a null value
      accessBox.setSelectedItem( accessKey );
    }

    // Last resort, set first as default
    if ( accessBox.getSelectedItem() == null ) {
      accessBox.setSelectedItem( DatabaseMeta.getAccessTypeDescLong( acc[ 0 ] ) );
    }

    Map<String, String> options = null;
    if ( this.databaseMeta != null ) {
      // Apply defaults to meta if set (only current db type will be displayed)
      this.databaseMeta.applyDefaultOptions( database );
      options = this.databaseMeta.getExtraOptions();
    } else {
      // Otherwise clear and display defaults directly
      clearOptionsData();
      options = database.getDefaultOptions();
    }
    setOptionsData( options );
    PartitionDatabaseMeta[] clusterInfo = null;
    if ( this.databaseMeta != null ) {
      clusterInfo = this.databaseMeta.getPartitioningInformation();
    }
    setClusterData( clusterInfo );

    popCache();

  }

  public void editOptions( int index ) {
    if ( index + 1 == optionsParameterTree.getRows() ) {
      // editing last row add a new one below

      Object[][] values = optionsParameterTree.getValues();
      Object[] row = values[ values.length - 1 ];
      if ( row != null && ( !StringUtils.isEmpty( (String) row[ 0 ] ) || !StringUtils.isEmpty( (String) row[ 1 ] ) ) ) {
        // acutally have something in current last row
        XulTreeRow newRow = optionsParameterTree.getRootChildren().addNewRow();

        newRow.addCellText( 0, "" );
        newRow.addCellText( 1, "" );
      }
    }
  }

  public void clearOptionsData() {
    getControls();
    if ( optionsParameterTree != null ) {
      optionsParameterTree.getRootChildren().removeAll();
    }
  }

  public void getOptionHelp() {

    String message = null;
    DatabaseMeta database = new DatabaseMeta();

    getInfo( database );
    String url = database.getExtraOptionsHelpText();

    if ( ( url == null ) || ( url.trim().length() == 0 ) ) {
      message = Messages.getString( "DataHandler.USER_NO_HELP_AVAILABLE" );
      showMessage( message, false );
      return;
    }

    Status status = launch.openURL( url );

    if ( status.equals( Status.Failed ) ) {
      message = Messages.getString( "DataHandler.USER_UNABLE_TO_LAUNCH_BROWSER", url );
      showMessage( message, false );
    }

  }

  public void setDeckChildIndex() {

    getControls();

    // if pooling selected, check the parameter validity before allowing
    // a deck panel switch...
    int originalSelection = ( dialogDeck == null ? -1 : dialogDeck.getSelectedIndex() );

    boolean passed = true;
    if ( originalSelection == 3 ) {
      passed = checkPoolingParameters();
    }

    if ( passed ) {
      int selected = deckOptionsBox.getSelectedIndex();
      if ( selected < 0 ) {
        selected = 0;
        deckOptionsBox.setSelectedIndex( 0 );
      }
      dialogDeck.setSelectedIndex( selected );
    } else {
      dialogDeck.setSelectedIndex( originalSelection );
      deckOptionsBox.setSelectedIndex( originalSelection );
    }

  }

  public void onPoolingCheck() {
    if ( poolingCheck != null ) {
      boolean dis = !poolingCheck.isChecked();
      if ( poolSizeBox != null ) {
        poolSizeBox.setDisabled( dis );
      }
      if ( maxPoolSizeBox != null ) {
        maxPoolSizeBox.setDisabled( dis );
      }
      if ( poolSizeLabel != null ) {
        poolSizeLabel.setDisabled( dis );
      }
      if ( maxPoolSizeLabel != null ) {
        maxPoolSizeLabel.setDisabled( dis );
      }
      if ( poolParameterTree != null ) {
        poolParameterTree.setDisabled( dis );
      }
      if ( poolingParameterDescriptionLabel != null ) {
        poolingParameterDescriptionLabel.setDisabled( dis );
      }
      if ( poolingDescriptionLabel != null ) {
        poolingDescriptionLabel.setDisabled( dis );
      }
      if ( poolingDescription != null ) {
        poolingDescription.setDisabled( dis );
      }

    }
  }

  public void onClusterCheck() {
    if ( clusteringCheck != null ) {
      boolean dis = !clusteringCheck.isChecked();
      if ( clusterParameterTree != null ) {
        clusterParameterTree.setDisabled( dis );
      }
      if ( clusterParameterDescriptionLabel != null ) {
        clusterParameterDescriptionLabel.setDisabled( dis );
      }
    }
  }

  public Object getData() {

    if ( databaseMeta == null ) {
      databaseMeta = new DatabaseMeta();
    }

    if ( !windowClosed() ) {
      this.getInfo( databaseMeta );
    }
    return databaseMeta;
  }

  public void setData( Object data ) {
    if ( data instanceof DatabaseMeta ) {
      databaseMeta = (DatabaseMeta) data;
    }
    setInfo( databaseMeta );
  }

  public void pushCache() {
    // Database type:
    if ( connectionBox != null ) {
      Object connection = connectionBox.getSelectedItem();
      if ( connection != null ) {
        cache.setDatabaseType( (String) connection );
      }
    }
    getConnectionSpecificInfo( cache );
  }

  public void popCache() {
    setConnectionSpecificInfo( cache );
  }

  public void onCancel() {
    close();
  }

  private void close() {
    XulComponent window = document.getElementById( "general-datasource-window" );

    if ( window == null ) { // window must be root
      window = document.getRootElement();
    }
    if ( window instanceof XulDialog ) {
      ( (XulDialog) window ).hide();
    } else if ( window instanceof XulWindow ) {
      ( (XulWindow) window ).close();
    }
  }

  private boolean windowClosed() {
    boolean closedWindow = true;
    XulComponent window = document.getElementById( "general-datasource-window" );

    if ( window == null ) { // window must be root
      window = document.getRootElement();
    }
    if ( window instanceof XulWindow ) {
      closedWindow = ( (XulWindow) window ).isClosed();
    }
    return closedWindow;
  }

  public void onOK() {

    DatabaseMeta database = new DatabaseMeta();
    this.getInfo( database );

    boolean passed = checkPoolingParameters();
    if ( !passed ) {
      return;
    }

    String[] remarks = database.checkParameters();
    String message = "";

    if ( remarks.length != 0 ) {
      for ( int i = 0; i < remarks.length; i++ ) {
        message = message.concat( "* " ).concat( remarks[ i ] ).concat( System.getProperty( "line.separator" ) );
      }
      showMessage( message, false );
    } else {
      if ( databaseMeta == null ) {
        databaseMeta = new DatabaseMeta();
      }
      this.getInfo( databaseMeta );
      databaseMeta.setChanged();
      close();
    }
  }

  public void testDatabaseConnection() {

    DatabaseMeta database = new DatabaseMeta();

    getInfo( database );
    String[] remarks = database.checkParameters();
    String message = "";

    if ( remarks.length != 0 ) {
      for ( int i = 0; i < remarks.length; i++ ) {
        message = message.concat( "* " ).concat( remarks[ i ] ).concat( System.getProperty( "line.separator" ) );
      }
      showMessage( message, message.length() > 300 );
    } else {
      showMessage( database.testConnectionSuccess() );
    }
  }

  protected void getInfo( DatabaseMeta meta ) {

    getControls();

    if ( this.databaseMeta != null && this.databaseMeta != meta ) {
      meta.initializeVariablesFrom( this.databaseMeta );
    }

    // Let's not remove any (default) options or attributes
    // We just need to display the correct ones for the database type below...
    //
    // In fact, let's just clear the database port...
    //
    // TODO: what about the port number?

    // Name:
    meta.setName( connectionNameBox.getValue() );
    // Display Name: (PDI-12292)
    meta.setDisplayName( connectionNameBox.getValue() );

    // Connection type:
    Object connection = connectionBox.getSelectedItem();
    if ( connection != null ) {
      meta.setDatabaseType( (String) connection );
    }

    // Access type:
    Object access = accessBox.getSelectedItem();
    if ( access != null ) {
      meta.setAccessType( DatabaseMeta.getAccessType( (String) access ) );
    }

    getConnectionSpecificInfo( meta );

    // Port number:
    if ( portNumberBox != null ) {
      meta.setDBPort( portNumberBox.getValue() );
    }

    // Option parameters:

    if ( optionsParameterTree != null ) {
      Object[][] values = optionsParameterTree.getValues();
      for ( int i = 0; i < values.length; i++ ) {

        String parameter = (String) values[ i ][ 0 ];
        String value = (String) values[ i ][ 1 ];

        if ( value == null ) {
          value = "";
        }

        String dbType = meta.getPluginId();

        // Only if parameter are supplied, we will add to the map...
        if ( ( parameter != null ) && ( parameter.trim().length() > 0 ) ) {
          if ( value.trim().length() <= 0 ) {
            value = DatabaseMeta.EMPTY_OPTIONS_STRING;
          }

          meta.addExtraOption( dbType, parameter, value );
        }
      }
    }

    // Advanced panel settings:

    if ( supportBooleanDataType != null ) {
      meta.setSupportsBooleanDataType( supportBooleanDataType.isChecked() );
    }

    if ( supportTimestampDataType != null ) {
      meta.setSupportsTimestampDataType( supportTimestampDataType.isChecked() );
    }

    if ( quoteIdentifiersCheck != null ) {
      meta.setQuoteAllFields( quoteIdentifiersCheck.isChecked() );
    }

    if ( lowerCaseIdentifiersCheck != null ) {
      meta.setForcingIdentifiersToLowerCase( lowerCaseIdentifiersCheck.isChecked() );
    }

    if ( upperCaseIdentifiersCheck != null ) {
      meta.setForcingIdentifiersToUpperCase( upperCaseIdentifiersCheck.isChecked() );
    }

    if ( preserveReservedCaseCheck != null ) {
      meta.setPreserveReservedCase( preserveReservedCaseCheck.isChecked() );
    }

    if ( strictBigNumberInterpretaion != null && meta.getDatabaseInterface() instanceof OracleDatabaseMeta ) {
      ( (OracleDatabaseMeta) meta.getDatabaseInterface() )
        .setStrictBigNumberInterpretation( strictBigNumberInterpretaion.isChecked() );
    }

    if ( preferredSchemaName != null ) {
      meta.setPreferredSchemaName( preferredSchemaName.getValue() );
    }

    if ( sqlBox != null ) {
      meta.setConnectSQL( sqlBox.getValue() );
    }

    // Cluster panel settings
    if ( clusteringCheck != null ) {
      meta.setPartitioned( clusteringCheck.isChecked() );
    }

    if ( ( clusterParameterTree != null ) && ( meta.isPartitioned() ) ) {

      Object[][] values = clusterParameterTree.getValues();
      List<PartitionDatabaseMeta> pdms = new ArrayList<PartitionDatabaseMeta>();
      for ( int i = 0; i < values.length; i++ ) {

        String partitionId = (String) values[ i ][ 0 ];

        if ( ( partitionId == null ) || ( partitionId.trim().length() <= 0 ) ) {
          continue;
        }

        String hostname = (String) values[ i ][ 1 ];
        String port = (String) values[ i ][ 2 ];
        String dbName = (String) values[ i ][ 3 ];
        String username = (String) values[ i ][ 4 ];
        String password = (String) values[ i ][ 5 ];
        PartitionDatabaseMeta pdm = new PartitionDatabaseMeta( partitionId, hostname, port, dbName );
        pdm.setUsername( username );
        pdm.setPassword( password );
        pdms.add( pdm );
      }
      PartitionDatabaseMeta[] pdmArray = new PartitionDatabaseMeta[ pdms.size() ];
      meta.setPartitioningInformation( pdms.toArray( pdmArray ) );
    }

    if ( poolingCheck != null ) {
      meta.setUsingConnectionPool( poolingCheck.isChecked() );
    }

    if ( meta.isUsingConnectionPool() ) {
      if ( poolSizeBox != null ) {
        try {
          meta.setInitialPoolSizeString( poolSizeBox.getValue() );
        } catch ( NumberFormatException e ) {
          // TODO log exception and move on ...
        }
      }

      if ( maxPoolSizeBox != null ) {
        try {
          meta.setMaximumPoolSizeString( maxPoolSizeBox.getValue() );
        } catch ( NumberFormatException e ) {
          // TODO log exception and move on ...
        }
      }

      if ( poolParameterTree != null ) {
        Object[][] values = poolParameterTree.getValues();
        Properties properties = new Properties();
        for ( int i = 0; i < values.length; i++ ) {

          boolean isChecked = false;
          if ( values[ i ][ 0 ] instanceof Boolean ) {
            isChecked = ( (Boolean) values[ i ][ 0 ] ).booleanValue();
          } else {
            isChecked = Boolean.valueOf( (String) values[ i ][ 0 ] );
          }

          if ( !isChecked ) {
            continue;
          }

          String parameter = (String) values[ i ][ 1 ];
          String value = (String) values[ i ][ 2 ];
          if ( ( parameter != null )
            && ( parameter.trim().length() > 0 ) && ( value != null ) && ( value.trim().length() > 0 ) ) {
            properties.setProperty( parameter, value );
          }

        }
        meta.setConnectionPoolingProperties( properties );
      }
    }
  }

  private void setInfo( DatabaseMeta meta ) {

    if ( meta == null ) {
      return;
    }

    if ( meta.getAttributes().containsKey( EXTRA_OPTION_WEB_APPLICATION_NAME ) ) {
      meta.setDBName( (String) meta.getAttributes().get( EXTRA_OPTION_WEB_APPLICATION_NAME ) );
      meta.getAttributes().remove( EXTRA_OPTION_WEB_APPLICATION_NAME );
      meta.setChanged();
    }

    getControls();

    // Name:
    if ( connectionNameBox != null ) {
      connectionNameBox.setValue( meta.getDisplayName() );
    }

    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface dInterface = registry.getPlugin( DatabasePluginType.class, meta.getPluginId() );

    // Connection type:
    int index = ( dInterface == null ? -1 : new ArrayList<>( connectionMap.keySet() ).indexOf( dInterface.getName() ) );
    if ( index >= 0 ) {
      connectionBox.setSelectedIndex( index );
    } else {
      LogChannel.GENERAL.logError( "Unable to find database type "
        + ( dInterface == null ? "null" : dInterface.getName() ) + " in our connection map" );
    }

    // Access type:
    accessBox.setSelectedItem( DatabaseMeta.getAccessTypeDescLong( meta.getAccessType() ) );

    // this is broken out so we can set the cache information only when caching
    // connection values
    setConnectionSpecificInfo( meta );
    loadAccessData();

    // Port number:
    if ( portNumberBox != null ) {
      portNumberBox.setValue( meta.getDatabasePortNumberString() );
    }

    // Options Parameters:

    setOptionsData( meta.getExtraOptions() );

    // Advanced panel settings:
    if ( supportBooleanDataType != null ) {
      supportBooleanDataType.setChecked( meta.supportsBooleanDataType() );
    }

    if ( supportTimestampDataType != null ) {
      supportTimestampDataType.setChecked( meta.supportsTimestampDataType() );
    }

    if ( quoteIdentifiersCheck != null ) {
      quoteIdentifiersCheck.setChecked( meta.isQuoteAllFields() );
    }

    if ( lowerCaseIdentifiersCheck != null ) {
      lowerCaseIdentifiersCheck.setChecked( meta.isForcingIdentifiersToLowerCase() );
    }

    if ( upperCaseIdentifiersCheck != null ) {
      upperCaseIdentifiersCheck.setChecked( meta.isForcingIdentifiersToUpperCase() );
    }

    if ( preserveReservedCaseCheck != null ) {
      preserveReservedCaseCheck.setChecked( meta.preserveReservedCase() );
    }

    if ( strictBigNumberInterpretaion != null ) {
      //check if Oracle
      if ( meta.getDatabaseInterface() instanceof OracleDatabaseMeta ) {
        strictBigNumberInterpretaion.setVisible( true );
        strictBigNumberInterpretaion.setChecked( ( (OracleDatabaseMeta) meta.getDatabaseInterface() )
          .strictBigNumberInterpretation() );
      } else {
        strictBigNumberInterpretaion.setVisible( false );
        strictBigNumberInterpretaion.setChecked( false );
      }
    }

    if ( preferredSchemaName != null ) {
      preferredSchemaName.setValue( Const.NVL( meta.getPreferredSchemaName(), "" ) );
    }

    if ( sqlBox != null ) {
      sqlBox.setValue( meta.getConnectSQL() == null ? "" : meta.getConnectSQL() );
    }

    // Clustering panel settings

    if ( clusteringCheck != null ) {
      clusteringCheck.setChecked( meta.isPartitioned() );
    }

    setClusterData( meta.getPartitioningInformation() );

    // Pooling panel settings

    if ( poolingCheck != null ) {
      poolingCheck.setChecked( meta.isUsingConnectionPool() );
    }

    if ( meta.isUsingConnectionPool() ) {
      if ( poolSizeBox != null ) {
        poolSizeBox.setValue( meta.getInitialPoolSizeString() );
      }

      if ( maxPoolSizeBox != null ) {
        maxPoolSizeBox.setValue( meta.getMaximumPoolSizeString() );
      }

      setPoolProperties( meta.getConnectionPoolingProperties() );
    }

    setReadOnly( meta.isReadOnly() );

    setDeckChildIndex();
    onPoolingCheck();
    onClusterCheck();
    enableAzureSqlDBEncryption();
    setAzureSqlDBAuthRelatedFieldsVisible();
  }

  public void setAzureSqlDBAuthRelatedFieldsVisible() {
    if ( passwordBox != null ) {
      passwordBox.setDisabled( azureSqlDBJdbcAuthMethod != null &&
        ( "Azure Active Directory - Universal With MFA".equals( azureSqlDBJdbcAuthMethod.getValue() ) ||
          "Azure Active Directory - Integrated".equals( azureSqlDBJdbcAuthMethod.getValue() ) ) );
    }
    if ( userNameBox != null ) {
      userNameBox.setDisabled( azureSqlDBJdbcAuthMethod != null && "Azure Active Directory - Integrated"
        .equals( azureSqlDBJdbcAuthMethod.getValue() ) );
    }
  }

  public void enableAzureSqlDBEncryption() {
    if ( azureSqlDBAlwaysEncryptionEnabled != null ) {
      boolean isAlwaysEncryptionEnabled = azureSqlDBAlwaysEncryptionEnabled.isChecked();
      if ( !isAlwaysEncryptionEnabled ) {
        azureSqlDBClientSecretId.setDisabled( true );
        azureSqlDBClientSecretKey.setDisabled( true );
      } else {
        azureSqlDBClientSecretId.setDisabled( false );
        azureSqlDBClientSecretKey.setDisabled( false );
      }
    }

  }

  @SuppressWarnings ( "unused" )
  public void setAuthFieldsVisible() {
    jdbcAuthMethod = (XulMenuList) document.getElementById( "redshift-auth-method-list" );
    XulVbox standardControls = (XulVbox) document.getElementById( "auth-standard-controls" );
    XulVbox iamControls = (XulVbox) document.getElementById( "auth-iam-controls" );
    XulVbox profileControls = (XulVbox) document.getElementById( "auth-profile-controls" );
    String jdbcAuthMethodValue = jdbcAuthMethod.getValue();
    switch ( jdbcAuthMethodValue ) {
      case IAM_CREDENTIALS:
        standardControls.setVisible( false );
        iamControls.setVisible( true );
        profileControls.setVisible( false );
        break;
      case PROFILE_CREDENTIALS:
        standardControls.setVisible( false );
        iamControls.setVisible( false );
        profileControls.setVisible( true );
        break;
      default:
        standardControls.setVisible( true );
        iamControls.setVisible( false );
        profileControls.setVisible( false );
        break;
    }
  }

  private void traverseDomSetReadOnly( XulComponent component, boolean readonly ) {
    component.setDisabled( readonly );
    List<XulComponent> children = component.getChildNodes();
    if ( children != null && children.size() > 0 ) {
      for ( XulComponent child : children ) {
        child.setDisabled( readonly );
        traverseDomSetReadOnly( child, readonly );
      }
    }
  }

  private void setReadOnly( boolean readonly ) {
    // set the readonly status of EVERYTHING!
    traverseDomSetReadOnly( document.getRootElement(), readonly );
    if ( noticeLabel != null ) {
      noticeLabel.setVisible( readonly );
    }

    if ( readonly ) {
      // now turn back on the cancel and test buttons
      if ( cancelButton != null ) {
        cancelButton.setDisabled( false );
      }
      if ( testButton != null ) {
        testButton.setDisabled( false );
      }
      noticeLabel.setValue( Messages.getString( "DatabaseDialog.label.ConnectionIsReadOnly" ) );
    }
  }

  /**
   * @return the list of parameters that were enabled, but had invalid return values (null or empty)
   */
  private boolean checkPoolingParameters() {

    List<String> returnList = new ArrayList<String>();
    if ( poolParameterTree != null ) {
      Object[][] values = poolParameterTree.getValues();
      for ( int i = 0; i < values.length; i++ ) {

        boolean isChecked = false;
        if ( values[ i ][ 0 ] instanceof Boolean ) {
          isChecked = ( (Boolean) values[ i ][ 0 ] ).booleanValue();
        } else {
          isChecked = Boolean.valueOf( (String) values[ i ][ 0 ] );
        }

        if ( !isChecked ) {
          continue;
        }

        String parameter = (String) values[ i ][ 1 ];
        String value = (String) values[ i ][ 2 ];
        if ( ( value == null ) || ( value.trim().length() <= 0 ) ) {
          returnList.add( parameter );
        }

      }
      if ( returnList.size() > 0 ) {
        String parameters = System.getProperty( "line.separator" );
        for ( String parameter : returnList ) {
          parameters = parameters.concat( parameter ).concat( System.getProperty( "line.separator" ) );
        }

        String message = Messages.getString( "DataHandler.USER_INVALID_PARAMETERS" ).concat( parameters );
        showMessage( message, false );
      }
    }
    return returnList.size() <= 0;
  }

  private void setPoolProperties( Properties properties ) {
    if ( poolParameterTree != null ) {
      Object[][] values = poolParameterTree.getValues();
      for ( int i = 0; i < values.length; i++ ) {

        String parameter = (String) values[ i ][ 1 ];
        boolean isChecked = properties.containsKey( parameter );

        if ( !isChecked ) {
          continue;
        }
        XulTreeItem item = poolParameterTree.getRootChildren().getItem( i );
        item.getRow().addCellText( 0, "true" ); // checks the checkbox

        String value = properties.getProperty( parameter );
        item.getRow().addCellText( 2, value );

      }
    }

  }

  public void restoreDefaults() {
    if ( poolParameterTree != null ) {
      for ( int i = 0; i < poolParameterTree.getRootChildren().getItemCount(); i++ ) {
        XulTreeItem item = poolParameterTree.getRootChildren().getItem( i );
        String parameterName = item.getRow().getCell( 1 ).getLabel();
        String defaultValue =
          DatabaseConnectionPoolParameter
            .findParameter( parameterName, BaseDatabaseMeta.poolingParameters ).getDefaultValue();
        if ( ( defaultValue == null ) || ( defaultValue.trim().length() <= 0 ) ) {
          continue;
        }
        item.getRow().addCellText( 2, defaultValue );
      }
    }

  }

  private void setDefaultPoolParameters() {
    if ( poolParameterTree != null ) {
      for ( DatabaseConnectionPoolParameter parameter : BaseDatabaseMeta.poolingParameters ) {
        XulTreeRow row = poolParameterTree.getRootChildren().addNewRow();
        row.addCellText( 0, "false" );
        row.addCellText( 1, parameter.getParameter() );
        row.addCellText( 2, parameter.getDefaultValue() );
      }
    }
  }

  private void removeTypedOptions( Map<String, String> extraOptions ) {

    List<Integer> removeList = new ArrayList<Integer>();

    Object[][] values = optionsParameterTree.getValues();
    for ( int i = 0; i < values.length; i++ ) {

      String parameter = (String) values[ i ][ 0 ];

      // See if it's defined
      Iterator<String> keys = extraOptions.keySet().iterator();
      if ( extraOptions.keySet().size() > 0 ) {
        while ( keys.hasNext() ) {
          String param = keys.next();
          String parameterKey = param.substring( param.indexOf( '.' ) + 1 );
          if ( parameter.equals( parameterKey ) || "".equals( parameter ) ) {
            // match, remove it if not already in the list
            if ( !removeList.contains( i ) ) {
              removeList.add( i );
            }
          }
        }
      } else if ( "".equals( parameter ) ) {
        if ( !removeList.contains( i ) ) {
          removeList.add( i );
        }
      }

    }

    for ( int i = removeList.size() - 1; i >= 0; i-- ) {
      optionsParameterTree.getRootChildren().removeItem( removeList.get( i ) );
    }

  }

  private void setOptionsData( Map<String, String> extraOptions ) {

    if ( optionsParameterTree == null ) {
      return;
    }
    if ( extraOptions != null ) {
      removeTypedOptions( extraOptions );
      Iterator<String> keys = extraOptions.keySet().iterator();

      Object connection = connectionBox.getSelectedItem();
      String currentType = null;

      if ( connection != null ) {
        currentType = connectionMap.get( connection.toString() ).getPluginId();
      }

      while ( keys.hasNext() ) {

        String parameter = keys.next();
        String value = extraOptions.get( parameter );
        if ( ( value == null )
          || ( value.trim().length() <= 0 ) || ( value.equals( DatabaseMeta.EMPTY_OPTIONS_STRING ) ) ) {
          value = "";
        }

        // If the parameter starts with a database type code we show it in the options, otherwise we don't.
        // For example MySQL.defaultFetchSize
        //

        int dotIndex = parameter.indexOf( '.' );
        if ( dotIndex >= 0 ) {
          String parameterOption = parameter.substring( dotIndex + 1 );
          String databaseTypeString = parameter.substring( 0, dotIndex );
          String databaseType = databaseTypeString;
          if ( currentType != null && currentType.equals( databaseType ) ) {
            XulTreeRow row = optionsParameterTree.getRootChildren().addNewRow();
            row.addCellText( 0, parameterOption );
            row.addCellText( 1, value );
          }
        }
      }

    }
    // Have at least 5 option rows, with at least one blank
    int numToAdd = 15;
    int numSet = optionsParameterTree.getRootChildren().getItemCount();
    if ( numSet < numToAdd ) {
      numToAdd -= numSet;
    } else {
      numToAdd = 1;
    }
    while ( numToAdd-- > 0 ) {
      XulTreeRow row = optionsParameterTree.getRootChildren().addNewRow();
      row.addCellText( 0, "" ); // easy way of putting new cells in the row
      row.addCellText( 1, "" );
    }
  }

  private void setClusterData( PartitionDatabaseMeta[] clusterInformation ) {

    if ( clusterParameterTree == null ) {
      // there's nothing to do
      return;
    }

    clusterParameterTree.getRootChildren().removeAll();

    if ( ( clusterInformation != null ) && ( clusterParameterTree != null ) ) {

      for ( int i = 0; i < clusterInformation.length; i++ ) {

        PartitionDatabaseMeta meta = clusterInformation[ i ];
        XulTreeRow row = clusterParameterTree.getRootChildren().addNewRow();
        row.addCellText( 0, Const.NVL( meta.getPartitionId(), "" ) );
        row.addCellText( 1, Const.NVL( meta.getHostname(), "" ) );
        row.addCellText( 2, Const.NVL( meta.getPort(), "" ) );
        row.addCellText( 3, Const.NVL( meta.getDatabaseName(), "" ) );
        row.addCellText( 4, Const.NVL( meta.getUsername(), "" ) );
        row.addCellText( 5, Const.NVL( meta.getPassword(), "" ) );
      }
    }

    // Add 5 blank rows if none are already there, otherwise, just add one.
    int numToAdd = 5;
    /*
     * if(clusterInformation != null && clusterInformation.length > 0){ numToAdd = 1; }
     */
    while ( numToAdd-- > 0 ) {
      XulTreeRow row = clusterParameterTree.getRootChildren().addNewRow();
      row.addCellText( 0, "" ); // easy way of putting new cells in the row
      row.addCellText( 1, "" );
      row.addCellText( 2, "" );
      row.addCellText( 3, "" );
      row.addCellText( 4, "" );
      row.addCellText( 5, "" );
    }
  }

  public void poolingRowChange( int idx ) {

    if ( idx != -1 ) {

      if ( idx >= BaseDatabaseMeta.poolingParameters.length ) {
        idx = BaseDatabaseMeta.poolingParameters.length - 1;
      }
      if ( idx < 0 ) {
        idx = 0;
      }
      poolingDescription.setValue( BaseDatabaseMeta.poolingParameters[ idx ].getDescription() );

      XulTreeRow row = poolParameterTree.getRootChildren().getItem( idx ).getRow();
      if ( row.getSelectedColumnIndex() == 2 ) {
        row.addCellText( 0, "true" );
      }

    }
  }

  protected void getConnectionSpecificInfo( DatabaseMeta meta ) {
    // Hostname:
    if ( hostNameBox != null ) {
      meta.setHostname( hostNameBox.getValue() );
    }

    // Database name:
    if ( databaseNameBox != null ) {
      meta.setDBName( databaseNameBox.getValue() );
    }

    // Username:
    if ( userNameBox != null ) {
      meta.setUsername( userNameBox.getValue() );
    }

    // Password:
    if ( passwordBox != null ) {
      meta.setPassword( passwordBox.getValue() );
    }

    if ( databaseDialectList != null ) {
      DatabaseInterface databaseInterface = meta.getDatabaseInterface();
      if ( databaseInterface instanceof GenericDatabaseMeta ) {
        ( (GenericDatabaseMeta) databaseInterface ).setDatabaseDialect( databaseDialectList.getValue() );
      }
    }

    // if(this.portNumberBox != null){
    // meta.setDBPort(portNumberBox.getValue());
    // }

    // Streaming result cursor:
    if ( resultStreamingCursorCheck != null ) {
      meta.setStreamingResults( resultStreamingCursorCheck.isChecked() );
    }

    // Data tablespace:
    if ( dataTablespaceBox != null ) {
      meta.setDataTablespace( dataTablespaceBox.getValue() );
    }

    // Index tablespace
    if ( indexTablespaceBox != null ) {
      meta.setIndexTablespace( indexTablespaceBox.getValue() );
    }

    // The SQL Server instance name overrides the option.
    // Empty doesn't clear the option, we have mercy.

    if ( serverInstanceBox != null ) {
      meta.setSQLServerInstance( serverInstanceBox.getValue() );
      if ( optionsParameterTree != null && optionsParameterTree.getRootChildren() != null ) {
        for ( int i = 0; i < optionsParameterTree.getRootChildren().getItemCount(); i++ ) {
          XulTreeItem potRow = optionsParameterTree.getRootChildren().getItem( i );
          if ( potRow != null && potRow.getRow() != null ) {
            XulTreeCell cell = potRow.getRow().getCell( 0 );
            XulTreeCell cell2 = potRow.getRow().getCell( 1 );
            if ( cell != null && cell.getLabel() != null && cell.getLabel().equals( "instance" ) ) {
              cell2.setLabel( serverInstanceBox.getValue() );
              if ( serverInstanceBox.getValue().trim().length() == 0 ) {
                cell.setLabel( "" );
              }
            }
          }
        }
      }
    }

    // SQL Server double decimal separator
    if ( doubleDecimalSeparatorCheck != null ) {
      meta.setUsingDoubleDecimalAsSchemaTableSeparator( doubleDecimalSeparatorCheck.isChecked() );
    }

    // SAP Attributes...
    if ( languageBox != null ) {
      meta.getAttributes().put( "SAPLanguage", languageBox.getValue() );
    }
    if ( systemNumberBox != null ) {
      meta.getAttributes().put( "SAPSystemNumber", systemNumberBox.getValue() );
    }
    if ( clientBox != null ) {
      meta.getAttributes().put( "SAPClient", clientBox.getValue() );
    }

    // Snowflake
    if ( warehouseBox != null ) {
      meta.getAttributes().put( WAREHOUSE, warehouseBox.getValue() );
    }

    // Generic settings...
    if ( customUrlBox != null ) {
      meta.getAttributes().put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL, customUrlBox.getValue() );
    }
    if ( customDriverClassBox != null ) {
      meta
        .getAttributes()
        .put( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, customDriverClassBox.getValue() );
    }

    // Server Name: (Informix)
    if ( serverNameBox != null ) {
      meta.setServername( serverNameBox.getValue() );
    }

    // Microsoft SQL Server Use Integrated Security
    if ( useIntegratedSecurityCheck != null ) {
      Boolean useIntegratedSecurity = useIntegratedSecurityCheck.isChecked();
      meta.getAttributes().put(
        MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY,
        useIntegratedSecurity != null ? useIntegratedSecurity.toString() : "false" );
    }
    //Azure SQL DB
    if ( azureSqlDBJdbcAuthMethod != null ) {
      meta.getAttributes().put( JDBC_AUTH_METHOD, azureSqlDBJdbcAuthMethod.getValue() );
    }

    if ( azureSqlDBClientSecretId != null ) {
      meta.getAttributes().put( CLIENT_ID, azureSqlDBClientSecretId.getValue() );
    }
    if ( azureSqlDBAlwaysEncryptionEnabled != null ) {
      if ( azureSqlDBAlwaysEncryptionEnabled.isChecked() ) {
        meta.getAttributes().put( IS_ALWAYS_ENCRYPTION_ENABLED, "true" );
      } else {
        meta.getAttributes().put( IS_ALWAYS_ENCRYPTION_ENABLED, "false" );
      }
    }

    if ( azureSqlDBClientSecretKey != null ) {
      meta.getAttributes().put( CLIENT_SECRET_KEY, azureSqlDBClientSecretKey.getValue() );
    }

    if ( jdbcAuthMethod != null ) {
      meta.getAttributes().put( JDBC_AUTH_METHOD, jdbcAuthMethod.getValue() );
    }
    if ( iamAccessKeyId != null ) {
      meta.getAttributes().put( IAM_ACCESS_KEY_ID, Encr.encryptPassword( iamAccessKeyId.getValue() ) );
    }
    if ( iamSecretKeyId != null ) {
      meta.getAttributes().put( IAM_SECRET_ACCESS_KEY, Encr.encryptPassword( iamSecretKeyId.getValue() ) );
    }
    if ( iamSessionToken != null ) {
      meta.getAttributes().put( IAM_SESSION_TOKEN, Encr.encryptPassword( iamSessionToken.getValue() ) );
    }
    if ( iamProfileName != null ) {
      meta.getAttributes().put( IAM_PROFILE_NAME, iamProfileName.getValue() );
    }

    if ( webAppName != null ) {
      meta.setDBName( webAppName.getValue() );
    }

    if ( namedClusterList != null ) {

      meta.getDatabaseInterface().setNamedCluster( namedClusterList.getValue() );
    }

    this.extraHandler.ifPresent( handler -> handler.saveConnectionSpecificInfo( meta ) );
  }

  protected void setConnectionSpecificInfo( DatabaseMeta meta ) {

    getControls();

    if ( databaseDialectList != null ) {
      databaseDialectList.setElements( databaseDialects );
      DatabaseInterface databaseInterface = meta.getDatabaseInterface();
      if ( databaseInterface instanceof GenericDatabaseMeta ) {
        databaseDialectList.setSelectedItem( ( (GenericDatabaseMeta) databaseInterface ).getDatabaseDialect() );
      }
    }

    if ( namedClusterList != null ) {
      List<String> namedClusters = meta.getDatabaseInterface().getNamedClusterList();
      if ( namedClusters != null ) {
        namedClusterList.setElements( namedClusters );
        if ( meta.getNamedCluster() != null ) {
          namedClusterList.setSelectedItem( meta.getDatabaseInterface().getNamedCluster() );
          namedClusterList.setValue( meta.getDatabaseInterface().getNamedCluster() );
        }
      }
    }

    if ( hostNameBox != null ) {
      hostNameBox.setValue( meta.getHostname() );
    }

    // Database name:
    if ( databaseNameBox != null ) {
      databaseNameBox.setValue( meta.getDatabaseName() );
    }

    // Username:
    if ( userNameBox != null ) {
      userNameBox.setValue( meta.getUsername() );
    }

    // Password:
    if ( passwordBox != null ) {
      passwordBox.setValue( meta.getPassword() );
    }

    // if(this.portNumberBox != null){
    // this.portNumberBox.setValue(meta.getDatabasePortNumberString());
    // }

    // Streaming result cursor:
    if ( resultStreamingCursorCheck != null ) {
      resultStreamingCursorCheck.setChecked( meta.isStreamingResults() );
    }

    // Data tablespace:
    if ( dataTablespaceBox != null ) {
      dataTablespaceBox.setValue( meta.getDataTablespace() );
    }

    // Index tablespace
    if ( indexTablespaceBox != null ) {
      indexTablespaceBox.setValue( meta.getIndexTablespace() );
    }

    // Strict Number(38) interpretation
    if ( strictBigNumberInterpretaion != null ) {
      // Check if oracle
      if ( meta.getDatabaseInterface() instanceof OracleDatabaseMeta ) {
        strictBigNumberInterpretaion.setVisible( true );
        strictBigNumberInterpretaion.setChecked( ( (OracleDatabaseMeta) meta.getDatabaseInterface() )
          .strictBigNumberInterpretation() );
      } else {
        strictBigNumberInterpretaion.setVisible( false );
        strictBigNumberInterpretaion.setChecked( false );
      }
    }

    if ( serverInstanceBox != null ) {
      serverInstanceBox.setValue( meta.getSQLServerInstance() );
    }

    // SQL Server double decimal separator
    if ( doubleDecimalSeparatorCheck != null ) {
      doubleDecimalSeparatorCheck.setChecked( meta.isUsingDoubleDecimalAsSchemaTableSeparator() );
    }

    // SAP Attributes...
    if ( languageBox != null ) {
      languageBox.setValue( meta.getAttributes().getProperty( "SAPLanguage" ) );
    }
    if ( systemNumberBox != null ) {
      systemNumberBox.setValue( meta.getAttributes().getProperty( "SAPSystemNumber" ) );
    }
    if ( clientBox != null ) {
      clientBox.setValue( meta.getAttributes().getProperty( "SAPClient" ) );
    }

    // Snowflake
    if ( warehouseBox != null ) {
      warehouseBox.setValue( meta.getAttributes().getProperty( WAREHOUSE ) );
    }

    // Generic settings...
    if ( customUrlBox != null ) {
      customUrlBox.setValue( meta.getAttributes().getProperty( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL ) );
    }
    if ( customDriverClassBox != null ) {
      customDriverClassBox.setValue( meta.getAttributes().getProperty(
        GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS ) );
    }

    // Server Name: (Informix)
    if ( serverNameBox != null ) {
      serverNameBox.setValue( meta.getServername() );
    }

    // Microsoft SQL Server Use Integrated Security
    if ( useIntegratedSecurityCheck != null ) {
      Object value = meta.getAttributes().get( MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY );
      if ( value != null && value instanceof String ) {
        String useIntegratedSecurity = (String) value;
        useIntegratedSecurityCheck.setChecked( Boolean.parseBoolean( useIntegratedSecurity ) );
      } else {
        useIntegratedSecurityCheck.setChecked( false );
      }
    }

    if ( azureSqlDBJdbcAuthMethod != null ) {
      azureSqlDBJdbcAuthMethod.setValue( meta.getAttributes().getProperty( JDBC_AUTH_METHOD, SQL_AUTHENTICATION ) );
    }

    if ( azureSqlDBAlwaysEncryptionEnabled != null && meta.getAttributes().getProperty( IS_ALWAYS_ENCRYPTION_ENABLED ) != null ) {
      azureSqlDBAlwaysEncryptionEnabled.setChecked( meta.getAttributes().getProperty( IS_ALWAYS_ENCRYPTION_ENABLED ).equals( "true" ) );
    }

    if ( azureSqlDBClientSecretId != null ) {
      azureSqlDBClientSecretId.setValue( meta.getAttributes().getProperty( CLIENT_ID ) );
    }

    if ( azureSqlDBClientSecretKey != null ) {
      azureSqlDBClientSecretKey.setValue( meta.getAttributes().getProperty( CLIENT_SECRET_KEY ) );
    }

    if ( jdbcAuthMethod != null ) {
      jdbcAuthMethod.setValue( meta.getAttributes().getProperty( JDBC_AUTH_METHOD, STANDARD_CREDENTIALS ) );
      setAuthFieldsVisible();
    }
    if ( iamAccessKeyId != null ) {
      iamAccessKeyId.setValue( Encr.decryptPassword( meta.getAttributes().getProperty( IAM_ACCESS_KEY_ID ) ) );
    }
    if ( iamSecretKeyId != null ) {
      iamSecretKeyId.setValue( Encr.decryptPassword( meta.getAttributes().getProperty( IAM_SECRET_ACCESS_KEY ) ) );
    }
    if ( iamSessionToken != null ) {
      iamSessionToken.setValue( Encr.decryptPassword( meta.getAttributes().getProperty( IAM_SESSION_TOKEN ) ) );
    }
    if ( iamProfileName != null ) {
      iamProfileName.setValue( meta.getAttributes().getProperty( IAM_PROFILE_NAME ) );
    }

    if ( webAppName != null ) {
      // Insert default value only for new connection, allowing it to be empty in case of editing existing one
      if ( databaseMeta == null || Utils.isEmpty( databaseMeta.getDisplayName() ) ) {
        webAppName.setValue( DEFAULT_WEB_APPLICATION_NAME );
      } else {
        webAppName.setValue( meta.getDatabaseName() );
      }
    }

    this.extraHandler.ifPresent( handler -> handler.loadConnectionSpecificInfo( meta ) );
  }

  protected void getControls() {

    // Not all of these controls are created at the same time.. that's OK, for now, just check
    // each one for null before using.

    dialogDeck = (XulDeck) document.getElementById( "dialog-panel-deck" );
    deckOptionsBox = (XulListbox) document.getElementById( "deck-options-list" );
    connectionBox = (XulListbox) document.getElementById( "connection-type-list" );
    databaseDialectList = (XulMenuList) document.getElementById( "database-dialect-list" );
    accessBox = (XulListbox) document.getElementById( "access-type-list" );
    connectionNameBox = (XulTextbox) document.getElementById( "connection-name-text" );
    hostNameBox = (XulTextbox) document.getElementById( "server-host-name-text" );
    databaseNameBox = (XulTextbox) document.getElementById( "database-name-text" );
    portNumberBox = (XulTextbox) document.getElementById( "port-number-text" );
    userNameBox = (XulTextbox) document.getElementById( "username-text" );
    passwordBox = (XulTextbox) document.getElementById( "password-text" );
    dataTablespaceBox = (XulTextbox) document.getElementById( "data-tablespace-text" );
    indexTablespaceBox = (XulTextbox) document.getElementById( "index-tablespace-text" );
    serverInstanceBox = (XulTextbox) document.getElementById( "instance-text" );
    serverNameBox = (XulTextbox) document.getElementById( "server-name-text" );
    customUrlBox = (XulTextbox) document.getElementById( "custom-url-text" );
    customDriverClassBox = (XulTextbox) document.getElementById( "custom-driver-class-text" );
    languageBox = (XulTextbox) document.getElementById( "language-text" );
    warehouseBox = (XulTextbox) document.getElementById( "warehouse-text" );
    systemNumberBox = (XulTextbox) document.getElementById( "system-number-text" );
    clientBox = (XulTextbox) document.getElementById( "client-text" );
    doubleDecimalSeparatorCheck = (XulCheckbox) document.getElementById( "decimal-separator-check" );
    resultStreamingCursorCheck = (XulCheckbox) document.getElementById( "result-streaming-check" );
    webAppName = (XulTextbox) document.getElementById( "web-application-name-text" );
    poolingCheck = (XulCheckbox) document.getElementById( "use-pool-check" );
    clusteringCheck = (XulCheckbox) document.getElementById( "use-cluster-check" );
    clusterParameterDescriptionLabel = (XulLabel) document.getElementById( "cluster-parameter-description-label" );
    poolSizeLabel = (XulLabel) document.getElementById( "pool-size-label" );
    poolSizeBox = (XulTextbox) document.getElementById( "pool-size-text" );
    maxPoolSizeLabel = (XulLabel) document.getElementById( "max-pool-size-label" );
    maxPoolSizeBox = (XulTextbox) document.getElementById( "max-pool-size-text" );
    poolParameterTree = (XulTree) document.getElementById( "pool-parameter-tree" );
    clusterParameterTree = (XulTree) document.getElementById( "cluster-parameter-tree" );
    optionsParameterTree = (XulTree) document.getElementById( "options-parameter-tree" );
    poolingDescription = (XulTextbox) document.getElementById( "pooling-description" );
    poolingParameterDescriptionLabel = (XulLabel) document.getElementById( "pool-parameter-description-label" );
    poolingDescriptionLabel = (XulLabel) document.getElementById( "pooling-description-label" );
    supportBooleanDataType = (XulCheckbox) document.getElementById( "supports-boolean-data-type" );
    supportTimestampDataType = (XulCheckbox) document.getElementById( "supports-timestamp-data-type" );
    quoteIdentifiersCheck = (XulCheckbox) document.getElementById( "quote-identifiers-check" );
    lowerCaseIdentifiersCheck = (XulCheckbox) document.getElementById( "force-lower-case-check" );
    upperCaseIdentifiersCheck = (XulCheckbox) document.getElementById( "force-upper-case-check" );
    preserveReservedCaseCheck = (XulCheckbox) document.getElementById( "preserve-reserved-case" );
    strictBigNumberInterpretaion = (XulCheckbox) document.getElementById( "strict-bignum-interpretation" );
    preferredSchemaName = (XulTextbox) document.getElementById( "preferred-schema-name-text" );
    sqlBox = (XulTextbox) document.getElementById( "sql-text" );
    useIntegratedSecurityCheck = (XulCheckbox) document.getElementById( "use-integrated-security-check" );
    acceptButton = (XulButton) document.getElementById( "general-datasource-window_accept" );
    cancelButton = (XulButton) document.getElementById( "general-datasource-window_cancel" );
    testButton = (XulButton) document.getElementById( "test-button" );
    noticeLabel = (XulLabel) document.getElementById( "notice-label" );
    jdbcAuthMethod = (XulMenuList) document.getElementById( "redshift-auth-method-list" );
    iamAccessKeyId = (XulTextbox) document.getElementById( "iam-access-key-id" );
    iamSecretKeyId = (XulTextbox) document.getElementById( "iam-secret-access-key" );
    iamSessionToken = (XulTextbox) document.getElementById( "iam-session-token" );
    iamProfileName = (XulTextbox) document.getElementById( "iam-profile-name" );
    namedClusterList = (XulMenuList) document.getElementById( "named-cluster-list" );
    //azure SQL DB
    azureSqlDBJdbcAuthMethod = (XulMenuList) document.getElementById( "azure-sql-db-auth-method-list" );
    azureSqlDBAlwaysEncryptionEnabled = (XulCheckbox) document.getElementById( "azure-sql-db-enable-always-encryption-on" );
    azureSqlDBClientSecretId = (XulTextbox) document.getElementById( "azure-sql-db-client-id" );
    azureSqlDBClientSecretKey = (XulTextbox) document.getElementById( "azure-sql-db-client-secret-key" );

    if ( portNumberBox != null && serverInstanceBox != null ) {
      if ( Boolean.parseBoolean( serverInstanceBox.getAttributeValue( "shouldDisablePortIfPopulated" ) ) ) {
        serverInstanceBox.addPropertyChangeListener( new PropertyChangeListener() {

          @Override
          public void propertyChange( PropertyChangeEvent evt ) {
            if ( "value".equals( evt.getPropertyName() ) ) {
              disablePortIfInstancePopulated();
            }
          }
        } );
      }
    }
  }

  public void disablePortIfInstancePopulated() {
    String serverInstance = serverInstanceBox.getValue();
    if ( serverInstance != null && serverInstance.length() > 0 ) {
      portNumberBox.setDisabled( true );
    } else {
      portNumberBox.setDisabled( false );
    }
  }

  protected void showMessage( String message, boolean scroll ) {
    try {
      XulMessageBox box = (XulMessageBox) document.createElement( "messagebox" );
      box.setMessage( message );
      box.setModalParent( ( (XulRoot) document.getElementById( "general-datasource-window" ) ).getRootObject() );
      if ( scroll ) {
        box.setScrollable( true );
        box.setWidth( 500 );
        box.setHeight( 400 );
      }
      box.open();
    } catch ( XulException e ) {
      System.out.println( "Error creating messagebox " + e.getMessage() );
    }
  }

  protected void showMessage( DatabaseTestResults databaseTestResults ) {
    // BACKLOG-23781 - provide a showMessage implementation for
    // those that don't override it (PRD case)
    String message = databaseTestResults.getMessage();
    showMessage( message, message.length() > 300 );
  }

  public void handleUseSecurityCheckbox() {
    if ( useIntegratedSecurityCheck != null ) {
      if ( useIntegratedSecurityCheck.isChecked() ) {
        userNameBox.setDisabled( true );
        passwordBox.setDisabled( true );
      } else {
        userNameBox.setDisabled( false );
        passwordBox.setDisabled( false );
      }
    }
  }

  protected abstract static class DatabaseTypeListener implements PluginTypeListener {
    private final PluginRegistry registry;

    public DatabaseTypeListener( PluginRegistry registry ) {
      this.registry = registry;
    }

    @Override
    public void pluginAdded( Object serviceObject ) {
      PluginInterface plugin = (PluginInterface) serviceObject;
      String pluginName = plugin.getName();
      try {
        DatabaseInterface databaseInterface = (DatabaseInterface) registry.loadClass( plugin );
        databaseInterface.setPluginId( plugin.getIds()[ 0 ] );
        databaseInterface.setName( pluginName );
        databaseTypeAdded( pluginName, databaseInterface );
      } catch ( KettleException e ) {
        Throwable t = e;
        if ( e.getCause() != null ) {
          t = e.getCause();
        }
        System.out.println( "Could not create connection entry for "
          + pluginName + ".  " + t.getClass().getName() );
        LogChannel.GENERAL.logError( "Could not create connection entry for "
          + pluginName + ".  " + t.getClass().getName() );
      }
    }

    public abstract void databaseTypeAdded( String pluginName, DatabaseInterface databaseInterface );

    @Override
    public void pluginRemoved( Object serviceObject ) {
      PluginInterface plugin = (PluginInterface) serviceObject;
      String pluginName = plugin.getName();
      databaseTypeRemoved( pluginName );
    }

    public abstract void databaseTypeRemoved( String pluginName );

    @Override
    public void pluginChanged( Object serviceObject ) {
      pluginRemoved( serviceObject );
      pluginAdded( serviceObject );
    }

  }
}
