/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LoggingRegistry {
  private static LoggingRegistry registry = new LoggingRegistry();
  private Map<String, LoggingObjectInterface> map;
  private Map<String, LogChannelFileWriterBuffer> fileWriterBuffers;
  private Map<String, Set<String>> childrenMap;
  private Map<String, Set<String>> recursiveChildrenMap;
  private Date lastModificationTime;
  private int maxSize;
  private final int DEFAULT_MAX_SIZE = 10000;
  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private LoggingRegistry() {
    // now that we're using Reentrant read/write locking, no need for concurrent maps here
    this.map = new HashMap<>();
    this.childrenMap = new HashMap<>();
    this.fileWriterBuffers = new HashMap<>();
    this.recursiveChildrenMap = new HashMap<>();

    this.lastModificationTime = new Date();
    this.maxSize = Const.toInt( EnvUtil.getSystemProperty( "KETTLE_MAX_LOGGING_REGISTRY_SIZE" ), DEFAULT_MAX_SIZE );
  }

  public static LoggingRegistry getInstance() {
    return registry;
  }

  public String registerLoggingSource( Object object ) {
    LoggingObject loggingSource = new LoggingObject( object );
    LoggingObjectInterface found = findExistingLoggingSource( loggingSource );
    if ( found != null ) {
      LoggingObjectInterface foundParent = found.getParent();
      LoggingObjectInterface loggingSourceParent = loggingSource.getParent();
      if ( foundParent != null && loggingSourceParent != null ) {
        String foundParentLogChannelId = foundParent.getLogChannelId();
        String sourceParentLogChannelId = loggingSourceParent.getLogChannelId();
        if ( foundParentLogChannelId != null && sourceParentLogChannelId != null
            && foundParentLogChannelId.equals( sourceParentLogChannelId ) ) {
          String foundLogChannelId = found.getLogChannelId();
          if ( foundLogChannelId != null ) {
            return foundLogChannelId;
          }
        }
      }
    }

    String logChannelId = UUID.randomUUID().toString();
    loggingSource.setLogChannelId( logChannelId );

    lock.writeLock().lock();
    try {
      this.map.put( logChannelId, loggingSource );
      this.lastModificationTime = new Date();
      this.recursiveChildrenMap.clear();
    } finally {
      lock.writeLock().unlock();
    }
    if ( loggingSource.getParent() != null ) {
      String parentLogChannelId = loggingSource.getParent().getLogChannelId();
      if ( parentLogChannelId != null ) {
        Set<String> parentChildren = this.childrenMap.get( parentLogChannelId );
        if ( parentChildren == null ) {
          // Using a Set based on ConcurrentHashMap because in the method getLogChannelChildren,
          // if the timing is right (ok, wrong), you can get a ConcurrentModificationException
          // if a transformation/job is going away at the same moment you're getting status on it.
          // This only happened once in my testing, but that was enough. It happens during the iteration
          // of the set in getLogChannelChildren - if you search for ConcurrentModificationException below
          // you will see my note.
          //
          // Marc
          parentChildren = ConcurrentHashMap.newKeySet();
          lock.writeLock().lock();
          try {
            this.childrenMap.put( parentLogChannelId, parentChildren );
            this.lastModificationTime = new Date();
            this.recursiveChildrenMap.clear();
          } finally {
            lock.writeLock().unlock();
          }
        }
        parentChildren.add( logChannelId );
      }
    }

    loggingSource.setRegistrationDate( this.lastModificationTime );

    if ( ( this.maxSize > 0 ) && ( this.map.size() > this.maxSize ) ) {
      List<LoggingObjectInterface> all = null;
      lock.readLock().lock();
      try {
        all = new ArrayList<>( this.map.values() );
      } finally {
        lock.readLock().unlock();
      }
      Collections.sort( all, new Comparator<LoggingObjectInterface>() {
        @Override
        public int compare( LoggingObjectInterface o1, LoggingObjectInterface o2 ) {
          if ( ( o1 == null ) && ( o2 != null ) ) {
            return -1;
          }
          if ( ( o1 != null ) && ( o2 == null ) ) {
            return 1;
          }
          if ( ( o1 == null ) && ( o2 == null ) ) {
            return 0;
          }
          if ( o1.getRegistrationDate() == null && o2.getRegistrationDate() != null ) {
            return -1;
          }
          if ( o1.getRegistrationDate() != null && o2.getRegistrationDate() == null ) {
            return 1;
          }
          if ( o1.getRegistrationDate() == null && o2.getRegistrationDate() == null ) {
            return 0;
          }
          return ( o1.getRegistrationDate().compareTo( o2.getRegistrationDate() ) );
        }
      } );
      int cutCount = this.maxSize < 1000 ? this.maxSize : 1000;
      List<String> channelsNotToRemove = getLogChannelFileWriterBufferIds();
      lock.writeLock().lock();
      try {
        for ( int i = 0; i < cutCount; i++ ) {
          LoggingObjectInterface toRemove = all.get( i );
          if ( !channelsNotToRemove.contains( toRemove.getLogChannelId() ) ) {
            this.map.remove( toRemove.getLogChannelId() );
            this.recursiveChildrenMap.clear();
          }
        }
      } finally {
        lock.writeLock().unlock();
      }
      removeOrphans();
    }
    return logChannelId;
  }

  public LoggingObjectInterface findExistingLoggingSource( LoggingObjectInterface loggingObject ) {
    LoggingObjectInterface found = null;
    lock.readLock().lock();
    try {
      for ( LoggingObjectInterface verify : this.map.values() ) {
        if ( loggingObject.equals( verify ) ) {
          found = verify;
          break;
        }
      }
    } finally {
      lock.readLock().unlock();
    }
    return found;
  }

  public LoggingObjectInterface getLoggingObject( String logChannelId ) {
    lock.readLock().lock();
    try {
      return this.map.get( logChannelId );
    } finally {
      lock.readLock().unlock();
    }
  }

  @VisibleForTesting
  public Map<String, LoggingObjectInterface> getMap() {
    return this.map;
  }

  public List<String> getLogChannelChildren( String parentLogChannelId ) {
    if ( parentLogChannelId == null ) {
      return null;
    }
    // Using a Set based on ConcurrentHashMap because in the method getLogChannelChildren,
    // if the timing is right (ok, wrong), you can get a ConcurrentModificationException
    // if a transformation/job is going away at the same moment you're getting status on it.
    // This only happened once in my testing, but that was enough. It happens during the iteration
    // of the set in getLogChannelChildren - if you search for ConcurrentModificationException below
    // you will see my note.
    //
    // Marc
    Set<String> childSet = getLogChannelChildren( ConcurrentHashMap.newKeySet(), parentLogChannelId );
    ArrayList rtn = new ArrayList<String>();
    rtn.addAll( childSet );
    rtn.add( parentLogChannelId );
    return rtn;
  }

  protected Set<String> getLogChannelChildrenSet( String parentLogChannelId ) {
    if ( parentLogChannelId == null ) {
      return null;
    }
    // Using a Set based on ConcurrentHashMap because in the method getLogChannelChildren,
    // if the timing is right (ok, wrong), you can get a ConcurrentModificationException
    // if a transformation/job is going away at the same moment you're getting status on it.
    // This only happened once in my testing, but that was enough. It happens during the iteration
    // of the set in getLogChannelChildren - if you search for ConcurrentModificationException below
    // you will see my note.
    //
    // Marc
    Set<String> childSet = getLogChannelChildren( ConcurrentHashMap.newKeySet(), parentLogChannelId );
    childSet.add( parentLogChannelId );
    return childSet;
  }

  private Set<String> getLogChannelChildren( Set<String> children, String parentLogChannelId ) {
    Set<String> childSet = null;
    lock.readLock().lock();
    try {
      childSet = this.recursiveChildrenMap.get( parentLogChannelId );
      if ( childSet != null ) {
        return childSet;
      }
      childSet = this.childrenMap.get( parentLogChannelId );
    } finally {
      lock.readLock().unlock();
    }
    if ( childSet == null ) {
      // Don't do anything, just return the input.
      return children;
    }

    Iterator<String> kids = childSet.iterator();
    while ( kids.hasNext() ) {
      // This next line will (if the conditions are right) throw a ConcurrentModificationException
      // If using a basic HashSet, and if a transformation/job is shutting down while you are in a
      // status polling loop. See the shell scripts attached on PDI-16658. While running perfTest.sh,
      // it was possible that the polling loop in the shell script would poll at the same time the
      // log channel list was being mutated ( GetJobStatusServlet.doGet ). So using a set backed by
      // a ConcurrentHashMap.
      String logChannelId = kids.next();

      // Add the children recursively
      getLogChannelChildren( children, logChannelId );

      // Also add the current parent
      children.add( logChannelId );
    }
    lock.writeLock().lock();
    try {
      this.recursiveChildrenMap.put( parentLogChannelId, children );
    } finally {
      lock.writeLock().unlock();
    }
    return children;
  }

  public LogChannelFileWriterBuffer getLogChannelFileWriterBuffer( String id ) {
    // Set<String> fileWriterKeySet = null;
    ArrayList<String> fileWriterKeys = null;
    lock.readLock().lock();
    try {
      fileWriterKeys = new ArrayList<>( this.fileWriterBuffers.keySet() ); // copy to arraylist to prevent concurrent modification exception below
    } finally {
      lock.readLock().unlock();
    }
    for ( String bufferId : fileWriterKeys ) {
      if ( getLogChannelChildren( bufferId ).contains( id ) ) {
        lock.readLock().lock();
        try {
          return this.fileWriterBuffers.get( bufferId );
        } finally {
          lock.readLock().unlock();
        }
      }
    }
    return null;
  }


  public Date getLastModificationTime() {
    return this.lastModificationTime;
  }

  public String dump( boolean includeGeneral ) {
    StringBuilder out = new StringBuilder( 50000 );
    lock.readLock().lock();
    try {
      for ( LoggingObjectInterface o : this.map.values() ) {
        if ( ( includeGeneral ) || ( !o.getObjectType().equals( LoggingObjectType.GENERAL ) ) ) {
          out.append( o.getContainerObjectId() );
          out.append( "\t" );
          out.append( o.getLogChannelId() );
          out.append( "\t" );
          out.append( o.getObjectType().name() );
          out.append( "\t" );
          out.append( o.getObjectName() );
          out.append( "\t" );
          out.append( o.getParent() != null ? o.getParent().getLogChannelId() : "-" );
          out.append( "\t" );
          out.append( o.getParent() != null ? o.getParent().getObjectType().name() : "-" );
          out.append( "\t" );
          out.append( o.getParent() != null ? o.getParent().getObjectName() : "-" );
          out.append( "\n" );
        }
      }
    } finally {
      lock.readLock().unlock();
    }
    return out.toString();
  }

  /**
   * For junit testing purposes
   * @return ro items map
   */
  Map<String, LoggingObjectInterface> dumpItems() {
    lock.readLock().lock();
    try {
      return Collections.unmodifiableMap( this.map );
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * For junit testing purposes
   * @return ro parent-child relations map
   */
  Map<String, Set<String>> dumpChildren() {
    lock.readLock().lock();
    try {
      return Collections.unmodifiableMap( this.childrenMap );
    } finally {
      lock.readLock().unlock();
    }
  }

  public void removeIncludingChildren( String logChannelId ) {
    Set<String> children = getLogChannelChildrenSet( logChannelId );
    lock.writeLock().lock();
    try {
      for ( String child : children ) {
        this.map.remove( child );
      }
      this.map.remove( logChannelId );
      this.recursiveChildrenMap.clear();
    } finally {
      lock.writeLock().unlock();
    }
    removeOrphans();
  }

  public void removeOrphans() {
    // Remove all orphaned children
    lock.writeLock().lock();
    try {
      this.childrenMap.keySet().retainAll( this.map.keySet() );
      this.recursiveChildrenMap.clear();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void registerLogChannelFileWriterBuffer( LogChannelFileWriterBuffer fileWriterBuffer ) {
    lock.writeLock().lock();
    try {
      this.fileWriterBuffers.put( fileWriterBuffer.getLogChannelId(), fileWriterBuffer );
      this.recursiveChildrenMap.clear();
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected List<String> getLogChannelFileWriterBufferIds() {
    ArrayList<String> bufferIds = null;
    lock.readLock().lock();
    try {
      bufferIds = new ArrayList<>( this.fileWriterBuffers.keySet() );
    } finally {
      lock.readLock().unlock();
    }

    List<String> ids = new ArrayList<>();
    for ( String id : bufferIds ) {
      ids.addAll( getLogChannelChildren( id ) );
    }

    ids.addAll( bufferIds );
    return ids;
  }

  public void removeLogChannelFileWriterBuffer( String id ) {
    ArrayList<String> bufferIds = null;
    lock.readLock().lock();
    try {
      bufferIds = new ArrayList<>( this.fileWriterBuffers.keySet() );
    } finally {
      lock.readLock().unlock();
    }
    for ( String bufferId : bufferIds ) {
      if ( getLogChannelChildren( id ).contains( bufferId ) ) {
        lock.writeLock().lock();
        try {
          this.fileWriterBuffers.remove( bufferId );
          this.recursiveChildrenMap.clear();
        } finally {
          lock.writeLock().unlock();
        }
      }
    }
  }
}
