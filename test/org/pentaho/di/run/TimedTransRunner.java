package org.pentaho.di.run;

import java.text.DecimalFormat;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowListener;

import be.ibridge.kettle.core.KettleVariables;

public class TimedTransRunner
{
    private String filename;
    private int logLevel;
    private long records;
    private double newRunTime;
    private double newSpeed;
    private double oldRunTime;
    private double oldSpeed;
    private be.ibridge.kettle.core.Result oldResult;
    private Result newResult;
    
    private String       newRowListenerStep;
    private RowListener  newRowListener;
    
    private String       oldRowListenerStep;
    private be.ibridge.kettle.trans.step.RowListener  oldRowListener;
    private be.ibridge.kettle.trans.TransMeta oldTransMeta;
    private TransMeta newTransMeta;
    private DatabaseMeta newTargetDatabaseMeta;
    private be.ibridge.kettle.core.database.DatabaseMeta oldTargetDatabaseMeta;
    
    
    public TimedTransRunner(String filename, int logLevel, be.ibridge.kettle.core.database.DatabaseMeta oldTargetDatabaseMeta, DatabaseMeta newTargetDatabaseMeta, long records)
    {
        this.filename = filename;
        this.logLevel = logLevel;
        this.oldTargetDatabaseMeta = oldTargetDatabaseMeta;
        this.newTargetDatabaseMeta = newTargetDatabaseMeta;
        this.records = records;
    }

    public boolean runOldAndNew() throws Exception
    {
        init();
        
        boolean ok = runOldEngine(true);
        ok &= runNewEngine(false);
        
        compareResults();
        return ok;
    }

    public void init()
    {
        EnvUtil.environmentInit();
        be.ibridge.kettle.core.util.EnvUtil.environmentInit();
        
        LogWriter.getInstance(logLevel);
        be.ibridge.kettle.core.LogWriter.getInstance(logLevel);
    }
    
    public void printOldTransDescription()
    {
        System.out.println();
        System.out.println("Transformation name         : "+oldTransMeta.getName());
        System.out.println("Transformation description  : "+Const.NVL(oldTransMeta.getDescription(), ""));
        System.out.println("-----------------------------------------------------------------------------------------------------");
    }

    public void printNewTransDescription()
    {
        System.out.println();
        System.out.println("Transformation name         : "+newTransMeta.getName());
        System.out.println("Transformation description  : "+Const.NVL(newTransMeta.getDescription(), ""));
        System.out.println("-----------------------------------------------------------------------------------------------------");
    }
    
    public void runOldEngine() throws Exception
    {
        runOldEngine(true);
    }
    
    public boolean runOldEngine(boolean printDescription) throws Exception
    {
        if (be.ibridge.kettle.trans.StepLoader.getInstance().getPluginList().size()==0) be.ibridge.kettle.trans.StepLoader.getInstance().read();

        KettleVariables.getInstance().setVariable("NR_OF_ROWS", Long.toString(records));
        
        oldTransMeta = new be.ibridge.kettle.trans.TransMeta(filename);
        if (printDescription) printOldTransDescription();
        
        // Replace the TARGET database connection settings with the one provided
        if (oldTargetDatabaseMeta!=null)
        {
            oldTransMeta.addOrReplaceDatabase(oldTargetDatabaseMeta);
        }
        
        // OK, now run this transFormation.
        be.ibridge.kettle.trans.Trans trans = new be.ibridge.kettle.trans.Trans(be.ibridge.kettle.core.LogWriter.getInstance(), oldTransMeta);
        if( !trans.prepareExecution(null) ) {
        	return false;
        }
        
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
        return true;
    }

    private static DecimalFormat recordsDF = new DecimalFormat("###,###,##0");
    private static DecimalFormat runtimeDF = new DecimalFormat("##0.00");
    private static DecimalFormat speedDF = new DecimalFormat("###,###,##0");
    
    private void printStats(String prefix, long lines, double runTime, double speed)
    {
        System.out.println(prefix+", rows: "+recordsDF.format(lines)+",   runtime: "+runtimeDF.format(runTime)+"s,   speed: "+speedDF.format(speed)+" rows/s");
    }

    public boolean runNewEngine() throws KettleException
    {
        return runNewEngine(false);
    }

    public boolean runNewEngine(boolean printDescription) throws KettleException
    {
        if (StepLoader.getInstance().getPluginList().size()==0) StepLoader.init();

        newTransMeta = new TransMeta(filename);
        newTransMeta.setVariable("NR_OF_ROWS", Long.toString(records));
        if (printDescription) printNewTransDescription();

        // Replace the TARGET database connection settings with the one provided
        if (newTargetDatabaseMeta!=null)
        {
            newTransMeta.addOrReplaceDatabase(newTargetDatabaseMeta);
        }
        
        // OK, now run this transFormation.
        Trans trans = new Trans(newTransMeta);
        
        try {
        	trans.prepareExecution(null);
        }
        catch (Exception e) {
        	LogWriter.getInstance().logError(trans.getName(), "Error preparing / initializing transformation", e);
        	return false;
		}
        
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
        
        return true;
    }
    
    private static DecimalFormat factorDF = new DecimalFormat("##0.00");
    
    public void compareResults()
    {
        compareResults(oldRunTime, newRunTime);
    }
    
    public static final void compareResults(double oldRunTime, double newRunTime)
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
    public be.ibridge.kettle.core.Result getOldResult()
    {
        return oldResult;
    }

    /**
     * @param oldResult the oldResult to set
     */
    public void setOldResult(be.ibridge.kettle.core.Result oldResult)
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

    /**
     * @return the oldTransMeta
     */
    public be.ibridge.kettle.trans.TransMeta getOldTransMeta()
    {
        return oldTransMeta;
    }

    /**
     * @param oldTransMeta the oldTransMeta to set
     */
    public void setOldTransMeta(be.ibridge.kettle.trans.TransMeta oldTransMeta)
    {
        this.oldTransMeta = oldTransMeta;
    }

    /**
     * @return the newTransMeta
     */
    public TransMeta getNewTransMeta()
    {
        return newTransMeta;
    }

    /**
     * @param newTransMeta the newTransMeta to set
     */
    public void setNewTransMeta(TransMeta newTransMeta)
    {
        this.newTransMeta = newTransMeta;
    }

    /**
     * @return the newRunTime
     */
    public double getNewRunTime()
    {
        return newRunTime;
    }

    /**
     * @param newRunTime the newRunTime to set
     */
    public void setNewRunTime(double newRunTime)
    {
        this.newRunTime = newRunTime;
    }

    /**
     * @return the newSpeed
     */
    public double getNewSpeed()
    {
        return newSpeed;
    }

    /**
     * @param newSpeed the newSpeed to set
     */
    public void setNewSpeed(double newSpeed)
    {
        this.newSpeed = newSpeed;
    }

    /**
     * @return the oldRunTime
     */
    public double getOldRunTime()
    {
        return oldRunTime;
    }

    /**
     * @param oldRunTime the oldRunTime to set
     */
    public void setOldRunTime(double oldRunTime)
    {
        this.oldRunTime = oldRunTime;
    }

    /**
     * @return the oldSpeed
     */
    public double getOldSpeed()
    {
        return oldSpeed;
    }

    /**
     * @param oldSpeed the oldSpeed to set
     */
    public void setOldSpeed(double oldSpeed)
    {
        this.oldSpeed = oldSpeed;
    }

    /**
     * @return the oldTargetDatabaseMeta
     */
    public be.ibridge.kettle.core.database.DatabaseMeta getOldTargetDatabaseMeta()
    {
        return oldTargetDatabaseMeta;
    }

    /**
     * @param oldTargetDatabaseMeta the oldTargetDatabaseMeta to set
     */
    public void setOldTargetDatabaseMeta(be.ibridge.kettle.core.database.DatabaseMeta oldTargetDatabaseMeta)
    {
        this.oldTargetDatabaseMeta = oldTargetDatabaseMeta;
    }

    /**
     * @return the newTargetDatabaseMeta
     */
    public DatabaseMeta getNewTargetDatabaseMeta()
    {
        return newTargetDatabaseMeta;
    }

    /**
     * @param newTargetDatabaseMeta the newTargetDatabaseMeta to set
     */
    public void setNewTargetDatabaseMeta(DatabaseMeta newTargetDatabaseMeta)
    {
        this.newTargetDatabaseMeta = newTargetDatabaseMeta;
    }
}
