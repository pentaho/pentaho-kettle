/**
 * 
 */
package org.pentaho.di.i18n;

import java.util.MissingResourceException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.laf.BasePropertyHandler;

/**
 * @author dhushon
 *
 */
public class LAFMessageHandler extends GlobalMessages {

	private static String replace = "org.pentaho.di";
	private static String replaceWith = null;	
	private static int offset = -1;
	private static String replaceSysBundle = null;
	
	static {
		replaceWith = BasePropertyHandler.getProperty("LAFpackage");
	}
    
	//TODO: modify base class to include a mandatory accessor so that this singleton instantiation can be 
	//TODO: better controlled
	public  LAFMessageHandler() {
		super();
		reinit();
	}
	
	public synchronized static MessageHandler getInstance() {
    	if (GMinstance == null) {
    		GMinstance = new LAFMessageHandler();
    	}
    	return (MessageHandler)GMinstance;
    }
	
	protected void reinit() {
		replaceWith = BasePropertyHandler.getProperty("LAFpackage");
		replaceSysBundle = replacePackage(SYSTEM_BUNDLE_PACKAGE);
		offset = -1;
	}
    
    /**
     * replace the application packagename target with ours for proper resolution
     * e.g. replace org.pentaho.di.* with pointers to new package structure
     * 
     * @param packageName
     * @return
     */
    private String replacePackage(String packageName) {
    	// we haven't yet discovered the offset for the trim
    	if (offset<0) {
    		offset = packageName.indexOf(replace);
    		if (offset>=0) {
    			offset = replace.length();
    		}
    	}
    	return new String(replaceWith + packageName.substring(offset));
    }
    
    private String internalCalc(String packageName, String global, String key, Object[] parameters) {
    	String string = null;
    	
    	// Then try the original package
        try { string = findString(packageName, langChoice.getDefaultLocale(), key, parameters); } catch(MissingResourceException e) {};
        if (string!=null) {
        	//System.out.println("found: "+key+"/"+string+" in "+packageName+" lang "+langChoice.getDefaultLocale());
        	return string;
        }
        
        // Then try to find it in the i18n package, in the system messages of the preferred language.
        try { string = findString(global, langChoice.getDefaultLocale(), key, parameters); } catch(MissingResourceException e) {};
        if (string!=null) {
        	//System.out.println("found: "+key+"/"+string+" in "+global+" lang "+langChoice.getDefaultLocale());
        	return string;
        }
        
        // Then try the failover locale, in the local package
        try { string = findString(packageName, langChoice.getFailoverLocale(), key, parameters); } catch(MissingResourceException e) {};
        if (string!=null) {
        	//System.out.println("found: "+key+"/"+string+" in "+packageName+" lang "+langChoice.getFailoverLocale());
        	return string;
        }
        
        // Then try to find it in the i18n package, in the system messages of the failover language.
        try { string = findString(global, langChoice.getFailoverLocale(), key, parameters); } catch(MissingResourceException e) {};
        //System.out.println("found: "+key+"/"+string+" in "+global+" lang "+langChoice.getFailoverLocale());
        return string; 
    }
    
    protected String calculateString(String packageName, String key, Object[] parameters)
    {
        String string=null;        
        if (replaceWith != null) {
        	string = internalCalc(replacePackage(packageName),replaceSysBundle,key, parameters);
            if (string!=null) return string;            
        }
       
        string = internalCalc(packageName,SYSTEM_BUNDLE_PACKAGE, key, parameters);
        if (string != null) return string;
        
        string = "!"+key+"!";
        String message = "Message not found in the preferred and failover locale: key=["+key+"], package="+packageName;
        LogWriter.getInstance().logDetailed("i18n", Const.getStackTracker(new KettleException(message)));

        return string;
    }

}
