/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.mutable.MutableInt;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class keeps the last N lines in a buffer
 *
 * @author matt
 */
public class LoggingBuffer {
  private String name;

  private ConcurrentSkipListMap<Integer, BufferLine> buffer;

  //Keeps track of the lead line number sent for a tail.  So the next time it can start from there instead of filtering
  //the entire buffer.  This matters when bufferSize is large
  private Map<String, Integer> tailMap = new ConcurrentHashMap<>();

  private ReadWriteLock lock = new ReentrantReadWriteLock();

  private int bufferSize;

  private KettleLogLayout layout;

  private List<KettleLoggingEventListener> eventListeners;

  private LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

  public LoggingBuffer( int bufferSize ) {
    this.bufferSize = bufferSize;
    buffer = new ConcurrentSkipListMap<>();
    layout = new KettleLogLayout( true );
    eventListeners = new CopyOnWriteArrayList<>();
  }

  /**
   * @return the number (sequence, 1..N) of the last log line. If no records are present in the buffer, 0 is returned.
   */
  public int getLastBufferLineNr() {
    try {
      return buffer.lastKey();
    } catch ( NoSuchElementException e ) {
      return 0; //This is the contract
    }
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
    return getLogBufferFromTo( channelId, includeGeneral, from, to, 0, "" );
  }

  /**
   * @param channelId      channel IDs to grab
   * @param includeGeneral include general log lines
   * @param from
   * @param to
   * @param tailLines Limit output to the last {tailLines} lines if non-zero
   * @return
   */
  public List<KettleLoggingEvent> getLogBufferFromTo( List<String> channelId, boolean includeGeneral, int from,
                                                      int to, int tailLines, String parentLogChannelId ) {

    if ( from > to ) {
      return Collections.<KettleLoggingEvent>emptyList();
    }
    Integer lastLineUsed;
    if ( tailLines > 0 && !Utils.isEmpty( parentLogChannelId )
      && ( lastLineUsed = tailMap.get( parentLogChannelId ) ) != null ) {
      from = Math.max( from, lastLineUsed );
    }

    //System.out.println(
    //  "****" + channelId.get( 0 ) + "   size:" + channelId.size() + "  general:" + includeGeneral + "  from:" + from
    //    + "  to:" + to + "  tailLines: " + tailLines );

    Stream<BufferLine> bufferStream = buffer.subMap( from, true, to, true ).values().stream();
    if ( !Utils.isEmpty( channelId ) ) {
      bufferStream = bufferStream.filter( line -> {
        String logChannelId = getLogChId( line );
        return includeGeneral ? isGeneral( logChannelId ) || channelId.contains( logChannelId )
          : channelId.contains( logChannelId );
      } );
    }

    if ( tailLines > 0 ) {
      //We have to make a list so we can get the line number since we can't tap a stream twice
      List<BufferLine> bl = bufferStream.collect( Collectors.toList() );
      int count = bl.size();  //Is this expensive? Not sure but just compute it once
      if ( count > tailLines ) {
        bl = bl.subList( count - tailLines, count );
        tailMap.put( parentLogChannelId, bl.get( 0 ).getNr() ); //remember the first item so we can start there next time
      }
      return bl.stream().map( BufferLine:: getEvent ).collect( Collectors.toList() );
    }

    return bufferStream.map( BufferLine::getEvent ).collect( Collectors.toList() );

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
    return getLogBufferFromTo( parentLogChannelId, includeGeneral, from, to, 0 );
  }

  /**
   * @param parentLogChannelId the parent log channel ID to grab
   * @param includeGeneral     include general log lines
   * @param from
   * @param to
   * @param tailLines Limit output to the last {tailLines} lines, if non-zero
   * @return
   */
  public List<KettleLoggingEvent> getLogBufferFromTo( String parentLogChannelId, boolean includeGeneral, int from,
                                                      int to, int tailLines ) {
    // Typically, the log channel id is the one from the transformation or job running currently.
    // However, we also want to see the details of the steps etc.
    // So we need to look at the parents all the way up if needed...
    //
    List<String> childIds = loggingRegistry.getLogChannelChildren( parentLogChannelId );

    return getLogBufferFromTo( childIds, includeGeneral, from, to, tailLines, parentLogChannelId );
  }

  public StringBuffer getBuffer( String parentLogChannelId, boolean includeGeneral, int startLineNr, int endLineNr ) {
    return getBuffer( parentLogChannelId, includeGeneral, startLineNr, endLineNr, 0 );
  }

  public StringBuffer getBuffer( String parentLogChannelId, boolean includeGeneral, int startLineNr, int endLineNr,
                                 int tailLines ) {
    StringBuilder eventBuffer = new StringBuilder( 10000 );

    List<KettleLoggingEvent> events = getLogBufferFromTo( parentLogChannelId, includeGeneral, startLineNr, endLineNr, tailLines );
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

  /**
   * This method also returns the last line number tapped to the sender in the form of a mutableInt
   * @param parentLogChannelId
   * @param includeGeneral
   * @param newLastNr receives the starting line number, returns the getLastBufferLineNr() to the caller
   * @return
   */
  public StringBuffer getBuffer( String parentLogChannelId, boolean includeGeneral, MutableInt newLastNr ) {
    int startLine = newLastNr.addAndGet( 1 );
    newLastNr.setValue( getLastBufferLineNr() );
    return getBuffer( parentLogChannelId, includeGeneral, startLine, newLastNr.getValue() );
  }

  public StringBuffer getBuffer() {
    return getBuffer( null, true );
  }

  public void close() {
  }

  public void doAppend( KettleLoggingEvent event ) {
    if ( event.getMessage() instanceof LogMessage ) {
      BufferLine bufferLine = new BufferLine( event );
      buffer.put( bufferLine.getNr(), bufferLine );
      while ( bufferSize > 0 && buffer.size() > bufferSize ) {
        buffer.remove( buffer.firstKey() );
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
    buffer.values().stream().filter( line -> id.equals( getLogChId( line ) ) ).forEach( line -> buffer.remove( line.getNr() ) );
    tailMap.remove( id );

/*    for ( BufferLine line : buffer.values() ) {
      if ( id.equals( getLogChId( line ) ) ) {
        buffer.remove( line.getNr() );
      }
    }*/
  }

  public int size() {
    return buffer.size();
  }

  public void removeGeneralMessages() {
    for ( BufferLine line : buffer.values() ) {
      if ( isGeneral( getLogChId( line ) ) ) {
        buffer.remove( line.getNr() );
      }
    }
  }

  /**
   * We should not expose iterator out of the class.
   * Looks like it's only used in tests.
   *
   * Marked deprecated for now.
   * TODO: To be made package-level in future.
   */
  @Deprecated
  @VisibleForTesting
  public Iterator<BufferLine> getBufferIterator() {
    return buffer.values().iterator();
  }

  /**
   * It looks like this method is not used in the project.
   */
  @Deprecated
  public String dump() {
    StringBuilder buf = new StringBuilder( 50000 );
    buffer.forEach( ( k, v ) -> {
      LogMessage message = (LogMessage) v.getEvent().getMessage();
      buf.append( message.getLogChannelId() ).append( "\t" )
        .append( message.getSubject() ).append( "\n" );
    } );
    return buf.toString();

  }

  /**
   * Was used in a pair with {@link #getBufferLinesBefore(long)}.
   *
   * @deprecated in favor of {@link #removeBufferLinesBefore(long)}.
   */
  @Deprecated
  public void removeBufferLines( List<BufferLine> linesToRemove ) {
    linesToRemove.stream().forEach( v -> buffer.remove( v.getNr() ) );
  }

  /**
   * Was used in a pair with {@link #removeBufferLines(List)}.
   *
   * @deprecated in favor of {@link #removeBufferLinesBefore(long)}.
   */
  @Deprecated
  public List<BufferLine> getBufferLinesBefore( long minTimeBoundary ) {
    return buffer.values().stream().filter( v -> v.getEvent().timeStamp < minTimeBoundary ).collect( Collectors.toList() );
  }

  public void removeBufferLinesBefore( long minTimeBoundary ) {
    buffer.values().stream().filter( v -> v.getEvent().timeStamp < minTimeBoundary ).forEach( v -> buffer.remove( v.getNr() ) );
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

  private static String getLogChId( BufferLine bufferLine ) {
    return ( (LogMessage) bufferLine.getEvent().getMessage() ).getLogChannelId();
  }
}
