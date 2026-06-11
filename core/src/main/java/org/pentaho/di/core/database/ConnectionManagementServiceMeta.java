package org.pentaho.di.core.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.SecretsManagementException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.HttpClientManager;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Acts as a placeholder for the Connection Management Service connection, which is not a real database, but needs to be
 * acting as one in the internal flow.
 * <p>
 * Internally contains an instance of a real database and proxies all calls to it after it's data is fetched from the
 * connection management service.
 */
public class ConnectionManagementServiceMeta extends BaseDatabaseMeta implements DatabaseInterface {

    public static final String SERVICE_URL_VAR = "CONNECTION_MANAGEMENT_SERVICE_URL";

    public static final String TOKEN_VAR = "CONNECTION_MANAGEMENT_SERVICE_TOKEN";

    private DatabaseInterface delegate;

    @Override
    public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean useAutoinc, boolean addFieldName, boolean addCr) {
        return delegate == null ? null : delegate.getFieldDefinition(v, tk, pk, useAutoinc, addFieldName, addCr);
    }

    @Override
    public int[] getAccessTypeList() {
        return delegate == null ? new int[]{} : delegate.getAccessTypeList();
    }

    @Override
    public String getDriverClass() {
        return delegate == null ? null : delegate.getDriverClass();
    }

    @Override
    public String getURL(String hostname, String port, String databaseName) throws KettleDatabaseException {
        return delegate == null ? null : delegate.getURL(hostname, port, databaseName);
    }

    @Override
    public boolean supportsIndexes() {
        return delegate != null && delegate.supportsIndexes();
    }

    @Override
    public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean useAutoinc, String pk, boolean semicolon) {
        return delegate == null ? null : delegate.getAddColumnStatement(tablename, v, tk, useAutoinc, pk, semicolon);
    }

    @Override
    public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean useAutoinc, String pk, boolean semicolon) {
        return delegate == null ? null : delegate.getModifyColumnStatement(tablename, v, tk, useAutoinc, pk, semicolon);
    }

    @Override
    public String[] getUsedLibraries() {
        return delegate == null ? new String[]{} : delegate.getUsedLibraries();
    }

    @Override
    public String getSQLListOfSchemas(DatabaseMeta dbMeta) {
        return delegate == null ? null : delegate.getSQLListOfSchemas(dbMeta);
    }

    @Override
    public SqlScriptParser createSqlScriptParser() {
        return delegate == null ? null : delegate.createSqlScriptParser();
    }

    @Override
    public boolean supportsStandardTableOutput() {
        return delegate != null && delegate.supportsStandardTableOutput();
    }

    @Override
    public String getUnsupportedTableOutputMessage() {
        return delegate == null ? null : delegate.getUnsupportedTableOutputMessage();
    }

    @Override
    public String getLegacyColumnName(DatabaseMetaData dbMetaData, ResultSetMetaData rsMetaData, int index) throws KettleDatabaseException {
        return delegate == null ? null : delegate.getLegacyColumnName(dbMetaData, rsMetaData, index);
    }

    @Override
    public void putOptionalOptions(Map<String, String> extraOptions) {
        if (delegate != null) {
            delegate.putOptionalOptions(extraOptions);
        };
    }

    @Override
    public ResultSet getSchemas(DatabaseMetaData databaseMetaData, DatabaseMeta dbMeta) throws SQLException {
        return delegate == null ? null : delegate.getSchemas(databaseMetaData, dbMeta);
    }

    @Override
    public ResultSet getTables(DatabaseMetaData databaseMetaData, DatabaseMeta dbMeta, String schemaPattern, String tableNamePattern, String[] tableTypes) throws SQLException {
        return delegate == null ? null : delegate.getTables(databaseMetaData, dbMeta, schemaPattern, tableNamePattern, tableTypes);
    }

    @Override
    public List<String> getNamedClusterList() {
        return delegate == null ? Collections.emptyList() : delegate.getNamedClusterList();
    }

    @Override
    public void setConnectionSpecificInfoFromAttributes(Map<String, String> attributes) {
        if (delegate != null) {
            delegate.setConnectionSpecificInfoFromAttributes(attributes);
        }
    }

    @Override
    public String getHostname() {
        return delegate == null ? null : delegate.getHostname();
    }

    @Override
    public String getDatabasePortNumberString() {
        return delegate == null ? null : delegate.getDatabasePortNumberString();
    }

    @Override
    public String getUsername() {
        return delegate == null ? null : delegate.getUsername();
    }

    @Override
    public String getPassword() {
        return delegate == null ? null : delegate.getPassword();
    }

    @Override
    public String getDatabaseName() {
        return delegate == null ? null : delegate.getDatabaseName();
    }

    public boolean isDataLoaded() {
        return delegate != null;
    }

    public DatabaseInterface getDelegateDatabaseInterface() {
        return delegate;
    }

    public void fetchConnectionDetails() {

        try (var client = getHttpClient()) {
            var request = prepareRequest();
            var response = client.execute(request);
            var connectionDetails = verifyResponse(response);

            parseConnectionResponse(connectionDetails);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private HttpGet prepareRequest() {
        var requestUrl = System.getenv(SERVICE_URL_VAR) + this.getId();
        var request =new HttpGet( requestUrl );

        request.addHeader("Content-Type", "application/json");

        try {
            var token = CmsTokenProvider.getInstance().getToken();

            if ( StringUtils.isNotBlank( token ) ) {
                request.addHeader("Authorization", "Bearer " + token);
            }
        } catch (KettleDatabaseException e) {
            throw new RuntimeException(e);
        }

        return request;
    }

    private JsonNode verifyResponse(HttpResponse response) throws IOException {
        var statusCode = response.getStatusLine().getStatusCode();

        if (statusCode != 200) {
            throw new RuntimeException("Failed to fetch connection details from the connection management service. Status code: " + statusCode);
        }

        var content = new String(response.getEntity().getContent().readAllBytes());
        var mapper = new ObjectMapper();

        return mapper.readTree(content);
    }

    private void parseConnectionResponse(JsonNode connectionNode) {
        var databaseConnection = connectionNode.path("databaseConnection");
        var detail = databaseConnection.path("detail");
        var type = detail.path("databaseType").path("shortName").asText();
        var secretsRef = databaseConnection.path("credentials").path("secretReference").asText();

        try {
            this.delegate = DatabaseMeta.getDatabaseInterface(type);

            this.delegate.setId(databaseConnection.path("id").asText());
            this.delegate.setName(databaseConnection.path("name").asText());
            this.delegate.setHostname(detail.path("hostname").asText() );
            this.delegate.setDatabaseName(detail.path("databaseName").asText());
            this.delegate.setDatabasePortNumberString(detail.path("databasePort").asText());

            var credentials = fetchCredentials( secretsRef );
            this.delegate.setUsername( credentials.get("username") );
            this.delegate.setPassword( credentials.get("password") );

            this.delegate.setDataTablespace(detail.path("dataTablespace").asText());
            this.delegate.setIndexTablespace(detail.path("indexTablespace").asText());

            // connection.put( "access", databaseConnectionDetail.path("accessType").asText() );
            // connection.put( "servername", dbConnectionDTO.getServer() );
        } catch (KettleDatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> fetchCredentials( String connectionId ) {
        try {
            var secrets = SecretsManagementClient.getInstance().getSecrets( connectionId );
            var user = secrets.get( "username" );
            var pass = secrets.get( "password" );

            if ( user == null && pass == null ) {
                throw new SecretsManagementException(
                        SecretsManagementException.Reason.INVALID_RESPONSE,
                        "Secret bundle for connection '" + connectionId + "' did not contain 'username' or 'password'" );
            }

            return secrets;
        } catch (SecretsManagementException ex) {
            throw new RuntimeException(ex);
        }
    }

    private CloseableHttpClient getHttpClient() {
        return HttpClientManager.getInstance()
                .createBuilder()
                .build();
    }
}


class SecretsManagementClient {

    private static final String SECRETS_PATH = "/api/v1/secrets";

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int SOCKET_TIMEOUT_MS = 10_000;

    private static final SecretsManagementClient INSTANCE =
            new SecretsManagementClient( HttpClientManager.getInstance().createBuilder().ignoreSsl(true).build() );


    private final CloseableHttpClient httpClient;

    public static SecretsManagementClient getInstance() {
        return INSTANCE;
    }

    /**
     * Package-private constructor for tests; production code uses {@link #getInstance()}.
     */
    SecretsManagementClient( CloseableHttpClient httpClient ) {
        this.httpClient = InsecureSSLHttpClient.createInsecureHttpClient(); //httpClient;
    }

    public Map<String, String> getSecrets( String secretsRef ) throws SecretsManagementException {
        if ( secretsRef == null || secretsRef.trim().isEmpty() ) {
            throw new IllegalStateException( "secretsRef must be a non-blank reference" );
        }
        String baseUrl = Const.getSecretsManagementUrl();
        if ( baseUrl == null || baseUrl.trim().isEmpty() ) {
            throw new IllegalStateException(
                    "SECRETS_MANAGEMENT_URL is not configured — cannot resolve secret '" + secretsRef + "'" );
        }

        String url = stripTrailingSlash( baseUrl ) + SECRETS_PATH + "/" + secretsRef;
        HttpGet request = new HttpGet( url );
        request.setConfig( RequestConfig.custom()
                .setConnectTimeout( CONNECT_TIMEOUT_MS )
                .setSocketTimeout( SOCKET_TIMEOUT_MS )
                .build() );
        request.setHeader( "Accept", "application/json" );

        String bearerToken;
        try {
            bearerToken = CmsTokenProvider.getInstance().getToken();
        } catch ( Exception e ) {
            // Token-endpoint failure: treat as unauthorized so the caller gets the standard message.
            throw new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED,
                    "Secret unauthorized or expired", e );
        }
        if ( bearerToken != null ) {
            request.setHeader( "Authorization", "Bearer " + bearerToken );
        }

        try ( CloseableHttpResponse response = httpClient.execute( request ) ) {
            int status = response.getStatusLine().getStatusCode();
            if ( status == 200 ) {
                return parseBody( response.getEntity(), secretsRef );
            }
            throw mapStatus( status, secretsRef );
        } catch ( SecretsManagementException e ) {
            throw e;
        } catch ( SocketTimeoutException e ) {
            throw new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE,
                    "Secret store unavailable", e );
        } catch ( IOException e ) {
            throw new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE,
                    "Secret store unavailable", e );
        }
    }

    private Map<String, String> parseBody(HttpEntity entity, String secretsRef ) throws SecretsManagementException {
        if ( entity == null ) {
            throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE,
                    "Secret response was empty for '" + secretsRef + "'" );
        }
        try {
            byte[] body = EntityUtils.toByteArray( entity );
            if ( body.length == 0 ) {
                throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE,
                        "Secret response was empty for '" + secretsRef + "'" );
            }
            Map<String, String> parsed = new ObjectMapper().readValue( body,
                    new TypeReference<Map<String, String>>() { } );
            return parsed == null ? Collections.emptyMap() : parsed;
        } catch ( SecretsManagementException e ) {
            throw e;
        } catch ( IOException e ) {
            // Do not include the body in the message — it may contain plaintext secret material.
            throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE,
                    "Secret response could not be parsed for '" + secretsRef + "'", e );
        }
    }

    private SecretsManagementException mapStatus( int status, String secretsRef ) {
        return switch (status) {
            case 401, 403 -> new SecretsManagementException(SecretsManagementException.Reason.UNAUTHORIZED,
                    "Secret unauthorized or expired");
            case 404 -> new SecretsManagementException(SecretsManagementException.Reason.NOT_FOUND,
                    "Secret not found: " + secretsRef);
            default -> {
                if (status >= 500) {
                    yield new SecretsManagementException(SecretsManagementException.Reason.UNAVAILABLE,
                            "Secret store unavailable");
                }
                yield new SecretsManagementException(SecretsManagementException.Reason.UNAVAILABLE,
                        "Secret store returned unexpected HTTP " + status);
            }
        };
    }

    private static String stripTrailingSlash( String s ) {
        return s.endsWith( "/" ) ? s.substring( 0, s.length() - 1 ) : s;
    }

    /** Test-only helper to avoid pulling in StandardCharsets from callers. */
    static byte[] toBytes( String s ) {
        return s.getBytes( StandardCharsets.UTF_8 );
    }
}

class InsecureSSLHttpClient {

    /**
     * Creates an HttpClient that ignores SSL certificate validation.
     * WARNING: This should only be used for development/testing.
     * Never use in production environments.
     *
     * @return HttpClient configured to ignore SSL certificates
     */
    public static CloseableHttpClient createInsecureHttpClient() {
        try {
            return HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContexts.custom()
                                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                                    .build()
                            )
                    ).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create insecure HTTP client", e);
        }
    }

    /**
     * Alternative: Using lambda expression (Java 8+)
     */
    public static HttpClient createInsecureHttpClientLambda() {
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();

            return HttpClients.custom()
                    .setSSLContext(sslContext)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create insecure HTTP client", e);
        }
    }

    // Usage example
    public static void main(String[] args) throws Exception {
        HttpClient httpClient = createInsecureHttpClient();

        // Use the client for HTTPS requests
        // Example: execute requests without SSL certificate validation
    }
}