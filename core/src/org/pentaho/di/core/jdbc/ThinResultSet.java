package org.pentaho.di.core.jdbc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.cluster.SlaveConnectionManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.www.WebResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ThinResultSet implements ResultSet {
  
  private ThinStatement statement;
  private ThinConnection connection;
  
  private DataInputStream dataInputStream;
  private RowMetaInterface rowMeta;
  private Object[] currentRow;
  private int rowNumber;
  private boolean lastNull;

  private String serviceName;

  private GetMethod method;

  private String serviceTransName;

  private String serviceObjectId;

  private String sqlTransName;

  private String sqlObjectId;
  private AtomicBoolean stopped;
  
  public ThinResultSet(ThinStatement statement, String urlString, String username, String password, String sql) throws SQLException {
    this.statement = statement;
    this.connection = (ThinConnection) statement.getConnection();
    
    rowNumber = 0;
    stopped = new AtomicBoolean(false);
    
    try {
      
      HttpClient client = null;
      
      try {
        client = SlaveConnectionManager.getInstance().createHttpClient();
        
        client.getHttpConnectionManager().getParams().setConnectionTimeout(0);
        client.getHttpConnectionManager().getParams().setSoTimeout(0);
        
        HttpUtil.addCredentials(client, new Variables(), connection.getHostname(), connection.getPort(), connection.getWebAppName(), connection.getUsername(), connection.getPassword());
        HttpUtil.addProxy(client, new Variables(), connection.getHostname(), connection.getProxyHostname(), connection.getProxyPort(), connection.getNonProxyHosts());
        
        method = new GetMethod(urlString);

        method.setDoAuthentication(true);
        method.addRequestHeader(new Header("Content-Type", "binary/jdbc"));
        method.addRequestHeader(new Header("SQL", ThinUtil.stripNewlines(sql)));
        method.addRequestHeader(new Header("MaxRows", Integer.toString(statement.getMaxRows())));
        method.getParams().setParameter("http.socket.timeout", new Integer(0));
        
        for (String arg : connection.getArguments().keySet()) {
          String value = connection.getArguments().get(arg);
          method.addRequestHeader(new Header(arg, value));
        }
        
        int result = client.executeMethod(method);
        
        if (result==500) {
          String response = getErrorString(method.getResponseBodyAsStream());
          throw new KettleException("Error 500 reading data from slave server: "+response);
        } 
        if (result==401) {
          String response = getErrorString(method.getResponseBodyAsStream());
          throw new KettleException("Access denied error 401 received while attempting to read data from server: "+response);
        }
        if (result!=200) {
          String response = getErrorString(method.getResponseBodyAsStream());
          throw new KettleException("Error received while attempting to read data from server: "+response);
        }
        
        dataInputStream = new DataInputStream(method.getResponseBodyAsStream());
         
        // Read the name of the service we're reading from
        //
        serviceName = dataInputStream.readUTF();

        // Get some information about what's going on on the slave server
        //
        serviceTransName = dataInputStream.readUTF();
        serviceObjectId =  dataInputStream.readUTF();
        sqlTransName = dataInputStream.readUTF();
        sqlObjectId =  dataInputStream.readUTF();
        
        // Get the row metadata...
        //
        rowMeta = new RowMeta(dataInputStream);
      } catch(KettleEOFException eof) {
         close();
      }
    } catch(Exception e) {
      throw new SQLException("Unable to get open query for SQL: "+sql+Const.CR+Const.getStackTracker(e), e);
    } 
  }

  public synchronized void cancel() throws SQLException {
    
    // Kill the service transformation on the server...
    // Only ever try once.
    //
    if (!stopped.get()) {
      stopped.set(true);
      try {
        String reply = HttpUtil.execService(new Variables(), 
            connection.getHostname(), connection.getPort(), connection.getWebAppName(), 
            connection.getService()+"/stopTrans"+"/?name="+URLEncoder.encode(serviceTransName, "UTF-8")+"&id="+Const.NVL(serviceObjectId, "")+"&xml=Y",
            connection.getUsername(), connection.getPassword(), 
            connection.getProxyHostname(), connection.getProxyPort(), connection.getNonProxyHosts());
        
        WebResult webResult = new WebResult(XMLHandler.loadXMLString(reply, WebResult.XML_TAG));
        if (!"OK".equals(webResult.getResult())) {
          throw new SQLException("Cancel on remote server failed: "+webResult.getMessage());
        }
        
      } catch(Exception e) {
        throw new SQLException("Couldn't cancel SQL query on slave server", e);
      }
    }
  }

  private String getErrorString(InputStream inputStream) throws IOException {
    StringBuffer bodyBuffer = new StringBuffer();
    int c;
    while ( (c=inputStream.read())!=-1) bodyBuffer.append((char)c);
    return bodyBuffer.toString();
    
  }

  @Override
  public boolean absolute(int rowNr) throws SQLException {
    if (rowNumber!=rowNr) {
      throw new SQLException("Scrolleable resultsets are not supported");
    }
    return true;
  }

  @Override
  public void afterLast() throws SQLException {
    throw new SQLException("Scrolleable resultsets are not supported");
  }

  @Override
  public void beforeFirst() throws SQLException {
    throw new SQLException("Scrolleable resultsets are not supported");
  }

  @Override
  public void cancelRowUpdates() throws SQLException {
    throw new SQLException("Scrolleable resultsets are not supported");
  }

  @Override
  public void clearWarnings() throws SQLException {
  }
  
  private void checkTransStatus(String transformationName, String transformationObjectId) throws SQLException {
    try {
      String xml = HttpUtil.execService(new Variables(), 
          connection.getHostname(), connection.getPort(), connection.getWebAppName(), 
          connection.getService()+"/transStatus/?name="+URLEncoder.encode(transformationName, "UTF-8")+"&id="+Const.NVL(transformationObjectId, "")+"&xml=Y", 
          connection.getUsername(), connection.getPassword(), connection.getProxyHostname(), connection.getProxyPort(), connection.getNonProxyHosts()
        );
      Document doc = XMLHandler.loadXMLString(xml);
      Node resultNode = XMLHandler.getSubNode(doc, "transstatus", "result");
      Result result = new Result(resultNode);
      String loggingString64 = XMLHandler.getNodeValue(XMLHandler.getSubNode(doc, "transstatus", "logging_string"));
      String log = HttpUtil.decodeBase64ZippedString(loggingString64);

      // Check for errors
      //
      if (!result.getResult() || result.getNrErrors()>0) {
        throw new KettleException("The SQL query transformation failed with the following log text:"+Const.CR+log);
      }

      // See if the transformation was stopped remotely
      //
      boolean stopped = "Stopped".equalsIgnoreCase(XMLHandler.getTagValue(doc, "transstatus", "status_desc"));
      if (stopped) {
        throw new KettleException("The SQL query transformation was stopped.  Logging text: "+Const.CR+log);
      }

      // All OK, only log the remote logging text if requested.
      //
      if (connection.isDebuggingRemoteLog()) {
        LogChannel.GENERAL.logBasic(log); 
      }
      
    } catch(Exception e) {
      throw new SQLException("Couldn't validate correct execution of SQL query for transformation ["+transformationName+"]", e);
    }

  }

  @Override
  public void close() throws SQLException {
    
    // Before we close this connection, let's verify if we got all records...
    //
    checkTransStatus(sqlTransName, sqlObjectId);
    
    currentRow = null;
    dataInputStream = null;
    if (method!=null) {
      method.releaseConnection();
    }
  }

  @Override
  public void deleteRow() throws SQLException {
    throw new SQLException("Scrolleable resultsets are not supported");
  }

  @Override
  public int findColumn(String column) throws SQLException {
    return rowMeta.indexOfValue(column)+1;
  }

  @Override
  public boolean first() throws SQLException {
    if (rowNumber!=0) {
      throw new SQLException("Scrolleable resultsets are not supported");
    }
    return true;
  }

  @Override
  public int getConcurrency() throws SQLException {
    return ResultSet.CONCUR_READ_ONLY;
  }

  @Override
  public String getCursorName() throws SQLException {
    return serviceName;
  }


  @Override
  public int getFetchDirection() throws SQLException {
    return ResultSet.FETCH_FORWARD;
  }

  @Override
  public int getFetchSize() throws SQLException {
    return 1;
  }

  @Override
  public int getHoldability() throws SQLException {
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return new ThinResultSetMetaData(serviceName, rowMeta);
  }

  @Override
  public int getRow() throws SQLException {
    return rowNumber;
  }

  @Override
  public Statement getStatement() throws SQLException {
    return statement;
  }

  @Override
  public int getType() throws SQLException {
    return ResultSet.TYPE_FORWARD_ONLY;
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return null;
  }

  @Override
  public void insertRow() throws SQLException {
    throw new SQLException("Updating resultsets are not supported");
  }

  @Override
  public boolean isAfterLast() throws SQLException {
    throw new SQLException("Scrolleable resultsets are not supported");
  }

  @Override
  public boolean isBeforeFirst() throws SQLException {
    throw new SQLException("Scrolleable resultsets are not supported");
  }

  @Override
  public boolean isClosed() throws SQLException {
    return dataInputStream==null && currentRow==null;
  }

  @Override
  public boolean isFirst() throws SQLException {
    return rowNumber==0;
  }

  @Override
  public boolean isLast() throws SQLException {
    return currentRow!=null && dataInputStream==null;
  }

  @Override
  public boolean last() throws SQLException {
    throw new SQLException("Scrolleable resultsets are not supported");
  }

  @Override
  public void moveToCurrentRow() throws SQLException {
  }

  @Override
  public void moveToInsertRow() throws SQLException {
    throw new SQLException("Scrolleable resultsets are not supported");
  }

  @Override
  public boolean next() throws SQLException {
    if (dataInputStream==null) return false;
    
    try {
      currentRow = rowMeta.readData(dataInputStream);
      return true;
    } catch(KettleEOFException e) {
      dataInputStream = null;
      return false;
    } catch(Exception e) {
      throw new SQLException(e);
    }
  }

  @Override
  public boolean previous() throws SQLException {
    throw new SQLException("Scrolleable resultsets are not supported");
  }

  @Override
  public void refreshRow() throws SQLException {
  }

  @Override
  public boolean relative(int rowNumber) throws SQLException {
    if (this.rowNumber!=rowNumber) {
      throw new SQLException("Scrolleable resultsets are not supported");
    }
    return true;
  }

  @Override
  public boolean rowDeleted() throws SQLException {
    return false;
  }

  @Override
  public boolean rowInserted() throws SQLException {
    return false;
  }

  @Override
  public boolean rowUpdated() throws SQLException {
    return false;
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
  }

  @Override
  public void setFetchSize(int direction) throws SQLException {
  }

  @Override
  public boolean wasNull() throws SQLException {
    return lastNull;
  }

  
  
  
  
  
  
  
  
  
  
  
  // Here are the getters...
  
  
  @Override
  public Date getDate(int index) throws SQLException {
    try {
      java.util.Date date = rowMeta.getDate(currentRow, index-1);
      if (date==null) {
        lastNull=true;
        return null;
      }
      lastNull=false;
      return new Date(date.getTime());
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public Date getDate(String columnName) throws SQLException {
    return getDate(rowMeta.indexOfValue(columnName));
  }

  @Override
  public Date getDate(int index, Calendar calendar) throws SQLException {
    return getDate(index);
  }

  @Override
  public Date getDate(String columnName, Calendar calendar) throws SQLException {
    return getDate(rowMeta.indexOfValue(columnName));
  }

  @Override
  public double getDouble(int index) throws SQLException {
    try {
      Double d = rowMeta.getNumber(currentRow, index-1);
      if (d==null) {
        lastNull=true;
        return 0.0;
      }
      lastNull=false;
      return d;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public double getDouble(String columnName) throws SQLException {
    return getDouble(rowMeta.indexOfValue(columnName));
  }

  @Override
  public Array getArray(int arg0) throws SQLException {
    throw new SQLException("Arrays are not supported");
  }

  @Override
  public Array getArray(String arg0) throws SQLException {
    throw new SQLException("Arrays are not supported");
  }

  @Override
  public InputStream getAsciiStream(int arg0) throws SQLException {
    throw new SQLException("ASCII streams are not supported");
  }

  @Override
  public InputStream getAsciiStream(String arg0) throws SQLException {
    throw new SQLException("ASCII streams are not supported");
  }

  @Override
  public BigDecimal getBigDecimal(int index) throws SQLException {
    try {
      BigDecimal d = rowMeta.getBigNumber(currentRow, index-1);
      if (d==null) {
        lastNull=true;
        return null;
      }
      lastNull=false;
      return d;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public BigDecimal getBigDecimal(String columnName) throws SQLException {
    return getBigDecimal(rowMeta.indexOfValue(columnName));
  }

  @Override
  public BigDecimal getBigDecimal(int index, int arg1) throws SQLException {
    return getBigDecimal(index);
  }

  @Override
  public BigDecimal getBigDecimal(String columnName, int arg1) throws SQLException {
    return getBigDecimal(rowMeta.indexOfValue(columnName));
  }

  @Override
  public InputStream getBinaryStream(int arg0) throws SQLException {
    throw new SQLException("Binary streams are not supported");
  }

  @Override
  public InputStream getBinaryStream(String arg0) throws SQLException {
    throw new SQLException("Binary streams are not supported");
  }

  @Override
  public Blob getBlob(int index) throws SQLException {
    throw new SQLException("BLOBs are not supported");
  }

  @Override
  public Blob getBlob(String arg0) throws SQLException {
    throw new SQLException("BLOBs are not supported");
  }

  @Override
  public boolean getBoolean(int index) throws SQLException {
    try {
      Boolean b = rowMeta.getBoolean(currentRow, index-1);
      if (b==null) {
        lastNull=true;
        return false;
      }
      lastNull=false;
      return b;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public boolean getBoolean(String columnName) throws SQLException {
    return getBoolean(rowMeta.indexOfValue(columnName));
  }

  @Override
  public byte getByte(int index) throws SQLException {
    long l = getLong(index);
    return (byte)l;
  }

  @Override
  public byte getByte(String columnName) throws SQLException {
    return getByte(rowMeta.indexOfValue(columnName));
  }

  @Override
  public byte[] getBytes(int index) throws SQLException {
    try {
      byte[] binary = rowMeta.getBinary(currentRow, index-1);
      if (binary==null) {
        lastNull=true;
        return null;
      }
      lastNull=false;
      return binary;
    } catch(Exception e) { 
      throw new SQLException(e);
    }  }

  @Override
  public byte[] getBytes(String columnName) throws SQLException {
    return getBytes(rowMeta.indexOfValue(columnName));
  }

  @Override
  public Reader getCharacterStream(int arg0) throws SQLException {
    throw new SQLException("Character streams are not supported");
  }

  @Override
  public Reader getCharacterStream(String arg0) throws SQLException {
    throw new SQLException("Character streams are not supported");
  }

  @Override
  public Clob getClob(int arg0) throws SQLException {
    throw new SQLException("CLOBs are not supported");
  }

  @Override
  public Clob getClob(String arg0) throws SQLException {
    throw new SQLException("CLOBs are not supported");
  }

  @Override
  public float getFloat(int index) throws SQLException {
    double d = getDouble(index);
    return (float)d;
  }

  @Override
  public float getFloat(String columnName) throws SQLException {
    double d = getDouble(columnName);
    return (float)d;
  }

  @Override
  public int getInt(int index) throws SQLException {
    long l = getLong(index);
    return (int)l;
  }

  @Override
  public int getInt(String columnName) throws SQLException {
    return getInt(rowMeta.indexOfValue(columnName));
  }

  @Override
  public long getLong(int index) throws SQLException {
    try {
      Long d = rowMeta.getInteger(currentRow, index-1);
      if (d==null) {
        lastNull=true;
        return 0;
      }
      lastNull=false;
      return d;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public long getLong(String columnName) throws SQLException {
    return getLong(rowMeta.indexOfValue(columnName));
  }

  @Override
  public Reader getNCharacterStream(int arg0) throws SQLException {
    throw new SQLException("NCharacter streams are not supported");
  }

  @Override
  public Reader getNCharacterStream(String arg0) throws SQLException {
    throw new SQLException("NCharacter streams are not supported");
  }

  @Override
  public NClob getNClob(int arg0) throws SQLException {
    throw new SQLException("NCLOBs are not supported");
  }

  @Override
  public NClob getNClob(String arg0) throws SQLException {
    throw new SQLException("NCLOBs are not supported");
  }

  @Override
  public String getNString(int arg0) throws SQLException {
    throw new SQLException("NStrings are not supported");
  }

  @Override
  public String getNString(String arg0) throws SQLException {
    throw new SQLException("NStrings are not supported");
  }

  @Override
  public Object getObject(int index) throws SQLException {
    return currentRow[index-1];
  }

  @Override
  public Object getObject(String columnName) throws SQLException {
    return getObject(rowMeta.indexOfValue(columnName));
  }

  @Override
  public Object getObject(int index, Map<String, Class<?>> arg1) throws SQLException {
    return getObject(index);
  }

  @Override
  public Object getObject(String columnName, Map<String, Class<?>> arg1) throws SQLException {
    return getObject(columnName);
  }

  @Override
  public Ref getRef(int arg0) throws SQLException {
    throw new SQLException("Refs are not supported");
  }

  @Override
  public Ref getRef(String arg0) throws SQLException {
    throw new SQLException("Refs are not supported");
  }

  @Override
  public RowId getRowId(int arg0) throws SQLException {
    throw new SQLException("RowIDs are not supported");
  }

  @Override
  public RowId getRowId(String arg0) throws SQLException {
    throw new SQLException("RowIDs are not supported");
  }

  @Override
  public SQLXML getSQLXML(int arg0) throws SQLException {
    throw new SQLException("SQLXML is not supported");
  }

  @Override
  public SQLXML getSQLXML(String arg0) throws SQLException {
    throw new SQLException("SQLXML is not supported");
  }

  @Override
  public short getShort(int index) throws SQLException {
    long l = getLong(index);
    return (short)l;
  }

  @Override
  public short getShort(String columnName) throws SQLException {
    return getShort(rowMeta.indexOfValue(columnName));
  }

  @Override
  public String getString(int index) throws SQLException {
    try {
      String string = rowMeta.getString(currentRow, index-1);
      if (string==null) {
        lastNull=true;
        return null;
      }
      lastNull=false;
      return string;
    } catch(Exception e) { 
      throw new SQLException(e);
    }
  }

  @Override
  public String getString(String columnName) throws SQLException {
    return getString(rowMeta.indexOfValue(columnName));
  }

  @Override
  public Time getTime(int arg0) throws SQLException {
    throw new SQLException("Time is not supported");
  }

  @Override
  public Time getTime(String arg0) throws SQLException {
    throw new SQLException("Time is not supported");
  }

  @Override
  public Time getTime(int arg0, Calendar arg1) throws SQLException {
    throw new SQLException("Time is not supported");
  }

  @Override
  public Time getTime(String arg0, Calendar arg1) throws SQLException {
    throw new SQLException("Time is not supported");
  }

  @Override
  public Timestamp getTimestamp(int index) throws SQLException {
    java.util.Date date = getDate(index);
    if (date==null) return null;
    return new Timestamp(date.getTime());
  }

  @Override
  public Timestamp getTimestamp(String columnName) throws SQLException {
    return getTimestamp(rowMeta.indexOfValue(columnName));
  }

  @Override
  public Timestamp getTimestamp(int index, Calendar arg1) throws SQLException {
    return getTimestamp(index);
  }

  @Override
  public Timestamp getTimestamp(String columnName, Calendar arg1) throws SQLException {
    return getTimestamp(columnName);
  }

  @Override
  public URL getURL(int arg0) throws SQLException {
    throw new SQLException("URLs are not supported");
  }

  @Override
  public URL getURL(String arg0) throws SQLException {
    throw new SQLException("URLs are not supported");
  }

  @Override
  public InputStream getUnicodeStream(int arg0) throws SQLException {
    throw new SQLException("Unicode streams are not supported");
  }

  @Override
  public InputStream getUnicodeStream(String arg0) throws SQLException {
    throw new SQLException("Unicode streams are not supported");
  }


  
  
  
  
  
  
  
  
  
  // Update section below: all not supported...
  
  
  @Override
  public void updateArray(int arg0, Array arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateArray(String arg0, Array arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(int arg0, Blob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(String arg0, Blob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(int arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(String arg0, InputStream arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBoolean(int arg0, boolean arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBoolean(String arg0, boolean arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateByte(int arg0, byte arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateByte(String arg0, byte arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBytes(int arg0, byte[] arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateBytes(String arg0, byte[] arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(int arg0, Clob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(String arg0, Clob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(int arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(String arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateDate(int arg0, Date arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateDate(String arg0, Date arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateDouble(int arg0, double arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateDouble(String arg0, double arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateFloat(int arg0, float arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateFloat(String arg0, float arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateInt(int arg0, int arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateInt(String arg0, int arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateLong(int arg0, long arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateLong(String arg0, long arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(int arg0, NClob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(String arg0, NClob arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(int arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(String arg0, Reader arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNString(int arg0, String arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNString(String arg0, String arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNull(int arg0) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateNull(String arg0) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateObject(int arg0, Object arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateObject(String arg0, Object arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateObject(int arg0, Object arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateObject(String arg0, Object arg1, int arg2) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRef(int arg0, Ref arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRef(String arg0, Ref arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRow() throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRowId(int arg0, RowId arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateRowId(String arg0, RowId arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateShort(int arg0, short arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateShort(String arg0, short arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateString(int arg0, String arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateString(String arg0, String arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateTime(int arg0, Time arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateTime(String arg0, Time arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  @Override
  public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException {
    throw new SQLException("Updates are not supported");
  }

  
  

  @Override
  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
    throw new SQLException("Wrapping not supperted");
  }

  @Override
  public <T> T unwrap(Class<T> arg0) throws SQLException {
    throw new SQLException("Wrapping not supperted");
  }

  /**
   * @return the serviceName
   */
  public String getServiceName() {
    return serviceName;
  }





  /**
   * @return the serviceTransName
   */
  public String getServiceTransName() {
    return serviceTransName;
  }



  /**
   * @return the serviceObjectId
   */
  public String getServiceObjectId() {
    return serviceObjectId;
  }



  /**
   * @return the sqlTransName
   */
  public String getSqlTransName() {
    return sqlTransName;
  }



  /**
   * @return the sqlObjectId
   */
  public String getSqlObjectId() {
    return sqlObjectId;
  }  
}
