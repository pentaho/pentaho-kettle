/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.docker;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the SSH KTR files of src/test/resources/ssh against the SSH servers and proxies on the test network.
 */
@Tag( "ssh" )
class SshKtrExecutionIT extends PdiIT {

  static Stream<Arguments> cases() throws Exception {
    List<KtrCase> ktrs = KtrCase.ssh();
    return ItConfig.executionTypes().stream()
      .flatMap( type -> ktrs.stream().map( ktr -> Arguments.of( type, ktr ) ) );
  }

  @ParameterizedTest( name = "{0} {1}" )
  @MethodSource( "cases" )
  void runsSshKtr( ExecutionType type, KtrCase ktr ) throws Exception {
    ExecutionResult result = execute( type, ktr );
    assertTrue( result.success(), result::detail );
  }
}
