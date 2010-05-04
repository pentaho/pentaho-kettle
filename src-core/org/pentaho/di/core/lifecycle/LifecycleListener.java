/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.core.lifecycle;

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
	 * @throws LifecycleException Whenever this listener is unable to start succesfully.
	 */
	public void onStart(LifeEventHandler handler) throws LifecycleException;
	
	/**
	 * Called when the application ends
	 * @throws LifecycleException If a problem prevents this listener from shutting down.
	 */
	public void onExit(LifeEventHandler handler) throws LifecycleException;
}
