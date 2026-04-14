package org.pentaho.di.core.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.HttpClientManager;

import java.io.IOException;
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

        var token = System.getenv(TOKEN_VAR);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", "Bearer " + token);

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

        try {
            this.delegate = DatabaseMeta.getDatabaseInterface(type);

            this.delegate.setId(databaseConnection.path("id").asText());
            this.delegate.setName(databaseConnection.path("name").asText());
            this.delegate.setHostname(detail.path("hostname").asText() );
            this.delegate.setDatabaseName(detail.path("databaseName").asText());
            this.delegate.setDatabasePortNumberString(detail.path("databasePort").asText());

            // TODO: process credentials properly
            // this.delegate.setUsername(databaseConnection.path("credentials").path("username").asText());
            // this.delegate.setPassword(databaseConnection.path("credentials").path("password").asText());
            this.delegate.setUsername("myuser");
            this.delegate.setPassword("Encrypted ");

            this.delegate.setDataTablespace(detail.path("dataTablespace").asText());
            this.delegate.setIndexTablespace(detail.path("indexTablespace").asText());

            // connection.put( "access", databaseConnectionDetail.path("accessType").asText() );
            // connection.put( "servername", dbConnectionDTO.getServer() );
        } catch (KettleDatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    private CloseableHttpClient getHttpClient() {
        return HttpClientManager.getInstance()
                .createBuilder()
                .build();
    }
}
