# SSH Step Migration Plan - Phase 5

## Current State Analysis

### Affected Components:
1. **SSH Step Implementation** (`trans/steps/ssh/`)
   - `SSH.java` - Main step execution
   - `SSHData.java` - Connection management (direct Trilead usage)
   - `SSHMeta.java` - Step metadata
   - `SSHDialog.java` - UI dialog

2. **Current Dependencies:**
   - Trilead SSH2 build213 (legacy, limited algorithm support)
   - Direct usage of `com.trilead.ssh2.Connection`
   - Direct usage of `com.trilead.ssh2.Session`

## Migration Implementation Plan

### Step 1: Create SSH Connection Adapter for Steps
Create an adapter that bridges the existing SSHData API with our new SshConnection abstraction.

### Step 2: Update SSHData to Use SshConnectionFactory
Replace direct Trilead usage with our factory pattern while maintaining backward compatibility.

### Step 3: Update SSH Step to Use New Connection
Modify the SSH step to use the new connection interface.

### Step 4: Add Configuration Options
Add step-level configuration to choose SSH implementation (Trilead vs MINA).

### Step 5: Testing and Validation
Comprehensive testing with both implementations.

## Detailed Implementation

### Phase 5a: SSH Connection Adapter
File: `engine/src/main/java/org/pentaho/di/trans/steps/ssh/SshStepConnectionAdapter.java`

```java
package org.pentaho.di.trans.steps.ssh;

import org.pentaho.di.core.ssh.*;
import org.pentaho.di.core.exception.KettleException;
import com.trilead.ssh2.Session;

/**
 * Adapter that wraps our new SshConnection interface to maintain 
 * compatibility with existing SSH step code.
 */
public class SshStepConnectionAdapter {
    private final SshConnection connection;
    
    public SshStepConnectionAdapter(SshConnection connection) {
        this.connection = connection;
    }
    
    public StepSessionAdapter openSession() throws Exception {
        // Return adapter that wraps both Trilead Session and our exec()
        return new StepSessionAdapter(connection);
    }
    
    public void close() throws Exception {
        connection.close();
    }
    
    /**
     * Adapter for session operations that maintains compatibility
     * with existing SSH step code that expects Trilead Session behavior.
     */
    public static class StepSessionAdapter {
        private final SshConnection connection;
        
        public StepSessionAdapter(SshConnection connection) {
            this.connection = connection;
        }
        
        public void execCommand(String command) throws Exception {
            // The existing SSH step code will handle the result via SessionResult
            // We just need to expose the command execution
            this.command = command;
        }
        
        public SshExecResult getResult() throws Exception {
            if (command == null) {
                throw new IllegalStateException("No command has been executed");
            }
            return connection.exec(command, 30000); // 30 second timeout
        }
        
        public void close() {
            // No-op, connection lifecycle managed by parent
        }
        
        private String command;
    }
}
```

### Phase 5b: Update SSHData.OpenConnection()
File: `engine/src/main/java/org/pentaho/di/trans/steps/ssh/SSHData.java`

Add new method that uses SshConnectionFactory:

```java
/**
 * New connection method using our SSH abstraction layer.
 * Falls back to legacy Trilead if specified implementation is not available.
 */
public static SshStepConnectionAdapter OpenSshConnection(
    Bowl bowl, String server, int port, String username, String password,
    boolean useKey, String keyFilename, String passPhrase, int timeOut, 
    VariableSpace space, String proxyhost, int proxyport, 
    String proxyusername, String proxypassword, 
    SshImplementation preferredImplementation) throws KettleException {
    
    try {
        SshConfig config = SshConfig.create()
            .host(server)
            .port(port)
            .username(username)
            .connectTimeoutMillis(timeOut * 1000);
            
        if (useKey && !Utils.isEmpty(keyFilename)) {
            config.authType(SshConfig.AuthType.PUBLIC_KEY)
                  .keyPath(keyFilename)
                  .passphrase(passPhrase);
        } else {
            config.authType(SshConfig.AuthType.PASSWORD)
                  .password(password);
        }
        
        // Set preferred implementation or let factory decide
        if (preferredImplementation != null) {
            config.implementation(preferredImplementation);
        }
        
        SshConnection connection = SshConnectionFactory.defaultFactory().open(config);
        return new SshStepConnectionAdapter(connection);
        
    } catch (Exception e) {
        // Fall back to legacy Trilead connection for backward compatibility
        logWarning("Failed to create modern SSH connection, falling back to legacy Trilead: " + e.getMessage());
        Connection triLeadConn = OpenConnection(bowl, server, port, username, password, 
                                              useKey, keyFilename, passPhrase, timeOut, space,
                                              proxyhost, proxyport, proxyusername, proxypassword);
        return new SshStepConnectionAdapter(new TrileadConnectionWrapper(triLeadConn));
    }
}

/**
 * Wrapper to make legacy Trilead Connection work with our SshConnection interface
 */
private static class TrileadConnectionWrapper implements SshConnection {
    private final Connection triLeadConn;
    
    public TrileadConnectionWrapper(Connection conn) {
        this.triLeadConn = conn;
    }
    
    @Override
    public SshExecResult exec(String command, long timeoutMs) throws Exception {
        Session session = triLeadConn.openSession();
        try {
            session.execCommand(command);
            SessionResult result = new SessionResult(session);
            return new SshExecResult(result.getExitStatus(), result.getStdOut(), result.getStdErr());
        } finally {
            session.close();
        }
    }
    
    @Override
    public SftpSession openSftp() throws Exception {
        throw new UnsupportedOperationException("SFTP not supported in legacy wrapper");
    }
    
    @Override
    public void close() throws Exception {
        triLeadConn.close();
    }
}
```

### Phase 5c: Update SSH Step Meta Configuration
File: `engine/src/main/java/org/pentaho/di/trans/steps/ssh/SSHMeta.java`

Add new field for SSH implementation preference:

```java
private SshImplementation sshImplementation = null; // null = auto-detect

public SshImplementation getSshImplementation() {
    return sshImplementation;
}

public void setSshImplementation(SshImplementation impl) {
    this.sshImplementation = impl;
}

// Update getXML() and loadXML() to persist the setting
```

## Risk Assessment

### Low Risk:
- Our adapter maintains full backward compatibility
- Fallback to existing Trilead implementation
- No breaking changes to existing transformations

### Medium Risk:
- Algorithm compatibility differences between implementations
- Timeout handling variations
- Error message format changes

### Mitigation:
- Extensive testing with both implementations
- Graceful fallback mechanisms
- Clear logging of which implementation is being used

## Success Criteria

1. ✅ Existing SSH steps continue to work unchanged
2. ✅ New SSH steps can use MINA implementation for Ed25519 support
3. ✅ Clear configuration option for implementation choice
4. ✅ Proper error handling and fallback mechanisms
5. ✅ Performance parity or improvement with new implementation

## Testing Strategy

### Phase 5d: Integration Tests
1. **Backward Compatibility Tests**
   - Run existing SSH step transformations
   - Verify identical behavior with Trilead fallback

2. **New Implementation Tests**  
   - Test MINA implementation with modern SSH servers
   - Verify Ed25519 key support
   - Test algorithm negotiation

3. **Configuration Tests**
   - Test explicit implementation selection
   - Test auto-detection behavior
   - Test fallback scenarios

## Timeline Estimation

- **Phase 5a (Adapter)**: 2-3 days
- **Phase 5b (SSHData Update)**: 2-3 days  
- **Phase 5c (Meta Configuration)**: 1-2 days
- **Phase 5d (Testing)**: 3-4 days
- **Total: 8-12 days**

## Next Steps After Phase 5

Phase 6: Security and Algorithm Configuration
Phase 7: Production Deployment Strategy
