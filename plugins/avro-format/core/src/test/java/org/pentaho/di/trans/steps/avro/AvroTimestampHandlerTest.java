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

package org.pentaho.di.trans.steps.avro;

import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for AvroTimestampHandler class.
 * Tests conversion between Avro timestamp formats (milliseconds, microseconds, nanoseconds)
 * and Java Timestamp objects.
 */
public class AvroTimestampHandlerTest {

  // Test constants
  private static final long TEST_MILLIS = 1609459200000L; // 2021-01-01 00:00:00.000 UTC
  private static final long TEST_MICROS = 1609459200000000L; // 2021-01-01 00:00:00.000000 UTC
  private static final long TEST_NANOS = 1609459200000000000L; // 2021-01-01 00:00:00.000000000 UTC

  private static final long TEST_MILLIS_WITH_FRACTION = 1609459200123L; // 2021-01-01 00:00:00.123 UTC
  private static final long TEST_MICROS_WITH_FRACTION = 1609459200123456L; // 2021-01-01 00:00:00.123456 UTC
  private static final long TEST_NANOS_WITH_FRACTION = 1609459200123456789L; // 2021-01-01 00:00:00.123456789 UTC

  // ==================== Tests for toTimestamp() method ====================

  @Test
  public void testToTimestampWithMillis() {
    Timestamp result = AvroTimestampHandler.toTimestamp( TEST_MILLIS, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertNotNull( result );
    assertEquals( TEST_MILLIS, result.getTime() );
  }

  @Test
  public void testToTimestampWithMicros() {
    Timestamp result = AvroTimestampHandler.toTimestamp( TEST_MICROS, AvroSpec.DataType.TIMESTAMP_MICROS );

    assertNotNull( result );
    assertEquals( TEST_MILLIS, result.getTime() );
  }

  @Test
  public void testToTimestampWithNanos() {
    Timestamp result = AvroTimestampHandler.toTimestamp( TEST_NANOS, AvroSpec.DataType.TIMESTAMP_NANOS );

    assertNotNull( result );
    assertEquals( TEST_MILLIS, result.getTime() );
  }

  @Test
  public void testToTimestampWithMicrosWithFraction() {
    Timestamp result = AvroTimestampHandler.toTimestamp( TEST_MICROS_WITH_FRACTION, AvroSpec.DataType.TIMESTAMP_MICROS );

    assertNotNull( result );
    assertEquals( TEST_MILLIS_WITH_FRACTION, result.getTime() );
    // Check nanoseconds: 456 microseconds = 456000 nanoseconds
    assertEquals( 123456000, result.getNanos() );
  }

  @Test
  public void testToTimestampWithNanosWithFraction() {
    Timestamp result = AvroTimestampHandler.toTimestamp( TEST_NANOS_WITH_FRACTION, AvroSpec.DataType.TIMESTAMP_NANOS );

    assertNotNull( result );
    assertEquals( TEST_MILLIS_WITH_FRACTION, result.getTime() );
    // Check full nanoseconds
    assertEquals( 123456789, result.getNanos() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testToTimestampWithUnsupportedType() {
    AvroTimestampHandler.toTimestamp( TEST_MILLIS, AvroSpec.DataType.DATE );
  }

  @Test
  public void testToTimestampWithZero() {
    Timestamp result = AvroTimestampHandler.toTimestamp( 0L, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertNotNull( result );
    assertEquals( 0L, result.getTime() );
  }

  @Test
  public void testToTimestampWithNegativeValue() {
    long negativeMillis = -1000L;
    Timestamp result = AvroTimestampHandler.toTimestamp( negativeMillis, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertNotNull( result );
    assertEquals( negativeMillis, result.getTime() );
  }

  // ==================== Tests for fromTimestamp() method ====================

  @Test
  public void testFromTimestampToMillis() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS );

    Long result = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertNotNull( result );
    assertEquals( Long.valueOf( TEST_MILLIS ), result );
  }

  @Test
  public void testFromTimestampToMicros() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS );

    Long result = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MICROS );

    assertNotNull( result );
    assertEquals( Long.valueOf( TEST_MICROS ), result );
  }

  @Test
  public void testFromTimestampToNanos() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS );

    Long result = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_NANOS );

    assertNotNull( result );
    assertEquals( Long.valueOf( TEST_NANOS ), result );
  }

  @Test
  public void testFromTimestampToMillisWithFraction() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS_WITH_FRACTION );

    Long result = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertNotNull( result );
    assertEquals( Long.valueOf( TEST_MILLIS_WITH_FRACTION ), result );
  }

  @Test
  public void testFromTimestampToMicrosWithFraction() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS_WITH_FRACTION );
    timestamp.setNanos( 123456789 ); // Set nanoseconds within the second

    Long result = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MICROS );

    assertNotNull( result );
    // Expected: 123ms * 1000 + 456 microseconds = 123456 microseconds within the second
    assertEquals( Long.valueOf( TEST_MICROS_WITH_FRACTION ), result );
  }

  @Test
  public void testFromTimestampToNanosWithFraction() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS_WITH_FRACTION );
    timestamp.setNanos( 123456789 ); // Set nanoseconds within the second

    Long result = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_NANOS );

    assertNotNull( result );
    assertEquals( Long.valueOf( TEST_NANOS_WITH_FRACTION ), result );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testFromTimestampWithUnsupportedType() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS );
    AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.DATE );
  }

  @Test
  public void testFromTimestampWithNull() {
    Long result = AvroTimestampHandler.fromTimestamp( null, AvroSpec.DataType.TIMESTAMP_MILLIS );
    assertNull( result );
  }

  @Test
  public void testFromTimestampWithZero() {
    Timestamp timestamp = new Timestamp( 0L );

    Long result = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertNotNull( result );
    assertEquals( Long.valueOf( 0L ), result );
  }

  @Test
  public void testFromTimestampWithNegativeValue() {
    long negativeMillis = -1000L;
    Timestamp timestamp = new Timestamp( negativeMillis );

    Long result = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertNotNull( result );
    assertEquals( Long.valueOf( negativeMillis ), result );
  }

  // ==================== Round-trip conversion tests ====================

  @Test
  public void testRoundTripMillis() {
    Timestamp original = new Timestamp( TEST_MILLIS_WITH_FRACTION );

    Long avroMillis = AvroTimestampHandler.fromTimestamp( original, AvroSpec.DataType.TIMESTAMP_MILLIS );
    Timestamp result = AvroTimestampHandler.toTimestamp( avroMillis, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertEquals( original.getTime(), result.getTime() );
  }

  @Test
  public void testRoundTripMicros() {
    Timestamp original = new Timestamp( TEST_MILLIS_WITH_FRACTION );
    original.setNanos( 123456000 ); // Microsecond precision (no sub-microsecond part)

    Long avroMicros = AvroTimestampHandler.fromTimestamp( original, AvroSpec.DataType.TIMESTAMP_MICROS );
    Timestamp result = AvroTimestampHandler.toTimestamp( avroMicros, AvroSpec.DataType.TIMESTAMP_MICROS );

    assertEquals( original.getTime(), result.getTime() );
    assertEquals( original.getNanos(), result.getNanos() );
  }

  @Test
  public void testRoundTripNanos() {
    Timestamp original = new Timestamp( TEST_MILLIS_WITH_FRACTION );
    original.setNanos( 123456789 ); // Full nanosecond precision

    Long avroNanos = AvroTimestampHandler.fromTimestamp( original, AvroSpec.DataType.TIMESTAMP_NANOS );
    Timestamp result = AvroTimestampHandler.toTimestamp( avroNanos, AvroSpec.DataType.TIMESTAMP_NANOS );

    assertEquals( original.getTime(), result.getTime() );
    assertEquals( original.getNanos(), result.getNanos() );
  }

  // ==================== Edge case tests ====================

  @Test
  public void testMaxTimestampValue() {
    long maxMillis = Long.MAX_VALUE / 1_000_000; // Avoid overflow in conversions
    Timestamp timestamp = new Timestamp( maxMillis );

    Long result = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertNotNull( result );
    assertEquals( Long.valueOf( maxMillis ), result );
  }

  @Test
  public void testSubMillisecondPrecisionLoss() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS );
    timestamp.setNanos( 123456789 ); // Full nanosecond precision

    // Convert to milliseconds and back - should lose sub-millisecond precision
    Long millis = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MILLIS );
    Timestamp result = AvroTimestampHandler.toTimestamp( millis, AvroSpec.DataType.TIMESTAMP_MILLIS );

    assertEquals( timestamp.getTime(), result.getTime() );
    // Note: Sub-millisecond precision is lost when converting to millis
  }

  @Test
  public void testSubMicrosecondPrecisionLoss() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS );
    timestamp.setNanos( 123456789 ); // Full nanosecond precision

    // Convert to microseconds and back - should lose sub-microsecond precision
    Long micros = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MICROS );
    Timestamp result = AvroTimestampHandler.toTimestamp( micros, AvroSpec.DataType.TIMESTAMP_MICROS );

    assertEquals( timestamp.getTime(), result.getTime() );
    // Sub-microsecond precision is lost (last 3 digits of nanos)
    assertEquals( 123456000, result.getNanos() );
  }

  @Test
  public void testZeroNanoseconds() {
    Timestamp timestamp = new Timestamp( TEST_MILLIS );
    timestamp.setNanos( 0 );

    Long micros = AvroTimestampHandler.fromTimestamp( timestamp, AvroSpec.DataType.TIMESTAMP_MICROS );

    assertNotNull( micros );
    assertEquals( Long.valueOf( TEST_MICROS ), micros );
  }

  @Test
  public void testConversionConsistency() {
    // Test that conversions between different precisions are consistent
    long millis = 1234567890123L;

    Timestamp tsFromMillis = AvroTimestampHandler.toTimestamp( millis, AvroSpec.DataType.TIMESTAMP_MILLIS );
    Timestamp tsFromMicros = AvroTimestampHandler.toTimestamp( millis * 1000, AvroSpec.DataType.TIMESTAMP_MICROS );
    Timestamp tsFromNanos = AvroTimestampHandler.toTimestamp( millis * 1_000_000, AvroSpec.DataType.TIMESTAMP_NANOS );

    assertEquals( tsFromMillis.getTime(), tsFromMicros.getTime() );
    assertEquals( tsFromMillis.getTime(), tsFromNanos.getTime() );
  }
}

