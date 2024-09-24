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
package org.pentaho.di.job.entries.talendjobexec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryTalendJobExecTest extends JobEntryLoadSaveTestSupport<JobEntryTalendJobExec> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();


  @Override
  protected Class<JobEntryTalendJobExec> getJobEntryClass() {
    return JobEntryTalendJobExec.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "filename",
        "className" );
  }

  @Test //PDI-8929
  public void testConcurrentExceptionClassLoaderCache3() throws Exception {

    Field field = JobEntryTalendJobExec.class.getDeclaredField( "classLoaderCache" );
    field.setAccessible( true );
    Map<String, ClassLoader> cache = (Map<String, ClassLoader>) field.get( null );

    cache.put( "test1", Mockito.mock( ClassLoader.class ) );
    cache.put( "test2", Mockito.mock( ClassLoader.class ) );
    cache.put( "test3", Mockito.mock( ClassLoader.class ) );
    List<ClassLoader> test3ClassLoader = new ArrayList<>();

    Thread thread1 = new Thread( () -> {
      for ( String key : cache.keySet() ) {
        try {
          Thread.sleep( 300 );
        } catch ( InterruptedException e ) {
          e.printStackTrace();
        }
        if ( key.equals( "test3" ) ) {
          test3ClassLoader.add( cache.get( key ) );
        }
      }
    } );

    Thread thread2 = new Thread( () -> {
      try {
        Thread.sleep( 100 );
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
      cache.put( "test4", Mockito.mock( ClassLoader.class ) );
    } );

    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();

    Assert.assertTrue( !test3ClassLoader.isEmpty() );
  }
}
