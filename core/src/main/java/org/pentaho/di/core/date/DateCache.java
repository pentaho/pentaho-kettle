/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @deprecated This class is not used within PDI
 *
 */
@Deprecated
public class DateCache {

  private Map<String, Date> cache;

  public DateCache() {
    cache = new HashMap<String, Date>();
  }

  public void populate( String datePattern, int fromYear, int toYear ) {
    SimpleDateFormat dateFormat = new SimpleDateFormat( datePattern );

    for ( int year = fromYear; year <= toYear; year++ ) {
      for ( int day = 0; day <= 367; day++ ) {

        Calendar calendar = Calendar.getInstance();
        calendar.set( Calendar.YEAR, year );
        calendar.set( Calendar.DAY_OF_YEAR, day );
        if ( calendar.get( Calendar.YEAR ) == year ) {
          String dateString = dateFormat.format( calendar.getTime() );
          cache.put( dateString, calendar.getTime() );
        }
      }
    }
  }

  public void addDate( String dateString, Date date ) {
    cache.put( dateString, date );
  }

  public Date lookupDate( String dateString ) {
    return cache.get( dateString );
  }

  public int getSize() {
    return cache.size();
  }

  public static void main( String[] args ) throws ParseException {
    final String dateFormatString = "yyyy/MM/dd";
    final int startYear = 1890;
    final int endYear = 2012;

    long start = System.currentTimeMillis();
    DateCache dateCache = new DateCache();
    dateCache.populate( dateFormatString, startYear + 5, endYear );
    long end = System.currentTimeMillis();
    System.out.println( "Creating cache of " + dateCache.getSize() + " dates : " + ( end - start ) + " ms" );

    SimpleDateFormat dateFormat = new SimpleDateFormat( dateFormatString );

    final int size = 10000000;
    List<String> randomDates = new ArrayList<String>( size );

    for ( int i = 0; i < size; i++ ) {
      Calendar cal = Calendar.getInstance();
      int rndYear = startYear + (int) Math.round( Math.random() * ( endYear - startYear ) );
      int rndDay = (int) Math.round( Math.random() * 365 );
      cal.set( Calendar.YEAR, rndYear );
      cal.set( Calendar.DAY_OF_YEAR, rndDay );
      String dateString = dateFormat.format( cal.getTime() );
      randomDates.add( dateString );
    }

    // Do some parsing the old way...
    //
    start = System.currentTimeMillis();
    for ( String randomDate : randomDates ) {
      dateFormat.parse( randomDate );
    }
    end = System.currentTimeMillis();
    System.out.println( "Parsing " + size + " dates : " + ( end - start ) + " ms" );

    // Do some parsing the new way...
    //
    int retries = 0;
    start = System.currentTimeMillis();
    for ( String randomDate : randomDates ) {
      Date date = dateCache.lookupDate( randomDate );
      if ( date == null ) {
        dateFormat.parse( randomDate );
        retries++;
      }
    }
    end = System.currentTimeMillis();
    System.out.println( "Looking up " + size + " dates : " + ( end - start ) + " ms  (" + retries + " retries)" );

    dateCache = new DateCache();

    // Build up the cache dynamically
    //
    retries = 0;
    start = System.currentTimeMillis();
    for ( String randomDate : randomDates ) {
      Date date = dateCache.lookupDate( randomDate );
      if ( date == null ) {
        date = dateFormat.parse( randomDate );
        dateCache.addDate( randomDate, date );
        retries++;
      }
    }
    end = System.currentTimeMillis();
    System.out.println( "Looking up "
      + size + " dates with incremental cache population: " + ( end - start ) + " ms  (" + retries + " misses)" );
  }
}
