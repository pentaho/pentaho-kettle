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


package org.pentaho.di.ui.repository.pur.repositoryexplorer.controller;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.pur.PurRepositoryMeta;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEETransformation;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.ui.xul.components.XulMenuitem;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RepositoryLockControllerTest {


  @Test
  public void testBlockLock() throws Exception {
    RepositoryLockController repositoryLockController = new RepositoryLockController();
    List<UIRepositoryObject> selectedRepoObjects = new ArrayList<>();
    UIEETransformation lockObject = Mockito.mock( UIEETransformation.class );
    selectedRepoObjects.add( lockObject );
    Mockito.when( lockObject.isLocked() ).thenReturn( true );
    ObjectId objectId = Mockito.mock( ObjectId.class );
    Mockito.when( lockObject.getObjectId() ).thenReturn( objectId );

    XulMenuitem lockFileMenuItem = Mockito.mock( XulMenuitem.class );
    Field lockFileMenuItemField = repositoryLockController.getClass().getDeclaredField( "lockFileMenuItem" );
    lockFileMenuItemField.setAccessible( true );
    lockFileMenuItemField.set( repositoryLockController, lockFileMenuItem );

    XulMenuitem deleteFileMenuItem = Mockito.mock( XulMenuitem.class );
    Field deleteFileMenuItemField = repositoryLockController.getClass().getDeclaredField( "deleteFileMenuItem" );
    deleteFileMenuItemField.setAccessible( true );
    deleteFileMenuItemField.set( repositoryLockController, deleteFileMenuItem );

    XulMenuitem renameFileMenuItem = Mockito.mock( XulMenuitem.class );
    Field renameFileMenuItemField = repositoryLockController.getClass().getDeclaredField( "renameFileMenuItem" );
    renameFileMenuItemField.setAccessible( true );
    renameFileMenuItemField.set( repositoryLockController, renameFileMenuItem );

    Repository repository = Mockito.mock( Repository.class );
    PurRepositoryMeta repositoryMeta = Mockito.mock( PurRepositoryMeta.class );
    Mockito.when( repository.getRepositoryMeta() ).thenReturn( repositoryMeta );
    Field repositoryField = repositoryLockController.getClass().getDeclaredField( "repository" );
    repositoryField.setAccessible( true );
    repositoryField.set( repositoryLockController, repository );

    ILockService service = Mockito.mock( ILockService.class );
    Mockito.when( service.canUnlockFileById( objectId ) ).thenReturn( true );
    Field serviceField = repositoryLockController.getClass().getDeclaredField( "service" );
    serviceField.setAccessible( true );
    serviceField.set( repositoryLockController, service );

    repositoryLockController.setMenuItemEnabledState( selectedRepoObjects );
    Assert.assertFalse( lockFileMenuItem.isDisabled() );

    Mockito.verify( lockFileMenuItem ).setDisabled( false );

  }

}
