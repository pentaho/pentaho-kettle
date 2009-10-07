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