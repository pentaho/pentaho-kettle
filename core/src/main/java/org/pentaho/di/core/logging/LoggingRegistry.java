/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class keeps track of all LoggingObjects that implement the LoggingObjectInterface which register with the
 * LoggingRegistry. It is a static singleton, and manages the in memory LoggingObjects as well as LoggingObjects that
 * have FileWriter Buffers.
 *
 * The class provides a central and thread safe place to register, remove and find LoggingObjects held in memory.
 *
 * It uses a purgeTimer task to attempt to remove older registry objects as the internal map reaches the maxSize which
 * is defined by the Kettle Property "KETTLE_MAX_LOGGING_REGISTRY_SIZE".
 */
public class LoggingRegistry {
  /** private static reference **/
  private static LoggingRegistry registry = new LoggingRegistry();

  /** Registry's LoggingObject Map containing objects keyed by object's LogChannelID **/
  private Map<String, LoggingObjectInterface> map;

  /** Registry's FileWriterBuffer Map containing objects keyed by object's LogChannelID **/
  private Map<String, LogChannelFileWriterBuffer> fileWriterBuffers;

  /** Map containing a list of LogChannelIds that belong to a parent Object. The key is the parent's LogChannelID **/
  private Map<String, List<String>> childrenMap;

  /** Registry's PurgeQueue where Registry will remove entries based on FIFO performed on PurgeTimer Task **/
  private Queue<LoggingObjectInterface> registerPurgeQueue;

  /** Last time a Logging object was registered to the Registry **/
  private Date lastModificationTime;

  /** Max Size of the Registry that it shall purge based on. Note: This is not a strict/hard limit, the Map object can
   * grow above this number and will it attempt to "purge" below it. **/
  private int maxSize;
  private static final int DEFAULT_MAX_SIZE = 10000;

  /** Timer object that executes Purge method based on purgeTimeout. **/
  private Timer purgeTimer;
  private int purgeTimeout;
  private static final int DEFAULT_PURGE_TIMER = 60000;

  // Statistics
  /** Stat that counts the amount of the times the purge task is invoked.**/
  private int purgeTimerCount;
  /** Stat that counts the amount of Objects removed from registry map.**/
  private int purgedObjectCount;
  private boolean purgeTimerScheduledAlready = false;

  /** Sync object **/
  private final Object syncObject = new Object();

  private LoggingRegistry() {
    this.map = new ConcurrentHashMap<>();
    this.childrenMap = new ConcurrentHashMap<>();
    this.fileWriterBuffers = new ConcurrentHashMap<>();
    this.registerPurgeQueue = new ConcurrentLinkedQueue<>();

    this.lastModificationTime = new Date();
    this.purgeTimerCount = 0;
    this.purgedObjectCount = 0;
  }

  public static LoggingRegistry getInstance() {
    return registry;
  }


  /**
   * Register Method for objects that implement the LoggingObjectInterface which adds them to the LoggingRegistry. Note,
   * this defaults the object to be purged by the registry.
   * @param object  the object to register.
   * @return  the LogChanelID which is a String UUID key.
   */
  public String registerLoggingSource( Object object ) {
    return registerLoggingSource( object, true );
  }

  /**
   * Register Method for objects that implement the LoggingObjectInterface which adds them to the LoggingRegistry,
   * Includes a flag to identify loggingObjects that should not be removed from the registry i.e. Singleton classes or
   * "General" that stick around the life of the application.
   *
   * @param object  the object to register.
   * @param isPurgeable  true will flag the object to be purged if needed.
   * @return  the LogChanelID which is a String UUID key.
   */
  public String registerLoggingSource( Object object, boolean isPurgeable ) {
    LoggingObject loggingSource = new LoggingObject( object );

    // First attempt to find an existing LoggingSource if so return it, instead of adding a duplicate.
    LoggingObjectInterface found = findExistingLoggingSource( loggingSource );

    if ( found != null ) {
      String foundLogChannelId = determineExistingLoggingSource( loggingSource, found );
      if ( !foundLogChannelId.isEmpty() ) {
        return foundLogChannelId;
      }
    }

    // Existing not found attempt to register the logging Source.
    synchronized ( this.syncObject ) {

      String logChannelId = UUID.randomUUID().toString();
      loggingSource.setLogChannelId( logChannelId );

      this.map.put( logChannelId, loggingSource );

      // If this is a child to something ensure it gets added to the Parents list of children.
      if ( loggingSource.getParent() != null ) {
        String parentLogChannelId = loggingSource.getParent().getLogChannelId();
        if ( parentLogChannelId != null ) {
          List<String> parentChildren =
            this.childrenMap.computeIfAbsent( parentLogChannelId, k -> new ArrayList<>() );
          parentChildren.add( logChannelId );
        }
      }

      this.lastModificationTime = new Date();
      loggingSource.setRegistrationDate( this.lastModificationTime );

      // If purgeable add it the PurgeQueue which will clean up the registry over time.
      if ( isPurgeable ) {
        this.registerPurgeQueue.add( loggingSource );
      }

      return logChannelId;
    }
  }

  /**
   * Finds an Existing LoggingObjectInterface in the registry using the LoggingObject's equals method.
   * @param loggingObject  the object to search for.
   * @return  the LoggingObjectInterface if found, null otherwise.
   */
  public LoggingObjectInterface findExistingLoggingSource( LoggingObjectInterface loggingObject ) {
    LoggingObjectInterface found = null;
    for ( LoggingObjectInterface verify : this.map.values() ) {
      if ( loggingObject.equals( verify ) ) {
        found = verify;
        break;
      }
    }
    return found;
  }

  /**
   * Determine if the "found" existing LoggingSource matches the loggingSource that is be attempted to register.
   * @param loggingSource  LoggingObjectInterface that is being registered
   * @param foundSource  LoggingObjectInterface which was "found"
   * @return LogChannelID if found otherwise empty string.
   */
  private String determineExistingLoggingSource( LoggingObjectInterface loggingSource, LoggingObjectInterface foundSource ) {
    LoggingObjectInterface foundParent = foundSource.getParent();
    LoggingObjectInterface loggingSourceParent = loggingSource.getParent();
    String foundLogChannelId = foundSource.getLogChannelId();

    if ( !foundLogChannelId.isEmpty() ) {

      if ( foundParent != null && loggingSourceParent != null ) {
        String foundParentLogChannelId = foundParent.getLogChannelId();
        String sourceParentLogChannelId = loggingSourceParent.getLogChannelId();

        if ( foundParentLogChannelId != null && foundParentLogChannelId.equals( sourceParentLogChannelId ) ) {
          return foundLogChannelId;
        }
      }

      if ( foundParent == null && loggingSourceParent == null  ) {
        return foundLogChannelId;
      }
    }
    return "";
  }

  /**
   * Returns a LoggingObjectInterface based on a LogChannelID which is the key for the LoggingRegistry.
   * @param logChannelId  the logChannelID to search for.
   * @return  the LoggingObjectInterface if found, null otherwise.
   */
  public LoggingObjectInterface getLoggingObject( String logChannelId ) {
    return this.map.get( logChannelId );
  }

  /**
   * @deprecated This is unsafe call and references to this method will be removed.
   */
  @Deprecated
  public Map<String, LoggingObjectInterface> getMap() {
    return this.map;
  }

  /**
   * Updates Class variables from the systemProperties / kettle.properties.
   */
  public void updateFromProperties( ) {
    this.maxSize = Const.toInt( EnvUtil.getSystemProperty( Const.KETTLE_MAX_LOGGING_REGISTRY_SIZE ), DEFAULT_MAX_SIZE );
    this.purgeTimeout = Const.toInt( EnvUtil.getSystemProperty( Const.KETTLE_LOGGING_REGISTRY_PURGE_TIMEOUT ),
      DEFAULT_PURGE_TIMER );
  }

  /**
   * Schedule the Purge Timer.
   */
  public void schedulePurgeTimer() {
    if ( !purgeTimerScheduledAlready ) {
      installPurgeTimer();
      purgeTimerScheduledAlready = true;
    }
  }


  /**
   * Searches for a LogChannel and returns a list of children IDs.
   * @param parentLogChannelId  The ID of the parent to search for.
   * @return  a list of LogChannelID's that are the children of the parent object (parent also included on the list).
   */
  public List<String> getLogChannelChildren( String parentLogChannelId ) {
    if ( parentLogChannelId == null ) {
      return new ArrayList<>();  // Return emtpy List.
    }
    List<String> list = getLogChannelChildren( new ArrayList<>(), parentLogChannelId );
    list.add( parentLogChannelId );
    return list;
  }

  /**
   * Helper method for the public getLogChannelChildren method that is recursive.
   * @param children  the list of children to populate
   * @param parentLogChannelId  LogChannel to search for children of.
   * @return  returns a populated list of children's LogChannelID's.
   */
  private List<String> getLogChannelChildren( List<String> children, String parentLogChannelId ) {
    synchronized ( this.syncObject ) {
      List<String> list = this.childrenMap.get( parentLogChannelId );
      if ( list == null ) {
        // Don't do anything, just return the input.
        return children;
      }

      for ( String logChannelId : list ) {
        // Add the children recursively
        getLogChannelChildren( children, logChannelId );

        // Also add the current parent
        children.add( logChannelId );
      }
    }

    return children;
  }

  /**
   * @return Last Modification Time of the registry.
   */
  public Date getLastModificationTime() {
    return this.lastModificationTime;
  }

  /**
   * Returns the contents of the Registry map as a string.
   * @param includeGeneral  Flag when true includes LoggingObjects of type GENERAL.
   * @return  String containing logging object.
   */
  public String dump( boolean includeGeneral ) {
    StringBuilder out = new StringBuilder( 50000 );
    for ( LoggingObjectInterface o : this.map.values() ) {
      if ( ( includeGeneral ) || ( !o.getObjectType().equals( LoggingObjectType.GENERAL ) ) ) {
        out.append( o.getContainerObjectId() );
        out.append( '\t' );
        out.append( o.getLogChannelId() );
        out.append( '\t' );
        out.append( o.getObjectType().name() );
        out.append( '\t' );
        out.append( o.getObjectName() );
        out.append( '\t' );
        out.append( o.getParent() != null ? o.getParent().getLogChannelId() : "-" );
        out.append( '\t' );
        out.append( o.getParent() != null ? o.getParent().getObjectType().name() : "-" );
        out.append( '\t' );
        out.append( o.getParent() != null ? o.getParent().getObjectName() : "-" );
        out.append( '\n' );
      }
    }
    return out.toString();
  }


  /**
   * Removes a LoggingObject entry and all its children, based on a LogChannelID.
   * @param logChannelId  LogChannelID of the parent object to remove.
   */
  public void removeIncludingChildren( String logChannelId ) {
    synchronized ( this.syncObject ) {
      // Collect all Log Channel IDs that are descendants of the given one
      List<String> children = getLogChannelChildren( logChannelId );

      // Remove from the Registry's Map
      children.forEach( s -> this.map.remove( s ) );

      // Remove from the Registry's PurgeQueue
      this.registerPurgeQueue.removeIf( loi -> children.contains( loi.getLogChannelId() ) );

      // Remove from the Registry Children's Map - well, technically this removes ALL orphans :-)
      removeOrphans();
    }
  }

  /**
   * Cleans up internal map of children that do not have parent in the main registry map.
   */
  public void removeOrphans() {
    // Remove all orphaned children
    synchronized ( this.syncObject ) {
      this.childrenMap.keySet().retainAll( this.map.keySet() );
    }
  }

  /**
   * Registers a LogChannelFileWriterBuffer with the registry. Existing Buffer must have a logChannelID set.
   * @param fileWriterBuffer  Object to register.
   */
  public void registerLogChannelFileWriterBuffer( LogChannelFileWriterBuffer fileWriterBuffer ) {
    synchronized ( this.syncObject ) {
      this.fileWriterBuffers.put( fileWriterBuffer.getLogChannelId(), fileWriterBuffer );
    }
  }

  /**
   * Searches and returns LogChannelFileWriterBuffer object based on buffer's LogChannelID.
   * @param id  LogChannelID to search for.
   * @return  Buffer Object, null if can't be determined.
   */
  public LogChannelFileWriterBuffer getLogChannelFileWriterBuffer( String id ) {
    synchronized ( syncObject ) {
      LogChannelFileWriterBuffer fileWriterBuffer = this.fileWriterBuffers.get( id );
      if ( fileWriterBuffer != null ) {
        return fileWriterBuffer;
      }

      ConcurrentHashMap<LogChannelFileWriterBuffer, List<String>> possibleWriters = new ConcurrentHashMap<>();

      for ( Map.Entry<String, LogChannelFileWriterBuffer> entry : this.fileWriterBuffers.entrySet() ) {
        final String bufferId = entry.getKey();
        List<String> logChannelChildren = getLogChannelChildren( bufferId );
        if ( logChannelChildren.contains( id ) ) {
          possibleWriters.put( entry.getValue(), logChannelChildren );
        }
      }

      return determineLogChannelFileWriterBuffer( possibleWriters );
    }
  }

  /**
   * Helper Method that determines a LogChannelFileWriterBuffer invoked by getLogChannelFileWriterBuffer and returns 1.
   * @param possibleWriters  Map to search from.
   * @return LogChannelFileWriterBuffer, null if could not be determined.
   */
  private LogChannelFileWriterBuffer determineLogChannelFileWriterBuffer( ConcurrentHashMap<LogChannelFileWriterBuffer,
    List<String>> possibleWriters ) {

    // Just one writer so just return it
    if ( possibleWriters.size() == 1 ) {
      return possibleWriters.keys().nextElement();
    } else {

      // Several possibilities, so, lets get the writer among them that is the "lowest in the chain",
      // meaning, the one that is not a parent of the others
      Enumeration<LogChannelFileWriterBuffer> possibleWritersIds = possibleWriters.keys();
      while ( possibleWritersIds.hasMoreElements() ) {
        LogChannelFileWriterBuffer writer = possibleWritersIds.nextElement();

        for ( Map.Entry<LogChannelFileWriterBuffer, List<String>> entry : possibleWriters.entrySet() ) {
          if ( entry.getKey().equals( writer ) ) {
            continue;
          }
          if ( !entry.getValue().contains( writer.getLogChannelId() ) ) {
            return entry.getKey();
          }
        }
      }
    }

    return null;
  }

  /**
   * Returns a Set of LogChannelIds associated with a FileWriterBuffer which includes a parent and its children.
   * @return  A set of LogChannelIds to avoid duplicates.
   */
  protected Set<String> getLogChannelFileWriterBufferIds() {
    Set<String> bufferIds = this.fileWriterBuffers.keySet();

    // Changed to a set as a band-aid for PDI-16658. This stuff really should be done
    // using a proper LRU cache.
    Set<String> ids = new HashSet<>();
    for ( String id : bufferIds ) {
      ids.addAll( getLogChannelChildren( id ) );
    }

    ids.addAll( bufferIds );
    return ids;
  }

  /**
   * Removes a LogChannelFileWriterBuffer by LogChannelId.
   * @param id  LogChannelId of the object to remove.
   */
  public void removeLogChannelFileWriterBuffer( String id ) {
    synchronized ( this.syncObject ) {
      for ( String bufferId : this.fileWriterBuffers.keySet() ) {
        if ( getLogChannelChildren( id ).contains( bufferId ) ) {
          this.fileWriterBuffers.remove( bufferId );
        }
      }
    }
  }

  /**
   * Resets all internal memory objects and counters.
   */
  public void reset() {
    synchronized ( this.syncObject ) {
      map.clear();
      childrenMap.clear();
      fileWriterBuffers.clear();
      registerPurgeQueue.clear();

      purgeTimerCount = 0;
      purgedObjectCount = 0;

      if ( purgeTimer != null ) {
        purgeTimer.cancel();
        purgeTimer.purge();
        purgeTimer = new Timer( "LoggingRegistryPurgeTimer", true );
        installPurgeTimer();
      }
    }
  }

  /**
   * Method that performs the cleanup the Registry on the PurgeTimerTasks.
   */
  private void purgeRegistry() {

    if ( ( maxSize > 0 )
            && ( ( map.size() > maxSize ) || ( registerPurgeQueue.size() > maxSize ) ) ) {

      synchronized ( syncObject ) {
        Set<String> channelsNotToRemove = getLogChannelFileWriterBufferIds();

        logDebug( String.format( "LoggingRegistry Stats:%n   MapSize= %d | PurgeQueueSize= %d | ChannelsNotToRemoveSize= %d | MaxSize= %d",
          map.size(), registerPurgeQueue.size(), channelsNotToRemove.size(), maxSize ) );

        // Let's start by cleaning the Registry's PurgeQueue by removing already purged objects
        // If this is not done, these objects will not be able to be garbage collected!
        registerPurgeQueue.removeIf( it -> !map.containsKey( it.getLogChannelId() ) );

        // The goal is to drop the size to 90% of the maximum configured or, if higher, 110% of the currently
        // "active" channels (the extra 10% is to not completely eradicate all other objects)
        int cutCount = (int) ( map.size() - Math.max( 0.9 * maxSize, 1.1 * channelsNotToRemove.size() ) );

        if ( cutCount <= 0 ) {
          // No point to attempt purge channels as there are more "active" channels that can be safely removed.
          logBasic( "Logging Registry is unable to purge LogChannels since there are too many active channels. "
            + "We recommend increasing the LoggingRegistry Size "
            + "(KETTLE_MAX_LOGGING_REGISTRY_SIZE) in kettle.properties." );
        } else {
          int limitSize = registerPurgeQueue.size(); // Never attempt to iterate more than the Size of the queue.
          int limitCounter = 0; // prevent locking loops
          int cutCounter = 0;

          // Avoid attempting to remove channels that can not be removed.
          cutCount -= channelsNotToRemove.size();

          // Attempt to purge LogChannels based on CutCount. Limit Size prevents looping longer than the size of the queue.
          do {
            if ( purgeObject( channelsNotToRemove ) ) {
              cutCounter++;
            }
            limitCounter++;

          } while ( !registerPurgeQueue.isEmpty() && cutCounter < cutCount && limitCounter < limitSize );

          logDebug( String.format( "LoggingRegistry Stats:%n   MapSize= %d | PurgeQueueSize= %d | CutCounter= %d | limitCounter= %d",
            map.size(), registerPurgeQueue.size(), cutCounter, limitCounter ) );
        }

        removeOrphans();
        purgeTimerCount++;
      }
    }

    logDebug( String.format( "LoggingRegistry Stats:%n   MapSize= %d | PurgeQueueSize= %d | PurgeCount= %d | PurgeObjectCount= %d ",
      map.size(), registerPurgeQueue.size(), purgeTimerCount, purgedObjectCount ) );
  }

  /**
   * Helper method that purges the single object
   * @param channelsNotToRemove  Set of LogChannelIds not remove.
   * @return boolean true if object was removed.
   */
  private boolean purgeObject( Set<String> channelsNotToRemove ) {
    boolean result = false;

    // Remove an item from the Queue and attempt to remove it.
    LoggingObjectInterface obj = registerPurgeQueue.poll();

    if ( obj != null && !obj.getLogChannelId().isEmpty()  ) {

      String objId = obj.getLogChannelId();

      // Only Objects that are tied to a buffer can be purged.
      if ( !channelsNotToRemove.contains( objId ) ) {
        // Object is safe to remove, but the counter for purged objects will only be incremented if it is really
        // removed from the map as it's possible for the object to not exist on the map.
        if ( null != map.remove( objId ) ) {
          purgedObjectCount++;
          result = true;
        }
      } else {
        // Object can't be removed right now add it back to the queue to remove it later
        registerPurgeQueue.add( obj );
      }
    }
    return result;
  }

  /**
   * Helper method to avoid logging when the logStore is not ready.
   * @param msg string to log.
   */
  private void logDebug( String msg ) {
    try {
      if ( KettleLogStore.isInitialized() ) {
        LogChannel.GENERAL.logDebug( msg );
      }
    } catch ( RuntimeException ignored ) {
      // Ignore this can occur if LogStore is not ready.
    }
  }

  /**
   * Helper method to avoid logging when the logStore is not ready.
   * @param msg string to log.
   */
  private void logError( String msg ) {
    try {
      if ( KettleLogStore.isInitialized() ) {
        LogChannel.GENERAL.logError( msg );
      }
    } catch ( RuntimeException ignored ) {
      // Ignore this can occur if LogStore is not ready.
    }
  }

  /**
   * Helper method to avoid logging when the logStore is not ready.
   * @param msg string to log.
   */
  private void logBasic( String msg ) {
    try {
      if ( KettleLogStore.isInitialized() ) {
        LogChannel.GENERAL.logBasic( msg );
      }
    } catch ( RuntimeException ignored ) {
      // Ignore this can occur if LogStore is not ready.
    }
  }

  /**
   * Setups and schedules the PurgeTimer task.
   */
  private void  installPurgeTimer( ) {

    if ( purgeTimer == null ) {
      purgeTimer = new Timer( "LoggingRegistryPurgeTimer", true );
    }

    final AtomicBoolean busy = new AtomicBoolean( false );

    TimerTask timerTask = new TimerTask() {
      public void run() {
        if ( busy.compareAndSet( false, true ) ) {
          try {
            purgeRegistry();
            purgeTimerCount++;

          } finally {
            busy.set( false );
          }
        }
      }
    };
    purgeTimer.schedule( timerTask, purgeTimeout, purgeTimeout );
  }


  /**
   * For junit testing purposes
   * @param maxSize sets maxSize
   */
  @VisibleForTesting
  void setMaxSize( int maxSize ) {
    this.maxSize = maxSize;
  }

  /**
   * For junit testing purposes
    * @param purgeTimeout sets timeout
   */
  @VisibleForTesting
  void setPurgeTimeout( int purgeTimeout ) {
    this.purgeTimeout = purgeTimeout;
  }

  /**
   * For junit testing purposes
   * @return mapSize
   */
  @VisibleForTesting
  int getRegistryMapSize() {
    return this.map.size();
  }

  /**
   * For junit testing purposes
   * @return purgedObjectCount
   */
  @VisibleForTesting
  int getPurgedObjectCount( ) {
    return purgedObjectCount;
  }

  /**
   * For junit testing purposes
   * @return boolean (wrapper for contain)
   */
  @VisibleForTesting
  boolean purgeQueueContains( LoggingObjectInterface obj ) {
    return registerPurgeQueue.contains( obj );
  }


  /**
   * For junit testing purposes
   * @return ro items map
   */
  @VisibleForTesting
  Map<String, LoggingObjectInterface> dumpItems() {
    return Collections.unmodifiableMap( this.map );
  }

  /**
   * For junit testing purposes
   * @return ro parent-child relations map
   */
  @VisibleForTesting
  Map<String, List<String>> dumpChildren() {
    return Collections.unmodifiableMap( this.childrenMap );
  }

  /**
   * Allows for testing purge logic without the timer.
   */
  @VisibleForTesting
  void invokePurge( ) {
    purgeRegistry();
  }

  /**
   * Allows for comparing changes to the purgeTimer Object.
   * @return object hashcode.
   */
  @VisibleForTesting
  int getTimerHashCode( ) {
    return purgeTimer.hashCode();
  }

  @VisibleForTesting
  void setFileWriterBuffers( Map<String, LogChannelFileWriterBuffer> buffers ) {
    fileWriterBuffers = buffers;
  }

  @VisibleForTesting
  void setChildrenMap( Map<String, List<String>> map ) {
    childrenMap = map;
  }
}
