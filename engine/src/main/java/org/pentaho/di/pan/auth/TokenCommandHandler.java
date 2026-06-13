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

package org.pentaho.di.pan.auth;

import com.pentaho.oauth.client.BrowserAuthSessionHolder;
import org.pentaho.di.i18n.BaseMessages;

import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles Pan token commands:
 *
 * <pre>
 *   Pan.bat -auth:set-token --server &lt;url&gt; [--token &lt;token&gt;]
 *   Pan.bat -auth:clear-token [--server &lt;url&gt;]
 * </pre>
 *
 * <p>
 * When {@code --token} is omitted the token is read from the console without
 * echoing (same as a password prompt). If no console is attached (CI
 * pipelines),
 * the user must supply the token via {@code --token} instead.
 *
 * <p>
 * <b>Security note</b>: The token is stored in
 * {@code ~/.kettle/.kettle-sessions}
 * by {@link BrowserAuthSessionHolder}. The IdP validates every token that
 * arrives at the server (via introspection or userinfo), so an expired or bogus
 * value
 * is rejected there. DPoP, when enabled, additionally binds tokens to the
 * client
 * keypair, preventing replay by an attacker who captured the file.
 * Theft of the file by the OS user themselves (or a process running as that
 * user)
 * is an accepted risk — the same risk applies to SSH private keys stored in
 * {@code ~/.ssh}.
 */
public class TokenCommandHandler {

  private static final String TOKEN_PREFIX = "-auth:";
  private static final Pattern JWT_EXP_PATTERN = Pattern.compile( "\"exp\"\\s*:\\s*(\\d+)" );
  private static final Pattern JWT_STR_CLAIM = Pattern.compile( "\"(%s)\"\\s*:\\s*\"([^\"]+)\"" );

  private final BrowserAuthSessionHolder sessionHolder;

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( TokenCommandHandler.class, key, tokens );
  }

  public TokenCommandHandler() {
    this( SharedBrowserAuthSessionHolder.get() );
  }

  TokenCommandHandler( BrowserAuthSessionHolder sessionHolder ) {
    this.sessionHolder = sessionHolder;
  }

  public static boolean hasTokenCommand( String[] args ) {
    if ( args == null ) {
      return false;
    }
    for ( String arg : args ) {
      if ( arg != null && arg.toLowerCase().startsWith( TOKEN_PREFIX ) ) {
        return true;
      }
    }
    return false;
  }

  public int execute( String[] args ) {
    String command = null;
    for ( String arg : args ) {
      if ( arg != null && arg.toLowerCase().startsWith( TOKEN_PREFIX ) ) {
        command = arg.substring( TOKEN_PREFIX.length() ).toLowerCase();
        break;
      }
    }
    if ( command == null ) {
      printError( message( "TokenCommandHandler.NoCommand" ) );
      return 1;
    }

    return switch ( command ) {
      case "set-token" -> handleSetToken( args );
      case "clear-token" -> handleClearToken( args );
      default -> {
        printError( message( "TokenCommandHandler.UnknownCommand", command ) );
        printUsage();
        yield 1;
      }
    };
  }

  private int handleSetToken( String[] args ) {
    String serverUrl = findArg( args, "--server" );
    if ( serverUrl == null || serverUrl.isBlank() ) {
      printError( message( "TokenCommandHandler.MissingServerArg" ) );
      printUsage();
      return 1;
    }

    String token = findArg( args, "--token" );
    if ( token == null || token.isBlank() ) {
      token = promptForToken();
      if ( token == null || token.isBlank() ) {
        printError( message( "TokenCommandHandler.NoTokenProvided" ) );
        return 1;
      }
    }

    // Parse what we can from the JWT payload so the session is as rich as
    // possible without needing a round-trip to the server.
    JwtClaims claims = extractJwtClaims( token );

    // For opaque tokens (no exp claim) the user can supply a lifetime hint.
    // This is ignored when JWT auto-detection succeeds.
    long expiresInSeconds = resolveExpiresInSeconds( args, claims );
    if ( expiresInSeconds < 0 ) {
      return 1;
    }

    // Pass the expiry info to the holder. If both are 0 (opaque token and no
    // --expires-in), the holder stores -1 in oauthTokenExpiry, which causes
    // the client-side expiry guard to be skipped — the token is always returned
    // and the server is authoritative for validity via introspection/userinfo.
    // Do not try to derive a broker registration ID from the token: the JWT iss
    // claim is an issuer URL, not the server's registration-id key.
    sessionHolder.storeOAuthToken(
      new BrowserAuthSessionHolder.OAuthTokenData(
        serverUrl, token, null, "Bearer", null,
        expiresInSeconds, claims.expEpochSeconds, claims.username ) );

    printStoredTokenSummary( serverUrl, claims, expiresInSeconds );
    return 0;
  }

  private int handleClearToken( String[] args ) {
    String serverUrl = findArg( args, "--server" );
    if ( serverUrl == null || serverUrl.isBlank() ) {
      sessionHolder.clearSession();
      print( message( "TokenCommandHandler.ClearedAllCredentials" ) );
    } else {
      sessionHolder.clearOAuthToken( serverUrl );
      print( message( "TokenCommandHandler.ClearedServerToken", serverUrl ) );
    }
    return 0;
  }

  /**
   * Minimal set of claims we read from the JWT payload in one pass.
   */
  private static final class JwtClaims {
    long expEpochSeconds;
    String username; // preferred_username → sub fallback → null for opaque
  }

  /**
   * Decodes the JWT payload and extracts {@code exp}, {@code preferred_username}
   * (or {@code sub} as fallback). Returns a zero-valued {@link JwtClaims} for
   * opaque tokens or malformed JWTs — callers must treat every field as optional.
   */
  private JwtClaims extractJwtClaims( String token ) {
    JwtClaims claims = new JwtClaims();
    if ( token == null ) {
      return claims;
    }
    String[] parts = token.split( "\\.", -1 );
    if ( parts.length < 2 ) {
      return claims;
    }
    try {
      String payload = new String(
        Base64.getUrlDecoder().decode( parts[ 1 ] ), StandardCharsets.UTF_8 );

      Matcher expMatcher = JWT_EXP_PATTERN.matcher( payload );
      if ( expMatcher.find() ) {
        claims.expEpochSeconds = Long.parseLong( expMatcher.group( 1 ) );
      }

      // preferred_username is the human-readable name set by the IdP.
      // sub is always present but is typically an opaque UUID — use it only
      // as a last resort so the session at least has some identity info.
      String preferredUsername = extractStrClaim( payload, "preferred_username" );
      claims.username = preferredUsername != null ? preferredUsername : extractStrClaim( payload, "sub" );

    } catch ( Exception ignored ) {
      // opaque token or base64 padding error — return zero-value claims
    }
    return claims;
  }

  private String extractStrClaim( String payload, String claimName ) {
    Matcher m = Pattern.compile(
      String.format( JWT_STR_CLAIM.pattern(), Pattern.quote( claimName ) ) ).matcher( payload );
    return m.find() ? m.group( 2 ) : null;
  }

  private long resolveExpiresInSeconds( String[] args, JwtClaims claims ) {
    if ( claims.expEpochSeconds > 0 ) {
      return 0;
    }

    String expiresInArg = findArg( args, "--expires-in" );
    if ( expiresInArg == null ) {
      return 0;
    }

    try {
      long expiresInSeconds = Long.parseLong( expiresInArg );
      if ( expiresInSeconds < 0 ) {
        printError( message( "TokenCommandHandler.ExpiresInPositive" ) );
        return -1;
      }
      return expiresInSeconds;
    } catch ( NumberFormatException e ) {
      printError( message( "TokenCommandHandler.ExpiresInNumber" ) );
      return -1;
    }
  }

  private void printStoredTokenSummary( String serverUrl, JwtClaims claims, long expiresInSeconds ) {
    print( message( "TokenCommandHandler.StoredServer", serverUrl ) );
    if ( claims.username != null ) {
      print( message( "TokenCommandHandler.DetectedUsername", claims.username ) );
    }
    if ( claims.expEpochSeconds > 0 ) {
      print( message( "TokenCommandHandler.DetectedExpiry", String.valueOf( claims.expEpochSeconds ) ) );
    } else if ( expiresInSeconds > 0 ) {
      print( message( "TokenCommandHandler.LifetimeHint", String.valueOf( expiresInSeconds ) ) );
    } else {
      print( message( "TokenCommandHandler.ExpiryUnknown" ) );
    }
    print( message( "TokenCommandHandler.NextInvocation" ) );
  }

  /**
   * Prompts the user to enter the token without echoing it to the terminal.
   * Returns null if no console is attached (e.g. CI/pipe).
   */
  private String promptForToken() {
    Console console = System.console();
    if ( console == null ) {
      return null;
    }
    char[] tokenChars = console.readPassword( message( "TokenCommandHandler.PromptToken" ) );
    if ( tokenChars == null ) {
      return null;
    }
    String token = new String( tokenChars );
    Arrays.fill( tokenChars, '\0' ); // wipe from memory
    return token;
  }

  /**
   * Scan args for {@code --key value} pairs.
   */
  private String findArg( String[] args, String flag ) {
    for ( int i = 0; i < args.length - 1; i++ ) {
      if ( flag.equalsIgnoreCase( args[ i ] ) ) {
        return args[ i + 1 ];
      }
    }
    return null;
  }

  private void printUsage() {
    print( "" );
    print( message( "TokenCommandHandler.UsageHeader" ) );
    print( message( "TokenCommandHandler.UsageSetToken" ) );
    print( message( "TokenCommandHandler.UsageSetTokenDesc" ) );
    print( message( "TokenCommandHandler.UsageSetTokenHiddenInput" ) );
    print( message( "TokenCommandHandler.UsageJwtExpiry" ) );
    print( message( "TokenCommandHandler.UsageOpaqueExpiry" ) );
    print( "" );
    print( message( "TokenCommandHandler.UsageClearToken" ) );
    print( message( "TokenCommandHandler.UsageClearTokenDesc" ) );
    print( "" );
    print( message( "TokenCommandHandler.UsageExamples" ) );
    print( message( "TokenCommandHandler.ExampleSetToken" ) );
    print( message( "TokenCommandHandler.ExampleSetTokenWithValue" ) );
    print( message( "TokenCommandHandler.ExampleSetOpaqueToken" ) );
    print( message( "TokenCommandHandler.ExampleClearServer" ) );
    print( message( "TokenCommandHandler.ExampleClearAll" ) );
  }

  @SuppressWarnings( "java:S106" )
  private void printError( String message ) {
    System.err.println( message );
  }

  @SuppressWarnings( "java:S106" )
  private void print( String message ) {
    System.out.println( message );
  }
}
