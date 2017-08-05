/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.logging;

import org.pentaho.di.core.Const;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * This class keeps the last N lines in a buffer
 *
 * @author matt
 */
public class LoggingBuffer {
  private String name;

  private LinkedBlockingDeque<BufferLine> buffer;

  private int bufferSize;

  private KettleLogLayout layout;

  private List<KettleLoggingEventListener> eventListeners;

  private LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

  public LoggingBuffer( int bufferSize ) {
    this.bufferSize = bufferSize;
    buffer = new LinkedBlockingDeque<BufferLine>();
    layout = new KettleLogLayout( true );
    eventListeners = new CopyOnWriteArrayList<KettleLoggingEventListener>();
  }

  /**
   * @return the number (sequence, 1..N) of the last log line. If no records are present in the buffer, 0 is returned.
   */
  public int getLastBufferLineNr() {
    BufferLine line = buffer.peekLast();
    return line != null ? line.getNr() : 0;
  }

  /**
   * @param channelId      channel IDs to grab
   * @param includeGeneral include general log lines
   * @param from
   * @param to
   * @return
   */
  public List<KettleLoggingEvent> getLogBufferFromTo( List<String> channelId, boolean includeGeneral, int from,
                                                      int to ) {
    if ( channelId == null ) {
      return buffer.stream().filter( line -> line.getNr() > from && line.getNr() <= to )
        .map( BufferLine::getEvent ).collect( Collectors.toList() );
    }
    if ( includeGeneral ) {
      return buffer.stream().filter( line -> line.getNr() > from && line.getNr() <= to )
        .filter( line -> {
          String logChannelId = getLogChId( line );
          return ( isGeneral( logChannelId ) || channelId.contains( logChannelId ) );
        } ).map( BufferLine::getEvent ).collect( Collectors.toList() );
    } else {
      return buffer.stream().filter( line -> line.getNr() > from && line.getNr() <= to )
        .filter( line -> channelId.contains( getLogChId( line ) ) )
        .map( BufferLine::getEvent ).collect( Collectors.toList() );
    }
  }

  /**
   * @param parentLogChannelId the parent log channel ID to grab
   * @param includeGeneral     include general log lines
   * @param from
   * @param to
   * @return
   */
  public List<KettleLoggingEvent> getLogBufferFromTo( String parentLogChannelId, boolean includeGeneral, int from,
                                                      int to ) {

    // Typically, the log channel id is the one from the transformation or job running currently.
    // However, we also want to see the details of the steps etc.
    // So we need to look at the parents all the way up if needed...
    //
    List<String> childIds = loggingRegistry.getLogChannelChildren( parentLogChannelId );

    return getLogBufferFromTo( childIds, includeGeneral, from, to );
  }

  public StringBuffer getBuffer( String parentLogChannelId, boolean includeGeneral, int startLineNr, int endLineNr ) {
    StringBuilder eventBuffer = new StringBuilder( 10000 );

    List<KettleLoggingEvent> events =
      getLogBufferFromTo( parentLogChannelId, includeGeneral, startLineNr, endLineNr );
    for ( KettleLoggingEvent event : events ) {
      eventBuffer.append( layout.format( event ) ).append( Const.CR );
    }

    return new StringBuffer( eventBuffer );
  }

  public StringBuffer getBuffer( String parentLogChannelId, boolean includeGeneral ) {
    return getBuffer( parentLogChannelId, includeGeneral, 0 );
  }

  public StringBuffer getBuffer( String parentLogChannelId, boolean includeGeneral, int startLineNr ) {
    return getBuffer( parentLogChannelId, includeGeneral, startLineNr, getLastBufferLineNr() );
  }

  public StringBuffer getBuffer() {
    return getBuffer( null, true );
  }

  public void close() {
  }

  public void doAppend( KettleLoggingEvent event ) {
    if ( event.getMessage() instanceof LogMessage ) {
      buffer.add( new BufferLine( event ) );
      while ( bufferSize > 0 && buffer.size() > bufferSize ) {
        buffer.poll();
      }
    }
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setLayout( KettleLogLayout layout ) {
    this.layout = layout;
  }

  public KettleLogLayout getLayout() {
    return layout;
  }

  public boolean requiresLayout() {
    return true;
  }

  public void clear() {
    buffer.clear();
  }

  /**
   * @return the maximum number of lines that this buffer contains, 0 or lower means: no limit
   */
  public int getMaxNrLines() {
    return bufferSize;
  }

  /**
   * @param maxNrLines the maximum number of lines that this buffer should contain, 0 or lower means: no limit
   */
  public void setMaxNrLines( int maxNrLines ) {
    this.bufferSize = maxNrLines;
  }

  /**
   * @return the nrLines
   */
  public int getNrLines() {
    return buffer.size();
  }

  /**
   * Removes all rows for the channel with the specified id
   *
   * @param id the id of the logging channel to remove
   */
  public void removeChannelFromBuffer( String id ) {
    buffer.removeIf( line -> id.equals( getLogChId( line ) ) );
  }

  public int size() {
    return buffer.size();
  }

  public void removeGeneralMessages() {
    buffer.removeIf( line -> isGeneral( getLogChId( line ) ) );
  }

  public Iterator<BufferLine> getBufferIterator() {
    return buffer.iterator();
  }

  /**
   * It looks like this method is not used in the project.
   */
  @Deprecated
  public String dump() {
    StringBuilder buf = new StringBuilder( 50000 );
    buffer.forEach( line -> {
      LogMessage message = (LogMessage) line.getEvent().getMessage();
      buf.append( message.getLogChannelId() ).append( "\t" )
        .append( message.getSubject() ).append( "\n" );
    } );
    return buf.toString();
  }

  public void removeBufferLines( List<BufferLine> linesToRemove ) {
    buffer.removeAll( linesToRemove );
  }

  public List<BufferLine> getBufferLinesBefore( long minTimeBoundary ) {
    return buffer.stream().filter( line -> line.getEvent().timeStamp < minTimeBoundary )
      .collect( Collectors.toList() );
  }

  public void removeBufferLinesBefore( long minTimeBoundary ) {
    buffer.removeIf( line -> line.getEvent().timeStamp < minTimeBoundary );
  }

  public void addLogggingEvent( KettleLoggingEvent loggingEvent ) {
    doAppend( loggingEvent );
    eventListeners.forEach( event -> event.eventAdded( loggingEvent ) );
  }

  public void addLoggingEventListener( KettleLoggingEventListener listener ) {
    eventListeners.add( listener );
  }

  public void removeLoggingEventListener( KettleLoggingEventListener listener ) {
    eventListeners.remove( listener );
  }

  private boolean isGeneral( String logChannelId ) {
    LoggingObjectInterface loggingObject = loggingRegistry.getLoggingObject( logChannelId );
    return loggingObject != null && LoggingObjectType.GENERAL.equals( loggingObject.getObjectType() );
  }

  private String getLogChId( BufferLine bufferLine ) {
    return ( (LogMessage) bufferLine.getEvent().getMessage() ).getLogChannelId();
  }
}
