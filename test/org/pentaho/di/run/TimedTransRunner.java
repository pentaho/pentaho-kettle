package org.pentaho.di.run;

import java.text.DecimalFormat;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;

public class TimedTransRunner
{
    private String filename;
    private int logLevel;
    private long records;
    private double runTime;
    private double speed;

    private Result result;
    
    private String       rowListenerStep;
    private RowListener  rowListener;
    
    private TransMeta transMeta;
    private DatabaseMeta targetDatabaseMeta;
    
    public TimedTransRunner(String filename, int logLevel, DatabaseMeta newTargetDatabaseMeta, long records)
    {
        this.filename = filename;
        this.logLevel = logLevel;
        this.targetDatabaseMeta = newTargetDatabaseMeta;
        this.records = records;
    }

    public boolean run() throws Exception
    {
        init();
        return runEngine(false);
    }

    public void init() throws KettleException
    {
        KettleEnvironment.init();
        LogWriter.getInstance(logLevel);
    }
    
    public void printTransDescription()
    {
        System.out.println();
        System.out.println("Transformation name         : "+transMeta.getName());
        System.out.println("Transformation description  : "+Const.NVL(transMeta.getDescription(), ""));
        System.out.println("-----------------------------------------------------------------------------------------------------");
    }
    
    private static DecimalFormat recordsDF = new DecimalFormat("###,###,##0");
    private static DecimalFormat runtimeDF = new DecimalFormat("##0.00");
    private static DecimalFormat speedDF = new DecimalFormat("#,###,###,##0");
    
    private void printStats(String prefix, long lines, double runTime, double speed)
    {
        System.out.println(prefix+", rows: "+recordsDF.format(lines)+",   runtime: "+runtimeDF.format(runTime)+"s,   speed: "+speedDF.format(speed)+" rows/s");
    }

    public boolean runEngine() throws KettleException
    {
        return runEngine(false);
    }

    public boolean runEngine(boolean printDescription) throws KettleException
    {
    	System.gc();
    	
        KettleEnvironment.init();

        transMeta = new TransMeta(filename);
        transMeta.setVariable("NR_OF_ROWS", Long.toString(records));
        if (printDescription) printTransDescription();

        // Replace the TARGET database connection settings with the one provided
        if (targetDatabaseMeta!=null)
        {
            transMeta.addOrReplaceDatabase(targetDatabaseMeta);
        }
        
        // OK, now run this transFormation.
        Trans trans = new Trans(transMeta);
        
        try {
        	trans.prepareExecution(null);
        }
        catch (Exception e) {
        	System.err.println(CentralLogStore.getAppender().getBuffer(trans.getLogChannelId(), true));
        	
        	trans.getLogChannel().logError("Error preparing / initializing transformation", e);
        	
        	return false;
		}
        
        if (!Const.isEmpty(rowListenerStep))
        {
            StepInterface step = trans.findRunThread(rowListenerStep);
            if (step!=null)
            {
                step.addRowListener(rowListener);
            }
        }
        
        long startTime = System.currentTimeMillis();
        
        trans.startThreads();
        
        trans.waitUntilFinished();
        
        long stopTime = System.currentTimeMillis();

        result = trans.getResult();
        
        runTime = (double)(stopTime - startTime) / 1000;
        speed = (double)records / (runTime);
        
        printStats("V3 results", records, runTime, speed);
        
        return true;
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
     * @return the result
     */
    public Result getNewResult()
    {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setNewResult(Result result)
    {
        this.result = result;
    }

    public void addRowListener(String stepname, RowListener rowListener)
    {
        this.rowListenerStep = stepname;
        this.rowListener = rowListener;
    }

    /**
     * @return the transMeta
     */
    public TransMeta getTransMeta()
    {
        return transMeta;
    }

    /**
     * @param transMeta the transMeta to set
     */
    public void setTransMeta(TransMeta transMeta)
    {
        this.transMeta = transMeta;
    }

    /**
     * @return the Run Time
     */
    public double getRunTime()
    {
        return runTime;
    }

    /**
     * @param runTime the run time to set
     */
    public void setNewRunTime(double runTime)
    {
        this.runTime = runTime;
    }

    /**
     * @return the speed
     */
    public double speed()
    {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    /**
     * @return the targetDatabaseMeta
     */
    public DatabaseMeta getTargetDatabaseMeta()
    {
        return targetDatabaseMeta;
    }

    /**
     * @param targetDatabaseMeta the targetDatabaseMeta to set
     */
    public void setTargetDatabaseMeta(DatabaseMeta targetDatabaseMeta)
    {
        this.targetDatabaseMeta = targetDatabaseMeta;
    }
}
