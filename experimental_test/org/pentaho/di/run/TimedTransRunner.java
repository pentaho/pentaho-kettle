package org.pentaho.di.run;

import junit.framework.TestCase;

import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class TimedTransRunner extends TestCase
{
    private Trans trans;
    private String filename;
    private int logLevel;
    private long records;
    private double newRunTime;
    private double newSpeed;
    private double oldRunTime;
    private double oldSpeed;
    private Result oldResult;
    private Result newResult;
    
    public TimedTransRunner(String filename, int logLevel, long records)
    {
        this.filename = filename;
        this.logLevel = logLevel;
        this.records = records;
    }
    
    public void runOldAndNew() throws KettleXMLException
    {
        EnvUtil.environmentInit();
        LogWriter.getInstance(logLevel);
        
        // Set environment variables ${NR_OF_ROWS}
        //
        KettleVariables.getInstance().setVariable("NR_OF_ROWS", Long.toString(records));
        
        runOldEngine();
        runNewEngine();
        
        compareResults();
    }

    public void runOldEngine() throws KettleXMLException
    {
        if (be.ibridge.kettle.trans.StepLoader.getInstance().getPluginList().size()==0) be.ibridge.kettle.trans.StepLoader.getInstance().read();

        be.ibridge.kettle.trans.TransMeta transMeta = new be.ibridge.kettle.trans.TransMeta(filename);
        System.out.println("Name of transformation: "+transMeta.getName());
        System.out.println("Transformation description: "+Const.NVL(transMeta.getDescription(), ""));
        
        long startTime = System.currentTimeMillis();
        
        // OK, now run this transFormation.
        be.ibridge.kettle.trans.Trans trans = new be.ibridge.kettle.trans.Trans(LogWriter.getInstance(), transMeta);
        trans.execute(null);
        
        trans.waitUntilFinished();
        
        long stopTime = System.currentTimeMillis();
        
        oldResult = trans.getResult();
        
        oldRunTime = (double)(stopTime - startTime) / 1000;
        oldSpeed = (double)records / (oldRunTime);
        
        System.out.println("V2 results: records="+records+", runtime="+oldRunTime+", speed="+oldSpeed);
    }

    public void runNewEngine() throws KettleXMLException
    {
        if (StepLoader.getInstance().getPluginList().size()==0) StepLoader.getInstance().read();

        TransMeta transMeta = new TransMeta(filename);
        
        long startTime = System.currentTimeMillis();
        
        // OK, now run this transFormation.
        Trans trans = new Trans(LogWriter.getInstance(), transMeta);
        trans.execute(null);
        
        trans.waitUntilFinished();
        
        long stopTime = System.currentTimeMillis();

        newResult = trans.getResult();
        
        newRunTime = (double)(stopTime - startTime) / 1000;
        newSpeed = (double)records / (newRunTime);
        
        System.out.println("V3 results: records="+records+", runtime="+newRunTime+", speed="+newSpeed);
    }
    
    private void compareResults()
    {
        double factor = oldRunTime/newRunTime;
        System.out.println("V3 / V2 = x"+factor);
    }
    
    /**
     * @return the filename
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * @return the logLevel
     */
    public int getLogLevel()
    {
        return logLevel;
    }

    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(int logLevel)
    {
        this.logLevel = logLevel;
    }

    /**
     * @return the trans
     */
    public Trans getTrans()
    {
        return trans;
    }

    /**
     * @return the records
     */
    public long getRecords()
    {
        return records;
    }

    /**
     * @param records the records to set
     */
    public void setRecords(long records)
    {
        this.records = records;
    }

    /**
     * @return the newResult
     */
    public Result getNewResult()
    {
        return newResult;
    }

    /**
     * @param newResult the newResult to set
     */
    public void setNewResult(Result newResult)
    {
        this.newResult = newResult;
    }

    /**
     * @return the oldResult
     */
    public Result getOldResult()
    {
        return oldResult;
    }

    /**
     * @param oldResult the oldResult to set
     */
    public void setOldResult(Result oldResult)
    {
        this.oldResult = oldResult;
    }
}
