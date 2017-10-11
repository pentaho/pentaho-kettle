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

package org.pentaho.di.trans.steps.tableagilemart;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.ProvidesDatabaseConnectionInformation;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.metadata.registry.Entity;
import org.pentaho.metadata.registry.IMetadataRegistry;
import org.pentaho.metadata.registry.OrderedFileRegistry;
import org.pentaho.metadata.registry.RegistryFactory;
import org.pentaho.metadata.registry.util.RegistryUtil;

public class AgileMartUtil {

  private LogChannelInterface log;

  protected void setLog( LogChannelInterface value ) {
    this.log = value;
  }

  public void updateMetadata( ProvidesDatabaseConnectionInformation dpci, long rowCount ) {
    // try to update the metadata registry

    RegistryFactory factory = RegistryFactory.getInstance();
    IMetadataRegistry registry = factory.getMetadataRegistry();

    // PDI-8908 - NPE in MonetDB Bulk Loader step
    if ( registry == null ) {
      try {
        registry = new OrderedFileRegistry();
        ( (OrderedFileRegistry) registry ).setFilePath( org.pentaho.di.core.Const.getKettleDirectory()
          + File.separator + "registry.xml" );
        factory.setMetadataRegistry( registry );
        registry.init();
      } catch ( Exception e ) {
        e.printStackTrace();
      }
    }

    RegistryUtil util = new RegistryUtil();
    String databaseName = dpci.getDatabaseMeta().getName();
    String schemaName = dpci.getSchemaName();
    String tableName = dpci.getTableName();
    Entity entity =
      util.getTableEntity(
        databaseName.toLowerCase(), ( schemaName == null ) ? null : schemaName.toLowerCase(), tableName
          .toLowerCase(), false );
    if ( entity != null ) {
      if ( ( log != null ) && log.isDebug() ) {
        log.logDebug( "Util.updateMetadata writing " + util.generateTableId( dpci.getDatabaseMeta().getName(), dpci.getSchemaName(), dpci.getTableName() ) + " rowCount=" + rowCount );
      }
      if ( rowCount == -1 ) {
        // the table has been emptied
        util.setAttribute( entity, "rowcount", 0 );
      } else {
        // add an offset
        util.updateAttribute( entity, "rowcount", rowCount );
      }
      DateFormat fmt = new SimpleDateFormat();
      Date now = new Date();
      util.setAttribute( entity, "lastupdate", fmt.format( now ) );
      util.setAttribute( entity, "lastupdatetick", now.getTime() );
    } else {
      if ( ( log != null ) && log.isDebug() ) {
        log.logDebug( "Util.updateMetadata failed writing " + util.generateTableId( dpci.getDatabaseMeta().getName(), dpci.getSchemaName(), dpci.getTableName() ) );
      }
    }
    try {
      registry.commit();
    } catch ( Exception e ) {
      // no biggie
    }
  }

}
