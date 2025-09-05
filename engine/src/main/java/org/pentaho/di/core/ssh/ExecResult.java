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

package org.pentaho.di.core.ssh;

public class ExecResult {
  private final String stdout;
  private final String stderr;
  private final String combined;
  private final int exitCode;
  private final boolean error;

  public ExecResult( String stdout, String stderr, int exitCode ) {
    this( stdout, stderr, stdout + ( stderr == null ? "" : stderr ), exitCode, exitCode != 0 );
  }

  public ExecResult( String stdout, String stderr, String combined, int exitCode, boolean error ) {
    this.stdout = stdout;
    this.stderr = stderr;
    this.combined = combined;
    this.exitCode = exitCode;
    this.error = error;
  }

  public String getStdout() {
    return stdout;
  }

  public String getStderr() {
    return stderr;
  }

  public int getExitCode() {
    return exitCode;
  }

  public String getCombined() {
    return combined;
  }

  public boolean hasErrorOutput() {
    return stderr != null && !stderr.isEmpty();
  }

  public boolean isError() {
    return error;
  }
}
