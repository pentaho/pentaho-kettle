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


package org.pentaho.di.repository;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
public class RepositoryCommonValidationsTest {

  @Test( expected = NullPointerException.class )
  public void checkUserInfo_Null() {
    RepositoryCommonValidations.checkUserInfo( null );
  }


  @Test
  public void checkUserInfo_LoginIsNull() {
    assertFalse( RepositoryCommonValidations.checkUserInfo( user( null, "name" ) ) );
  }

  @Test
  public void checkUserInfo_LoginIsBlank() {
    assertFalse( RepositoryCommonValidations.checkUserInfo( user( "", "name" ) ) );
  }

  @Test
  public void checkUserInfo_LoginContainsSpaces() {
    assertFalse( RepositoryCommonValidations.checkUserInfo( user( "  \t\n ", "name" ) ) );
  }


  @Test
  public void checkUserInfo_BothAreMeaningful() {
    assertTrue( RepositoryCommonValidations.checkUserInfo( user( "login", "name" ) ) );
  }


  @Test( expected = NullPointerException.class )
  public void normalizeUserInfo_Null() {
    RepositoryCommonValidations.normalizeUserInfo( null );
  }

  @Test
  public void normalizeUserInfo_Valid() {
    IUser normalized = RepositoryCommonValidations.normalizeUserInfo( user( "login", "name" ) );
    assertEquals( "login", normalized.getLogin() );
    assertEquals( "login", normalized.getName() );
  }

  @Test
  public void normalizeUserInfo_WithSpaces() {
    IUser normalized = RepositoryCommonValidations.normalizeUserInfo( user( "  login \t\n ", "name" ) );
    assertEquals( "login", normalized.getLogin() );
    assertEquals( "login", normalized.getName() );
  }


  private static IUser user( String login, String name ) {
    return new UserInfo( login, null, name, name, true );
  }
}
