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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs the KTR files in src/test/resources/ktr with each selected execution type.
 */
@Tag( "basic" )
class KtrExecutionIT extends PdiIT {

  static Stream<Arguments> cases() throws Exception {
    List<KtrCase> ktrs = KtrCase.basic();
    return ItConfig.executionTypes().stream()
      .flatMap( type -> ktrs.stream().map( ktr -> Arguments.of( type, ktr ) ) );
  }

  @ParameterizedTest( name = "{0} {1}" )
  @MethodSource( "cases" )
  void runsKtr( ExecutionType type, KtrCase ktr ) throws Exception {
    ExecutionResult result = execute( type, ktr );
    assertEquals( !ktr.expectFailure(), result.success(), result::detail );
  }
}
