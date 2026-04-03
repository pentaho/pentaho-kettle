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

package org.pentaho.di.ui.spoon.delegates;

import org.eclipse.swt.graphics.Image;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.xul.swt.tab.TabItem;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpoonEETransformationDelegateTest {

  private Spoon spoon;
  private Repository repository;
  private ILockService lockService;
  private SpoonEETransformationDelegate delegate;

  @Before
  public void setUp() {
    spoon = mock( Spoon.class );
    repository = mock( Repository.class );
    lockService = mock( ILockService.class );
    delegate = new SpoonEETransformationDelegate( spoon );
  }

  /**
   * Helper that invokes the private getLockService() via reflection.
   */
  private ILockService invokeLockService() throws Exception {
    Method m = SpoonEETransformationDelegate.class.getDeclaredMethod( "getLockService" );
    m.setAccessible( true );
    return (ILockService) m.invoke( delegate );
  }

  @Test
  public void testGetLockServiceReturnsNullWhenRepositoryIsNull() throws Exception {
    when( spoon.getRepository() ).thenReturn( null );

    ILockService result = invokeLockService();

    assertNull( result );
  }

  @Test
  public void testGetLockServiceReturnsServiceWhenAvailable() throws Exception {
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( true );
    when( repository.getService( ILockService.class ) ).thenReturn( lockService );

    ILockService result = invokeLockService();

    assertNotNull( result );
    verify( repository ).hasService( ILockService.class );
    verify( repository ).getService( ILockService.class );
  }

  @Test
  public void testGetLockServiceReturnsNullWhenServiceNotAvailable() throws Exception {
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( false );

    ILockService result = invokeLockService();

    assertNull( result );
    verify( repository, never() ).getService( any() );
  }

  @Test
  public void testGetLockServiceReturnsNullOnKettleException() throws Exception {
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenThrow( new KettleException( "service error" ) );

    ILockService result = invokeLockService();

    assertNull( result );
  }

  @Test
  public void testGetLockServiceFetchesFromRepositoryEachTime() throws Exception {
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( true );
    when( repository.getService( ILockService.class ) ).thenReturn( lockService );

    // Call twice to confirm no caching
    invokeLockService();
    invokeLockService();

    verify( repository, times( 2 ) ).hasService( ILockService.class );
    verify( repository, times( 2 ) ).getService( ILockService.class );
  }

  @Test
  public void testGetLockServiceFetchesFreshAfterRepositoryChange() throws Exception {
    Repository repository2 = mock( Repository.class );
    ILockService lockService2 = mock( ILockService.class );

    // First call returns original repository
    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( true );
    when( repository.getService( ILockService.class ) ).thenReturn( lockService );

    ILockService first = invokeLockService();
    assertNotNull( first );

    // Simulate reconnection - new repository instance
    when( spoon.getRepository() ).thenReturn( repository2 );
    when( repository2.hasService( ILockService.class ) ).thenReturn( true );
    when( repository2.getService( ILockService.class ) ).thenReturn( lockService2 );

    ILockService second = invokeLockService();
    assertNotNull( second );

    // Verify the second call used repository2, not the original
    verify( repository2 ).hasService( ILockService.class );
    verify( repository2 ).getService( ILockService.class );
  }

  @Test
  public void addTransGraphDoesNotSetLockedImageWhenTransformationIsNotLocked() throws Exception {
    TransMeta transMeta = mock( TransMeta.class );
    ObjectId objectId = new StringObjectId( "trans-1" );
    when( transMeta.getObjectId() ).thenReturn( objectId );

    SpoonEETransformationDelegate spyDelegate = spy( delegate );
    doNothing().when( (SpoonTransformationDelegate) spyDelegate ).addTransGraph( transMeta );

    SpoonTabsDelegate tabsDelegate = mock( SpoonTabsDelegate.class );
    TabMapEntry tabEntry = mock( TabMapEntry.class );
    TabItem tabItem = mock( TabItem.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = tabsDelegate;
    when( tabsDelegate.findTabMapEntry( transMeta ) ).thenReturn( tabEntry );
    when( tabEntry.getTabItem() ).thenReturn( tabItem );

    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( true );
    when( repository.getService( ILockService.class ) ).thenReturn( lockService );
    when( lockService.getTransformationLock( objectId ) ).thenReturn( null );

    spyDelegate.addTransGraph( transMeta );

    verify( tabItem, never() ).setImage( any( Image.class ) );
  }

  @Test
  public void addTransGraphSkipsLockCheckWhenObjectIdIsNull() throws Exception {
    TransMeta transMeta = mock( TransMeta.class );
    when( transMeta.getObjectId() ).thenReturn( null );

    SpoonEETransformationDelegate spyDelegate = spy( delegate );
    doNothing().when( (SpoonTransformationDelegate) spyDelegate ).addTransGraph( transMeta );

    SpoonTabsDelegate tabsDelegate = mock( SpoonTabsDelegate.class );
    TabMapEntry tabEntry = mock( TabMapEntry.class );
    TabItem tabItem = mock( TabItem.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = tabsDelegate;
    when( tabsDelegate.findTabMapEntry( transMeta ) ).thenReturn( tabEntry );
    when( tabEntry.getTabItem() ).thenReturn( tabItem );

    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( true );
    when( repository.getService( ILockService.class ) ).thenReturn( lockService );

    spyDelegate.addTransGraph( transMeta );

    verify( lockService, never() ).getTransformationLock( any() );
    verify( tabItem, never() ).setImage( any( Image.class ) );
  }

  @Test
  public void addTransGraphSkipsLockCheckWhenTabEntryIsNull() {
    TransMeta transMeta = mock( TransMeta.class );

    SpoonEETransformationDelegate spyDelegate = spy( delegate );
    doNothing().when( (SpoonTransformationDelegate) spyDelegate ).addTransGraph( transMeta );

    SpoonTabsDelegate tabsDelegate = mock( SpoonTabsDelegate.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = tabsDelegate;
    when( tabsDelegate.findTabMapEntry( transMeta ) ).thenReturn( null );

    spyDelegate.addTransGraph( transMeta );

    verify( spoon, never() ).getRepository();
  }

  @Test
  public void addTransGraphSkipsLockIconWhenLockServiceIsUnavailable() throws Exception {
    TransMeta transMeta = mock( TransMeta.class );
    ObjectId objectId = new StringObjectId( "trans-1" );
    when( transMeta.getObjectId() ).thenReturn( objectId );

    SpoonEETransformationDelegate spyDelegate = spy( delegate );
    doNothing().when( (SpoonTransformationDelegate) spyDelegate ).addTransGraph( transMeta );

    SpoonTabsDelegate tabsDelegate = mock( SpoonTabsDelegate.class );
    TabMapEntry tabEntry = mock( TabMapEntry.class );
    TabItem tabItem = mock( TabItem.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = tabsDelegate;
    when( tabsDelegate.findTabMapEntry( transMeta ) ).thenReturn( tabEntry );
    when( tabEntry.getTabItem() ).thenReturn( tabItem );

    when( spoon.getRepository() ).thenReturn( repository );
    when( repository.hasService( ILockService.class ) ).thenReturn( false );

    spyDelegate.addTransGraph( transMeta );

    verify( tabItem, never() ).setImage( any( Image.class ) );
  }


  @Test
  public void addTransGraphSkipsLockIconWhenRepositoryIsNull() {
    TransMeta transMeta = mock( TransMeta.class );
    ObjectId objectId = new StringObjectId( "trans-1" );
    when( transMeta.getObjectId() ).thenReturn( objectId );

    SpoonEETransformationDelegate spyDelegate = spy( delegate );
    doNothing().when( (SpoonTransformationDelegate) spyDelegate ).addTransGraph( transMeta );

    SpoonTabsDelegate tabsDelegate = mock( SpoonTabsDelegate.class );
    TabMapEntry tabEntry = mock( TabMapEntry.class );
    TabItem tabItem = mock( TabItem.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = tabsDelegate;
    when( tabsDelegate.findTabMapEntry( transMeta ) ).thenReturn( tabEntry );
    when( tabEntry.getTabItem() ).thenReturn( tabItem );

    when( spoon.getRepository() ).thenReturn( null );

    spyDelegate.addTransGraph( transMeta );

    verify( tabItem, never() ).setImage( any( Image.class ) );
  }
}

