/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.base;

import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

/**
 * This cache lives for the runtime of the parent Job or Trans.  It is used to cache the JobMeta or TransMeta of any kjb
 * or ktr that it needs to load to the course of the main job/trans execution.  The intent here is to minimize the
 * overhead of loading and parsing the jobs/transformations into their associated metas.  Loading a ktr/kjb will require
 * a call to the server if attached.  If that kjb/ktr is called multiple times during the run, then each execution will
 * again make the call to get the meta.  This cache eliminates the need to repetitively load the kjb/ktr.
 */
public interface IMetaFileCache {
  /**
   * Each new job/transformation should call this method once to get the instance of the cache for the currently running
   * job.  This instance should then be passed to the new job's/transformation's meta by calling {@link
   * #setCacheInstance} after the Meta has been created. ThereAfter the cache can be referenced through {@link
   * org.pentaho.di.job.JobMeta#getMetaFileCache}
   *
   * @param parentJob The parent job, or null if none.
   * @return the IMetaFileCache in play
   */
  static IMetaFileCache initialize( Job parentJob, LogChannelInterface logger ) {
    IMetaFileCache cache = null;
    if ( parentJob != null ) {
      cache = parentJob.getJobMeta().getMetaFileCache(); //Pass cache reference from parent to lower level
    } else {
      cache = new MetaFileCacheImpl( logger ); //Top level job.  Create a new cache
    }
    return cache;
  }

  /**
   * Each new transformation should call this method once to get the instance of the cache for the currently running
   * parent transformation/job.  This instance should then be passed to the new job's/transformation's meta by calling
   * {@link #setCacheInstance} after the Meta has been created. ThereAfter the cache can be referenced through {@link
   * org.pentaho.di.trans.TransMeta#getMetaFileCache}
   *
   * @param parentTrans The parent job, or null if none.
   * @return the IMetaFileCache in play
   */
  static IMetaFileCache initialize( Trans parentTrans, LogChannelInterface logger ) {
    IMetaFileCache cache = null;
    if ( parentTrans != null && parentTrans.getTransMeta() != null ) {
      cache = parentTrans.getTransMeta().getMetaFileCache(); //Pass cache reference from parent to lower level
    } else {
      cache = new MetaFileCacheImpl( logger ); //Top level job.  Create a new cache
    }
    return cache;
  }

  static void setCacheInstance( JobMeta jobMeta, IMetaFileCache cache ) {
    jobMeta.setMetaFileCache( cache );
  }

  static void setCacheInstance( TransMeta transMeta, IMetaFileCache cache ) {
    transMeta.setMetaFileCache( cache );
  }

  JobMeta getCachedJobMeta( String key );

  TransMeta getCachedTransMeta( String key );

  void cacheMeta( String key, JobMeta meta );

  void cacheMeta( String key, TransMeta meta );

  void logCacheSummary( LogChannelInterface logger );

  /**
   * Keys to the cache should always be generated using a method provided by this interface.  This ensures that keys
   * will remain unique across different nameSpace catagories.
   *
   * @param specificationMethod Defines different sources for the Meta defined by the {@link
   *                            org.pentaho.di.core.ObjectLocationSpecificationMethod} enum.
   * @param realFilenameOrId    Contains the full file path or jackrabbit Id for the node
   * @return The key to use to load or save an item to the cache.
   */
  default String getKey( ObjectLocationSpecificationMethod specificationMethod, String realFilenameOrId ) {
    return specificationMethod.name() + ":" + realFilenameOrId;
  }
}
