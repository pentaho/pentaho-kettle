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

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.eobjects.analyzer.beans.BooleanAnalyzer;
import org.eobjects.analyzer.beans.DateAndTimeAnalyzer;
import org.eobjects.analyzer.beans.NumberAnalyzer;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.JaxbJobWriter;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
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
      final String schemaTable = dbMeta.getQuotedSchemaTableCombination(schemaName, tableName);
      

      // Pass along the configuration of the KettleDatabaseStore...
      //
      AnalyzerBeansConfiguration abc = new AnalyzerBeansConfigurationImpl();
      AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(abc);
      Datastore datastore = new JdbcDatastore(dbMeta.getName(), dbMeta.getURL(), dbMeta.getDriverClass(), dbMeta.getUsername(), dbMeta.getPassword(), false);
      analysisJobBuilder.setDatastore(datastore);
      DatastoreConnection connection  = null;
      
      try {
        connection = datastore.openConnection();
        DataContext dataContext = connection.getDataContext();
        
        // add all columns of a table
        Table table = dataContext.getTableByQualifiedLabel(schemaTable);
        if (table == null) {
            Schema schema = dataContext.getSchemaByName(schemaName);
            if (schema != null) {
                table = schema.getTableByName(tableName);
            }
        }
        
        final FileObject jobFile;
        if (table == null) {
            // Could not resolve table, this sometimes happens
            jobFile = null;
        } else {
            Column[] customerColumns = table.getColumns();
            analysisJobBuilder.addSourceColumns(customerColumns);
            
            List<InputColumn<?>> numberColumns = analysisJobBuilder.getAvailableInputColumns(Number.class);
            if (!numberColumns.isEmpty()) {
                analysisJobBuilder.addAnalyzer(NumberAnalyzer.class).addInputColumns(numberColumns);
            }
            
            List<InputColumn<?>> dateColumns = analysisJobBuilder.getAvailableInputColumns(Date.class);
            if (!dateColumns.isEmpty()) {
                analysisJobBuilder.addAnalyzer(DateAndTimeAnalyzer.class).addInputColumns(dateColumns);
            }
            
            List<InputColumn<?>> booleanColumns = analysisJobBuilder.getAvailableInputColumns(Boolean.class);
            if (!booleanColumns.isEmpty()) {
                analysisJobBuilder.addAnalyzer(BooleanAnalyzer.class).addInputColumns(booleanColumns);
            }
            
            List<InputColumn<?>> stringColumns = analysisJobBuilder.getAvailableInputColumns(String.class);
            if (!stringColumns.isEmpty()) {
                analysisJobBuilder.addAnalyzer(StringAnalyzer.class).addInputColumns(stringColumns);
            }
            
            // Write the job.xml to a temporary file...
            jobFile = KettleVFS.createTempFile("datacleaner-job", ".xml", System.getProperty("java.io.tmpdir"), new Variables());
            OutputStream jobOutputStream = null;
            try {
                jobOutputStream = KettleVFS.getOutputStream(jobFile, false);
                new JaxbJobWriter(abc).write(analysisJobBuilder.toAnalysisJob(), jobOutputStream);
                jobOutputStream.close();
            } finally {
                if (jobOutputStream!=null) {
                    jobOutputStream.close();
                }
            }
        }
        

        
        // Write the conf.xml to a temporary file...
        //
        String confXml = generateConfXml(dbMeta.getName(), dbMeta.getURL(), dbMeta.getDriverClass(), dbMeta.getUsername(), dbMeta.getPassword());
        final FileObject confFile = KettleVFS.createTempFile("datacleaner-conf", ".xml", System.getProperty("java.io.tmpdir"), new Variables());
        OutputStream confOutputStream  = null;
        try {
          confOutputStream = KettleVFS.getOutputStream(confFile, false);
          confOutputStream.write(confXml.getBytes(Const.XML_ENCODING));
          confOutputStream.close();
        } finally {
          if (confOutputStream!=null) {
            confOutputStream.close();
          }
        }

        // Launch DataCleaner and point to the generated configuration and job XML files...
        //
        
        // Launch DataCleaner and point to the generated configuration and job XML files...
        //
        Spoon.getInstance().getDisplay().syncExec(new Runnable() {          
          public void run() {
            new Thread() {
              public void run() {
                final String jobFileName;
                if (jobFile == null) {
                    jobFileName = null;
                } else {
                    jobFileName = KettleVFS.getFilename(jobFile);
                }
                ModelerHelper.launchDataCleaner(KettleVFS.getFilename(confFile), jobFileName, dbMeta.getName(), null);
              }
            }.start();
          }
        });


      } finally {
        if (connection!=null) {
          connection.close();
        }
      }

    } catch (final Exception ex) {
      new ErrorDialog(spoon.getShell(), "Error", "unexpected error occurred", ex);
    }

  }

  private String generateConfXml(String name, String url, String driver, String username, String password) {
    StringBuilder xml = new StringBuilder();
    
    xml.append(XMLHandler.getXMLHeader());
    xml.append("<configuration xmlns=\"http://eobjects.org/analyzerbeans/configuration/1.0\"");
    xml.append("   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">").append(Const.CR);
    xml.append(XMLHandler.openTag("datastore-catalog"));
    
    /*
            <jdbc-datastore name="my_jdbc_connection" description="jdbc_con">
              <url>jdbc:hsqldb:file:../../clie/examples/orderdb;readonly=true</url>
              <driver>org.hsqldb.jdbcDriver</driver>
              <username>SA</username>
              <password></password>
            </jdbc-datastore>
     */
    
    xml.append("<jdbc-datastore name=\""+name+"\" description=\"Database defined in Pentaho Data Integration\">").append(Const.CR);
    xml.append(XMLHandler.addTagValue("url", url));
    xml.append(XMLHandler.addTagValue("driver", driver));
    xml.append(XMLHandler.addTagValue("username", username));
    xml.append(XMLHandler.addTagValue("password", password));
        
    xml.append(XMLHandler.closeTag("jdbc-datastore"));
    xml.append(XMLHandler.closeTag("datastore-catalog"));
    
    xml.append("<multithreaded-taskrunner max-threads=\"30\" />");
    xml.append(XMLHandler.openTag("classpath-scanner"));
    xml.append("<package recursive=\"true\">org.eobjects.analyzer.beans</package> <package>org.eobjects.analyzer.result.renderer</package> <package>org.eobjects.datacleaner.output.beans</package> <package>org.eobjects.datacleaner.panels</package> <package recursive=\"true\">org.eobjects.datacleaner.widgets.result</package> <package recursive=\"true\">com.hi</package>");
    xml.append(XMLHandler.closeTag("classpath-scanner"));
    
    xml.append(XMLHandler.closeTag("configuration"));
    
    return xml.toString();
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
