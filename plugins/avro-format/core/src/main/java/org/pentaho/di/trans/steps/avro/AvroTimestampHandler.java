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

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Utility class for handling conversions between Avro timestamp formats and Java `Timestamp`.
 */
public class AvroTimestampHandler {

  /**
   * Converts Avro timestamp data to a Java `Timestamp` based on the specified Avro data type.
   *
   * @param avroData The timestamp data in Avro format.
   * @param dataType The Avro data type (e.g., TIMESTAMP_MILLIS, TIMESTAMP_MICROS, TIMESTAMP_NANOS).
   * @return The corresponding Java `Timestamp` having milli/micro/nanosecond precision depending on type.
   * @throws IllegalArgumentException if the data type is unsupported.
   */
  public static Timestamp toTimestamp( long avroData, AvroSpec.DataType dataType ) {
    return switch ( dataType ) {
      case TIMESTAMP_MILLIS -> toTimestampMillis( avroData );
      case TIMESTAMP_MICROS -> toTimestampMicros( avroData );
      case TIMESTAMP_NANOS -> toTimestampNanos( avroData );
      default -> throw new IllegalArgumentException(
        "Unsupported data type for timestamp conversion: " + dataType.name() );
    };
  }

  /**
   * Converts a timestamp in milliseconds to a Java `Timestamp`.
   * <p>
   * Calculation: Direct conversion - milliseconds since epoch to Timestamp constructor.
   *
   * @param millis The timestamp in milliseconds.
   * @return The corresponding Java `Timestamp` having millisecond precision.
   */
  private static Timestamp toTimestampMillis( long millis ) {
    return new Timestamp( millis );
  }

  /**
   * Converts a timestamp in microseconds to a Java `Timestamp`.
   * <p>
   * Calculation: micros / 1,000,000 = seconds; micros % 1,000,000 = remaining microseconds;
   * microseconds * 1,000 = nanoseconds for Instant.
   *
   * @param micros The timestamp in microseconds.
   * @return The corresponding Java `Timestamp` having microsecond precision.
   */
  private static Timestamp toTimestampMicros( long micros ) {
    long seconds = micros / 1_000_000L;
    long microPart = micros % 1_000_000L;

    Instant instant = Instant.ofEpochSecond( seconds, microPart * 1_000 );
    return Timestamp.from( instant );
  }

  /**
   * Converts a timestamp in nanoseconds to a Java `Timestamp`.
   * <p>
   * Calculation: nanos / 1,000,000,000 = seconds; nanos % 1,000,000,000 = remaining nanoseconds.
   *
   * @param nanos The timestamp in nanoseconds.
   * @return The corresponding Java `Timestamp` having nanosecond precision.
   */
  private static Timestamp toTimestampNanos( long nanos ) {
    long seconds = nanos / 1_000_000_000L;
    long nanoPart = nanos % 1_000_000_000L;

    Instant instant = Instant.ofEpochSecond( seconds, nanoPart );
    return Timestamp.from( instant );
  }

  /**
   * Converts a Java `Timestamp` to Avro timestamp data based on the specified Avro data type.
   *
   * @param timestamp The Java `Timestamp` to convert.
   * @param dataType The Avro data type (e.g., TIMESTAMP_MILLIS, TIMESTAMP_MICROS, TIMESTAMP_NANOS).
   * @return The corresponding Avro timestamp data as Long datatype having milli/micro/nanosecond precision.
   * @throws IllegalArgumentException if the data type is unsupported.
   */
  public static Long fromTimestamp( Timestamp timestamp, AvroSpec.DataType dataType ) {
    return switch ( dataType ) {
      case TIMESTAMP_MILLIS -> fromTimestampMillis( timestamp );
      case TIMESTAMP_MICROS -> fromTimestampMicros( timestamp );
      case TIMESTAMP_NANOS -> fromTimestampNanos( timestamp );
      default -> throw new IllegalArgumentException(
        "Unsupported data type for timestamp conversion: " + dataType.name() );
    };
  }

  /**
   * Converts a Java `Timestamp` to a timestamp in milliseconds.
   * <p>
   * Calculation: Direct extraction using Timestamp.getTime() which returns milliseconds since epoch.
   *
   * @param timestampMillis The Java `Timestamp` to convert.
   * @return The corresponding timestamp as Long datatype in milliseconds, or `null` if the input is `null`.
   */
  private static Long fromTimestampMillis( Timestamp timestampMillis ) {
    Long time = null;
    if ( timestampMillis != null ) {
      time = timestampMillis.getTime();
    }
    return time;
  }

  /**
   * Converts a Java `Timestamp` to a timestamp in microseconds.
   * <p>
   * Calculation: millis * 1,000 = microseconds; add sub-millisecond part: (nanos / 1,000) % 1,000.
   *
   * @param timestampMicros The Java `Timestamp` to convert.
   * @return The corresponding timestamp as Long datatype in microseconds, or `null` if the input is `null`.
   */
  private static Long fromTimestampMicros( Timestamp timestampMicros ) {
    Long time = null;
    if ( timestampMicros != null ) {
      long millis = timestampMicros.getTime();
      long nanos = timestampMicros.getNanos();
      time = millis * 1_000L;
      if ( nanos > 0 ) {
        time = time + ( nanos / 1_000L ) % 1_000L;
      }
    }
    return time;
  }

  /**
   * Converts a Java `Timestamp` to a timestamp in nanoseconds.
   * <p>
   * Calculation: millis * 1,000,000 = nanoseconds; add sub-millisecond part: nanos % 1,000,000.
   *
   * @param timeStampNanos The Java `Timestamp` to convert.
   * @return The corresponding timestamp as Long datatype in nanoseconds, or `null` if the input is `null`.
   */
  private static Long fromTimestampNanos( Timestamp timeStampNanos ) {
    Long timeNanos = null;
    if ( timeStampNanos != null ) {
      long millis = timeStampNanos.getTime();
      long nanos = timeStampNanos.getNanos();
      timeNanos = millis * 1_000_000L;
      if ( nanos > 0 ) {
        timeNanos = timeNanos + nanos % 1_000_000L;
      }
    }
    return timeNanos;
  }

}
