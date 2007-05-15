package org.pentaho.di.run;

import java.text.DecimalFormat;

import junit.framework.TestCase;

import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowListener;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class TimedTransRunner extends TestCase
{
    private String filename;
    private int logLevel;
    private long records;
    private double newRunTime;
    private double newSpeed;
    private double oldRunTime;
    private double oldSpeed;
    private Result oldResult;
    private Result newResult;
    
    private String       newRowListenerStep;
    private RowListener  newRowListener;
    
    private String       oldRowListenerStep;
    private be.ibridge.kettle.trans.step.RowListener  oldRowListener;
    
    
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
        System.out.println();
        System.out.println("Name of transformation: "+transMeta.getName());
        System.out.println("Transformation description: "+Const.NVL(transMeta.getDescription(), ""));
        System.out.println("-----------------------------------------------------------------------------------------------------");
        
        // OK, now run this transFormation.
        be.ibridge.kettle.trans.Trans trans = new be.ibridge.kettle.trans.Trans(LogWriter.getInstance(), transMeta);
        trans.prepareExecution(null);
        
        if (!Const.isEmpty(oldRowListenerStep))
        {
            be.ibridge.kettle.trans.step.BaseStep baseStep = trans.findRunThread(oldRowListenerStep);
            if (baseStep!=null)
            {
                baseStep.addRowListener(oldRowListener);
            }
        }

        long startTime = System.currentTimeMillis();
        
        trans.startThreads();
        
        trans.waitUntilFinished();
        
        long stopTime = System.currentTimeMillis();
        
        oldResult = trans.getResult();
        
        oldRunTime = (double)(stopTime - startTime) / 1000;
        oldSpeed = (double)records / (oldRunTime);
        
        printStats("V2 results", records, oldRunTime, oldSpeed);
    }

    private static DecimalFormat recordsDF = new DecimalFormat("###,###,##0");
    private static DecimalFormat runtimeDF = new DecimalFormat("##0.00");
    private static DecimalFormat speedDF = new DecimalFormat("###,###,##0");
    
    private void printStats(String prefix, long lines, double runTime, double speed)
    {
        System.out.println(prefix+", rows: "+recordsDF.format(lines)+",   runtime: "+runtimeDF.format(runTime)+"s,   speed: "+speedDF.format(speed)+" rows/s");
    }

    public void runNewEngine() throws KettleXMLException
    {
        if (StepLoader.getInstance().getPluginList().size()==0) StepLoader.getInstance().read();

        TransMeta transMeta = new TransMeta(filename);
        
        // OK, now run this transFormation.
        Trans trans = new Trans(LogWriter.getInstance(), transMeta);
        trans.prepareExecution(null);
        
        if (!Const.isEmpty(newRowListenerStep))
        {
            BaseStep baseStep = trans.findRunThread(newRowListenerStep);
            if (baseStep!=null)
            {
                baseStep.addRowListener(newRowListener);
            }
        }
        
        long startTime = System.currentTimeMillis();
        
        trans.startThreads();
        
        trans.waitUntilFinished();
        
        long stopTime = System.currentTimeMillis();

        newResult = trans.getResult();
        
        newRunTime = (double)(stopTime - startTime) / 1000;
        newSpeed = (double)records / (newRunTime);
        
        printStats("V3 results", records, newRunTime, newSpeed);
    }
    
    private static DecimalFormat factorDF = new DecimalFormat("##0.00");
    
    private void compareResults()
    {
        double factor = oldRunTime/newRunTime;
        System.out.println("V3 / V2 = x"+factorDF.format(factor));
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

    public void addOldRowListener(String stepname, be.ibridge.kettle.trans.step.RowListener rowListener)
    {
        this.oldRowListenerStep = stepname;
        this.oldRowListener = rowListener;
    }

    public void addNewRowListener(String stepname, RowListener rowListener)
    {
        this.newRowListenerStep = stepname;
        this.newRowListener = rowListener;
    }
}
