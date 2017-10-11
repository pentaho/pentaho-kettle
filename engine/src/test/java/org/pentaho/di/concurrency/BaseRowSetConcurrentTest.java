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

package org.pentaho.di.concurrency;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.BlockingRowSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * We have a {@link org.pentaho.di.core.BaseRowSet} with a bunch of attributes (originStepName, originStepCopy,
 * destinationStepName,destinationStepCopy and others).
 *
 * The goal of this test is to verify that attributes from one BaseRowSet doesn't mix with attributes of
 * another BaseRowSet in concurrent environment when executing
 * {@link org.pentaho.di.core.BaseRowSet#setThreadNameFromToCopy(String, int, String, int)}.
 *
 * We have a {@link BlockingRowSet} that will be share across {@link Mutator} and {@link Getter}
 * We take {@link BlockingRowSet} here because {@link org.pentaho.di.core.BaseRowSet} has a package level visibility
 * and is not reachable from this package (and this package is designed collects all concurrent tests and concurrent
 * framework runner)
 *
 * {@link Mutator} generates consistent blockingRowSet's. By consistence the following is meant:
 * Each blockingRowSet has it's id (random int) and all his fields are named in pattern:
 * - SOME_STRING + blockingRowSetId (in case it is a string)
 * - blockingRowSetIs (in case it is a number)
 *
 * Then mutator mutates blockingRowSet by calling:
 * {@link org.pentaho.di.core.BaseRowSet#setThreadNameFromToCopy(String, int, String, int)}.
 * where all inputs have the same id.
 *
 * It is expected that shared blockingRowSet will always be in consistent state (all his fields will end
 * with the same id).
 *
 * And that's exactly what {@link Getter} does:
 *
 * It calls toString method of shared blockingRowSet and verifies it's consistency.
 *
 */
public class BaseRowSetConcurrentTest {
  private static final int MUTATE_CIRCLES = 100;
  private static final int NUMBER_OF_MUTATORS = 20;
  private static final int NUMBER_OF_GETTERS = 20;

  @Test
  public void test() throws Exception {
    BlockingRowSet sharedBlockingRowSet = new BlockingRowSet( 100 );
    // fill data with initial values
    sharedBlockingRowSet.setThreadNameFromToCopy( "1", 1, "1", 1 );
    AtomicBoolean condition = new AtomicBoolean( true );

    List<Mutator> mutators = generateMutators( sharedBlockingRowSet, condition );
    List<Getter> getters = generateGetters( sharedBlockingRowSet, condition );

    ConcurrencyTestRunner.runAndCheckNoExceptionRaised( mutators, getters, condition );
  }

  private class Mutator extends StopOnErrorCallable<Object> {
    private static final String STRING_DEFAULT = "<def>";
    private final BlockingRowSet blockingRowSet;
    private final Random random;

    public Mutator( BlockingRowSet blockingRowSet, AtomicBoolean condition ) {
      super( condition );
      this.blockingRowSet = blockingRowSet;
      random = new Random();
    }

    @Override
    Object doCall() throws Exception {
      for ( int i = 0; i < MUTATE_CIRCLES; i++ ) {
        final int id = generateId();
        blockingRowSet.setThreadNameFromToCopy( STRING_DEFAULT + id, id, STRING_DEFAULT + id, id );
      }

      return null;
    }

    private int generateId() {
      return random.nextInt();
    }
  }

  private class Getter extends StopOnErrorCallable<Object> {
    private final BlockingRowSet blockingRowSet;

    private Getter( BlockingRowSet blockingRowSet, AtomicBoolean condition ) {
      super( condition );
      this.blockingRowSet = blockingRowSet;
    }

    @Override
    Object doCall() throws Exception {
      while ( condition.get() ) {
        checkConsistency();
      }

      return null;
    }

    private void checkConsistency() {
      Set<String> ids = extractIds( blockingRowSet.toString() );

      // we expect that all ids (and all digits are ids here) refer to the same set,
      // that means that they are equal.
      Assert.assertEquals( 1, ids.size() );
    }

    /**
     * Goal of this method is to extract all numbers (that are expected to be ids) from
     * a string and populate it into a set.
     *
     * Example:
     * input -> 123-124
     * output -> set with values "123", "124" in it.
     *
     */
    Set<String> extractIds( String string ) {
      Set<String> ids = new HashSet<>();
      Pattern pattern = Pattern.compile( "\\d+" );
      Matcher matcher = pattern.matcher( string );

      while ( matcher.find() ) {
        ids.add( matcher.group() );
      }

      return ids;
    }


  }


  private List<Getter> generateGetters( BlockingRowSet blockingRowSet, AtomicBoolean condition ) {
    List<Getter> getters = new ArrayList<>( NUMBER_OF_GETTERS );

    for ( int i = 0; i < NUMBER_OF_GETTERS; i++ ) {
      getters.add( new Getter( blockingRowSet, condition ) );
    }

    return getters;
  }

  private List<Mutator> generateMutators( BlockingRowSet blockingRowSet, AtomicBoolean condition ) {
    List<Mutator> mutators = new ArrayList<>( NUMBER_OF_MUTATORS );

    for ( int i = 0; i < NUMBER_OF_MUTATORS; i++ ) {
      mutators.add( new Mutator( blockingRowSet, condition ) );
    }

    return mutators;
  }
}
