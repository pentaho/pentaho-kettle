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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoggingRegistry {
  private static LoggingRegistry registry = new LoggingRegistry();
  private Map<String, LoggingObjectInterface> map;
  private Map<String, LogChannelFileWriterBuffer> fileWriterBuffers;
  private Map<String, List<String>> childrenMap;
  private Date lastModificationTime;
  private int maxSize;
  private final int DEFAULT_MAX_SIZE = 10000;

  private Object syncObject = new Object();

  private LoggingRegistry() {
    this.map = new ConcurrentHashMap<String, LoggingObjectInterface>();
    this.childrenMap = new ConcurrentHashMap<String, List<String>>();
    this.fileWriterBuffers = new ConcurrentHashMap<>();

    this.lastModificationTime = new Date();
    this.maxSize = Const.toInt( EnvUtil.getSystemProperty( "KETTLE_MAX_LOGGING_REGISTRY_SIZE" ), DEFAULT_MAX_SIZE );
  }

  public static LoggingRegistry getInstance() {
    return registry;
  }

  public String registerLoggingSource( Object object ) {
    synchronized ( this.syncObject ) {

      LoggingObject loggingSource = new LoggingObject( object );

      LoggingObjectInterface found = findExistingLoggingSource( loggingSource );
      if ( found != null ) {
        LoggingObjectInterface foundParent = found.getParent();
        LoggingObjectInterface loggingSourceParent = loggingSource.getParent();
        String foundLogChannelId = found.getLogChannelId();
        if ( foundParent != null && loggingSourceParent != null ) {
          String foundParentLogChannelId = foundParent.getLogChannelId();
          String sourceParentLogChannelId = loggingSourceParent.getLogChannelId();
          if ( foundParentLogChannelId != null && sourceParentLogChannelId != null
            && foundParentLogChannelId.equals( sourceParentLogChannelId ) ) {
            if ( foundLogChannelId != null ) {
              return foundLogChannelId;
            }
          }
        }
        if ( foundParent == null && loggingSourceParent == null ) {
          if ( foundLogChannelId != null ) {
            return foundLogChannelId;
          }
        }
      }

      String logChannelId = UUID.randomUUID().toString();
      loggingSource.setLogChannelId( logChannelId );

      this.map.put( logChannelId, loggingSource );

      if ( loggingSource.getParent() != null ) {
        String parentLogChannelId = loggingSource.getParent().getLogChannelId();
        if ( parentLogChannelId != null ) {
          List<String> parentChildren =
            this.childrenMap.computeIfAbsent( parentLogChannelId, k -> new ArrayList<String>() );
          parentChildren.add( logChannelId );
        }
      }

      this.lastModificationTime = new Date();
      loggingSource.setRegistrationDate( this.lastModificationTime );

      if ( ( this.maxSize > 0 ) && ( this.map.size() > this.maxSize ) ) {
        List<LoggingObjectInterface> all = new ArrayList<LoggingObjectInterface>( this.map.values() );
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
        Set<String> channelsNotToRemove = getLogChannelFileWriterBufferIds();
        for ( int i = 0; i < cutCount; i++ ) {
          LoggingObjectInterface toRemove = all.get( i );
          if ( !channelsNotToRemove.contains( toRemove.getLogChannelId() ) ) {
            this.map.remove( toRemove.getLogChannelId() );
          }
        }
        removeOrphans();
      }
      return logChannelId;
    }
  }

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

  public LoggingObjectInterface getLoggingObject( String logChannelId ) {
    return this.map.get( logChannelId );
  }

  public Map<String, LoggingObjectInterface> getMap() {
    return this.map;
  }

  public List<String> getLogChannelChildren( String parentLogChannelId ) {
    if ( parentLogChannelId == null ) {
      return null;
    }
    List<String> list = getLogChannelChildren( new ArrayList<String>(), parentLogChannelId );
    list.add( parentLogChannelId );
    return list;
  }

  private List<String> getLogChannelChildren( List<String> children, String parentLogChannelId ) {
    synchronized ( this.syncObject ) {
      List<String> list = this.childrenMap.get( parentLogChannelId );
      if ( list == null ) {
        // Don't do anything, just return the input.
        return children;
      }

      Iterator<String> kids = list.iterator();
      while ( kids.hasNext() ) {
        String logChannelId = kids.next();

        // Add the children recursively
        getLogChannelChildren( children, logChannelId );

        // Also add the current parent
        children.add( logChannelId );
      }
    }

    return children;
  }

  public Date getLastModificationTime() {
    return this.lastModificationTime;
  }

  public String dump( boolean includeGeneral ) {
    StringBuilder out = new StringBuilder( 50000 );
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
    return out.toString();
  }

  /**
   * For junit testing purposes
   * @return ro items map
   */
  Map<String, LoggingObjectInterface> dumpItems() {
    return Collections.unmodifiableMap( this.map );
  }

  /**
   * For junit testing purposes
   * @return ro parent-child relations map
   */
  Map<String, List<String>> dumpChildren() {
    return Collections.unmodifiableMap( this.childrenMap );
  }

  public void removeIncludingChildren( String logChannelId ) {
    synchronized ( this.map ) {
      List<String> children = getLogChannelChildren( logChannelId );
      for ( String child : children ) {
        this.map.remove( child );
      }
      this.map.remove( logChannelId );
      removeOrphans();
    }
  }

  public void removeOrphans() {
    // Remove all orphaned children
    this.childrenMap.keySet().retainAll( this.map.keySet() );
  }

  public void registerLogChannelFileWriterBuffer( LogChannelFileWriterBuffer fileWriterBuffer ) {
    this.fileWriterBuffers.put( fileWriterBuffer.getLogChannelId(), fileWriterBuffer );
  }

  public LogChannelFileWriterBuffer getLogChannelFileWriterBuffer( String id ) {
    for ( String bufferId : this.fileWriterBuffers.keySet() ) {
      if ( getLogChannelChildren( bufferId ).contains( id ) ) {
        return this.fileWriterBuffers.get( bufferId );
      }
    }
    return null;
  }

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

  public void removeLogChannelFileWriterBuffer( String id ) {
    Set<String> bufferIds = this.fileWriterBuffers.keySet();

    for ( String bufferId : bufferIds ) {
      if ( getLogChannelChildren( id ).contains( bufferId ) ) {
        this.fileWriterBuffers.remove( bufferId );
      }
    }
  }
}
