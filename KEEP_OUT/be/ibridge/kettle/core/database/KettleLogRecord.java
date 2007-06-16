package be.ibridge.kettle.core.database;

import java.util.Date;

import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;

/**
 * Handles the logging of transformations and jobs...
 * 
 * @author Matt
 *
 */
public class KettleLogRecord
{
    private DatabaseMeta logConnection;
    private String logTable;
    private boolean batchIdUsed;
    private long batchId;
    private boolean jobType;
    private String name;
    private String status;
    private long read;
    private long written;
    private long updated; 
    private long input; 
    private long output;
    private long errors;
    private Date startdate; 
    private Date enddate;
    private Date logdate;
    private Date depdate;
    private String logString;

    /**
     * Create an empty Kettle log record
     */
    public KettleLogRecord()
    {
    }

    /**
     * Copies logging info from the transformation into this logging record...
     * @param trans The transformation to copy the information from...
     */
    public KettleLogRecord(String status, Trans trans, boolean jobType)
    {
        TransMeta transMeta = trans.getTransMeta();
        Result result = trans.getResult();
        
        this.logConnection = transMeta.getLogConnection();
        this.logTable = transMeta.getLogTable();
        this.batchIdUsed = transMeta.isBatchIdUsed();
        this.batchId = trans.getBatchId();
        this.jobType = jobType;
        this.name = transMeta.getName();
        this.status = status;
        this.read = result.getNrLinesRead();
        this.written = result.getNrLinesWritten();
        this.input = result.getNrLinesInput();
        this.output = result.getNrLinesOutput();
        this.updated = result.getNrLinesUpdated();
        this.errors = result.getNrErrors();
        this.startdate = trans.getStartDate();
        this.enddate = trans.getEndDate();
        this.depdate = trans.getDepDate();
        this.logdate = trans.getLogDate();
    }

    /**
     * @return the logging connection (database metadata) used
     */
    public DatabaseMeta getLogConnection()
    {
        return logConnection;
    }
    
    /**
     * Set the logging connection (database metadata) to use 
     * @param logConnection
     */
    public void setLogConnection(DatabaseMeta logConnection)
    {
        this.logConnection = logConnection;
    }
    
    /**
     * @return Returns the batchId.
     */
    public long getBatchId()
    {
        return batchId;
    }
    /**
     * @param batchId The batchId to set.
     */
    public void setBatchId(long batchId)
    {
        this.batchId = batchId;
    }
    /**
     * @return Returns the batchIdUsed.
     */
    public boolean isBatchIdUsed()
    {
        return batchIdUsed;
    }
    /**
     * @param batchIdUsed The batchIdUsed to set.
     */
    public void setBatchIdUsed(boolean batchIdUsed)
    {
        this.batchIdUsed = batchIdUsed;
    }
    /**
     * @return Returns the depdate.
     */
    public Date getDepdate()
    {
        return depdate;
    }
    /**
     * @param depdate The depdate to set.
     */
    public void setDepdate(Date depdate)
    {
        this.depdate = depdate;
    }
    /**
     * @return Returns the enddate.
     */
    public Date getEnddate()
    {
        return enddate;
    }
    /**
     * @param enddate The enddate to set.
     */
    public void setEnddate(Date enddate)
    {
        this.enddate = enddate;
    }
    /**
     * @return Returns the errors.
     */
    public long getErrors()
    {
        return errors;
    }
    /**
     * @param errors The errors to set.
     */
    public void setErrors(long errors)
    {
        this.errors = errors;
    }
    /**
     * @return Returns the input.
     */
    public long getInput()
    {
        return input;
    }
    /**
     * @param input The input to set.
     */
    public void setInput(long input)
    {
        this.input = input;
    }
    /**
     * @return Returns the jobType.
     */
    public boolean isJobType()
    {
        return jobType;
    }
    /**
     * @param jobType The jobType to set.
     */
    public void setJobType(boolean jobType)
    {
        this.jobType = jobType;
    }
    /**
     * @return Returns the logdate.
     */
    public Date getLogdate()
    {
        return logdate;
    }
    /**
     * @param logdate The logdate to set.
     */
    public void setLogdate(Date logdate)
    {
        this.logdate = logdate;
    }
    /**
     * @return Returns the logString.
     */
    public String getLogString()
    {
        return logString;
    }
    /**
     * @param logString The logString to set.
     */
    public void setLogString(String logString)
    {
        this.logString = logString;
    }
    /**
     * @return Returns the logTable.
     */
    public String getLogTable()
    {
        return logTable;
    }
    /**
     * @param logTable The logTable to set.
     */
    public void setLogTable(String logTable)
    {
        this.logTable = logTable;
    }
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    /**
     * @return Returns the output.
     */
    public long getOutput()
    {
        return output;
    }
    /**
     * @param output The output to set.
     */
    public void setOutput(long output)
    {
        this.output = output;
    }
    /**
     * @return Returns the read.
     */
    public long getRead()
    {
        return read;
    }
    /**
     * @param read The read to set.
     */
    public void setRead(long read)
    {
        this.read = read;
    }
    /**
     * @return Returns the startdate.
     */
    public Date getStartdate()
    {
        return startdate;
    }
    /**
     * @param startdate The startdate to set.
     */
    public void setStartdate(Date startdate)
    {
        this.startdate = startdate;
    }
    /**
     * @return Returns the status.
     */
    public String getStatus()
    {
        return status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(String status)
    {
        this.status = status;
    }
    /**
     * @return Returns the updated.
     */
    public long getUpdated()
    {
        return updated;
    }
    /**
     * @param updated The updated to set.
     */
    public void setUpdated(long updated)
    {
        this.updated = updated;
    }
    /**
     * @return Returns the written.
     */
    public long getWritten()
    {
        return written;
    }
    /**
     * @param written The written to set.
     */
    public void setWritten(long written)
    {
        this.written = written;
    }
    
    public void writeLogRecord() throws KettleException
    {
        Database ldb = new Database(logConnection);
        try
        {
            ldb.connect();

            // OK, construct the SQL to insert or update the database logging record...
            // Insert or update?
            
            String sql = "";
            Row r = new Row(); // parameters
            
            if (!status.equalsIgnoreCase("start") && batchIdUsed) // Update using batch ID
            {
                sql = "UPDATE " + logTable + " SET STATUS=?, LINES_READ=?, LINES_WRITTEN=?, LINES_INPUT=?,"
                        + " LINES_OUTPUT=?, LINES_UPDATED=?, ERRORS=?, STARTDATE=?, ENDDATE=?, LOGDATE=?, DEPDATE=?";
                if (logString != null) sql += ", LOG_FIELD=? ";
                sql += "WHERE ID_BATCH=?";

                r.addValue(new Value("STATUS", status));
                r.addValue(new Value("LINES_READ", (long) read));
                r.addValue(new Value("LINES_WRITTEN", (long) written));
                r.addValue(new Value("LINES_INPUT", (long) input));
                r.addValue(new Value("LINES_OUTPUT", (long) output));
                r.addValue(new Value("LINES_UPDATED", (long) updated));
                r.addValue(new Value("ERRORS", (long) errors));
                r.addValue(new Value("STARTDATE", startdate));
                r.addValue(new Value("ENDDATE", enddate));
                r.addValue(new Value("LOGDATE", logdate));
                r.addValue(new Value("DEPDATE", depdate));
                if (logString != null)
                {
                    Value logfield = new Value("LOG_FIELD", logString);
                    logfield.setLength(DatabaseMeta.CLOB_LENGTH);
                    r.addValue(logfield);
                }
                r.addValue(new Value("ID_BATCH", batchId));
            }
            else
            {
                sql += "INSERT INTO "+logTable;
            }
            
        }
        catch(Exception e)
        {
            throw new KettleException("Error writing log record to table ["+logTable+"]", e);
        }
        finally
        {
            ldb.disconnect();
        }
    }
    
}
