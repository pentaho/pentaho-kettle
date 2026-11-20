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

/**
 * Runs the KTR with pan.sh inside the PDI container.
 */
final class PanRunner implements KtrRunner {

  static final String KTR_DIR = "/tmp/docker-it/ktr";

  private final String containerId;

  PanRunner( String containerId ) {
    this.containerId = containerId;
  }

  @Override
  public ExecutionResult run( KtrCase ktr, byte[] xml ) throws Exception {
    String path = KTR_DIR + "/" + ktr.flatId() + ".ktr";
    DockerCli.writeFile( containerId, path, xml );
    DockerCli.Result result = DockerCli.exec( containerId, "sh", "-c",
      "cd " + ItConfig.pdiHome() + " && ./pan.sh -file=" + path + " -level=Basic" );
    String detail = "pan exit code " + result.exitCode() + "\n" + result.text();
    return new ExecutionResult( result.exitCode() == 0, detail );
  }
}
