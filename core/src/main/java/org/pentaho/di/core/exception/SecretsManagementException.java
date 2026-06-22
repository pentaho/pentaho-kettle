package org.pentaho.di.core.exception;

import java.io.Serial;
import java.io.Serializable;

/**
 * Thrown when the Fusion secrets-management service cannot return a usable secret
 * during JDBC connection setup.
 *
 * <p>Carries a {@link Reason} so callers can map the failure to a stable, user-facing
 * message without inspecting HTTP status codes or exception chains. The original cause
 * is preserved via {@link #getCause()} for debugging in server logs, but the
 * {@link #getMessage() message} returned to end users is intentionally short and
 * contains no backend stack trace.
 */
public class SecretsManagementException extends KettleDatabaseException implements Serializable {

  @Serial
  private static final long serialVersionUID = 6681891833111833619L;
  private final Reason reason;

  public SecretsManagementException( Reason reason, String message ) {
    super( message );
    this.reason = reason;
  }

  public SecretsManagementException( Reason reason, String message, Throwable cause ) {
    super( message, cause );
    this.reason = reason;
  }

  public Reason getReason() {
    return reason;
  }

  public enum Reason {
    /**
     * 401 or 403 — caller's token is missing, malformed, expired, or lacks permission.
     */
    UNAUTHORIZED,
    /**
     * 404 — no secret exists at the given reference.
     */
    NOT_FOUND,
    /**
     * 5xx, network error, timeout — secret store reachable failure.
     */
    UNAVAILABLE,
    /**
     * Response was empty / malformed / missing expected fields.
     */
    INVALID_RESPONSE
  }
}