/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 * 
 * Author: Ezequiel Cuellar
 */
package org.pentaho.di.profiling.datacleaner;

import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.bootstrap.Bootstrap;
import org.eobjects.datacleaner.bootstrap.BootstrapOptions;
import org.eobjects.datacleaner.bootstrap.ExitActionListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.ui.core.database.dialog.XulDatabaseExplorerController;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class ProfilerDatabaseExplorerController extends AbstractXulEventHandler {

	private XulDatabaseExplorerController dbExplorerController;

	public ProfilerDatabaseExplorerController() {
	}

	public String getName() {
		return "profiler_database"; //$NON-NLS-1$
	}
	
  public void profileDbTable() {
    getDbController();
    
    // Close the db explorer...
    //
    this.dbExplorerController.close();
    
    final DatabaseMeta dbMeta = this.dbExplorerController.getDatabaseMeta();
    final String tableName= this.dbExplorerController.getSelectedTable();
    final String schemaName = this.dbExplorerController.getSelectedSchema();
    
    // TODO: profile the table...
    //
    System.out.println("Database: "+dbMeta.getName());
    System.out.println("Table: "+dbMeta.getQuotedSchemaTableCombination(schemaName, tableName));
    System.out.println("Database: "+dbMeta.getName());
    
    BootstrapOptions bootstrapOptions = new BootstrapOptions() {

      public Datastore getSingleDatastore(DatastoreCatalog catalog) {
        try {
          return new JdbcDatastore(dbMeta.getName(), 
              dbMeta.getURL(), 
              dbMeta.getDriverClass(), 
              dbMeta.getUsername(), 
              dbMeta.getPassword()
            );
        } catch (KettleDatabaseException e) {
          throw new RuntimeException(e);
        }
      };

      @Override
      public boolean isSingleDatastoreMode() {
        return true;
      }

      @Override
      public boolean isCommandLineMode() {
        return false;
      }

      @Override
      public ExitActionListener getExitActionListener() {
        return null;
      }

      @Override
      public CliArguments getCommandLineArguments() {
        // TODO Auto-generated method stub
        return null;
      }
    };
    Bootstrap bootstrap = new Bootstrap(bootstrapOptions);
    bootstrap.run();

  }
  

	private XulDatabaseExplorerController getDbController() {
	  if (dbExplorerController == null) {
	    try {
        dbExplorerController = (XulDatabaseExplorerController) this.getXulDomContainer().getEventHandler("dbexplorer");
      } catch (XulException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	  }
	  return dbExplorerController;
	}
	
	public void setData(Object aDatabaseDialog) {
		this.dbExplorerController = (XulDatabaseExplorerController) aDatabaseDialog;
	}
}
