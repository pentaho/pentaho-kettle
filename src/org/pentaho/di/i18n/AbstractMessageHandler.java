package org.pentaho.di.i18n;

import java.util.Locale;

/**
 * Standard Message handler that takes a root package, plus key and resolves that into one/more
 * resultant messages.  This Handler is used by all message types to enable flexible look and feel
 * as well as i18n to be implemented in variable ways.
 * 
 * @author dhushon
 *
 */
public abstract class AbstractMessageHandler implements MessageHandler {

	/**
	 * forced override to allow singleton instantiation through dynamic class loader
	 * @see org.pentaho.di.i18n.GlobalMessages for sample
	 * 
	 * @return MessageHandler
	 */
	public synchronized static MessageHandler getInstance() {
		return null;
	}
	
	/**
	 * forced override, concrete implementations must provide implementation
	 * 
	 * @return Locale
	 */
	public synchronized static Locale getLocale() {
		return null;
	}
	
	/**
	 * forced override, concrete implementations must provide implementation
	 * 
	 * @param newLocale
	 */
	public synchronized static void setLocale(Locale newLocale) {}
	
}