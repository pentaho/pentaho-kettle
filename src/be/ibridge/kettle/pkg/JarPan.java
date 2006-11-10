 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 

/**
 *   Kettle was (re-)started in March 2003
 */

package be.ibridge.kettle.pkg;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;

/**
 * Executes a transformation calles transformation.xml from within a jar file.
 * 
 * @author Matt
 * @since
 */
public class JarPan
{
	public static void main(String[] a) throws KettleException
	{
		EnvUtil.environmentInit();
		
		Trans          trans    = null;

        LogWriter log;
        LogWriter.setConsoleAppenderDebug();
        log=LogWriter.getInstance( LogWriter.LOG_LEVEL_BASIC );
        
        log.logMinimal("JarPan", "Start of run.");
		
		/* Load the plugins etc.*/
		StepLoader steploader = StepLoader.getInstance();
		if (!steploader.read())
		{
			log.logError("JarPan", "Error loading steps... halting Pan!");
            System.exit(8);
		}
		
		Date start, stop;
		Calendar cal;
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		cal=Calendar.getInstance();
		start=cal.getTime();

        
        
		try
		{
            log.logDebug("JarPan", "Load the transformation.");
            TransMeta transMeta = new TransMeta(JarfileGenerator.TRANSFORMATION_FILENAME);
            trans = new Trans(log, transMeta);
		}
		catch(KettleException e)
		{
			trans=null;
			System.out.println("Processing has stopped because of an error: "+e.getMessage());
		}

		if (trans==null)
		{
            System.out.println("ERROR: JarPan can't continue because the transformation couldn't be loaded.");
            System.exit(7);
		}
		
		try
		{
		    // allocate & run the required sub-threads
			boolean ok = trans.execute(null); // TODO: how to pass arguments?  From file? 
            if (!ok)
            {
                System.out.println("Unable to prepare and initialize this transformation");
                System.exit(3);
            }
			trans.waitUntilFinished();
			trans.endProcessing("end");

			log.logMinimal("JarPan", "Finished!");
			
			cal=Calendar.getInstance();
			stop=cal.getTime();
			String begin=df.format(start).toString();
			String end  =df.format(stop).toString();

			log.logMinimal("JarPan", "Start="+begin+", Stop="+end);
			long millis=stop.getTime()-start.getTime();
			log.logMinimal("JarPan", "Processing ended after "+(millis/1000)+" seconds.");
			if (trans.getResult().getNrErrors()==0) 
			{
				trans.printStats((int)millis/1000);
                System.exit(0);
			}
			else
			{
                System.exit(1);
			}
		}
		catch(KettleException ke)
		{
			System.out.println("ERROR occurred: "+ke.getMessage());
            log.logError("JarPan", "Unexpected error occurred: "+ke.getMessage());
            System.exit(2);
		}

	}
}
