/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.s3.vfs;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.s3.vfs.support.KettleS3Environment;
import org.pentaho.s3.vfs.support.TestBowl;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PDI-21063 spike, workstream A (PDI-21035, PDI-20723, PDI-21092): cost of a PVFS lookup per resolution design.
 * <p>
 * Opt-in ({@code -Dpdi21063.bench=true}); it only measures and asserts nothing about timings. Run it once per
 * production variant and label the output with {@code -Dpdi21063.variant=<name>}. Results are appended as TSV to
 * {@code target/pdi21063-a-bench.tsv}. Lookups only resolve file objects; no S3 request is timed.
 */
@EnabledIfSystemProperty( named = "pdi21063.bench", matches = "true" )
class PvfsLookupCostIT {
  private static final int[] EXTRA_VARIABLES = { 0, 200, 1000, 5000 };
  private static final long WARMUP_NANOS = Long.getLong( "pdi21063.bench.warmupMs", 3000 ) * 1_000_000L;
  private static final long ITERATION_NANOS = Long.getLong( "pdi21063.bench.iterationMs", 1000 ) * 1_000_000L;
  private static final int ITERATIONS = Integer.getInteger( "pdi21063.bench.iterations", 7 );
  private static final String VARIANT = System.getProperty( "pdi21063.variant", "unlabelled" );
  private static final String BUCKET = "bench-" + UUID.randomUUID().toString().substring( 0, 8 );

  private static MinIOContainer minio;
  private static Bowl bowl;
  private static String connection;
  private static volatile Object sink;

  @BeforeAll
  static void startMinio() throws Exception {
    minio = new MinIOContainer(
      DockerImageName.parse( "quay.io/minio/minio:latest" ).asCompatibleSubstituteFor( "minio/minio" ) );
    minio.start();
    minio.execInContainer( "mc", "alias", "set", "local", "http://localhost:9000", minio.getUserName(),
      minio.getPassword() );
    minio.execInContainer( "mc", "mb", "local/" + BUCKET );

    KettleS3Environment env = new KettleS3Environment( minio.getS3URL() );
    IMetaStore metastore = new XmlMetaStore( Files.createTempDirectory( "pdi21063-bench-metastore" ).toString() );
    bowl = new TestBowl() {
      @Override public IMetaStore getMetastore() {
        return metastore;
      }
    };
    S3Details details = env.details( "conn-bench-" + UUID.randomUUID().toString().substring( 0, 8 ) );
    details.setAccessKey( minio.getUserName() );
    details.setSecretKey( minio.getPassword() );
    assertTrue( bowl.getManager( ConnectionManager.class ).save( details ), "precondition: connection saved" );
    connection = details.getName();
  }

  @AfterAll
  static void stopMinio() {
    if ( minio != null ) {
      minio.stop();
    }
  }

  @Test
  void measureLookupCost() throws Exception {
    IKettleVFS vfs = KettleVFS.getInstance( bowl );
    String prefix = "pvfs://" + connection + "/" + BUCKET + "/";
    Path report = Paths.get( "target", "pdi21063-a-bench.tsv" );
    if ( !Files.exists( report ) ) {
      Files.write( report, ( "variant\tvariables\tcase\tns_per_op\tbytes_per_op\n" ).getBytes(
        StandardCharsets.UTF_8 ) );
    }

    for ( int extra : EXTRA_VARIABLES ) {
      // A transformation space: System properties (initializeVariablesFrom(null)) plus user/kettle.properties ones.
      VariableSpace space = new Variables();
      space.initializeVariablesFrom( null );
      for ( int i = 0; i < extra; i++ ) {
        space.setVariable( "bench.variable." + i, "value-" + i + "-" + UUID.randomUUID() );
      }
      int variables = space.listVariables().length;

      long[] counter = { 0 };
      record( report, variables, "copy_variables", () -> {
        Variables copy = new Variables();
        copy.initializeVariablesFrom( space );
        sink = copy;
      } );
      record( report, variables, "lookup_new_file", () -> sink =
        vfs.getFileObject( prefix + "new-" + extra + "-" + counter[ 0 ]++ + ".txt", space ) );
      record( report, variables, "lookup_same_file", () -> sink =
        vfs.getFileObject( prefix + "same.txt", space ) );
    }
  }

  private static void record( Path report, int variables, String name, Operation operation ) throws Exception {
    run( operation, WARMUP_NANOS );
    double[] nanosPerOp = new double[ ITERATIONS ];
    double[] bytesPerOp = new double[ ITERATIONS ];
    for ( int i = 0; i < ITERATIONS; i++ ) {
      long bytesBefore = allocatedBytes();
      long start = System.nanoTime();
      long ops = run( operation, ITERATION_NANOS );
      long elapsed = System.nanoTime() - start;
      nanosPerOp[ i ] = (double) elapsed / ops;
      bytesPerOp[ i ] = (double) ( allocatedBytes() - bytesBefore ) / ops;
    }
    String line = String.format( "%s\t%d\t%s\t%.0f\t%.0f%n", VARIANT, variables, name, median( nanosPerOp ),
      median( bytesPerOp ) );
    System.out.print( "PDI21063-BENCH " + line );
    Files.write( report, line.getBytes( StandardCharsets.UTF_8 ), StandardOpenOption.APPEND );
  }

  private static long run( Operation operation, long durationNanos ) throws Exception {
    long ops = 0;
    long end = System.nanoTime() + durationNanos;
    do {
      for ( int i = 0; i < 64; i++ ) {
        operation.run();
      }
      ops += 64;
    } while ( System.nanoTime() < end );
    return ops;
  }

  private static long allocatedBytes() {
    return ( (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean() ).getCurrentThreadAllocatedBytes();
  }

  private static double median( double[] values ) {
    double[] sorted = values.clone();
    Arrays.sort( sorted );
    return sorted[ sorted.length / 2 ];
  }

  @FunctionalInterface
  private interface Operation {
    void run() throws Exception;
  }
}
