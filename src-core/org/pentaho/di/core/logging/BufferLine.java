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
package org.pentaho.di.core.logging;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.spi.LoggingEvent;

public class BufferLine {
	private static AtomicInteger sequence = new AtomicInteger(0);
	
	private int nr;
	private LoggingEvent event;
	
	public BufferLine(LoggingEvent event) {
		this.event = event;
		this.nr = sequence.incrementAndGet();
	}
	
	public int getNr() {
		return nr;
	}

	public LoggingEvent getEvent() {
		return event;
	}	
	
	@Override
	public String toString() {
		return event.toString();
	}
}