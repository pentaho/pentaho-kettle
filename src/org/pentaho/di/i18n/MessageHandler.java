package org.pentaho.di.i18n;

import org.pentaho.di.laf.Handler;

/**
 * Standard Message handler that takes a root package, plus key and resolves that into one/more
 * resultant messages.  This Handler is used by all message types to enable flexible look and feel
 * as well as i18n to be implemented in variable ways.
 * 
 * @author dhushon
 *
 */
public interface MessageHandler extends Handler {

	/**
	 * get a key from the default (System global) bundle
	 * @param key
	 * @return
	 */
	public String getString(String key);
	
	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @return
	 */
	public String getString(String packageName, String key);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @return
	 */
	public String getString(String packageName, String key, String param1);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @param param3
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2, String param3);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @param param3
	 * @param param4
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2, String param3, String param4);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @param param3
	 * @param param4
	 * @param param5
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2, String param3, String param4, String param5);

	/**
	 * get a key from the defined package bundle, by key
	 * @param packageName
	 * @param key
	 * @param param1
	 * @param param2
	 * @param param3
	 * @param param4
	 * @param param5
	 * @param param6
	 * @return
	 */
	public String getString(String packageName, String key, String param1, String param2, String param3, String param4, String param5, String param6);
}
