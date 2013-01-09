package org.pentaho.di.core.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateCache {
  
  private Map<String,Date> cache;
  
  public DateCache() {
    cache = new HashMap<String, Date>();
  }
  
  public void populate(String datePattern, int fromYear, int toYear) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
    
    for (int year=fromYear;year<=toYear;year++) {
      for (int day = 0;day<=367;day++) {
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, day);
        if (calendar.get(Calendar.YEAR)==year) {
          String dateString = dateFormat.format(calendar.getTime());
          cache.put(dateString, calendar.getTime());
        }
      }
    }
  }
  
  public void addDate(String dateString, Date date) {
    cache.put(dateString, date);
  }
  
  public Date lookupDate(String dateString) {
    return cache.get(dateString);
  }
  
  public int getSize() {
    return cache.size();
  }
  
  public static void main(String[] args) throws ParseException {
    final String dateFormatString = "yyyy/MM/dd";
    final int startYear = 1890;
    final int endYear = 2012;
    
    long start = System.currentTimeMillis();
    DateCache dateCache = new DateCache();
    dateCache.populate(dateFormatString, startYear+5, endYear);
    long end = System.currentTimeMillis();
    System.out.println("Creating cache of "+dateCache.getSize()+" dates : "+(end-start)+" ms");

    
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    
    final int size = 10000000; 
    List<String> randomDates = new ArrayList<String>(size);
    
    for (int i=0;i<size;i++) {
      Calendar cal = Calendar.getInstance();
      int rndYear = startYear + (int)Math.round( Math.random()* (endYear - startYear) );
      int rndDay  = (int)Math.round( Math.random()* 365 );
      cal.set(Calendar.YEAR, rndYear);
      cal.set(Calendar.DAY_OF_YEAR, rndDay);
      String dateString = dateFormat.format(cal.getTime());
      randomDates.add(dateString);
    }
    
    // Do some parsing the old way...
    //
    start = System.currentTimeMillis();
    for (String randomDate : randomDates) {
      dateFormat.parse(randomDate);
    }
    end = System.currentTimeMillis();
    System.out.println("Parsing "+size+" dates : "+(end-start)+" ms");
    
    // Do some parsing the new way...
    //
    int retries = 0;
    start = System.currentTimeMillis();
    for (String randomDate : randomDates) {
      Date date = dateCache.lookupDate(randomDate);
      if (date==null) {
        dateFormat.parse(randomDate);
        retries++;
      }
    }
    end = System.currentTimeMillis();
    System.out.println("Looking up "+size+" dates : "+(end-start)+" ms  ("+retries+" retries)");
    
    dateCache = new DateCache();
    
    // Build up the cache dynamically
    //
    retries = 0;
    start = System.currentTimeMillis();
    for (String randomDate : randomDates) {
      Date date = dateCache.lookupDate(randomDate);
      if (date==null) {
        date = dateFormat.parse(randomDate);
        dateCache.addDate(randomDate, date);
        retries++;
      }
    }
    end = System.currentTimeMillis();
    System.out.println("Looking up "+size+" dates with incremental cache population: "+(end-start)+" ms  ("+retries+" misses)");
  }
}
