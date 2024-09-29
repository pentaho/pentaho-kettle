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

package org.pentaho.di.core.util;

import org.apache.commons.io.FileUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StorageUnitConverter {

  /**
   * Converts byte count to the human readable representation. The size is rounded to nearest X-byte.
   * <p>
   * For example: 13.1MB in byte count will return 13MB and 13.9MB in byte count wil return 13MB.
   * <p>
   *  Supported types: EB, PB, TB, GB, MB, KB or B (for bytes).
   *
   * @param byteCount
   * @return human reabable display size
   */
  public String byteCountToDisplaySize( long byteCount ) {
    String spacedDisplaySize = FileUtils.byteCountToDisplaySize( byteCount );
    return spacedDisplaySize.replace( "bytes", "B" ).replace( " ", "" );
  }

  /**
   * Convert human  human readable file size format to byte equivalent.
   * <p>Accepted units:
   * <ul>
   *   <li>B - Bytes</li>
   *   <li>KB - Kilobytes</li>
   *   <li>MB - Megabytes</li>
   *   <li>GB - Gigabytes</li>
   * </ul>
   * Example display sizes:
   * <br>"5MB"
   * <br>"123B"
   * <br>"1.5GB"
   * <br>"1,5GB"
   * @param displaySize human readable size format
   * @return total number of bytes
   */
  public long displaySizeToByteCount( String displaySize ) {
    long returnValue = -1;
    // replace "," for int'l decimal convention
    String displaySizeDecimal = ( displaySize == null ) ? "" : displaySize.replace( ",", "." );
    Pattern pattern = Pattern.compile( "([\\d.]+)([GMK]?B)", Pattern.CASE_INSENSITIVE );
    Matcher matcher = pattern.matcher( displaySizeDecimal );
    Map<String, Integer> powerMap = new HashMap<>();
    powerMap.put( "GB", 3 );
    powerMap.put( "MB", 2 );
    powerMap.put( "KB", 1 );
    powerMap.put( "B", 0 );
    if ( matcher.find() ) {
      String number = matcher.group( 1 );
      int pow = powerMap.get( matcher.group( 2 ).toUpperCase() );
      BigDecimal bytes = new BigDecimal( number );
      bytes = bytes.multiply( BigDecimal.valueOf( 1024 ).pow( pow ) );
      returnValue = bytes.longValue();
    }
    return returnValue;
  }
}
