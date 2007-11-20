package org.pentaho.di.core.listeners;

/**
 * A callback interface that listens to specific lifecycle events triggered when Spoon starts and stops.
 * 
 * Listeners are loaded dynamically by PDI.  In order to register a listener with Spoon, a class that implements this
 * interface must be placed in the "org.pentaho.di.core.listeners.pdi" package, and it will be loaded automatically when Spoon starts.
 *  
 * @author Alex Silva
 *
 */
public interface LifecycleListener
{
	/**
	 * Called when the application starts.
	 * 
	 * @throws LifecycleException Whenever this listener is unable to start succesfully.  This exception is handled by Spoon as follows:
	 * If it is severe (as returned by isSevere()) the application will automatically quit after displaying an error dialog;
	 * otherwise an information dialog is shown to the user, but the application continues
	 * loading after that.
	 */
	public void onStart() throws LifecycleException;
	
	/**
	 * Called when the application ends
	 * @throws LifecycleException If a problem prevents this listener from shutting down.  No dialog/messages are displayed to the user, but 
	 * the proper exception message is logged to the console.
	 */
	public void onExit() throws LifecycleException;
}
