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

import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.core.ssh.SshConnection;

/**
 * Adapter that wraps our new SshConnection interface to maintain
 * compatibility with existing SSH step code that expects Trilead-style session management.
 * 
 * This allows us to migrate the SSH step to use our modern SshConnection abstraction
 * while preserving the existing API contract.
 */
public class SshStepConnectionAdapter {
    private final SshConnection connection;

    public SshStepConnectionAdapter( SshConnection connection ) {
        this.connection = connection;
    }

    /**
     * Opens a session that can execute SSH commands.
     * Returns an adapter that maintains compatibility with existing SSH step code.
     */
    public StepSessionAdapter openSession() throws Exception {
        return new StepSessionAdapter( connection );
    }

    public void close() throws Exception {
        connection.close();
    }

    /**
     * Adapter for session operations that maintains compatibility
     * with existing SSH step code that expects Trilead Session behavior.
     * 
     * The existing SSH step follows this pattern:
     * 1. session.execCommand(command)
     * 2. Create SessionResult(session) to read stdout/stderr/exit code
     * 
     * We adapt this to use our SshConnection.exec() method.
     */
    public static class StepSessionAdapter {
        private SshConnection connection;
        private String executedCommand;
        private ExecResult execResult;

        public StepSessionAdapter( SshConnection connection ) {
            this.connection = connection;
        }

        /**
         * Execute a command. This matches the Trilead Session.execCommand() signature.
         * The command execution is deferred until SessionResult tries to read the results.
         */
        public void execCommand( String command ) throws Exception {
            this.executedCommand = command;
            // Execute immediately to match Trilead behavior
            this.execResult = connection.exec( command, 30000L ); // 30 second default timeout
        }

        /**
         * Get the executed command (for SessionResult compatibility).
         */
        public String getExecutedCommand() {
            return executedCommand;
        }

        /**
         * Get the execution result (for SessionResult compatibility).
         */
        public ExecResult getExecResult() {
            return execResult;
        }

        /**
         * Check if command has been executed.
         */
        public boolean hasExecuted() {
            return execResult != null;
        }

        public void close() {
            // Session lifecycle is managed by the connection
            // No explicit close needed here
        }
    }
}
