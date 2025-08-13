/*
 * ! ******************************************************************************
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

package org.pentaho.di.trans.steps.ssh;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.core.util.Utils;

/**
 * Adapter version of SessionResult that can work with both:
 * 1. Traditional Trilead Session objects (via existing SessionResult)
 * 2. Our new SshExecResult from the abstraction layer
 * 
 * This maintains API compatibility while allowing the SSH step to use
 * either implementation seamlessly.
 */
public class SessionResultAdapter {

    private String stdout;
    private String stderr;
    private boolean stderrortype;
    private Integer exitStatus;

    /**
     * Constructor for traditional Trilead Session (backward compatibility).
     */
    public SessionResultAdapter( com.trilead.ssh2.Session session ) throws KettleException {
        SessionResult legacyResult = new SessionResult( session );
        this.stdout = legacyResult.getStdOut();
        this.stderr = legacyResult.getStdErr();
        this.stderrortype = legacyResult.isStdTypeErr();
        this.exitStatus = session.getExitStatus();
    }

    /**
     * Constructor for our new ExecResult (modern implementation).
     */
    public SessionResultAdapter( ExecResult execResult ) throws KettleException {
        this.stdout = execResult.getStdout();
        this.stderr = execResult.getStderr();
        this.stderrortype = !Utils.isEmpty( stderr );
        this.exitStatus = execResult.getExitCode();
    }

    /**
     * Constructor for adapter session (from SshStepConnectionAdapter).
     */
    public SessionResultAdapter( SshStepConnectionAdapter.StepSessionAdapter session ) throws KettleException {
        if ( !session.hasExecuted() ) {
            throw new KettleException( "No command has been executed on this session" );
        }

        ExecResult execResult = session.getExecResult();
        this.stdout = execResult.getStdout();
        this.stderr = execResult.getStderr();
        this.stderrortype = !Utils.isEmpty( stderr );
        this.exitStatus = execResult.getExitCode();
    }

    public String getStdErr() {
        return this.stderr;
    }

    public String getStd() {
        return getStdOut() + getStdErr();
    }

    public String getStdOut() {
        return this.stdout;
    }

    public boolean isStdTypeErr() {
        return this.stderrortype;
    }

    public Integer getExitStatus() {
        return this.exitStatus;
    }
}
