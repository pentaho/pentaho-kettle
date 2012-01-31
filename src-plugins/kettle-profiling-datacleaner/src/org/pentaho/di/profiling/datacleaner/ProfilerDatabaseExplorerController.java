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

import java.awt.Image;
import java.util.List;

import org.eobjects.analyzer.beans.BooleanAnalyzer;
import org.eobjects.analyzer.beans.DateAndTimeAnalyzer;
import org.eobjects.analyzer.beans.NumberAnalyzer;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.DataTypeFamily;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.bootstrap.Bootstrap;
import org.eobjects.datacleaner.bootstrap.BootstrapOptions;
import org.eobjects.datacleaner.bootstrap.ExitActionListener;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.schema.Column;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.ui.core.database.dialog.XulDatabaseExplorerController;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class ProfilerDatabaseExplorerController extends AbstractXulEventHandler {

  private XulDatabaseExplorerController dbExplorerController;

  public ProfilerDatabaseExplorerController() {
  }

  public String getName() {
    return "profiler_database"; //$NON-NLS-1$
  }

  public void profileDbTable() throws Exception {

    final Spoon spoon = ((Spoon) SpoonFactory.getInstance());

    try {

      getDbController();
      // Close the db explorer...
      //
      dbExplorerController.close();

      final DatabaseMeta dbMeta = dbExplorerController.getDatabaseMeta();
      final String tableName = dbExplorerController.getSelectedTable();
      final String schemaName = dbExplorerController.getSelectedSchema();

      // TODO: profile the table...
      //
      final String schemaTable = dbMeta.getQuotedSchemaTableCombination(schemaName, tableName);

      final BootstrapOptions bootstrapOptions = new BootstrapOptions() {

        public Datastore getSingleDatastore(DatastoreCatalog catalog) {
          try {
            return new JdbcDatastore(dbMeta.getName(), dbMeta.getURL(), dbMeta.getDriverClass(), dbMeta.getUsername(), dbMeta.getPassword(), false);
          } catch (KettleDatabaseException e) {
            throw new RuntimeException(e);
          }
        };

        public Image getWelcomeImage() {
          return null;
        }

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

        @Override
        public void initializeSingleDatastoreJob(AnalysisJobBuilder analysisJobBuilder, DataContext dataContext) {

          // add all columns of a table
          Column[] customerColumns = dataContext.getTableByQualifiedLabel(schemaTable).getColumns();
          analysisJobBuilder.addSourceColumns(customerColumns);

          List<InputColumn<?>> numberColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.NUMBER);
          if (!numberColumns.isEmpty()) {
            analysisJobBuilder.addAnalyzer(NumberAnalyzer.class).addInputColumns(numberColumns);
          }

          List<InputColumn<?>> dateColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.DATE);
          if (!dateColumns.isEmpty()) {
            analysisJobBuilder.addAnalyzer(DateAndTimeAnalyzer.class).addInputColumns(dateColumns);
          }

          List<InputColumn<?>> booleanColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.BOOLEAN);
          if (!booleanColumns.isEmpty()) {
            analysisJobBuilder.addAnalyzer(BooleanAnalyzer.class).addInputColumns(booleanColumns);
          }

          List<InputColumn<?>> stringColumns = analysisJobBuilder.getAvailableInputColumns(DataTypeFamily.STRING);
          if (!stringColumns.isEmpty()) {
            analysisJobBuilder.addAnalyzer(StringAnalyzer.class).addInputColumns(stringColumns);
          }
        }
      };

      Bootstrap bootstrap = new Bootstrap(bootstrapOptions);
      bootstrap.run();

    } catch (final Exception ex) {
      new ErrorDialog(spoon.getShell(), "Error", "unexpected error occurred", ex);
    } finally {
      //
    }

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
