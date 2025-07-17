# Carte REST API Documentation

## Overview

Carte is Pentaho Data Integration's (PDI) remote server component that provides a web-based API for executing and monitoring transformations and jobs. This document describes the REST API endpoints available in Carte.

### Base URL
```
http://{hostname}:{port}/kettle
```

Default port: 8080

### Authentication
Carte supports basic HTTP authentication. You can configure username and password in the carte configuration.

### Response Formats
Most endpoints support both XML and HTML output formats controlled by the `xml` parameter:
- `xml=Y` - Returns XML response
- `xml=N` or omitted - Returns HTML response

---

## Server Management Endpoints

### Get Server Status
```
GET /kettle/status
```

Retrieves comprehensive server status including system information, running transformations, and jobs.

**Parameters:**
- `xml` (optional, boolean) - Output format. `Y` for XML, HTML otherwise.

**Response:** Server status including:
- Memory usage
- CPU information
- Running transformations and jobs
- System information

**Example:**
```bash
curl "http://localhost:8080/kettle/status?xml=Y"
```

### Stop Carte Server
```
GET /kettle/stopCarte
```

Gracefully shuts down the Carte server.

**Parameters:**
- `xml` (optional, boolean) - Output format

**Response:** Confirmation of shutdown request

---

## Transformation Management Endpoints

### Add Transformation
```
POST /kettle/addTrans
```

Uploads and registers a transformation for execution.

**Parameters:**
- Transformation XML content in request body

**Response:** Transformation ID and status

### Register Transformation
```
GET /kettle/registerTrans
```

Registers a transformation from repository or file system.

**Parameters:**
- `name` (required) - Transformation name
- `xml` (optional) - Output format

### Get Transformation Status
```
GET /kettle/transStatus
```

Retrieves status of a specific transformation.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format
- `from` (optional) - Starting log line number
- `sendResult` (optional) - Include execution results

**Response:** Transformation status including:
- Execution status
- Step statuses
- Log information
- Performance metrics

**Example:**
```bash
curl "http://localhost:8080/kettle/transStatus?name=my-transformation&xml=Y"
```

### Start Transformation
```
GET /kettle/startTrans
```

Starts execution of a registered transformation.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format

### Stop Transformation
```
GET /kettle/stopTrans
```

Stops execution of a running transformation.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format

### Pause Transformation
```
GET /kettle/pauseTrans
```

Pauses execution of a running transformation.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format

### Remove Transformation
```
GET /kettle/removeTrans
```

Removes a transformation from the server.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format

### Execute Transformation
```
GET /kettle/executeTrans
```

Executes a transformation synchronously and returns results.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format

### Run Transformation
```
GET /kettle/runTrans
```

Runs a transformation asynchronously.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format

### Prepare Transformation Execution
```
GET /kettle/prepareExec
```

Prepares a transformation for execution without starting it.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format

### Start Transformation Execution
```
GET /kettle/startExec
```

Starts execution of a prepared transformation.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format

### Cleanup Transformation
```
GET /kettle/cleanupTrans
```

Cleans up resources used by a transformation.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID
- `xml` (optional) - Output format

### Get Transformation Image
```
GET /kettle/transImage
```

Retrieves a visual representation (image) of the transformation.

**Parameters:**
- `name` (required) - Transformation name
- `id` (optional) - Transformation ID

**Response:** PNG image of the transformation flow

---

## Job Management Endpoints

### Add Job
```
POST /kettle/addJob
```

Uploads and registers a job for execution.

**Parameters:**
- Job XML content in request body

**Response:** Job ID and status

### Register Job
```
GET /kettle/registerJob
```

Registers a job from repository or file system.

**Parameters:**
- `name` (required) - Job name
- `xml` (optional) - Output format

### Get Job Status
```
GET /kettle/jobStatus
```

Retrieves status of a specific job.

**Parameters:**
- `name` (required) - Job name
- `id` (optional) - Job ID
- `xml` (optional) - Output format
- `from` (optional) - Starting log line number

**Response:** Job status including:
- Execution status
- Job entry statuses
- Log information
- Performance metrics

**Example:**
```bash
curl "http://localhost:8080/kettle/jobStatus?name=my-job&xml=Y"
```

### Start Job
```
GET /kettle/startJob
```

Starts execution of a registered job.

**Parameters:**
- `name` (required) - Job name
- `id` (optional) - Job ID
- `xml` (optional) - Output format

### Stop Job
```
GET /kettle/stopJob
```

Stops execution of a running job.

**Parameters:**
- `name` (required) - Job name
- `id` (optional) - Job ID
- `xml` (optional) - Output format

### Remove Job
```
GET /kettle/removeJob
```

Removes a job from the server.

**Parameters:**
- `name` (required) - Job name
- `id` (optional) - Job ID
- `xml` (optional) - Output format

### Execute Job
```
GET /kettle/executeJob
```

Executes a job synchronously and returns results.

**Parameters:**
- `name` (required) - Job name
- `id` (optional) - Job ID
- `xml` (optional) - Output format

### Run Job
```
GET /kettle/runJob
```

Runs a job asynchronously.

**Parameters:**
- `name` (required) - Job name
- `id` (optional) - Job ID
- `xml` (optional) - Output format

### Get Job Image
```
GET /kettle/jobImage
```

Retrieves a visual representation (image) of the job.

**Parameters:**
- `name` (required) - Job name
- `id` (optional) - Job ID

**Response:** PNG image of the job flow

---

## Utility Endpoints

### Get Properties
```
GET /kettle/properties
```

Retrieves server configuration properties.

**Parameters:**
- `xml` (optional) - Output format

**Response:** Server properties and configuration

### Next Sequence Value
```
GET /kettle/nextSequence
```

Gets the next value from a named sequence.

**Parameters:**
- `name` (required) - Sequence name
- `increment` (optional) - Increment amount
- `xml` (optional) - Output format

### Sniff Step
```
GET /kettle/sniffStep
```

Sniffs data output from a transformation step for analysis.

**Parameters:**
- `trans` (required) - Transformation name
- `step` (required) - Step name
- `copynr` (optional) - Copy number
- `type` (optional) - Data type
- `xml` (optional) - Output format

### Add Export
```
POST /kettle/addExport
```

Exports transformation or job resources.

**Parameters:**
- Export configuration in request body

---

## Cluster Management Endpoints

### Register Slave Server
```
GET /kettle/registerSlave
```

Registers a slave server in the cluster.

**Parameters:**
- Slave server configuration
- `xml` (optional) - Output format

### Get Slaves
```
GET /kettle/getSlaves
```

Retrieves list of registered slave servers.

**Parameters:**
- `xml` (optional) - Output format

### Register Package
```
POST /kettle/registerPackage
```

Registers a deployment package.

**Parameters:**
- Package content in request body

---

## Socket Management Endpoints

### Allocate Server Socket
```
GET /kettle/allocateSocket
```

Allocates a server socket for communication.

**Parameters:**
- `rangeStart` (required) - Starting port range
- `hostname` (required) - Hostname
- `id` (required) - Socket ID
- `xml` (optional) - Output format

### List Server Sockets
```
GET /kettle/listSocket
```

Lists allocated server sockets.

**Parameters:**
- `xml` (optional) - Output format

---

## JAX-RS REST Endpoints

### System Information
```
GET /carte/systemInfo
```

Retrieves system information in JSON format.

**Response:** JSON object with server status

### Configuration Details
```
GET /carte/configDetails
```

Retrieves configuration details in JSON format.

**Response:** JSON array with configuration parameters

### List Transformations
```
GET /carte/transformations
```

Retrieves list of transformations in JSON format.

**Response:** JSON array of transformation entries

### List Transformations (Detailed)
```
GET /carte/transformations/detailed
```

Retrieves detailed list of transformations in JSON format.

**Response:** JSON array with detailed transformation information

---

## Error Handling

All endpoints return appropriate HTTP status codes:

- `200 OK` - Request successful
- `404 Not Found` - Transformation/Job not found
- `500 Internal Server Error` - Server error occurred

Error responses include error messages in the specified format (XML or HTML).

---

## Authentication

Configure authentication in the carte configuration XML:

```xml
<slaveserver>
  <name>slave-server-name</name>
  <hostname>localhost</hostname>
  <port>8080</port>
  <username>admin</username>
  <password>password</password>
</slaveserver>
```

Use HTTP Basic Authentication:

```bash
curl -u username:password "http://localhost:8080/kettle/status?xml=Y"
```

---

## Examples

### Get Server Status
```bash
# Get status in XML format
curl "http://localhost:8080/kettle/status?xml=Y"

# Get status in HTML format
curl "http://localhost:8080/kettle/status"
```

### Execute a Transformation
```bash
# Start a transformation
curl "http://localhost:8080/kettle/startTrans?name=my-transformation&xml=Y"

# Check transformation status
curl "http://localhost:8080/kettle/transStatus?name=my-transformation&xml=Y"

# Stop the transformation
curl "http://localhost:8080/kettle/stopTrans?name=my-transformation&xml=Y"
```

### Execute a Job
```bash
# Start a job
curl "http://localhost:8080/kettle/startJob?name=my-job&xml=Y"

# Check job status
curl "http://localhost:8080/kettle/jobStatus?name=my-job&xml=Y"

# Stop the job
curl "http://localhost:8080/kettle/stopJob?name=my-job&xml=Y"
```

---

## Security Considerations

1. **Enable Authentication**: Always configure username/password authentication
2. **Use HTTPS**: Configure SSL/TLS for production environments
3. **Network Security**: Restrict access to Carte ports using firewalls
4. **Input Validation**: Carte validates input parameters to prevent injection attacks

---

## Configuration

Sample carte configuration (`carte-config.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<slave_config>
  <slaveserver>
    <name>Carte-Server</name>
    <hostname>localhost</hostname>
    <port>8080</port>
    <username>admin</username>
    <password>password</password>
    <master>N</master>
  </slaveserver>
  <max_log_lines>10000</max_log_lines>
  <max_log_timeout_minutes>1440</max_log_timeout_minutes>
  <object_timeout_minutes>1440</object_timeout_minutes>
</slave_config>
```

Start Carte with configuration:
```bash
./carte.sh carte-config.xml
```

---

## Troubleshooting

### Common Issues

1. **Connection Refused**: Check if Carte is running and port is accessible
2. **Authentication Failed**: Verify username/password configuration
3. **Transformation Not Found**: Ensure transformation is registered with correct name
4. **Memory Issues**: Monitor server resources and adjust JVM settings

### Logging

Enable debug logging to troubleshoot issues:
```bash
./carte.sh carte-config.xml -l DEBUG
```

### Health Check

Simple health check endpoint:
```bash
curl "http://localhost:8080/kettle/status"
```

If this returns a 200 status code, Carte is running properly.
