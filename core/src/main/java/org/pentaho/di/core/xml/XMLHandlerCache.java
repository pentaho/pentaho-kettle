/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.xml;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Singleton to help speed up lookups in an XML DOM tree.<br>
 * The theory is that you often loop over occurrences of a certain tag in a Node.<br>
 * If there are 20 occurrences, you go from index 0..19.<br>
 * Every time we do the following<br>
 * - found node 0<br>
 * - found node 0, 1<br>
 * - found node 0, 1, 2<br>
 * - ...<br>
 * So the time to search node index 19 is 20 times larger on average then index 0.<br>
 * <br>
 * We can solve this by caching the position of index 18 and by starting back at that position.<br>
 * <br>
 * This class is a singleton to keep everyting 100% compatible with the rest of the codebase. <br>
 *
 * @author Matt
 * @since 22-Apr-2006
 */
public class XMLHandlerCache {

  private static final XMLHandlerCache instance = new XMLHandlerCache();

  Map<XMLHandlerCacheEntry, Integer> cache;

  private volatile int cacheHits;

  private XMLHandlerCache() {
    cache = Collections.synchronizedMap( new WeakHashMap<XMLHandlerCacheEntry, Integer>() );
    cacheHits = 0;
  }

  public static XMLHandlerCache getInstance() {
    return instance;
  }

  /**
   * Store a cache entry
   *
   * @param entry
   *          The cache entry to store
   */
  public void storeCache( XMLHandlerCacheEntry entry, int lastChildNr ) {
    cache.put( entry, lastChildNr );
  }

  /**
   * Retrieve the last child were we left off...
   *
   * @param entry
   *          The cache entry to look for.
   * @return the last child position or -1 if nothing was found.
   */
  public int getLastChildNr( XMLHandlerCacheEntry entry ) {
    Integer lastChildNr = cache.get( entry );
    if ( lastChildNr != null ) {
      cacheHits++;
      return lastChildNr;
    }
    return -1;
  }

  /**
   * @return the number of cache hits for your statistical pleasure.
   */
  public int getCacheHits() {
    return cacheHits;
  }

  /**
   * Allows you to (re-)set the number of cache hits
   *
   * @param cacheHits
   *          the number of cache hits.
   */
  public void setCacheHits( int cacheHits ) {
    this.cacheHits = cacheHits;
  }

  /**
   * Clears the cache
   *
   */
  public void clear() {
    cache.clear();
  }
}
