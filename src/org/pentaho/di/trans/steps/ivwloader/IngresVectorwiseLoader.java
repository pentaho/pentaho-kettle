/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.ivwloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Performs a streaming bulk load to a VectorWise table.
 *
 * Based on Sven Boden's Oracle Bulk Loader step
 * 
 * @author matt
 * @since  14-apr-2009
 */
public class IngresVectorwiseLoader extends BaseStep implements StepInterface
{
  private static Class<?> PKG = IngresVectorwiseLoaderMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private IngresVectorwiseLoaderMeta meta;
  private IngresVectorwiseLoaderData data;
    
  public IngresVectorwiseLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
  {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }
  
  public boolean execute(IngresVectorwiseLoaderMeta meta) throws KettleException
  {
        Runtime rt = Runtime.getRuntime();

        try  
        {
          // 1) Create the FIFO file using the "mkfifo" command...
          //    Make sure to log all the possible output, also from STDERR
          //
          data.fifoFilename = environmentSubstitute(meta.getFifoFileName());
          
          File fifoFile = new File(data.fifoFilename);
          if (!fifoFile.exists()) {
            // MKFIFO!
            //
            String mkFifoCmd = "mkfifo "+data.fifoFilename;
            logDetailed("Creating FIFO file using this command : "+mkFifoCmd);
            Process mkFifoProcess = rt.exec(mkFifoCmd);
            StreamLogger errorLogger = new StreamLogger(log, mkFifoProcess.getErrorStream(), "mkFifoError");
            StreamLogger outputLogger = new StreamLogger(log, mkFifoProcess.getInputStream(), "mkFifoOuptut");
            new Thread(errorLogger).start();
            new Thread(outputLogger).start();
            int result = mkFifoProcess.waitFor();
            if (result!=0) {
              throw new Exception("Return code "+result+" received from statement : "+mkFifoCmd);
            }

            String chmodCmd = "chmod 666 "+data.fifoFilename;
            logDetailed("Setting FIFO file permissings using this command : "+chmodCmd);
            Process chmodProcess = rt.exec(chmodCmd);
            errorLogger = new StreamLogger(log, chmodProcess.getErrorStream(), "chmodError");
            outputLogger = new StreamLogger(log, chmodProcess.getInputStream(), "chmodOuptut");
            new Thread(errorLogger).start();
            new Thread(outputLogger).start();
            result = chmodProcess.waitFor();
            if (result!=0) {
              throw new Exception("Return code "+result+" received from statement : "+chmodCmd);
            }
          }
          
          // 2) Execute the Ingres "sql" command...
          //

            String cmd = createCommandLine(meta);
            
            try  
            {
              //masquerading the password for log
              if(meta.isUseDynamicVNode())
              {
                logDetailed("Executing command: "+cmd.substring(0,cmd.indexOf("["))+"[username,password]"+cmd.substring(cmd.indexOf("]")+1));
              }else{
                logDetailed("Executing command: "+cmd);
              }
                data.sqlProcess = rt.exec(cmd);
                
                // any error message?
                //
                data.errorLogger = new StreamLogger(log, data.sqlProcess.getErrorStream(), "ERR_SQL");
            
                // any output?
                data.outputLogger = new StreamLogger(log, data.sqlProcess.getInputStream(), "OUT_SQL");
                
                // Where do we send the data to? --> To STDIN of the sql process
                //
                data.sqlOutputStream = data.sqlProcess.getOutputStream();
                
                // kick them off
                new Thread(data.errorLogger).start();
                new Thread(data.outputLogger).start();                              

            }
            catch ( Exception ex )
            {
                throw new KettleException("Error while executing psql : " + cmd, ex);
            }

            logDetailed("Connected to VectorWise with the 'sql' command.");
            
            // OK, from here on, we need to feed in the COPY command followed by the data into the pgOutputStream
            //
            String loadCommand = createLoadCommand();
            logDetailed("Executing command: "+loadCommand);
            data.sqlRunner = new SqlRunner(data, loadCommand);
            data.sqlRunner.start();

            logDetailed("LOAD TABLE command started");

            // Open a new fifo output stream, buffered.
            //
            openFifoFile();

            logDetailed("Fifo stream opened");
            
            // Wait until it all hooks up in the FIFO
            // 
            waitForAConnection();
            
            logDetailed("Ready to start bulk loading!");
        }
        catch ( Exception ex )
        {
          throw new KettleException(ex);
        }
        
        return true;
  }
  
  private String createLoadCommand() {
      String loadCommand = "";
      loadCommand += "COPY TABLE "+meta.getDatabaseMeta().getQuotedSchemaTableCombination(null, meta.getTablename())+" ";
      
      // Build list of column names to set
      //
      loadCommand += "("+Const.CR;
      for (int cnt = 0; cnt < meta.getFieldDatabase().length; cnt++){
          loadCommand += "  "+meta.getFieldDatabase()[cnt];
          if (cnt < meta.getFieldDatabase().length -1) {
            //loadCommand+="= c0";
            if(meta.isUseSSV()){
              loadCommand+="= c0ssv ";
            }
            else{
                loadCommand+="= char(0) ";
              if ("\t".equals(meta.getDelimiter())) {
                loadCommand+="TAB";
              } else {
                loadCommand+="'"+meta.getDelimiter()+"'";
              }
            }
          } else {
            if(meta.isUseSSV()){
              loadCommand+="= c0ssv";
            }else{
              loadCommand+="= char(0) NL";
            }
          }
          
          
          if (cnt < meta.getFieldDatabase().length - 1) {
              loadCommand += ",";
          }
          loadCommand+=Const.CR;
      }
      loadCommand += ") FROM '"+environmentSubstitute(meta.getFifoFileName())+"'";
      if (meta.isContinueOnError())
      {
        loadCommand += "WITH ON_ERROR=CONTINUE, LOG='" + environmentSubstitute(meta.getErrorFileName()) + "'";
      }
      loadCommand += " \\g"+Const.CR;
      
      // Also quite this session after the load
      //
      loadCommand += " \\q"+Const.CR;

      return loadCommand;
  }

  private void openFifoFile() throws Exception {
    
        // Ready to start writing rows to the FIFO file now...
        //
      logDetailed("Opening fifo file " + data.fifoFilename + " for writing.");
        data.fifoOpener = new FifoOpener(data.fifoFilename);
        data.fifoOpener.start();
  }
  
  private void waitForAConnection() throws Exception {
      // Wait for either the sql statement to throw an error or the
      // fifo writer to throw an error
      //
      while (!isStopped()){
        data.fifoOpener.join(1000);
        //check if SQL Proces is still running has exited throw Error
       
        if(!checkSqlProcessRunning(data.sqlProcess)){
          throw new Exception("Ingres SQL process has stopped");
        }
          
          
          if (data.fifoOpener.getState() == Thread.State.TERMINATED)
              break;
          
          try{
              data.sqlRunner.checkExcn();
          }
          catch (Exception e){
              // We need to open a stream to the fifo to unblock the fifo writer
              // that was waiting for the sqlRunner that now isn't running
              data.fifoOpener.join();
              logError("Make sure user has been granted the FILE privilege.");
              logError("");
              throw e;
          }
          
          try{
              data.fifoOpener.checkExcn();
          }
          catch (Exception e){
              throw e;
          }
      }
                
      logDetailed("Opened fifo file " + data.fifoFilename + " for writing.");
  }
  
     /**
     * Create the command line for a sql process depending on the meta
     * information supplied.
     * 
     * @param meta The meta data to create the command line from
     * 
     * @return The string to execute.
     * 
     * @throws KettleException Upon any exception
     */
    public String createCommandLine(IngresVectorwiseLoaderMeta meta) throws KettleException
    {
       StringBuffer sb = new StringBuffer(300);
       
       if ( !Const.isEmpty(meta.getSqlPath()) )
       {
           try
           {
               FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(meta.getSqlPath()), getTransMeta());
               String sqlexec = Const.optionallyQuoteStringByOS(KettleVFS.getFilename(fileObject));
               sb.append(sqlexec);
               //sql @tc-dwh-test.timocom.net,tcp_ip,VW[ingres,pwd]::dwh
           }
           catch ( KettleFileException ex )
           {
               throw new KettleException("Error retrieving 'sql' command string", ex);
           }               
       }
       else
       {
           if(isDetailed()) logDetailed( "sql defaults to system path");
           sb.append("sql");
       }

       DatabaseMeta dm = meta.getDatabaseMeta();
       if ( dm != null )
       {
         if(meta.isUseDynamicVNode()){
           //logical portname in JDBC use a 7
           String port = environmentSubstitute(Const.NVL(dm.getDatabasePortNumberString(), "")).replace("7", "");
               String userName = environmentSubstitute(Const.NVL(dm.getDatabaseInterface().getUsername(), ""));
               String passWord = Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(Const.NVL(dm.getDatabaseInterface().getPassword(), "")));
               String hostName = environmentSubstitute(Const.NVL(dm.getDatabaseInterface().getHostname(), ""));
               String dnName  = environmentSubstitute(Const.NVL(dm.getDatabaseName(), ""));
               
               sb.append(" @").append(hostName).append(",").append(port).append("[").append(userName).append(",").append(passWord).append("]::").append(dnName);
         }
         else{
           // Database Name
           // 
           String dnName  = environmentSubstitute(Const.NVL(dm.getDatabaseName(), ""));
           sb.append(" ").append(dnName);
         }
       }
       else
       {
           throw new KettleException("No connection specified");
       }

       return sb.toString(); 
    }


  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
  {
    meta=(IngresVectorwiseLoaderMeta)smi;
    data=(IngresVectorwiseLoaderData)sdi;

    try
    {
      Object[] r=getRow();  // Get row from input rowset & set row busy!
      if (r==null)          // no more input to be expected...
      {
        setOutputDone();
        //only close output after the first row was processed 
        //to prevent error (NPE) on empty rows set
        if(!first){
          closeOutput();
        }
              
        return false;
      }
      
      if (first)
      {
        first=false;
        
        // Cache field indexes.
        //
        data.keynrs = new int[meta.getFieldStream().length];
        for (int i=0;i<data.keynrs.length;i++) {
          data.keynrs[i] = getInputRowMeta().indexOfValue(meta.getFieldStream()[i]);
        }
        
        // execute the client statement...
        //
        execute(meta);
      }
      
      //check if SQL process is still running before processing row
      if(!checkSqlProcessRunning(data.sqlProcess)) throw new Exception("Ingres SQL process has stopped");

      writeRowToBulk(getInputRowMeta(), r);
      putRow(getInputRowMeta(), r);
      incrementLinesOutput();
      
          if (checkFeedback(getLinesOutput())) logBasic(BaseMessages.getString(PKG, "IngresVectorwiseLoader.Log.LineNumber")+getLinesOutput()); //$NON-NLS-1$

      return true;
      
      
    }
    catch(Exception e)
    {
      logError(BaseMessages.getString(PKG, "IngresVectorwiseLoader.Log.ErrorInStep"), e); //$NON-NLS-1$
      setErrors(1);
      stopAll();
      setOutputDone();  // signal end to receiver(s)
      return false;
    } 
  }

  private void closeOutput() throws Exception 
  {
    // Close the fifo file...
    //
    data.fifoOpener.close();
    data.fileChannel=null;
    
        // wait for the INSERT statement to finish and check for any
        // error and/or warning...
    // 
        data.sqlRunner.join();
        SqlRunner sqlRunner = data.sqlRunner;
        data.sqlRunner = null;
        sqlRunner.checkExcn();
        
        data.sqlOutputStream.close();
        data.sqlOutputStream=null;
  }
  
  private void writeRowToBulk(RowMetaInterface rowMeta, Object[] r) throws KettleException {

      try {
        // So, we have this output stream to which we can write CSV data to.
        // Basically, what we need to do is write the binary data (from strings to it as part of this proof of concept)
        //
        // The data format required is essentially "value|value|value|value"
        // new feature implemented "use SSV which requires the fomat to be '"value";"value","value"'
        byte[] delimiter;
        if(meta.isUseSSV()){
        delimiter= data.getBytes( ";" );
      }else {
        delimiter=data.separator;
      }
        
        for (int i=0;i<data.keynrs.length;i++) {
        if (i>0) {
            // Write a separator 
            //
          write(delimiter);
        }
        
          int index = data.keynrs[i];
          ValueMetaInterface valueMeta = rowMeta.getValueMeta(index);
          Object valueData = r[index];

            if (valueData == null) {
            // Don't output anything for null
            //
          } else {
                  if (valueMeta.isStorageBinaryString()) {
                    byte[] value = valueMeta.getBinaryString(valueData);
                    write( value );
                  } else {
                  if(meta.isUseStandardConversion()){
                    if(valueMeta.isDate()){
                      valueMeta.setConversionMask("yyyy-MM-dd HH:mm:ss.SSS");
                    }else if (valueMeta.isNumber()){
                      valueMeta.setDecimalSymbol(".");
                      valueMeta.setGroupingSymbol("");
            }
                  }
                    String string = valueMeta.getString(valueData); 
                    if (string!=null) {
                      // support of SSV feature
                      if(meta.isUseSSV()) {
                        //replace " in string fields
                        if(meta.isEscapingSpecialCharacters() && valueMeta.isString()) {
                          
                          StringBuilder builder = new StringBuilder(string);
                          String[] escapeStrings = new String[] { "\"", "\n", "\r", };
                          String[] replaceStrings = new String[] { "\\\"", "\\n", "\\r", };
                          for (int e=0;e<escapeStrings.length;e++) {
                            String chr = escapeStrings[e];
                            String rep = replaceStrings[e];
                            int idx = builder.indexOf(chr, 0);
                            while (idx>0) {
                              builder.replace(idx, idx+chr.length(), rep);
                              idx = builder.indexOf(chr, idx+rep.length());
                            }
                          }
                          string = builder.toString();
                        }
                        
                        string = '"'+string+'"';
                      }
                      byte[] value = data.getBytes( string );
                      write(value);
                    }
                  }
                }
        }
      
      // finally write a newline
      //
        write(data.newline);
      }
      catch(Exception e)
      { 
        // If something went wrong with the import,  
        // rather return that error, in stead of "Pipe Broken"
        try{
          data.sqlRunner.checkExcn();
        }
        catch (Exception loadEx){
          throw new KettleException("Error serializing rows of data to the fifo file", loadEx);
        }
        
        throw new KettleException("Error serializing rows of data to the fifo file", e);
      }
    
  }

  private void write(byte[] content) throws IOException {
    if (data.fileChannel==null) {
      data.fileChannel = data.fifoOpener.getFileChannel();
    }

    if (data.byteBuffer!=null) {
      if (data.byteBuffer.capacity()<content.length) {
        data.byteBuffer.limit(content.length);
      }
      data.byteBuffer.clear();
    } else {
      data.byteBuffer = ByteBuffer.allocateDirect(content.length);
    }
    
    data.byteBuffer.put(content);
    data.fileChannel.write(data.byteBuffer);
  }

  public boolean init(StepMetaInterface smi, StepDataInterface sdi)
  {
    meta=(IngresVectorwiseLoaderMeta)smi;
    data=(IngresVectorwiseLoaderData)sdi;

    if (super.init(smi, sdi))
    {     
      if (Const.isEmpty(meta.getDelimiter())) {
        data.separator = data.getBytes( "|" );
       } else {
        data.separator = data.getBytes( meta.getDelimiter() );
      } 

        data.newline = data.getBytes( "\n" );
      
      // Schema-table combination...
      data.schemaTable = meta.getDatabaseMeta().getQuotedSchemaTableCombination(null, environmentSubstitute(meta.getTablename()));
      
      data.encoding = environmentSubstitute(meta.getEncoding());
      data.isEncoding = !Const.isEmpty(environmentSubstitute(meta.getEncoding()));
      
      data.byteBuffer  = null;
      
      return true;
    }
    return false;
  }
  
  public boolean checkSqlProcessRunning(Process sqlProcess){
    try{
        int exitValue = data.sqlProcess.exitValue();
        logError("SQL process exit code: " + exitValue);
        return false;
      }
     //ignore this exception since it is thrown when exitValue() is called on a running process 
     catch (IllegalThreadStateException e){
        //Do nothing SQL Process still running
       return true;
      }
  }

  public void dispose(StepMetaInterface smi, StepDataInterface sdi)
  {
      meta = (IngresVectorwiseLoaderMeta)smi;
      data = (IngresVectorwiseLoaderData)sdi;
      
      closeClientConnections(data);
      
      super.dispose(smi, sdi);
  }
  
  // Class to try and open a writer to a FIFO in a different thread.
  // Opening the FIFO is a blocking call, so we need to check for errors
  // after a small waiting period
  //
  public class FifoOpener extends Thread
  {
    private FileOutputStream fileOutputStream = null;
    private FileChannel fileChannel = null;
    private Exception ex;
    private String fifoName;
         
    public FifoOpener(String fifoName)
        {
      this.fifoName = fifoName;
        }
    
    public void run()
      {
      try{
        
        fileOutputStream = new FileOutputStream( this.fifoName );
        fileChannel = fileOutputStream.getChannel();
      } catch (Exception ex) {
              this.ex = ex;
            }
        }
    
    public void checkExcn() throws Exception
        {
            // This is called from the main thread context to re-throw any saved
            // exception.
        //
            if (ex != null) {
                throw ex;
            }
        }
    
    public FileChannel getFileChannel(){
      return fileChannel;
    }
    
    public void close() throws IOException {
      if (fileChannel!=null && fileOutputStream!=null) {
        fileChannel.close();
        fileOutputStream.close();
      }
    }
  }
  
    static class SqlRunner extends Thread
    {
        private IngresVectorwiseLoaderData data;
        
        private String loadCommand;

        private Exception ex;
        
        SqlRunner(IngresVectorwiseLoaderData data, String loadCommand)
        {
            this.data = data;
            this.loadCommand = loadCommand;
        }
        
        public void run()
        {
            try {
              data.sqlOutputStream.write( data.getBytes(loadCommand));
              data.sqlOutputStream.flush();
            } catch (Exception ex) {
              this.ex = ex;
            }
        }

        void checkExcn() throws Exception
        {
            // This is called from the main thread context to re-throw any saved
            // exception.
            //
            if (ex != null) {
                throw ex;
            }
        }
    }
    
    public boolean closeClientConnections(IngresVectorwiseLoaderData data){
   // Close the output streams if still needed.
      //
      try {
        if (data.fifoOpener!=null) {
          data.fifoOpener.close();
        }
  
          // Stop the SQL execution thread
        //
          if (data.sqlRunner!= null) {
              data.sqlRunner.join();
              data.sqlRunner = null;
          }
          
          // remove the fifo file...
          //
          try {
            if (data.fifoFilename!=null) {
              new File(data.fifoFilename).delete();
            }
          } catch(Exception e) {
            logError("Unable to delete FIFO file : "+data.fifoFilename, e);
          }
      }
      catch(Exception e) {
        setErrors(1L);
        logError("Unexpected error encountered while closing the client connection", e);
        return false;
      }
      return true;
    }
}
