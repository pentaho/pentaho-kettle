/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.di.trans.steps.cassandrainput;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * Class providing an input step for reading data from an Cassandra 
 * column family (table).
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
@Step(id = "CassandraInput", image = "HB.png", name = "Cassandra Input", description="Reads data from a Cassandra table", categoryDescription="Cassandra")
public class CassandraInputMeta extends BaseStepMeta implements StepMetaInterface {
  
  /** The host to contact */
  protected String m_cassandraHost = "localhost";
  
  /** The port that cassandra is listening on */
  protected String m_cassandraPort = "9160";
  
  /** The keyspace (database) to use */
  protected String m_cassandraKeyspace;
  
  /** Whether to use GZIP compression of CQL queries */
  protected boolean m_useCompression;
  
  /** The select query to execute */
  protected String m_cqlSelectQuery = "SELECT <fields> FROM <column family> WHERE <condition>;";
  
  /**
   * Set the cassandra node hostname to connect to
   * 
   * @param host the host to connect to
   */
  public void setCassandraHost(String host) {
    m_cassandraHost = host;
  }
  
  /**
   * Get the name of the cassandra node to connect to
   * 
   * @return the name of the cassandra node to connect to
   */
  public String getCassandraHost() {
    return m_cassandraHost;
  }
  
  /**
   * Set the port that cassandra is listening on
   * 
   * @param port the port that cassandra is listening on
   */
  public void setCassandraPort(String port) {
    m_cassandraPort = port;
  }
  
  /**
   * Get the port that cassandra is listening on
   * 
   * @return the port that cassandra is listening on
   */
  public String getCassandraPort() {
    return m_cassandraPort;
  }
  
  /**
   * Set the keyspace (db) to use
   * 
   * @param keyspace the keyspace to use
   */
  public void setCassandraKeyspace(String keyspace) {
    m_cassandraKeyspace = keyspace;
  }
  
  /**
   * Get the keyspace (db) to use
   * 
   * @return the keyspace (db) to use
   */
  public String getCassandraKeyspace() {
    return m_cassandraKeyspace;
  }
  
  /**
   * Set whether to compress (GZIP) CQL queries when transmitting them
   * to the server
   * 
   * @param c true if CQL queries are to be compressed
   */
  public void setUseCompression(boolean c) {
    m_useCompression = c;
  }
  
  /**
   * Get whether CQL queries will be compressed (GZIP) or not
   * 
   * @return true if CQL queries will be compressed when sending to the server
   */
  public boolean getUseCompression() {
    return m_useCompression;
  }
  
  /**
   * Set the CQL SELECT query to execute.
   * 
   * @param query the query to execute
   */
  public void setCQLSelectQuery(String query) {
    m_cqlSelectQuery = query;
  }
  
  /**
   * Get the CQL SELECT query to execute
   * 
   * @return the query to execute
   */
  public String getCQLSelectQuery() {
    return m_cqlSelectQuery;
  }
  
  public String getXML() {
    StringBuffer retval = new StringBuffer();
    
    if (!Const.isEmpty(m_cassandraHost)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cassandra_host", 
          m_cassandraHost));
    }
    
    if (!Const.isEmpty(m_cassandraPort)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cassandra_port", 
          m_cassandraPort));
    }
    
    if (!Const.isEmpty(m_cassandraKeyspace)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cassandra_keyspace", 
          m_cassandraKeyspace));
    }
    
    retval.append("\n    ").append(XMLHandler.addTagValue("use_compression", 
        m_useCompression));
    
    if (!Const.isEmpty(m_cqlSelectQuery)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cql_select_query", 
          m_cqlSelectQuery));
    }
            
    return retval.toString();
  }
  
  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    m_cassandraHost = XMLHandler.getTagValue(stepnode, "cassandra_host");
    m_cassandraPort = XMLHandler.getTagValue(stepnode, "cassandra_port");
    m_cassandraKeyspace = XMLHandler.getTagValue(stepnode, "cassandra_keyspace");
    m_cqlSelectQuery = XMLHandler.getTagValue(stepnode, "cql_select_query");
    m_useCompression = XMLHandler.getTagValue(stepnode, "use_compression").
      equalsIgnoreCase("Y");
  }
  
  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleException {
    m_cassandraHost = rep.getStepAttributeString(id_step, 0, "cassandra_host");
    m_cassandraPort = rep.getStepAttributeString(id_step, 0, "cassandra_port");
    m_cassandraKeyspace = rep.getStepAttributeString(id_step, 0, "cassandra_keyspace");
    m_cqlSelectQuery = rep.getStepAttributeString(id_step, 0, "cql_select_query");
    m_useCompression = rep.getStepAttributeString(id_step, 0, "use_compression").
      equalsIgnoreCase("Y");
  }

  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
      throws KettleException {
    if (!Const.isEmpty(m_cassandraHost)) {
      rep.saveStepAttribute(id_transformation, id_step, "cassandra_host",
          m_cassandraHost);
    }
    
    if (!Const.isEmpty(m_cassandraPort)) {
      rep.saveStepAttribute(id_transformation, id_step, "cassandra_port",
          m_cassandraPort);
    }
    
    if (!Const.isEmpty(m_cassandraKeyspace)) {
      rep.saveStepAttribute(id_transformation, id_step, "cassandra_keyspace",
          m_cassandraKeyspace);
    }
    
    rep.saveStepAttribute(id_transformation, id_step, "use_compression",
        m_useCompression);
    
    if (!Const.isEmpty(m_cqlSelectQuery)) {
      rep.saveStepAttribute(id_transformation, id_step, "cql_select_query",
          m_cqlSelectQuery);
    }
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output,
      RowMetaInterface info) {
    // TODO Auto-generated method stub
    
  }

  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {
    
    return new CassandraInput(stepMeta, stepDataInterface, copyNr,
        transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new CassandraInputData();
  }

  public void setDefault() {
    m_cassandraHost = "localhost";
    m_cassandraPort = "9160";
    m_cqlSelectQuery = "SELECT <fields> FROM <column family> WHERE <condition>;";
    m_useCompression = false;
  }
  
  public void getFields(RowMetaInterface rowMeta, String origin, 
      RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) 
    throws KettleStepException {
    
    rowMeta.clear(); // start afresh - eats the input
    
    if (Const.isEmpty(m_cassandraKeyspace)) {
      // no keyspace!
      return;
    }
    
    String colFamName = null;
    if (!Const.isEmpty(m_cqlSelectQuery)) {            
      String subQ = space.environmentSubstitute(m_cqlSelectQuery);
      
      if (!subQ.toLowerCase().startsWith("select")) {
        // not a select statement!
        logError("No 'SELECT' in query!");
        return;
      }
      
      if (subQ.indexOf(';') < 0) {
        // query must end with a ';' or it will wait for more!
        logError("Query must be terminated by a ';'");
        return;
      }
      
      //subQ = subQ.toLowerCase();
      
      // strip off where clause (if any)      
      if (subQ.toLowerCase().lastIndexOf("where") > 0) {
        subQ = subQ.substring(0, subQ.toLowerCase().lastIndexOf("where"));
      }
      
      // first determine the source column family
      // look for a FROM that is surrounded by space
      int fromIndex = subQ.toLowerCase().indexOf("from");
      String tempS = subQ.toLowerCase();
      int offset = fromIndex;
      while (fromIndex > 0 && tempS.charAt(fromIndex - 1) != ' ' && 
          (fromIndex + 4 < tempS.length()) && tempS.charAt(fromIndex + 4) != ' ') {
        tempS = tempS.substring(fromIndex + 4, tempS.length());
        fromIndex = tempS.indexOf("from");
        offset += (4 + fromIndex);
      }
      
      fromIndex = offset;
//      int fromIndex = subQ.toLowerCase().lastIndexOf("from");
      if (fromIndex < 0) {
        logError("Must specify a column family using a 'FROM' clause");
        return; // no from clause
      }
      
      colFamName = subQ.substring(fromIndex + 4, subQ.length()).trim();
      if (colFamName.indexOf(' ') > 0) {
        colFamName = colFamName.substring(0, colFamName.indexOf(' '));
      } else {
        colFamName = colFamName.replace(";", "");
      }
      
      if (colFamName.length() == 0) {
        return; // no column family specified
      }      
      
      // now determine if its a select * or specific set of columns
      String[] cols = null;
      if (subQ.indexOf("*") > 0) {
        // 
      } else {
        String colsS = subQ.substring(7, fromIndex);
        cols = colsS.split(",");
      }
      
      // try and connect to get meta data
      String hostS = space.environmentSubstitute(m_cassandraHost);
      String portS = space.environmentSubstitute(m_cassandraPort);
      String keyspaceS = space.environmentSubstitute(m_cassandraKeyspace);
      CassandraConnection conn = null;
      try {
        conn = CassandraInputData.getCassandraConnection(hostS, 
            Integer.parseInt(portS));
        conn.setKeyspace(keyspaceS);        
      } catch (Exception ex) {
        logError(ex.getMessage());
        return;
      }
      try {
        CassandraColumnMetaData colMeta = new CassandraColumnMetaData(conn, colFamName);
        
        // Do the key first
        ValueMetaInterface km = colMeta.getValueMetaForKey();
        rowMeta.addValueMeta(km);
        
        if (cols == null) {
          // select * - use all the columns that are defined in the schema
          List<ValueMetaInterface> vms = colMeta.getValueMetasForSchema();
          for (ValueMetaInterface vm : vms) {
            rowMeta.addValueMeta(vm);
          }
        } else {
          // do the individual columns
          for (String col : cols) {
            col = col.trim();
            col = col.replace("'", "");
            col = col.replace("\"", "");
            if (!colMeta.columnExistsInSchema(col)) {
              // this one isn't known about in about in the schema - we can output it
              // as long as its values satisfy the default validator...
              logBasic("Query specifies column '" + col + "', however this column is " +
                        "not in the column family schema. The default column family " +
                        "validator will be used");
            }
            ValueMetaInterface vm = colMeta.getValueMetaForColumn(col);
            rowMeta.addValueMeta(vm);
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        logBasic("Unable to connect to retrieve column meta data for '" + colFamName 
            + "' at this stage.");
        return;
      } finally {
        if (conn != null) {
          conn.close();
        }
      }            
    }
  }

  /**
   * Get the UI for this step.
   *
   * @param shell a <code>Shell</code> value
   * @param meta a <code>StepMetaInterface</code> value
   * @param transMeta a <code>TransMeta</code> value
   * @param name a <code>String</code> value
   * @return a <code>StepDialogInterface</code> value
   */
  public StepDialogInterface getDialog(Shell shell, 
                                       StepMetaInterface meta,
                                       TransMeta transMeta, 
                                       String name) {

    return new CassandraInputDialog(shell, meta, transMeta, name);
  }
}
