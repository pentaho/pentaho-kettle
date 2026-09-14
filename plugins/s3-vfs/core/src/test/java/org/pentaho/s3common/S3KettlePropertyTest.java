/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.s3common;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

public class S3KettlePropertyTest {

  @Test
  public void testGetPartSize() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
           MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      props.setProperty( S3KettleProperty.S3VFS_PART_SIZE, "10MB" );
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertEquals( "10MB", property.getPartSize() );
    }
  }

  @Test
  public void testGetProperty_MissingProperty() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
           MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertNull( property.getProperty( "not.a.real.property" ) );
    }
  }

  @Test
  public void testGetProperty_KettleException() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
           MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenThrow( new KettleException( "fail" ) );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      // Should return empty string on exception
      assertEquals( "", property.getProperty( "any.property" ) );
    }
  }

  @Test
  public void testGetCacheTtlSeconds_DefaultWhenUnset() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
          MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertEquals( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS_DEFAULT, property.getCacheTtlSeconds() );
    }
  }

  @Test
  public void testGetCacheTtlSeconds_UsesConfiguredValue() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
          MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      props.setProperty( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS, "120" );
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertEquals( 120, property.getCacheTtlSeconds() );
    }
  }

  @Test
  public void testGetCacheTtlSeconds_ZeroFallsBackToDefault() {
    // Disabling the cache is now the job of S3VFS_CACHE_ENABLED, not TTL=0 - a non-positive TTL is
    // treated as invalid configuration and falls back to the default.
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
          MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      props.setProperty( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS, "0" );
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertEquals( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS_DEFAULT, property.getCacheTtlSeconds() );
    }
  }

  @Test
  public void testGetCacheTtlSeconds_NegativeValueFallsBackToDefault() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
          MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      props.setProperty( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS, "-5" );
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertEquals( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS_DEFAULT, property.getCacheTtlSeconds() );
    }
  }

  @Test
  public void testGetCacheTtlSeconds_NonNumericValueFallsBackToDefault() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
          MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      props.setProperty( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS, "not-a-number" );
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertEquals( S3KettleProperty.S3VFS_CACHE_TTL_SECONDS_DEFAULT, property.getCacheTtlSeconds() );
    }
  }

  @Test
  public void testIsCacheEnabled_DefaultWhenUnset() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
          MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertEquals( S3KettleProperty.S3VFS_CACHE_ENABLED_DEFAULT, property.isCacheEnabled() );
    }
  }

  @Test
  public void testIsCacheEnabled_FalseDisablesCache() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
          MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      props.setProperty( S3KettleProperty.S3VFS_CACHE_ENABLED, "false" );
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertFalse( property.isCacheEnabled() );
    }
  }

  @Test
  public void testIsCacheEnabled_FalseIsCaseInsensitive() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
          MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      props.setProperty( S3KettleProperty.S3VFS_CACHE_ENABLED, "FALSE" );
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertFalse( property.isCacheEnabled() );
    }
  }

  @Test
  public void testIsCacheEnabled_TrueWhenExplicitlySet() {
    try ( MockedStatic<EnvUtil> envUtilMock = mockStatic( EnvUtil.class );
          MockedStatic<Const> constMock = mockStatic( Const.class ) ) {
      Properties props = new Properties();
      props.setProperty( S3KettleProperty.S3VFS_CACHE_ENABLED, "true" );
      envUtilMock.when( () -> EnvUtil.readProperties( "kettle.properties" ) ).thenReturn( props );
      constMock.when( Const::getKettlePropertiesFilename ).thenReturn( "kettle.properties" );
      S3KettleProperty property = new S3KettleProperty();
      assertTrue( property.isCacheEnabled() );
    }
  }
}
