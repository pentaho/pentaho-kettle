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


package org.pentaho.amazon.s3;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class S3DetailsTest {

  private S3Details s3Details = new S3Details();

  @Test
  public void getProperties() {
    s3Details.setName( "name" );
    s3Details.setRegion( "aws-west-1" );
    s3Details.setAccessKey( "ASIAXJ3TZZPFVO3NK6O" );
    s3Details.setSecretKey( "jKbmptEdHk6cTXXqGodacxJn5yaETIIhKjJb/oZ" );
    s3Details.setRootPath( "test/path" );

    Map<String, String> props = s3Details.getProperties();
    assertThat( props.get( "name" ), equalTo( "name" ) );
    assertThat( props.get( "region" ), equalTo( "aws-west-1" ) );
    assertThat( props.get( "accessKey" ), equalTo( "ASIAXJ3TZZPFVO3NK6O" ) );
    assertThat( props.get( "secretKey" ), equalTo( "jKbmptEdHk6cTXXqGodacxJn5yaETIIhKjJb/oZ" ) );
    assertThat( props.get( "rootPath" ), equalTo( "test/path" ) );
    assertThat( props.size(), equalTo( 17 ) );
    assertThat( props.entrySet().stream().filter( e -> e.getValue() != null ).count(), equalTo( 6L ) );
  }

  @Test
  public void getPropertiesWithEndpoint() {
    s3Details.setName( "name" );
    s3Details.setRegion( "aws-west-1" );
    s3Details.setAccessKey( "ASIAXJ3TZZPFVO3NK6O" );
    s3Details.setSecretKey( "jKbmptEdHk6cTXXqGodacxJn5yaETIIhKjJb/oZ" );
    s3Details.setEndpoint( "http://localhost:9000" );
    s3Details.setPathStyleAccess( "true");
    s3Details.setSignatureVersion( "v4" );

    Map<String, String> props = s3Details.getProperties();
    assertThat( props.get( "name" ), equalTo( "name" ) );
    assertThat( props.get( "region" ), equalTo( "aws-west-1" ) );
    assertThat( props.get( "accessKey" ), equalTo( "ASIAXJ3TZZPFVO3NK6O" ) );
    assertThat( props.get( "secretKey" ), equalTo( "jKbmptEdHk6cTXXqGodacxJn5yaETIIhKjJb/oZ" ) );
    assertThat( props.get( "endpoint" ), equalTo( "http://localhost:9000" ) );
    assertThat( props.get( "pathStyleAccess" ), equalTo( "true" ) );
    assertThat( props.get( "signatureVersion" ), equalTo( "v4" ) );

    assertThat( props.size(), equalTo( 17 ) );
    assertThat( props.entrySet().stream().filter( e -> e.getValue() != null ).count(), equalTo( 8L ) );
  }

}
