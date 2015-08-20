/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.pentaho.di.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.PerformanceLogTable;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.di.repository.pur.PurRepository;
import org.pentaho.di.repository.pur.model.ObjectAce;
import org.pentaho.di.repository.pur.model.ObjectAcl;
import org.pentaho.di.repository.pur.model.RepositoryObjectAce;
import org.pentaho.di.repository.pur.model.RepositoryObjectRecipient;
import org.pentaho.di.trans.SlaveStepCopyPartitionDistribution;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransDependency;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.ui.repository.pur.services.IAclService;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.repository.pur.services.IRevisionService;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.w3c.dom.Node;

public abstract class RepositoryTestBase {

  // ~ Static fields/initializers ======================================================================================

  protected static final String EXP_USERNAME = "Apache Tomcat";

  protected static final String EXP_LOGIN = "admin";
  protected static final String EXP_TENANT = "acme";
  
  protected static final String EXP_LOGIN_PLUS_TENANT = EXP_LOGIN + "-/pentaho/" + EXP_TENANT;

  protected static final String VERSION_COMMENT_V1 = "hello";

  protected static final String VERSION_LABEL_V1 = "1.0";

  protected static final Log logger = LogFactory.getLog(RepositoryTestBase.class);

  protected static final String DIR_CONNECTIONS = "connections";

  protected static final String DIR_SCHEMAS = "schemas";

  protected static final String DIR_SLAVES = "slaves";

  protected static final String DIR_CLUSTERS = "clusters";

  protected static final String DIR_TRANSFORMATIONS = "transformations";

  protected static final String DIR_JOBS = "jobs";

  protected static final String DIR_TMP = "tmp";

  protected static final String EXP_JOB_NAME = "job1";

  protected static final String EXP_JOB_DESC = "jobDesc";

  protected static final String EXP_JOB_EXTENDED_DESC = "jobExtDesc";

  protected static final String EXP_JOB_VERSION = "anything";

  protected static final int EXP_JOB_STATUS = 12;

  protected static final String EXP_JOB_CREATED_USER = "jerry";

  protected static final Date EXP_JOB_CREATED_DATE = new Date();

  protected static final String EXP_JOB_MOD_USER = "george";

  protected static final Date EXP_JOB_MOD_DATE = new Date();

  protected static final String EXP_JOB_PARAM_1_DESC = "param1desc";

  protected static final String EXP_JOB_PARAM_1_NAME = "param1";

  protected static final String EXP_JOB_PARAM_1_DEF = "param1default";

  protected static final String EXP_JOB_LOG_TABLE_INTERVAL = "15";

  protected static final String EXP_JOB_LOG_TABLE_CONN_NAME = "connName";

  protected static final String EXP_JOB_LOG_TABLE_SCHEMA_NAME = "schemaName";

  protected static final String EXP_JOB_LOG_TABLE_TABLE_NAME = "tableName";

  protected static final String EXP_JOB_LOG_TABLE_TIMEOUT_IN_DAYS = "2";

  protected static final String EXP_JOB_LOG_TABLE_SIZE_LIMIT = "250";

  protected static final boolean EXP_JOB_BATCH_ID_PASSED = true;

  protected static final String EXP_JOB_SHARED_OBJECTS_FILE = ".kettle/whatever";

  protected static final String EXP_JOB_ENTRY_1_NAME = "createFile";

  protected static final String EXP_JOB_ENTRY_1_FILENAME = "/tmp/whatever";

  protected static final String EXP_JOB_ENTRY_2_NAME = "deleteFile";

  protected static final String EXP_JOB_ENTRY_2_FILENAME = "/tmp/whatever";

  protected static final int EXP_JOB_ENTRY_1_COPY_X_LOC = 10;

  protected static final int EXP_JOB_ENTRY_1_COPY_Y_LOC = 10;

  protected static final int EXP_JOB_ENTRY_2_COPY_X_LOC = 75;

  protected static final int EXP_JOB_ENTRY_2_COPY_Y_LOC = 10;

  protected static final int EXP_NOTEPAD_X = 10;

  protected static final String EXP_NOTEPAD_NOTE = "blah";

  protected static final int EXP_NOTEPAD_Y = 200;

  protected static final int EXP_NOTEPAD_WIDTH = 50;

  protected static final int EXP_NOTEPAD_HEIGHT = 25;

  protected static final String EXP_DBMETA_NAME = "haha";

  protected static final String EXP_DBMETA_HOSTNAME = "acme";

  protected static final String EXP_DBMETA_TYPE = "ORACLE";

  protected static final int EXP_DBMETA_ACCESS = DatabaseMeta.TYPE_ACCESS_NATIVE;

  protected static final String EXP_DBMETA_DBNAME = "lksjdf";

  protected static final String EXP_DBMETA_PORT = "10521";

  protected static final String EXP_DBMETA_USERNAME = "elaine";

  protected static final String EXP_DBMETA_PASSWORD = "password";

  protected static final String EXP_DBMETA_SERVERNAME = "serverName";

  protected static final String EXP_DBMETA_DATA_TABLESPACE = "dataTablespace";

  protected static final String EXP_DBMETA_INDEX_TABLESPACE = "indexTablespace";

  protected static final String EXP_SLAVE_NAME = "slave54545";

  protected static final String EXP_SLAVE_HOSTNAME = "slave98745";

  protected static final String EXP_SLAVE_PORT = "11111";

  protected static final String EXP_SLAVE_USERNAME = "cosmo";

  protected static final String EXP_SLAVE_PASSWORD = "password";

  protected static final String EXP_SLAVE_PROXY_HOSTNAME = "proxySlave542254";

  protected static final String EXP_SLAVE_PROXY_PORT = "11112";

  protected static final String EXP_SLAVE_NON_PROXY_HOSTS = "ljksdflsdf";

  protected static final boolean EXP_SLAVE_MASTER = true;

  protected static final String EXP_SLAVE_HOSTNAME_V2 = "slave98561111";

  protected static final String EXP_DBMETA_HOSTNAME_V2 = "acme98734";

  protected static final String VERSION_COMMENT_V2 = "v2 blah blah blah";

  protected static final String EXP_JOB_DESC_V2 = "jobDesc0368";

  protected static final String EXP_TRANS_NAME = "transMeta";

  protected static final String EXP_TRANS_DESC = "transMetaDesc";

  protected static final String EXP_TRANS_EXTENDED_DESC = "transMetaExtDesc";

  protected static final String EXP_TRANS_VERSION = "2.0";

  protected static final int EXP_TRANS_STATUS = 2;

  protected static final String EXP_TRANS_PARAM_1_DESC = "transParam1Desc";

  protected static final String EXP_TRANS_PARAM_1_DEF = "transParam1Def";

  protected static final String EXP_TRANS_PARAM_1_NAME = "transParamName";

  protected static final String EXP_TRANS_CREATED_USER = "newman";

  protected static final Date EXP_TRANS_CREATED_DATE = new Date();

  protected static final String EXP_TRANS_MOD_USER = "banya";

  protected static final Date EXP_TRANS_MOD_DATE = new Date();

  protected static final String EXP_TRANS_LOG_TABLE_CONN_NAME = "transLogTableConnName";

  protected static final String EXP_TRANS_LOG_TABLE_INTERVAL = "34";

  protected static final String EXP_TRANS_LOG_TABLE_SCHEMA_NAME = "transLogTableSchemaName";

  protected static final String EXP_TRANS_LOG_TABLE_SIZE_LIMIT = "600";

  protected static final String EXP_TRANS_LOG_TABLE_TABLE_NAME = "transLogTableTableName";

  protected static final String EXP_TRANS_LOG_TABLE_TIMEOUT_IN_DAYS = "5";

  protected static final String EXP_TRANS_MAX_DATE_TABLE = "transMaxDateTable";

  protected static final String EXP_TRANS_MAX_DATE_FIELD = "transMaxDateField";

  protected static final double EXP_TRANS_MAX_DATE_OFFSET = 55;

  protected static final double EXP_TRANS_MAX_DATE_DIFF = 70;

  protected static final int EXP_TRANS_SIZE_ROWSET = 833;

  protected static final int EXP_TRANS_SLEEP_TIME_EMPTY = 4;

  protected static final int EXP_TRANS_SLEEP_TIME_FULL = 9;

  protected static final boolean EXP_TRANS_USING_UNIQUE_CONN = true;

  protected static final boolean EXP_TRANS_FEEDBACK_SHOWN = true;

  protected static final int EXP_TRANS_FEEDBACK_SIZE = 222;

  protected static final boolean EXP_TRANS_USING_THREAD_PRIORITY_MGMT = true;

  protected static final String EXP_TRANS_SHARED_OBJECTS_FILE = "transSharedObjectsFile";

  protected static final boolean EXP_TRANS_CAPTURE_STEP_PERF_SNAPSHOTS = true;

  protected static final long EXP_TRANS_STEP_PERF_CAP_DELAY = 81;

  protected static final String EXP_TRANS_DEP_TABLE_NAME = "KLKJSDF";

  protected static final String EXP_TRANS_DEP_FIELD_NAME = "lkjsdfflll11";

  protected static final String EXP_PART_SCHEMA_NAME = "partitionSchemaName";

  protected static final String EXP_PART_SCHEMA_PARTID_2 = "partitionSchemaId2";

  protected static final boolean EXP_PART_SCHEMA_DYN_DEF = true;

  protected static final String EXP_PART_SCHEMA_PART_PER_SLAVE_COUNT = "562";

  protected static final String EXP_PART_SCHEMA_PARTID_1 = "partitionSchemaId1";

  protected static final String EXP_PART_SCHEMA_DESC = "partitionSchemaDesc";

  protected static final String EXP_PART_SCHEMA_PART_PER_SLAVE_COUNT_V2 = "563";

  protected static final String EXP_CLUSTER_SCHEMA_NAME = "clusterSchemaName";

  protected static final String EXP_CLUSTER_SCHEMA_SOCKETS_BUFFER_SIZE = "2048";

  protected static final String EXP_CLUSTER_SCHEMA_BASE_PORT = "12456";

  protected static final String EXP_CLUSTER_SCHEMA_SOCKETS_FLUSH_INTERVAL = "1500";

  protected static final boolean EXP_CLUSTER_SCHEMA_SOCKETS_COMPRESSED = true;

  protected static final boolean EXP_CLUSTER_SCHEMA_DYN = true;

  protected static final String EXP_CLUSTER_SCHEMA_BASE_PORT_V2 = "12457";

  protected static final String EXP_TRANS_STEP_1_NAME = "transStep1";

  protected static final String EXP_TRANS_STEP_2_NAME = "transStep2";

  protected static final boolean EXP_TRANS_STEP_ERROR_META_1_ENABLED = true;

  protected static final String EXP_TRANS_STEP_ERROR_META_1_NR_ERRORS_VALUE_NAME = "ihwefmcd";

  protected static final String EXP_TRANS_STEP_ERROR_META_1_DESC_VALUE_NAME = "lxeslsdff";

  protected static final String EXP_TRANS_STEP_ERROR_META_1_FIELDS_VALUE_NAME = "uiwcm";

  protected static final String EXP_TRANS_STEP_ERROR_META_1_CODES_VALUE_NAME = "wedsse";

  protected static final String EXP_TRANS_STEP_ERROR_META_1_MAX_ERRORS = "2000";

  protected static final String EXP_TRANS_STEP_ERROR_META_1_MAX_PERCENT_ERRORS = "29";

  protected static final String EXP_TRANS_STEP_ERROR_META_1_MIN_PERCENT_ROWS = "12";

  protected static final boolean EXP_TRANS_SLAVE_TRANSFORMATION = true;

  protected static final String EXP_TRANS_DESC_V2 = "transMetaDesc2";

  protected static final String EXP_TRANS_LOCK_MSG = "98u344jerfnsdmklfe";

  protected static final String EXP_JOB_LOCK_MSG = "ihesfdnmsdm348iesdm";

  protected static final String DIR_TMP2_NEW_NAME = "tmp2_new";

  protected static final String DIR_TMP2 = "tmp2";

  protected static final String EXP_JOB_NAME_NEW = "job98u34u5";

  protected static final String EXP_TRANS_NAME_NEW = "trans98jksdf32";

  protected static final String EXP_DBMETA_NAME_NEW = "database983kdaerer";

  private static final String EXP_DBMETA_ATTR1_VALUE = "LKJSDFKDSJKF";

  private static final String EXP_DBMETA_ATTR1_KEY = "IOWUEIOUEWR";

  private static final String EXP_DBMETA_ATTR2_KEY = "XDKDSDF";

  private static final String EXP_DBMETA_ATTR2_VALUE = "POYIUPOUI";

  private static final String EXP_DBMETA2_NAME = "abc_db2";

  private static final String EXP_DBMETA_NAME_STEP = "khdfsghk438";

  private static final String EXP_DBMETA_NAME_JOB = "KLJSDFJKL2";

  // ~ Instance fields =================================================================================================

  protected RepositoryMeta repositoryMeta;

  protected Repository repository;

  protected UserInfo userInfo;
  
  // necessary to delete in the correct order to avoid referential integrity problems
  protected Stack<RepositoryElementInterface> deleteStack;

  // ~ Constructors ====================================================================================================

  // ~ Methods =========================================================================================================

  public void setUp() throws Exception {
    deleteStack = new Stack<RepositoryElementInterface>();
  }
  
  public void tearDown() throws Exception {
    while (!deleteStack.empty()) {
      delete(deleteStack.pop().getObjectId());
    }
  }
  
  protected abstract void delete(ObjectId id);
  
  /**
   * getUserInfo()
   * getVersion()
   * getName()
   * isConnected()
   * getRepositoryMeta()
   * getLog()
   */
  @Test
  public void testVarious() throws Exception {
    // unfortunately UserInfo doesn't override equals()
    
    // for now, disable user checks, as a connection isn't made so no 
    // user info is available
    
    // UserInfo userInfo = repository.getUserInfo();
    // assertEquals(EXP_LOGIN, userInfo.getName());
    // assertEquals("password", userInfo.getPassword());
    // assertEquals(EXP_USERNAME, userInfo.getUsername());
    // assertEquals("Apache Tomcat user", userInfo.getDescription());
    // assertTrue(userInfo.isEnabled());
    
    assertEquals(VERSION_LABEL_V1, repository.getVersion());
    assertEquals("JackRabbit", repository.getName());
    assertTrue(repository.isConnected());
    RepositoryMeta repoMeta = repository.getRepositoryMeta();
    assertEquals("JackRabbit", repoMeta.getName());
    assertEquals("JackRabbit test repository", repoMeta.getDescription());
    RepositoryCapabilities caps = repoMeta.getRepositoryCapabilities();
    assertTrue(caps.supportsUsers());
    assertTrue(caps.managesUsers());
    assertFalse(caps.isReadOnly());
    assertTrue(caps.supportsRevisions());
    assertTrue(caps.supportsMetadata());
    assertTrue(caps.supportsLocking());
    assertTrue(caps.hasVersionRegistry());
    assertNotNull(repository.getLog());
  }

  protected RepositoryDirectoryInterface initRepo() throws Exception {
    RepositoryDirectoryInterface startDir = loadStartDirectory();
    repository.createRepositoryDirectory(startDir, DIR_CONNECTIONS);
    repository.createRepositoryDirectory(startDir, DIR_SCHEMAS);
    repository.createRepositoryDirectory(startDir, DIR_SLAVES);
    repository.createRepositoryDirectory(startDir, DIR_CLUSTERS);
    repository.createRepositoryDirectory(startDir, DIR_TRANSFORMATIONS);
    repository.createRepositoryDirectory(startDir, DIR_JOBS);
    return loadStartDirectory();
  }

  /**
   * createRepositoryDirectory()
   * loadRepositoryTree()
   * deleteRepositoryDirectory()
   * getDirectoryNames()
   * saveRepositoryDirectory()
   */
  @Ignore
  @Test
  public void testDirectories() throws Exception {
    RepositoryDirectoryInterface startDir = loadStartDirectory();
    RepositoryDirectoryInterface connDir = repository.createRepositoryDirectory(startDir, DIR_CONNECTIONS);
    assertNotNull(connDir);
    assertNotNull(connDir.getObjectId());
    assertEquals(startDir.getPath() + (startDir.getPath().endsWith("/") ? "" : RepositoryDirectory.DIRECTORY_SEPARATOR)
        + DIR_CONNECTIONS, connDir.getPath());
    repository.createRepositoryDirectory(startDir, DIR_SCHEMAS);
    repository.createRepositoryDirectory(startDir, DIR_SLAVES);
    repository.saveRepositoryDirectory(new RepositoryDirectory(startDir, DIR_CLUSTERS));
    repository.createRepositoryDirectory(startDir, DIR_TRANSFORMATIONS);
    repository.createRepositoryDirectory(startDir, DIR_JOBS);
    startDir = loadStartDirectory();
    assertNotNull(startDir.findDirectory(DIR_CONNECTIONS));
    assertNotNull(startDir.findDirectory(DIR_SCHEMAS));
    assertNotNull(startDir.findDirectory(DIR_SLAVES));
    assertNotNull(startDir.findDirectory(DIR_CLUSTERS));
    assertNotNull(startDir.findDirectory(DIR_TRANSFORMATIONS));
    assertNotNull(startDir.findDirectory(DIR_JOBS));

    RepositoryDirectoryInterface tmpDir = repository.createRepositoryDirectory(startDir, DIR_TMP);
    repository.deleteRepositoryDirectory(tmpDir);
    startDir = loadStartDirectory();
    assertNull(startDir.findDirectory(DIR_TMP));

    RepositoryDirectoryInterface moveTestDestDir = repository.createRepositoryDirectory(startDir, "moveTestDest");
    assertNotNull(moveTestDestDir.getObjectId());
    RepositoryDirectoryInterface moveTestSrcDir = repository.createRepositoryDirectory(startDir, "moveTestSrc");
    assertNotNull(moveTestSrcDir.getObjectId());
    // Rename and move the folder
    repository.renameRepositoryDirectory(moveTestSrcDir.getObjectId(), moveTestDestDir, "moveTestSrcNewName");
    startDir = loadStartDirectory();
    assertNull(startDir.findDirectory("moveTestSrc"));
    assertNotNull(startDir.findDirectory("moveTestDest/moveTestSrcNewName"));

    String[] dirs = repository.getDirectoryNames(startDir.getObjectId());
    assertEquals(7, dirs.length);
    boolean foundDir = false;
    for (String dir : dirs) {
      if (dir.equals(DIR_CONNECTIONS)) { // spot check
        foundDir = true;
        break;
      }
    }
    assertTrue(foundDir);

  }

  protected RepositoryDirectoryInterface loadStartDirectory() throws Exception {
    return repository.loadRepositoryDirectoryTree();
    
  }

  /**
   * save(job)
   * loadJob()
   * exists()
   * deleteJob()
   * getJobNames()
   * getJobObjects()
   * getJobId()
   * getJobLock()
   * lockJob()
   * unlockJob()
   */
  @Ignore
  @Test
  public void testJobs() throws Exception {
    ILockService service = (ILockService) repository.getService(ILockService.class);
    RepositoryDirectoryInterface rootDir = initRepo();
    JobMeta jobMeta = createJobMeta(EXP_JOB_NAME);
    RepositoryDirectoryInterface jobsDir = rootDir.findDirectory(DIR_JOBS);
    repository.save(jobMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(jobMeta);
    assertNotNull(jobMeta.getObjectId());
    ObjectRevision version = jobMeta.getObjectRevision();
    assertNotNull(version);
    assertTrue(hasVersionWithComment(jobMeta, VERSION_COMMENT_V1));
    assertTrue(repository.exists(EXP_JOB_NAME, jobsDir, RepositoryObjectType.JOB));

    JobMeta fetchedJob = repository.loadJob(EXP_JOB_NAME, jobsDir, null, null);
    assertEquals(EXP_JOB_NAME, fetchedJob.getName());
    assertEquals(EXP_JOB_DESC, fetchedJob.getDescription());
    assertEquals(EXP_JOB_EXTENDED_DESC, fetchedJob.getExtendedDescription());
    assertEquals(jobsDir.getPath(), fetchedJob.getRepositoryDirectory().getPath());
    assertEquals(EXP_JOB_VERSION, fetchedJob.getJobversion());
    assertEquals(EXP_JOB_STATUS, fetchedJob.getJobstatus());
    assertEquals(EXP_JOB_CREATED_USER, fetchedJob.getCreatedUser());
    assertEquals(EXP_JOB_CREATED_DATE, fetchedJob.getCreatedDate());
    assertEquals(EXP_JOB_MOD_USER, fetchedJob.getModifiedUser());
    assertEquals(EXP_JOB_MOD_DATE, fetchedJob.getModifiedDate());
    assertEquals(1, fetchedJob.listParameters().length);
    assertEquals(EXP_JOB_PARAM_1_DEF, fetchedJob.getParameterDefault(EXP_JOB_PARAM_1_NAME));
    assertEquals(EXP_JOB_PARAM_1_DESC, fetchedJob.getParameterDescription(EXP_JOB_PARAM_1_NAME));
    // JobLogTable jobLogTable = fetchedJob.getJobLogTable();
    // TODO mlowery why doesn't this work?
    //    assertEquals(EXP_LOG_TABLE_CONN_NAME, jobLogTable.getConnectionName());
    //    assertEquals(EXP_JOB_LOG_TABLE_INTERVAL, jobLogTable.getLogInterval());
    //    assertEquals(EXP_LOG_TABLE_SCHEMA_NAME, jobLogTable.getSchemaName());
    //    assertEquals(EXP_LOG_TABLE_SIZE_LIMIT, jobLogTable.getLogSizeLimit());
    //    assertEquals(EXP_LOG_TABLE_TABLE_NAME, jobLogTable.getTableName());
    //    assertEquals(EXP_LOG_TABLE_TIMEOUT_IN_DAYS, jobLogTable.getTimeoutInDays());
    // JobEntryLogTable jobEntryLogTable = fetchedJob.getJobEntryLogTable();
    // TODO mlowery why doesn't this work?
    //    assertEquals(EXP_LOG_TABLE_CONN_NAME, jobEntryLogTable.getConnectionName());
    //    assertEquals(EXP_LOG_TABLE_SCHEMA_NAME, jobEntryLogTable.getSchemaName());
    //    assertEquals(EXP_LOG_TABLE_TABLE_NAME, jobEntryLogTable.getTableName());
    //    assertEquals(EXP_LOG_TABLE_TIMEOUT_IN_DAYS, jobEntryLogTable.getTimeoutInDays());
    // ChannelLogTable channelLogTable = fetchedJob.getChannelLogTable();
    // TODO mlowery why doesn't this work?
    //    assertEquals(EXP_LOG_TABLE_CONN_NAME, channelLogTable.getConnectionName());
    //    assertEquals(EXP_LOG_TABLE_SCHEMA_NAME, channelLogTable.getSchemaName());
    //    assertEquals(EXP_LOG_TABLE_TABLE_NAME, channelLogTable.getTableName());
    //    assertEquals(EXP_LOG_TABLE_TIMEOUT_IN_DAYS, channelLogTable.getTimeoutInDays());
    assertEquals(EXP_JOB_BATCH_ID_PASSED, fetchedJob.isBatchIdPassed());
    assertEquals(EXP_JOB_SHARED_OBJECTS_FILE, fetchedJob.getSharedObjectsFile());
    assertEquals(2, fetchedJob.getJobCopies().size());
    assertEquals("JobEntryAttributeTester", fetchedJob.getJobEntry(0).getEntry().getPluginId());
    assertEquals(EXP_JOB_ENTRY_1_COPY_X_LOC, fetchedJob.getJobEntry(0).getLocation().x);
    assertEquals(EXP_JOB_ENTRY_1_COPY_Y_LOC, fetchedJob.getJobEntry(0).getLocation().y);
    assertEquals("JobEntryAttributeTester", fetchedJob.getJobEntry(1).getEntry().getPluginId());
    assertEquals(EXP_JOB_ENTRY_2_COPY_X_LOC, fetchedJob.getJobEntry(1).getLocation().x);
    assertEquals(EXP_JOB_ENTRY_2_COPY_Y_LOC, fetchedJob.getJobEntry(1).getLocation().y);
    assertEquals(1, fetchedJob.getJobhops().size());
    assertEquals(EXP_JOB_ENTRY_1_NAME, fetchedJob.getJobHop(0).getFromEntry().getEntry().getName());
    assertEquals("JobEntryAttributeTester", fetchedJob.getJobHop(0).getFromEntry().getEntry().getPluginId());
    assertEquals(EXP_JOB_ENTRY_2_NAME, fetchedJob.getJobHop(0).getToEntry().getEntry().getName());
    assertEquals("JobEntryAttributeTester", fetchedJob.getJobHop(0).getToEntry().getEntry().getPluginId());
    assertEquals(1, fetchedJob.getNotes().size());
    assertTrue(fetchedJob.getNote(0).getNote().startsWith(EXP_NOTEPAD_NOTE));
    assertEquals(EXP_NOTEPAD_X, fetchedJob.getNote(0).getLocation().x);
    assertEquals(EXP_NOTEPAD_Y, fetchedJob.getNote(0).getLocation().y);
    assertEquals(EXP_NOTEPAD_WIDTH, fetchedJob.getNote(0).getWidth());
    assertEquals(EXP_NOTEPAD_HEIGHT, fetchedJob.getNote(0).getHeight());

    JobMeta jobMetaById = repository.loadJob(jobMeta.getObjectId(), null);
    assertEquals(fetchedJob, jobMetaById);
    
    assertNull(service.getJobLock(jobMeta.getObjectId()));
    service.lockJob(jobMeta.getObjectId(), EXP_JOB_LOCK_MSG);
    assertEquals(EXP_JOB_LOCK_MSG, service.getJobLock(jobMeta.getObjectId()).getMessage());
    assertEquals(getDate(new Date()), getDate(service.getJobLock(jobMeta.getObjectId()).getLockDate()));
    assertEquals(EXP_LOGIN_PLUS_TENANT, service.getJobLock(jobMeta.getObjectId()).getLogin());
    // TODO mlowery currently PUR lock only stores "login"; why do we need username too? 
    //    assertEquals(EXP_USERNAME, repository.getJobLock(jobMeta.getObjectId()).getUsername());
    assertEquals(jobMeta.getObjectId(), service.getJobLock(jobMeta.getObjectId()).getObjectId());
    service.unlockJob(jobMeta.getObjectId());
    assertNull(service.getJobLock(jobMeta.getObjectId()));

    jobMeta.setDescription(EXP_JOB_DESC_V2);
    repository.save(jobMeta, VERSION_COMMENT_V2, null);
    assertEquals(VERSION_COMMENT_V2, jobMeta.getObjectRevision().getComment());
    fetchedJob = repository.loadJob(EXP_JOB_NAME, jobsDir, null, null);
    assertEquals(EXP_JOB_DESC_V2, fetchedJob.getDescription());
    fetchedJob = repository.loadJob(EXP_JOB_NAME, jobsDir, null, VERSION_LABEL_V1);
    assertEquals(EXP_JOB_DESC, fetchedJob.getDescription());

    jobMetaById = repository.loadJob(jobMeta.getObjectId(), VERSION_LABEL_V1);
    assertEquals(fetchedJob, jobMetaById);
    
    assertEquals(jobMeta.getObjectId(), repository.getJobId(EXP_JOB_NAME, jobsDir));

    assertEquals(1, repository.getJobObjects(jobsDir.getObjectId(), false).size());
    assertEquals(1, repository.getJobObjects(jobsDir.getObjectId(), true).size());
    assertEquals(jobMeta.getName(), repository.getJobObjects(jobsDir.getObjectId(), false).get(0).getName());

    assertEquals(1, repository.getJobNames(jobsDir.getObjectId(), false).length);
    assertEquals(1, repository.getJobNames(jobsDir.getObjectId(), true).length);
    assertEquals(jobMeta.getName(), repository.getJobNames(jobsDir.getObjectId(), false)[0]);

    repository.deleteJob(jobMeta.getObjectId());
    assertFalse(repository.exists(EXP_JOB_NAME, jobsDir, RepositoryObjectType.JOB));

    assertEquals(0, repository.getJobObjects(jobsDir.getObjectId(), false).size());
    assertEquals(1, repository.getJobObjects(jobsDir.getObjectId(), true).size());
    assertEquals(jobMeta.getName(), repository.getJobObjects(jobsDir.getObjectId(), true).get(0).getName());

    assertEquals(0, repository.getJobNames(jobsDir.getObjectId(), false).length);
    assertEquals(1, repository.getJobNames(jobsDir.getObjectId(), true).length);
    assertEquals(jobMeta.getName(), repository.getJobNames(jobsDir.getObjectId(), true)[0]);
  }

  @SuppressWarnings("deprecation")
  private int getDate(Date date) {
    return date.getDate();
  }

  protected JobMeta createJobMeta(String jobName) throws Exception {
    RepositoryDirectoryInterface rootDir = loadStartDirectory();
    JobMeta jobMeta = new JobMeta();
    jobMeta.setName(jobName);
    jobMeta.setDescription(EXP_JOB_DESC);
    jobMeta.setExtendedDescription(EXP_JOB_EXTENDED_DESC);
    jobMeta.setRepositoryDirectory(rootDir.findDirectory(DIR_JOBS));
    jobMeta.setJobversion(EXP_JOB_VERSION);
    jobMeta.setJobstatus(EXP_JOB_STATUS);
    jobMeta.setCreatedUser(EXP_JOB_CREATED_USER);
    jobMeta.setCreatedDate(EXP_JOB_CREATED_DATE);
    jobMeta.setModifiedUser(EXP_JOB_MOD_USER);
    jobMeta.setModifiedDate(EXP_JOB_MOD_DATE);
    jobMeta.addParameterDefinition(EXP_JOB_PARAM_1_NAME, EXP_JOB_PARAM_1_DEF, EXP_JOB_PARAM_1_DESC);
    // TODO mlowery other jobLogTable fields could be set for testing here    
    JobLogTable jobLogTable = JobLogTable.getDefault(jobMeta, jobMeta);
    jobLogTable.setConnectionName(EXP_JOB_LOG_TABLE_CONN_NAME);
    jobLogTable.setLogInterval(EXP_JOB_LOG_TABLE_INTERVAL);
    jobLogTable.setSchemaName(EXP_JOB_LOG_TABLE_SCHEMA_NAME);
    jobLogTable.setLogSizeLimit(EXP_JOB_LOG_TABLE_SIZE_LIMIT);
    jobLogTable.setTableName(EXP_JOB_LOG_TABLE_TABLE_NAME);
    jobLogTable.setTimeoutInDays(EXP_JOB_LOG_TABLE_TIMEOUT_IN_DAYS);
    jobMeta.setJobLogTable(jobLogTable);
    // TODO mlowery other jobEntryLogTable fields could be set for testing here    
    JobEntryLogTable jobEntryLogTable = JobEntryLogTable.getDefault(jobMeta, jobMeta);
    jobEntryLogTable.setConnectionName(EXP_JOB_LOG_TABLE_CONN_NAME);
    jobEntryLogTable.setSchemaName(EXP_JOB_LOG_TABLE_SCHEMA_NAME);
    jobEntryLogTable.setTableName(EXP_JOB_LOG_TABLE_TABLE_NAME);
    jobEntryLogTable.setTimeoutInDays(EXP_JOB_LOG_TABLE_TIMEOUT_IN_DAYS);
    jobMeta.setJobEntryLogTable(jobEntryLogTable);
    // TODO mlowery other channelLogTable fields could be set for testing here    
    ChannelLogTable channelLogTable = ChannelLogTable.getDefault(jobMeta, jobMeta);
    channelLogTable.setConnectionName(EXP_JOB_LOG_TABLE_CONN_NAME);
    channelLogTable.setSchemaName(EXP_JOB_LOG_TABLE_SCHEMA_NAME);
    channelLogTable.setTableName(EXP_JOB_LOG_TABLE_TABLE_NAME);
    channelLogTable.setTimeoutInDays(EXP_JOB_LOG_TABLE_TIMEOUT_IN_DAYS);
    jobMeta.setChannelLogTable(channelLogTable);
    jobMeta.setBatchIdPassed(EXP_JOB_BATCH_ID_PASSED);
    jobMeta.setSharedObjectsFile(EXP_JOB_SHARED_OBJECTS_FILE);
    DatabaseMeta entryDbMeta = createDatabaseMeta(EXP_DBMETA_NAME_JOB.concat(jobName));
    repository.save(entryDbMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(entryDbMeta);
    JobEntryCopy jobEntryCopy1 = createJobEntry1Copy(entryDbMeta);
    jobMeta.addJobEntry(jobEntryCopy1);
    JobEntryCopy jobEntryCopy2 = createJobEntry2Copy(entryDbMeta);
    jobMeta.addJobEntry(jobEntryCopy2);
    jobMeta.addJobHop(createJobHopMeta(jobEntryCopy1, jobEntryCopy2));
    jobMeta.addNote(createNotePadMeta(jobName));
    return jobMeta;
  }

  /**
   * save(trans)
   * loadTransformation()
   * exists()
   * getTransformationLock()
   * lockTransformation()
   * unlockTransformation()
   * getTransformationID()
   * getTransformationObjects()
   * getTransformationNames()
   */
  @Ignore
  @Test
  public void testTransformations() throws Exception {
    ILockService service = (ILockService) repository.getService(ILockService.class);
    RepositoryDirectoryInterface rootDir = initRepo();
    String uniqueTransName = EXP_TRANS_NAME.concat(EXP_DBMETA_NAME);
    TransMeta transMeta = createTransMeta(EXP_DBMETA_NAME);
    
    // Create a database association
    DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA_NAME);
    repository.save(dbMeta, VERSION_COMMENT_V1, null);
    
    TableInputMeta tableInputMeta = new TableInputMeta();
    tableInputMeta.setDatabaseMeta(dbMeta);
    
    transMeta.addStep(new StepMeta(EXP_TRANS_STEP_1_NAME, tableInputMeta));
    
    RepositoryDirectoryInterface transDir = rootDir.findDirectory(DIR_TRANSFORMATIONS);
    repository.save(transMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(transMeta);
    assertNotNull(transMeta.getObjectId());
    ObjectRevision version = transMeta.getObjectRevision();
    assertNotNull(version);
    assertTrue(hasVersionWithComment(transMeta, VERSION_COMMENT_V1));

    assertTrue(repository.exists(uniqueTransName, transDir, RepositoryObjectType.TRANSFORMATION));

    TransMeta fetchedTrans = repository.loadTransformation(uniqueTransName, transDir, null, false, null);
    assertEquals(uniqueTransName, fetchedTrans.getName());
    assertEquals(EXP_TRANS_DESC, fetchedTrans.getDescription());
    assertEquals(EXP_TRANS_EXTENDED_DESC, fetchedTrans.getExtendedDescription());
    assertEquals(transDir.getPath(), fetchedTrans.getRepositoryDirectory().getPath());
    assertEquals(EXP_TRANS_VERSION, fetchedTrans.getTransversion());
    assertEquals(EXP_TRANS_STATUS, fetchedTrans.getTransstatus());
    assertEquals(EXP_TRANS_CREATED_USER, fetchedTrans.getCreatedUser());
    assertEquals(EXP_TRANS_CREATED_DATE, fetchedTrans.getCreatedDate());
    assertEquals(EXP_TRANS_MOD_USER, fetchedTrans.getModifiedUser());
    assertEquals(EXP_TRANS_MOD_DATE, fetchedTrans.getModifiedDate());
    assertEquals(1, fetchedTrans.listParameters().length);
    assertEquals(EXP_TRANS_PARAM_1_DEF, fetchedTrans.getParameterDefault(EXP_TRANS_PARAM_1_NAME));
    assertEquals(EXP_TRANS_PARAM_1_DESC, fetchedTrans.getParameterDescription(EXP_TRANS_PARAM_1_NAME));
    
    // Test reference to database connection 
    String[] transformations = repository.getTransformationsUsingDatabase(dbMeta.getObjectId());
    assertNotNull(transformations);
    assertEquals(1, transformations.length);
    assertTrue(transformations[0].contains(fetchedTrans.getName()));
    
    // TransLogTable transLogTable = fetchedTrans.getTransLogTable();
    // TODO mlowery why doesn't this work?
    //    assertEquals(EXP_TRANS_LOG_TABLE_CONN_NAME, transLogTable.getConnectionName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_INTERVAL, transLogTable.getLogInterval());
    //    assertEquals(EXP_TRANS_LOG_TABLE_SCHEMA_NAME, transLogTable.getSchemaName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_SIZE_LIMIT, transLogTable.getLogSizeLimit());
    //    assertEquals(EXP_TRANS_LOG_TABLE_TABLE_NAME, transLogTable.getTableName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_TIMEOUT_IN_DAYS, transLogTable.getTimeoutInDays());
    // PerformanceLogTable perfLogTable = fetchedTrans.getPerformanceLogTable();
    // TODO mlowery why doesn't this work?
    //    assertEquals(EXP_TRANS_LOG_TABLE_CONN_NAME, perfLogTable.getConnectionName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_INTERVAL, perfLogTable.getLogInterval());
    //    assertEquals(EXP_TRANS_LOG_TABLE_SCHEMA_NAME, perfLogTable.getSchemaName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_TABLE_NAME, perfLogTable.getTableName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_TIMEOUT_IN_DAYS, perfLogTable.getTimeoutInDays());
    // ChannelLogTable channelLogTable = fetchedTrans.getChannelLogTable();
    // TODO mlowery why doesn't this work?
    //    assertEquals(EXP_TRANS_LOG_TABLE_CONN_NAME, channelLogTable.getConnectionName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_SCHEMA_NAME, channelLogTable.getSchemaName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_TABLE_NAME, channelLogTable.getTableName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_TIMEOUT_IN_DAYS, channelLogTable.getTimeoutInDays());
    // StepLogTable stepLogTable = fetchedTrans.getStepLogTable();
    // TODO mlowery why doesn't this work?
    //    assertEquals(EXP_TRANS_LOG_TABLE_CONN_NAME, stepLogTable.getConnectionName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_SCHEMA_NAME, stepLogTable.getSchemaName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_TABLE_NAME, stepLogTable.getTableName());
    //    assertEquals(EXP_TRANS_LOG_TABLE_TIMEOUT_IN_DAYS, stepLogTable.getTimeoutInDays());
    assertEquals(EXP_DBMETA_NAME, fetchedTrans.getMaxDateConnection().getName());
    assertEquals(EXP_TRANS_MAX_DATE_TABLE, fetchedTrans.getMaxDateTable());
    assertEquals(EXP_TRANS_MAX_DATE_FIELD, fetchedTrans.getMaxDateField());
    assertEquals(EXP_TRANS_MAX_DATE_OFFSET, fetchedTrans.getMaxDateOffset(), 0);
    assertEquals(EXP_TRANS_MAX_DATE_DIFF, fetchedTrans.getMaxDateDifference(), 0);

    assertEquals(EXP_TRANS_SIZE_ROWSET, fetchedTrans.getSizeRowset());
    // TODO mlowery why don't next two sleep fields work?
    //    assertEquals(EXP_TRANS_SLEEP_TIME_EMPTY, fetchedTrans.getSleepTimeEmpty());
    //    assertEquals(EXP_TRANS_SLEEP_TIME_FULL, fetchedTrans.getSleepTimeFull());
    assertEquals(EXP_TRANS_USING_UNIQUE_CONN, fetchedTrans.isUsingUniqueConnections());
    assertEquals(EXP_TRANS_FEEDBACK_SHOWN, fetchedTrans.isFeedbackShown());
    assertEquals(EXP_TRANS_FEEDBACK_SIZE, fetchedTrans.getFeedbackSize());
    assertEquals(EXP_TRANS_USING_THREAD_PRIORITY_MGMT, fetchedTrans.isUsingThreadPriorityManagment());
    assertEquals(EXP_TRANS_SHARED_OBJECTS_FILE, fetchedTrans.getSharedObjectsFile());
    assertEquals(EXP_TRANS_CAPTURE_STEP_PERF_SNAPSHOTS, fetchedTrans.isCapturingStepPerformanceSnapShots());
    assertEquals(EXP_TRANS_STEP_PERF_CAP_DELAY, fetchedTrans.getStepPerformanceCapturingDelay());
    // TODO mlowery why doesn't this work?
    //    assertEquals(1, fetchedTrans.getDependencies().size());
    //    assertEquals(EXP_DBMETA_NAME, fetchedTrans.getDependency(0).getDatabase().getName());
    //    assertEquals(EXP_TRANS_DEP_TABLE_NAME, fetchedTrans.getDependency(0).getTablename());
    //    assertEquals(EXP_TRANS_DEP_FIELD_NAME, fetchedTrans.getDependency(0).getFieldname());

    assertEquals(3, fetchedTrans.getSteps().size());
    assertEquals(EXP_TRANS_STEP_1_NAME, fetchedTrans.getStep(0).getName());
    assertEquals(EXP_TRANS_STEP_ERROR_META_1_ENABLED, fetchedTrans.getStep(0).getStepErrorMeta().isEnabled());
    assertEquals(EXP_TRANS_STEP_ERROR_META_1_NR_ERRORS_VALUE_NAME, fetchedTrans.getStep(0).getStepErrorMeta()
        .getNrErrorsValuename());
    assertEquals(EXP_TRANS_STEP_ERROR_META_1_DESC_VALUE_NAME, fetchedTrans.getStep(0).getStepErrorMeta()
        .getErrorDescriptionsValuename());
    assertEquals(EXP_TRANS_STEP_ERROR_META_1_FIELDS_VALUE_NAME, fetchedTrans.getStep(0).getStepErrorMeta()
        .getErrorFieldsValuename());
    assertEquals(EXP_TRANS_STEP_ERROR_META_1_CODES_VALUE_NAME, fetchedTrans.getStep(0).getStepErrorMeta()
        .getErrorCodesValuename());
    assertEquals(EXP_TRANS_STEP_ERROR_META_1_MAX_ERRORS, fetchedTrans.getStep(0).getStepErrorMeta().getMaxErrors());
    assertEquals(EXP_TRANS_STEP_ERROR_META_1_MAX_PERCENT_ERRORS, fetchedTrans.getStep(0).getStepErrorMeta()
        .getMaxPercentErrors());
    assertEquals(EXP_TRANS_STEP_ERROR_META_1_MIN_PERCENT_ROWS, fetchedTrans.getStep(0).getStepErrorMeta()
        .getMinPercentRows());

    assertEquals(EXP_TRANS_STEP_2_NAME, fetchedTrans.getStep(1).getName());

    assertEquals(EXP_TRANS_STEP_1_NAME, fetchedTrans.getTransHop(0).getFromStep().getName());
    assertEquals(EXP_TRANS_STEP_2_NAME, fetchedTrans.getTransHop(0).getToStep().getName());

    assertEquals(1, transMeta.getSlaveStepCopyPartitionDistribution().getOriginalPartitionSchemas().size());
    assertTrue(transMeta.getSlaveStepCopyPartitionDistribution().getOriginalPartitionSchemas()
        .get(0).getName().startsWith(EXP_PART_SCHEMA_NAME));

    assertTrue(-1 != transMeta.getSlaveStepCopyPartitionDistribution().getPartition(EXP_SLAVE_NAME,
        EXP_PART_SCHEMA_NAME, 0));
    assertEquals(EXP_TRANS_SLAVE_TRANSFORMATION, transMeta.isSlaveTransformation());

    TransMeta transMetaById = repository.loadTransformation(transMeta.getObjectId(), null);
    assertEquals(fetchedTrans, transMetaById);
    
    assertNull(service.getTransformationLock(transMeta.getObjectId()));
    service.lockTransformation(transMeta.getObjectId(), EXP_TRANS_LOCK_MSG);
    assertEquals(EXP_TRANS_LOCK_MSG, service.getTransformationLock(transMeta.getObjectId()).getMessage());
    assertEquals(getDate(new Date()), getDate(service.getTransformationLock(transMeta.getObjectId()).getLockDate()));
    assertEquals(EXP_LOGIN_PLUS_TENANT, service.getTransformationLock(transMeta.getObjectId()).getLogin());
    // TODO mlowery currently PUR lock only stores "login"; why do we need username too? 
    //    assertEquals(EXP_USERNAME, repository.getTransformationLock(transMeta.getObjectId()).getUsername());
    assertEquals(transMeta.getObjectId(), service.getTransformationLock(transMeta.getObjectId()).getObjectId());
    service.unlockTransformation(transMeta.getObjectId());
    assertNull(service.getTransformationLock(transMeta.getObjectId()));

    transMeta.setDescription(EXP_TRANS_DESC_V2);
    repository.save(transMeta, VERSION_COMMENT_V2, null);
    assertTrue(hasVersionWithComment(transMeta, VERSION_COMMENT_V2));
    fetchedTrans = repository.loadTransformation(uniqueTransName, transDir, null, false, null);
    assertEquals(EXP_TRANS_DESC_V2, fetchedTrans.getDescription());
    fetchedTrans = repository.loadTransformation(uniqueTransName, transDir, null, false, VERSION_LABEL_V1);
    assertEquals(EXP_TRANS_DESC, fetchedTrans.getDescription());
    
    transMetaById = repository.loadTransformation(transMeta.getObjectId(), VERSION_LABEL_V1);
    assertEquals(fetchedTrans, transMetaById);

    assertEquals(transMeta.getObjectId(), repository.getTransformationID(uniqueTransName, transDir));

    assertEquals(1, repository.getTransformationObjects(transDir.getObjectId(), false).size());
    assertEquals(1, repository.getTransformationObjects(transDir.getObjectId(), true).size());
    assertEquals(transMeta.getName(), repository.getTransformationObjects(transDir.getObjectId(), false).get(0)
        .getName());

    assertEquals(1, repository.getTransformationNames(transDir.getObjectId(), false).length);
    assertEquals(1, repository.getTransformationNames(transDir.getObjectId(), true).length);
    assertEquals(transMeta.getName(), repository.getTransformationNames(transDir.getObjectId(), false)[0]);

    repository.deleteTransformation(transMeta.getObjectId());
    assertFalse(repository.exists(uniqueTransName, transDir, RepositoryObjectType.TRANSFORMATION));

    assertEquals(0, repository.getTransformationObjects(transDir.getObjectId(), false).size());
    assertEquals(1, repository.getTransformationObjects(transDir.getObjectId(), true).size());
    assertEquals(transMeta.getName(), repository.getTransformationObjects(transDir.getObjectId(), true).get(0)
        .getName());

    assertEquals(0, repository.getTransformationNames(transDir.getObjectId(), false).length);
    assertEquals(1, repository.getTransformationNames(transDir.getObjectId(), true).length);
    assertEquals(transMeta.getName(), repository.getTransformationNames(transDir.getObjectId(), true)[0]);
  }

  protected TransMeta createTransMeta(final String dbName) throws Exception {
    RepositoryDirectoryInterface rootDir = loadStartDirectory();
    TransMeta transMeta = new TransMeta();
    transMeta.setName(EXP_TRANS_NAME.concat(dbName));
    transMeta.setDescription(EXP_TRANS_DESC);
    transMeta.setExtendedDescription(EXP_TRANS_EXTENDED_DESC);
    transMeta.setRepositoryDirectory(rootDir.findDirectory(DIR_TRANSFORMATIONS));
    transMeta.setTransversion(EXP_TRANS_VERSION);
    transMeta.setTransstatus(EXP_TRANS_STATUS);
    transMeta.setCreatedUser(EXP_TRANS_CREATED_USER);
    transMeta.setCreatedDate(EXP_TRANS_CREATED_DATE);
    transMeta.setModifiedUser(EXP_TRANS_MOD_USER);
    transMeta.setModifiedDate(EXP_TRANS_MOD_DATE);
    transMeta.addParameterDefinition(EXP_TRANS_PARAM_1_NAME, EXP_TRANS_PARAM_1_DEF, EXP_TRANS_PARAM_1_DESC);

    // TODO mlowery other transLogTable fields could be set for testing here  
    TransLogTable transLogTable = TransLogTable.getDefault(transMeta, transMeta, new ArrayList<StepMeta>(0));
    transLogTable.setConnectionName(EXP_TRANS_LOG_TABLE_CONN_NAME);
    transLogTable.setLogInterval(EXP_TRANS_LOG_TABLE_INTERVAL);
    transLogTable.setSchemaName(EXP_TRANS_LOG_TABLE_SCHEMA_NAME);
    transLogTable.setLogSizeLimit(EXP_TRANS_LOG_TABLE_SIZE_LIMIT);
    transLogTable.setTableName(EXP_TRANS_LOG_TABLE_TABLE_NAME);
    transLogTable.setTimeoutInDays(EXP_TRANS_LOG_TABLE_TIMEOUT_IN_DAYS);
    transMeta.setTransLogTable(transLogTable);
    // TODO mlowery other perfLogTable fields could be set for testing here  
    PerformanceLogTable perfLogTable = PerformanceLogTable.getDefault(transMeta, transMeta);
    perfLogTable.setConnectionName(EXP_TRANS_LOG_TABLE_CONN_NAME);
    perfLogTable.setLogInterval(EXP_TRANS_LOG_TABLE_INTERVAL);
    perfLogTable.setSchemaName(EXP_TRANS_LOG_TABLE_SCHEMA_NAME);
    perfLogTable.setTableName(EXP_TRANS_LOG_TABLE_TABLE_NAME);
    perfLogTable.setTimeoutInDays(EXP_TRANS_LOG_TABLE_TIMEOUT_IN_DAYS);
    transMeta.setPerformanceLogTable(perfLogTable);
    // TODO mlowery other channelLogTable fields could be set for testing here    
    ChannelLogTable channelLogTable = ChannelLogTable.getDefault(transMeta, transMeta);
    channelLogTable.setConnectionName(EXP_TRANS_LOG_TABLE_CONN_NAME);
    channelLogTable.setSchemaName(EXP_TRANS_LOG_TABLE_SCHEMA_NAME);
    channelLogTable.setTableName(EXP_TRANS_LOG_TABLE_TABLE_NAME);
    channelLogTable.setTimeoutInDays(EXP_TRANS_LOG_TABLE_TIMEOUT_IN_DAYS);
    transMeta.setChannelLogTable(channelLogTable);
    // TODO mlowery other stepLogTable fields could be set for testing here
    StepLogTable stepLogTable = StepLogTable.getDefault(transMeta, transMeta);
    stepLogTable.setConnectionName(EXP_TRANS_LOG_TABLE_CONN_NAME);
    stepLogTable.setSchemaName(EXP_TRANS_LOG_TABLE_SCHEMA_NAME);
    stepLogTable.setTableName(EXP_TRANS_LOG_TABLE_TABLE_NAME);
    stepLogTable.setTimeoutInDays(EXP_TRANS_LOG_TABLE_TIMEOUT_IN_DAYS);
    transMeta.setStepLogTable(stepLogTable);
    DatabaseMeta dbMeta = createDatabaseMeta(dbName);
    // dbMeta must be saved so that it gets an ID
    repository.save(dbMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(dbMeta);
    transMeta.setMaxDateConnection(dbMeta);
    transMeta.setMaxDateTable(EXP_TRANS_MAX_DATE_TABLE);
    transMeta.setMaxDateField(EXP_TRANS_MAX_DATE_FIELD);
    transMeta.setMaxDateOffset(EXP_TRANS_MAX_DATE_OFFSET);
    transMeta.setMaxDateDifference(EXP_TRANS_MAX_DATE_DIFF);
    transMeta.setSizeRowset(EXP_TRANS_SIZE_ROWSET);
    transMeta.setSleepTimeEmpty(EXP_TRANS_SLEEP_TIME_EMPTY);
    transMeta.setSleepTimeFull(EXP_TRANS_SLEEP_TIME_FULL);
    transMeta.setUsingUniqueConnections(EXP_TRANS_USING_UNIQUE_CONN);
    transMeta.setFeedbackShown(EXP_TRANS_FEEDBACK_SHOWN);
    transMeta.setFeedbackSize(EXP_TRANS_FEEDBACK_SIZE);
    transMeta.setUsingThreadPriorityManagment(EXP_TRANS_USING_THREAD_PRIORITY_MGMT);
    transMeta.setSharedObjectsFile(EXP_TRANS_SHARED_OBJECTS_FILE);
    transMeta.setCapturingStepPerformanceSnapShots(EXP_TRANS_CAPTURE_STEP_PERF_SNAPSHOTS);
    transMeta.setStepPerformanceCapturingDelay(EXP_TRANS_STEP_PERF_CAP_DELAY);
    transMeta.addDependency(new TransDependency(dbMeta, EXP_TRANS_DEP_TABLE_NAME, EXP_TRANS_DEP_FIELD_NAME));
    DatabaseMeta stepDbMeta = createDatabaseMeta(EXP_DBMETA_NAME_STEP.concat(dbName));
    repository.save(stepDbMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(stepDbMeta);
    Condition cond = new Condition();
    StepMeta step1 = createStepMeta1(transMeta, stepDbMeta, cond);
    transMeta.addStep(step1);
    StepMeta step2 = createStepMeta2(stepDbMeta, cond);
    transMeta.addStep(step2);
    transMeta.addTransHop(createTransHopMeta(step1, step2));

    SlaveServer slaveServer = createSlaveServer(dbName);
    PartitionSchema partSchema = createPartitionSchema(dbName);
    // slaveServer, partSchema must be saved so that they get IDs
    repository.save(slaveServer, VERSION_COMMENT_V1, null);
    deleteStack.push(slaveServer);
    repository.save(partSchema, VERSION_COMMENT_V1, null);
    deleteStack.push(partSchema);

    SlaveStepCopyPartitionDistribution slaveStepCopyPartitionDistribution = new SlaveStepCopyPartitionDistribution();
    slaveStepCopyPartitionDistribution.addPartition(EXP_SLAVE_NAME, EXP_PART_SCHEMA_NAME, 0);
    slaveStepCopyPartitionDistribution.setOriginalPartitionSchemas(Arrays.asList(new PartitionSchema[] { partSchema }));
    transMeta.setSlaveStepCopyPartitionDistribution(slaveStepCopyPartitionDistribution);
    transMeta.setSlaveTransformation(EXP_TRANS_SLAVE_TRANSFORMATION);
    return transMeta;
  }

  protected PartitionSchema createPartitionSchema(String partName) throws Exception {
    PartitionSchema partSchema = new PartitionSchema();
    partSchema.setName(EXP_PART_SCHEMA_NAME.concat(partName));
    partSchema.setDescription(EXP_PART_SCHEMA_DESC);
    partSchema.setPartitionIDs(Arrays.asList(new String[] { EXP_PART_SCHEMA_PARTID_1, EXP_PART_SCHEMA_PARTID_2 }));
    partSchema.setDynamicallyDefined(EXP_PART_SCHEMA_DYN_DEF);
    partSchema.setNumberOfPartitionsPerSlave(EXP_PART_SCHEMA_PART_PER_SLAVE_COUNT);
    return partSchema;
  }

  /**
   * save(partitionSchema)
   * exists()
   * loadPartitionSchema()
   * deletePartitionSchema()
   * getPartitionSchemaID()
   * getPartitionSchemaIDs()
   * getPartitionSchemaNames()
   */
  @Test
  public void testPartitionSchemas() throws Exception {
    // RepositoryDirectoryInterface rootDir = 
    initRepo();
    PartitionSchema partSchema = createPartitionSchema("");
    repository.save(partSchema, VERSION_COMMENT_V1, null);
    assertNotNull(partSchema.getObjectId());
    ObjectRevision version = partSchema.getObjectRevision();
    assertNotNull(version);
    assertTrue(hasVersionWithComment(partSchema, VERSION_COMMENT_V1));
    assertTrue(repository.exists(EXP_PART_SCHEMA_NAME, null, RepositoryObjectType.PARTITION_SCHEMA));

    PartitionSchema fetchedPartSchema = repository.loadPartitionSchema(partSchema.getObjectId(), null);
    assertEquals(EXP_PART_SCHEMA_NAME, fetchedPartSchema.getName());
    // TODO mlowery partitionSchema.getXML doesn't output desc either; should it?
    //    assertEquals(EXP_PART_SCHEMA_DESC, fetchedPartSchema.getDescription());

    assertEquals(Arrays.asList(new String[] { EXP_PART_SCHEMA_PARTID_1, EXP_PART_SCHEMA_PARTID_2 }), fetchedPartSchema
        .getPartitionIDs());
    assertEquals(EXP_PART_SCHEMA_DYN_DEF, fetchedPartSchema.isDynamicallyDefined());
    assertEquals(EXP_PART_SCHEMA_PART_PER_SLAVE_COUNT, fetchedPartSchema.getNumberOfPartitionsPerSlave());

    partSchema.setNumberOfPartitionsPerSlave(EXP_PART_SCHEMA_PART_PER_SLAVE_COUNT_V2);
    repository.save(partSchema, VERSION_COMMENT_V2, null);
    assertEquals(VERSION_COMMENT_V2, partSchema.getObjectRevision().getComment());
    fetchedPartSchema = repository.loadPartitionSchema(partSchema.getObjectId(), null);
    assertEquals(EXP_PART_SCHEMA_PART_PER_SLAVE_COUNT_V2, fetchedPartSchema.getNumberOfPartitionsPerSlave());
    fetchedPartSchema = repository.loadPartitionSchema(partSchema.getObjectId(), VERSION_LABEL_V1);
    assertEquals(EXP_PART_SCHEMA_PART_PER_SLAVE_COUNT, fetchedPartSchema.getNumberOfPartitionsPerSlave());

    assertEquals(partSchema.getObjectId(), repository.getPartitionSchemaID(EXP_PART_SCHEMA_NAME));

    assertEquals(1, repository.getPartitionSchemaIDs(false).length);
    assertEquals(1, repository.getPartitionSchemaIDs(true).length);
    assertEquals(partSchema.getObjectId(), repository.getPartitionSchemaIDs(false)[0]);

    assertEquals(1, repository.getPartitionSchemaNames(false).length);
    assertEquals(1, repository.getPartitionSchemaNames(true).length);
    assertEquals(EXP_PART_SCHEMA_NAME, repository.getPartitionSchemaNames(false)[0]);

    repository.deletePartitionSchema(partSchema.getObjectId());
    assertFalse(repository.exists(EXP_PART_SCHEMA_NAME, null, RepositoryObjectType.PARTITION_SCHEMA));

    assertEquals(0, repository.getPartitionSchemaIDs(false).length);
    // shared object deletion is permanent by default
    assertEquals(0, repository.getPartitionSchemaIDs(true).length);

    assertEquals(0, repository.getPartitionSchemaNames(false).length);
    // shared object deletion is permanent by default
    assertEquals(0, repository.getPartitionSchemaNames(true).length);
  }

  /**
   * save(clusterSchema)
   * exists()
   * loadClusterSchema()
   * deleteClusterSchema()
   * getClusterID()
   * getClusterIDs()
   * getClusterNames()
   */
  @Test
  public void testClusterSchemas() throws Exception {
    // RepositoryDirectoryInterface rootDir = 
    initRepo();
    ClusterSchema clusterSchema = createClusterSchema(EXP_CLUSTER_SCHEMA_NAME);
    repository.save(clusterSchema, VERSION_COMMENT_V1, null);
    assertNotNull(clusterSchema.getObjectId());
    ObjectRevision version = clusterSchema.getObjectRevision();
    assertNotNull(version);
    assertTrue(hasVersionWithComment(clusterSchema, VERSION_COMMENT_V1));
    assertTrue(repository.exists(EXP_CLUSTER_SCHEMA_NAME, null, RepositoryObjectType.CLUSTER_SCHEMA));

    ClusterSchema fetchedClusterSchema = repository.loadClusterSchema(clusterSchema.getObjectId(), repository
        .getSlaveServers(), null);
    assertEquals(EXP_CLUSTER_SCHEMA_NAME, fetchedClusterSchema.getName());
    // TODO mlowery clusterSchema.getXML doesn't output desc either; should it?
    //    assertEquals(EXP_CLUSTER_SCHEMA_DESC, fetchedClusterSchema.getDescription());

    assertEquals(EXP_CLUSTER_SCHEMA_BASE_PORT, fetchedClusterSchema.getBasePort());
    assertEquals(EXP_CLUSTER_SCHEMA_SOCKETS_BUFFER_SIZE, fetchedClusterSchema.getSocketsBufferSize());
    assertEquals(EXP_CLUSTER_SCHEMA_SOCKETS_FLUSH_INTERVAL, fetchedClusterSchema.getSocketsFlushInterval());
    assertEquals(EXP_CLUSTER_SCHEMA_SOCKETS_COMPRESSED, fetchedClusterSchema.isSocketsCompressed());
    assertEquals(EXP_CLUSTER_SCHEMA_DYN, fetchedClusterSchema.isDynamic());
    assertEquals(1, fetchedClusterSchema.getSlaveServers().size());
    assertTrue(fetchedClusterSchema.getSlaveServers().get(0).getName().startsWith(EXP_SLAVE_NAME));

    // versioning test
    clusterSchema.setBasePort(EXP_CLUSTER_SCHEMA_BASE_PORT_V2);
    repository.save(clusterSchema, VERSION_COMMENT_V2, null);
    assertEquals(VERSION_COMMENT_V2, clusterSchema.getObjectRevision().getComment());
    fetchedClusterSchema = repository
        .loadClusterSchema(clusterSchema.getObjectId(), repository.getSlaveServers(), null);
    assertEquals(EXP_CLUSTER_SCHEMA_BASE_PORT_V2, fetchedClusterSchema.getBasePort());
    fetchedClusterSchema = repository.loadClusterSchema(clusterSchema.getObjectId(), repository.getSlaveServers(),
        VERSION_LABEL_V1);
    assertEquals(EXP_CLUSTER_SCHEMA_BASE_PORT, fetchedClusterSchema.getBasePort());

    assertEquals(clusterSchema.getObjectId(), repository.getClusterID(EXP_CLUSTER_SCHEMA_NAME));

    assertEquals(1, repository.getClusterIDs(false).length);
    assertEquals(1, repository.getClusterIDs(true).length);
    assertEquals(clusterSchema.getObjectId(), repository.getClusterIDs(false)[0]);

    assertEquals(1, repository.getClusterNames(false).length);
    assertEquals(1, repository.getClusterNames(true).length);
    assertEquals(EXP_CLUSTER_SCHEMA_NAME, repository.getClusterNames(false)[0]);

    repository.deleteClusterSchema(clusterSchema.getObjectId());
    assertFalse(repository.exists(EXP_CLUSTER_SCHEMA_NAME, null, RepositoryObjectType.CLUSTER_SCHEMA));

    assertEquals(0, repository.getClusterIDs(false).length);
    // shared object deletion is permanent by default
    assertEquals(0, repository.getClusterIDs(true).length);

    assertEquals(0, repository.getClusterNames(false).length);
    // shared object deletion is permanent by default
    assertEquals(0, repository.getClusterNames(true).length);
  }

  protected ClusterSchema createClusterSchema(String clusterName) throws Exception {
    ClusterSchema clusterSchema = new ClusterSchema();
    clusterSchema.setName(clusterName);
    clusterSchema.setBasePort(EXP_CLUSTER_SCHEMA_BASE_PORT);
    clusterSchema.setSocketsBufferSize(EXP_CLUSTER_SCHEMA_SOCKETS_BUFFER_SIZE);
    clusterSchema.setSocketsFlushInterval(EXP_CLUSTER_SCHEMA_SOCKETS_FLUSH_INTERVAL);
    clusterSchema.setSocketsCompressed(EXP_CLUSTER_SCHEMA_SOCKETS_COMPRESSED);
    clusterSchema.setDynamic(EXP_CLUSTER_SCHEMA_DYN);
    SlaveServer slaveServer = createSlaveServer(clusterName);
    repository.save(slaveServer, VERSION_COMMENT_V1, null);
    deleteStack.push(slaveServer);
    clusterSchema.setSlaveServers(Collections.singletonList(slaveServer));
    return clusterSchema;
  }

  protected JobEntryInterface createJobEntry1(final DatabaseMeta dbMeta) throws Exception {
    JobEntryAttributeTesterJobEntry entry1 = new JobEntryAttributeTesterJobEntry(EXP_JOB_ENTRY_1_NAME);
    entry1.setDatabaseMeta(dbMeta);
    return entry1;

  }

  protected JobEntryInterface createJobEntry2(final DatabaseMeta dbMeta) throws Exception {
    JobEntryAttributeTesterJobEntry entry2 = new JobEntryAttributeTesterJobEntry(EXP_JOB_ENTRY_2_NAME);
    entry2.setDatabaseMeta(dbMeta);
    return entry2;
  }

  protected JobEntryCopy createJobEntry1Copy(final DatabaseMeta dbMeta) throws Exception {
    JobEntryCopy copy = new JobEntryCopy(createJobEntry1(dbMeta));
    copy.setLocation(EXP_JOB_ENTRY_1_COPY_X_LOC, EXP_JOB_ENTRY_1_COPY_Y_LOC);
    return copy;
  }

  protected JobEntryCopy createJobEntry2Copy(final DatabaseMeta dbMeta) throws Exception {
    JobEntryCopy copy = new JobEntryCopy(createJobEntry2(dbMeta));
    copy.setLocation(EXP_JOB_ENTRY_2_COPY_X_LOC, EXP_JOB_ENTRY_2_COPY_Y_LOC);
    return copy;
  }

  protected StepMeta createStepMeta1(final TransMeta transMeta, final DatabaseMeta dbMeta, final Condition condition)
      throws Exception {
    TransStepAttributeTesterTransStep step1 = new TransStepAttributeTesterTransStep();
    step1.setDatabaseMeta(dbMeta);
    step1.setCondition(condition);
    StepMeta stepMeta1 = new StepMeta(EXP_TRANS_STEP_1_NAME, step1);
    StepErrorMeta stepErrorMeta1 = new StepErrorMeta(transMeta, stepMeta1);
    stepErrorMeta1.setEnabled(EXP_TRANS_STEP_ERROR_META_1_ENABLED);
    stepErrorMeta1.setNrErrorsValuename(EXP_TRANS_STEP_ERROR_META_1_NR_ERRORS_VALUE_NAME);
    stepErrorMeta1.setErrorDescriptionsValuename(EXP_TRANS_STEP_ERROR_META_1_DESC_VALUE_NAME);
    stepErrorMeta1.setErrorFieldsValuename(EXP_TRANS_STEP_ERROR_META_1_FIELDS_VALUE_NAME);
    stepErrorMeta1.setErrorCodesValuename(EXP_TRANS_STEP_ERROR_META_1_CODES_VALUE_NAME);
    stepErrorMeta1.setMaxErrors(EXP_TRANS_STEP_ERROR_META_1_MAX_ERRORS);
    stepErrorMeta1.setMaxPercentErrors(EXP_TRANS_STEP_ERROR_META_1_MAX_PERCENT_ERRORS);
    stepErrorMeta1.setMinPercentRows(EXP_TRANS_STEP_ERROR_META_1_MIN_PERCENT_ROWS);
    stepMeta1.setStepErrorMeta(stepErrorMeta1);
    return stepMeta1;
  }

  protected StepMeta createStepMeta2(final DatabaseMeta dbMeta, final Condition condition) throws Exception {
    TransStepAttributeTesterTransStep step2 = new TransStepAttributeTesterTransStep();
    step2.setDatabaseMeta(dbMeta);
    step2.setCondition(condition);
    return new StepMeta(EXP_TRANS_STEP_2_NAME, step2);
  }

  protected TransHopMeta createTransHopMeta(final StepMeta stepMeta1, final StepMeta stepMeta2) throws Exception {
    return new TransHopMeta(stepMeta1, stepMeta2);
  }

  protected NotePadMeta createNotePadMeta(String note) throws Exception {
    return new NotePadMeta(EXP_NOTEPAD_NOTE.concat(note), EXP_NOTEPAD_X, EXP_NOTEPAD_Y, EXP_NOTEPAD_WIDTH, EXP_NOTEPAD_HEIGHT);
  }

  protected JobHopMeta createJobHopMeta(final JobEntryCopy from, final JobEntryCopy to) throws Exception {
    return new JobHopMeta(from, to);
  }

  protected DatabaseMeta createDatabaseMeta(final String dbName) throws Exception {
    DatabaseMeta dbMeta = new DatabaseMeta();
    dbMeta.setName(dbName);
    dbMeta.setHostname(EXP_DBMETA_HOSTNAME);
    dbMeta.setDatabaseType(EXP_DBMETA_TYPE);
    dbMeta.setAccessType(EXP_DBMETA_ACCESS);
    dbMeta.setDBName(EXP_DBMETA_DBNAME);
    dbMeta.setDBPort(EXP_DBMETA_PORT);
    dbMeta.setUsername(EXP_DBMETA_USERNAME);
    dbMeta.setPassword(EXP_DBMETA_PASSWORD);
    dbMeta.setServername(EXP_DBMETA_SERVERNAME);
    dbMeta.setDataTablespace(EXP_DBMETA_DATA_TABLESPACE);
    dbMeta.setIndexTablespace(EXP_DBMETA_INDEX_TABLESPACE);
    // Properties attrs = new Properties();
    // exposed mutable state; yikes
    dbMeta.getAttributes().put(EXP_DBMETA_ATTR1_KEY, EXP_DBMETA_ATTR1_VALUE);
    dbMeta.getAttributes().put(EXP_DBMETA_ATTR2_KEY, EXP_DBMETA_ATTR2_VALUE);
    // TODO mlowery more testing on DatabaseMeta options
    return dbMeta;
  }

  /**
   * save(databaseMeta)
   * loadDatabaseMeta()
   * exists()
   * deleteDatabaseMeta()
   * getDatabaseID()
   * getDatabaseIDs()
   * getDatabaseNames()
   * readDatabases()
   */
  @Test
  public void testDatabases() throws Exception {
    DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA_NAME);
    repository.save(dbMeta, VERSION_COMMENT_V1, null);
    assertNotNull(dbMeta.getObjectId());
    ObjectRevision v1 = dbMeta.getObjectRevision();
    assertNotNull(v1);
    assertTrue(hasVersionWithComment(dbMeta, VERSION_COMMENT_V1));
    // setting repository directory on dbMeta is not supported; use null parent directory
    assertTrue(repository.exists(EXP_DBMETA_NAME, null, RepositoryObjectType.DATABASE));

    DatabaseMeta fetchedDatabase = repository.loadDatabaseMeta(dbMeta.getObjectId(), null);
    assertEquals(EXP_DBMETA_NAME, fetchedDatabase.getName());
    assertEquals(EXP_DBMETA_HOSTNAME, fetchedDatabase.getHostname());
    assertEquals(EXP_DBMETA_TYPE, fetchedDatabase.getPluginId());
    assertEquals(EXP_DBMETA_ACCESS, fetchedDatabase.getAccessType());
    assertEquals(EXP_DBMETA_DBNAME, fetchedDatabase.getDatabaseName());
    assertEquals(EXP_DBMETA_PORT, fetchedDatabase.getDatabasePortNumberString());
    assertEquals(EXP_DBMETA_USERNAME, fetchedDatabase.getUsername());
    assertEquals(EXP_DBMETA_PASSWORD, fetchedDatabase.getPassword());
    assertEquals(EXP_DBMETA_SERVERNAME, fetchedDatabase.getServername());
    assertEquals(EXP_DBMETA_DATA_TABLESPACE, fetchedDatabase.getDataTablespace());
    assertEquals(EXP_DBMETA_INDEX_TABLESPACE, fetchedDatabase.getIndexTablespace());

    // 2 for the ones explicitly set and 1 for port (set behind the scenes)
    assertEquals(2 + 1, fetchedDatabase.getAttributes().size());
    assertEquals(EXP_DBMETA_ATTR1_VALUE, fetchedDatabase.getAttributes().getProperty(EXP_DBMETA_ATTR1_KEY));
    assertEquals(EXP_DBMETA_ATTR2_VALUE, fetchedDatabase.getAttributes().getProperty(EXP_DBMETA_ATTR2_KEY));

    dbMeta.setHostname(EXP_DBMETA_HOSTNAME_V2);
    repository.save(dbMeta, VERSION_COMMENT_V2, null);
    assertTrue(hasVersionWithComment(dbMeta, VERSION_COMMENT_V2));
    fetchedDatabase = repository.loadDatabaseMeta(dbMeta.getObjectId(), null);
    assertEquals(EXP_DBMETA_HOSTNAME_V2, fetchedDatabase.getHostname());
    fetchedDatabase = repository.loadDatabaseMeta(dbMeta.getObjectId(), v1.getName());
    assertEquals(EXP_DBMETA_HOSTNAME, fetchedDatabase.getHostname());

    assertEquals(dbMeta.getObjectId(), repository.getDatabaseID(EXP_DBMETA_NAME));

    assertEquals(1, repository.getDatabaseIDs(false).length);
    assertEquals(1, repository.getDatabaseIDs(true).length);
    assertEquals(dbMeta.getObjectId(), repository.getDatabaseIDs(false)[0]);

    assertEquals(1, repository.getDatabaseNames(false).length);
    assertEquals(1, repository.getDatabaseNames(true).length);
    assertEquals(EXP_DBMETA_NAME, repository.getDatabaseNames(false)[0]);

    assertEquals(1, repository.readDatabases().size());

    repository.deleteDatabaseMeta(EXP_DBMETA_NAME);
    assertFalse(repository.exists(EXP_DBMETA_NAME, null, RepositoryObjectType.DATABASE));

    assertEquals(0, repository.getDatabaseIDs(false).length);
    // shared object deletion is permanent by default
    assertEquals(0, repository.getDatabaseIDs(true).length);

    assertEquals(0, repository.getDatabaseNames(false).length);
    // shared object deletion is permanent by default
    assertEquals(0, repository.getDatabaseNames(true).length);

    assertEquals(0, repository.readDatabases().size());
  }

  protected boolean hasVersionWithComment(final RepositoryElementInterface element, final String comment)
      throws Exception {
    IRevisionService service = (IRevisionService) repository.getService(IRevisionService.class);
    List<ObjectRevision> versions = service.getRevisions(element);
    for (ObjectRevision version : versions) {
      if (version.getComment().equals(comment)) {
        return true;
      }
    }
    return false;
  }

  protected boolean hasVersionWithCal(final RepositoryElementInterface element, final Calendar cal)
      throws Exception {
    IRevisionService service = (IRevisionService) repository.getService(IRevisionService.class);
    List<ObjectRevision> versions = service.getRevisions(element);
    for (ObjectRevision version : versions) {
      if (version.getCreationDate().equals(cal.getTime())) {
        return true;
      }
    }
    return false;
  }
  /**
   * save(slave)
   * loadSlaveServer()
   * exists()
   * deleteSlave()
   * getSlaveID()
   * getSlaveIDs()
   * getSlaveNames()
   * getSlaveServers()
   */
  @Test
  public void testSlaves() throws Exception {
    SlaveServer slave = createSlaveServer("");
    repository.save(slave, VERSION_COMMENT_V1, null);
    assertNotNull(slave.getObjectId());
    ObjectRevision version = slave.getObjectRevision();
    assertNotNull(version);
    assertTrue(hasVersionWithComment(slave, VERSION_COMMENT_V1));
    // setting repository directory on slave is not supported; use null parent directory
    assertTrue(repository.exists(EXP_SLAVE_NAME, null, RepositoryObjectType.SLAVE_SERVER));

    SlaveServer fetchedSlave = repository.loadSlaveServer(slave.getObjectId(), null);
    assertEquals(EXP_SLAVE_NAME, fetchedSlave.getName());
    assertEquals(EXP_SLAVE_HOSTNAME, fetchedSlave.getHostname());
    assertEquals(EXP_SLAVE_PORT, fetchedSlave.getPort());
    assertEquals(EXP_SLAVE_USERNAME, fetchedSlave.getUsername());
    assertEquals(EXP_SLAVE_PASSWORD, fetchedSlave.getPassword());
    assertEquals(EXP_SLAVE_PROXY_HOSTNAME, fetchedSlave.getProxyHostname());
    assertEquals(EXP_SLAVE_PROXY_PORT, fetchedSlave.getProxyPort());
    assertEquals(EXP_SLAVE_NON_PROXY_HOSTS, fetchedSlave.getNonProxyHosts());
    assertEquals(EXP_SLAVE_MASTER, fetchedSlave.isMaster());

    slave.setHostname(EXP_SLAVE_HOSTNAME_V2);
    repository.save(slave, VERSION_COMMENT_V2, null);
    assertEquals(VERSION_COMMENT_V2, slave.getObjectRevision().getComment());
    fetchedSlave = repository.loadSlaveServer(slave.getObjectId(), null);
    assertEquals(EXP_SLAVE_HOSTNAME_V2, fetchedSlave.getHostname());
    fetchedSlave = repository.loadSlaveServer(slave.getObjectId(), VERSION_LABEL_V1);
    assertEquals(EXP_SLAVE_HOSTNAME, fetchedSlave.getHostname());

    assertEquals(slave.getObjectId(), repository.getSlaveID(EXP_SLAVE_NAME));

    assertEquals(1, repository.getSlaveIDs(false).length);
    assertEquals(1, repository.getSlaveIDs(true).length);
    assertEquals(slave.getObjectId(), repository.getSlaveIDs(false)[0]);

    assertEquals(1, repository.getSlaveNames(false).length);
    assertEquals(1, repository.getSlaveNames(true).length);
    assertEquals(EXP_SLAVE_NAME, repository.getSlaveNames(false)[0]);

    assertEquals(1, repository.getSlaveServers().size());
    assertEquals(EXP_SLAVE_NAME, repository.getSlaveServers().get(0).getName());

    repository.deleteSlave(slave.getObjectId());
    assertFalse(repository.exists(EXP_SLAVE_NAME, null, RepositoryObjectType.SLAVE_SERVER));

    assertEquals(0, repository.getSlaveIDs(false).length);
    // shared object deletion is permanent by default
    assertEquals(0, repository.getSlaveIDs(true).length);

    assertEquals(0, repository.getSlaveNames(false).length);
    // shared object deletion is permanent by default
    assertEquals(0, repository.getSlaveNames(true).length);

    assertEquals(0, repository.getSlaveServers().size());
  }

  protected SlaveServer createSlaveServer(String slaveName) throws Exception {
    SlaveServer slaveServer = new SlaveServer();
    slaveServer.setName(EXP_SLAVE_NAME.concat(slaveName));
    slaveServer.setHostname(EXP_SLAVE_HOSTNAME);
    slaveServer.setPort(EXP_SLAVE_PORT);
    slaveServer.setUsername(EXP_SLAVE_USERNAME);
    slaveServer.setPassword(EXP_SLAVE_PASSWORD);
    slaveServer.setProxyHostname(EXP_SLAVE_PROXY_HOSTNAME);
    slaveServer.setProxyPort(EXP_SLAVE_PROXY_PORT);
    slaveServer.setNonProxyHosts(EXP_SLAVE_NON_PROXY_HOSTS);
    slaveServer.setMaster(EXP_SLAVE_MASTER);
    return slaveServer;
  }

  @Ignore
  @Test
  public void testRenameAndUndelete() throws Exception {
    RepositoryDirectoryInterface rootDir = initRepo();
    JobMeta jobMeta = createJobMeta(EXP_JOB_NAME);
    RepositoryDirectoryInterface jobsDir = rootDir.findDirectory(DIR_JOBS);
    repository.save(jobMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(jobMeta);

    repository.deleteJob(jobMeta.getObjectId());
    assertFalse(repository.exists(EXP_JOB_NAME, jobsDir, RepositoryObjectType.JOB));
    RepositoryObject robj = new RepositoryObject(jobMeta.getObjectId(), jobMeta.getName(), jobMeta.getRepositoryDirectory(), null, null, jobMeta.getRepositoryElementType(), null, false);
    repository.undeleteObject(robj);
    assertTrue(repository.exists(EXP_JOB_NAME, jobsDir, RepositoryObjectType.JOB));

    repository.renameJob(jobMeta.getObjectId(), jobsDir, EXP_JOB_NAME_NEW);
    assertFalse(repository.exists(EXP_JOB_NAME, jobsDir, RepositoryObjectType.JOB));
    assertTrue(repository.exists(EXP_JOB_NAME_NEW, jobsDir, RepositoryObjectType.JOB));

    TransMeta transMeta = createTransMeta(EXP_DBMETA_NAME);
    RepositoryDirectoryInterface transDir = rootDir.findDirectory(DIR_TRANSFORMATIONS);
    repository.save(transMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(transMeta);
    repository.renameTransformation(transMeta.getObjectId(), transDir, EXP_TRANS_NAME_NEW);
    assertFalse(repository.exists(EXP_TRANS_NAME.concat(EXP_DBMETA_NAME), transDir, RepositoryObjectType.TRANSFORMATION));
    assertTrue(repository.exists(EXP_TRANS_NAME_NEW, transDir, RepositoryObjectType.TRANSFORMATION));

    DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA2_NAME);
    repository.save(dbMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(dbMeta);
    
    dbMeta.setName(EXP_DBMETA_NAME_NEW);
    repository.save(dbMeta, VERSION_COMMENT_V2, null);
    assertFalse(repository.exists(EXP_DBMETA2_NAME, null, RepositoryObjectType.DATABASE));
    assertTrue(repository.exists(EXP_DBMETA_NAME_NEW, null, RepositoryObjectType.DATABASE));
  }

  @Test
  public void testVersions() throws Exception {
    IRevisionService service = (IRevisionService) repository.getService(IRevisionService.class);
    DatabaseMeta dbMeta = createDatabaseMeta(EXP_DBMETA_NAME);
    repository.save(dbMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(dbMeta);
    List<ObjectRevision> revs = service.getRevisions(dbMeta);
    assertTrue(revs.size() >= 1);
    dbMeta.setHostname(EXP_DBMETA_HOSTNAME_V2);
    repository.save(dbMeta, VERSION_COMMENT_V2, null);
    revs = service.getRevisions(dbMeta);
    assertTrue(revs.size() >= 2);

    //    RepositoryVersionRegistry vReg = repository.getVersionRegistry();
    //    assertEquals(0, vReg.getVersions().size());
    //    vReg.addVersion(new SimpleObjectVersion(EXP_OBJECT_VERSION_LABEL, null, null, null));
    //    assertEquals(2, versions.size());
    //    assertEquals("1.0", versions.get(0).getLabel());
    //    assertEquals("1.1", versions.get(1).getLabel());

    // TODO mlowery finish me
  }

  @Test
  @Ignore
  public void testGetSecurityProvider() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testGetVersionRegistry() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testInsertJobEntryDatabase() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testInsertLogEntry() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testInsertStepDatabase() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testLoadConditionFromStepAttribute() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testLoadDatabaseMetaFromJobEntryAttribute() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testLoadDatabaseMetaFromStepAttribute() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testReadJobMetaSharedObjects() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testReadTransSharedObjects() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testSaveConditionStepAttribute() throws Exception {
    fail("Not yet implemented");
  }
  @Test
  @Ignore
  public void testGetAcl() throws Exception{
    RepositoryDirectoryInterface rootDir = initRepo();
    JobMeta jobMeta = createJobMeta(EXP_JOB_NAME);
    RepositoryDirectoryInterface jobsDir = rootDir.findDirectory(DIR_JOBS);
    repository.save(jobMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(jobMeta);
    assertNotNull(jobMeta.getObjectId());
    ObjectRevision version = jobMeta.getObjectRevision();
    assertNotNull(version);
    assertTrue(hasVersionWithComment(jobMeta, VERSION_COMMENT_V1));
    assertTrue(repository.exists(EXP_JOB_NAME, jobsDir, RepositoryObjectType.JOB));
    ObjectAcl acl = ((IAclService)repository).getAcl(jobMeta.getObjectId(), false);
    assertNotNull(acl);    
  }
  @Test
  @Ignore   
  public void testSetAcl() throws Exception{
    RepositoryDirectoryInterface rootDir = initRepo();
    JobMeta jobMeta = createJobMeta(EXP_JOB_NAME);
    RepositoryDirectoryInterface jobsDir = rootDir.findDirectory(DIR_JOBS);
    repository.save(jobMeta, VERSION_COMMENT_V1, null);
    deleteStack.push(jobMeta);
    assertNotNull(jobMeta.getObjectId());
    ObjectRevision version = jobMeta.getObjectRevision();
    assertNotNull(version);
    assertTrue(hasVersionWithComment(jobMeta, VERSION_COMMENT_V1));
    assertTrue(repository.exists(EXP_JOB_NAME, jobsDir, RepositoryObjectType.JOB));
    ObjectAcl acl = ((IAclService)repository).getAcl(jobMeta.getObjectId(), false);
    assertNotNull(acl);
    acl.setEntriesInheriting(false);
    ObjectAce ace = new RepositoryObjectAce(new RepositoryObjectRecipient("suzy", Type.USER),EnumSet.of(RepositoryFilePermission.READ));
    List<ObjectAce> aceList = new ArrayList<ObjectAce>();
    aceList.add(ace);
    acl.setAces(aceList);
    ((IAclService)repository).setAcl(jobMeta.getObjectId(), acl);
    ObjectAcl acl1 = ((IAclService)repository).getAcl(jobMeta.getObjectId(), false);
    assertEquals(Boolean.FALSE, acl1.isEntriesInheriting());
    assertEquals(1, acl1.getAces().size());
    ObjectAce ace1 = acl1.getAces().get(0);
    assertEquals(ace1.getRecipient().getName(), "suzy");
    assertTrue(ace1.getPermissions().contains(RepositoryFilePermission.READ));
  }
  
  @Test
  @Ignore
  public void testSaveDatabaseMetaJobEntryAttribute() throws Exception {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testSaveDatabaseMetaStepAttribute() throws Exception {
    fail("Not yet implemented");
  }

  public static interface EntryAndStepConstants {

    String ATTR_BOOL = "KDF";

    boolean VALUE_BOOL = true;

    String ATTR_BOOL_MULTI = "DFS";

    boolean VALUE_BOOL_MULTI_0 = true;

    boolean VALUE_BOOL_MULTI_1 = false;

    String ATTR_BOOL_NOEXIST = "IXS";

    boolean VALUE_BOOL_NOEXIST_DEF = true;

    String ATTR_INT = "TAS";

    int VALUE_INT = 4;

    String ATTR_INT_MULTI = "EZA";

    int VALUE_INT_MULTI_0 = 13;

    int VALUE_INT_MULTI_1 = 42;

    String ATTR_STRING = "YAZ";

    String VALUE_STRING = "sdfsdfsdfswe2222";

    String ATTR_STRING_MULTI = "LKS";

    String VALUE_STRING_MULTI_0 = "LKS";

    String VALUE_STRING_MULTI_1 = "LKS";

    String ATTR_DB = "dbMeta1";

    String ATTR_COND = "cond1";
  }

  /**
   * Does assertions on all repository.getJobEntryAttribute* and repository.saveJobEntryAttribute* methods.
   */
  @JobEntry(id = "JobEntryAttributeTester", image = "")
  public static class JobEntryAttributeTesterJobEntry extends JobEntryBase implements Cloneable, JobEntryInterface,
      EntryAndStepConstants {

    private DatabaseMeta databaseMeta;

    public JobEntryAttributeTesterJobEntry() {
      this("");
    }

    public JobEntryAttributeTesterJobEntry(final String name) {
      super(name, "");
    }

    @Override
    public void loadRep(final Repository rep, final IMetaStore metaStore, final ObjectId idJobentry, final List<DatabaseMeta> databases,
        final List<SlaveServer> slaveServers) throws KettleException {
      assertEquals(2, rep.countNrJobEntryAttributes(idJobentry, ATTR_BOOL_MULTI));
      assertEquals(VALUE_BOOL, rep.getJobEntryAttributeBoolean(idJobentry, ATTR_BOOL));
      assertEquals(VALUE_BOOL_MULTI_0, rep.getJobEntryAttributeBoolean(idJobentry, 0, ATTR_BOOL_MULTI));
      assertEquals(VALUE_BOOL_MULTI_1, rep.getJobEntryAttributeBoolean(idJobentry, 1, ATTR_BOOL_MULTI));
      assertEquals(VALUE_BOOL_NOEXIST_DEF, rep.getJobEntryAttributeBoolean(idJobentry, ATTR_BOOL_NOEXIST,
          VALUE_BOOL_NOEXIST_DEF));
      assertEquals(VALUE_INT, rep.getJobEntryAttributeInteger(idJobentry, ATTR_INT));
      assertEquals(VALUE_INT_MULTI_0, rep.getJobEntryAttributeInteger(idJobentry, 0, ATTR_INT_MULTI));
      assertEquals(VALUE_INT_MULTI_1, rep.getJobEntryAttributeInteger(idJobentry, 1, ATTR_INT_MULTI));
      assertEquals(VALUE_STRING, rep.getJobEntryAttributeString(idJobentry, ATTR_STRING));
      assertEquals(VALUE_STRING_MULTI_0, rep.getJobEntryAttributeString(idJobentry, 0, ATTR_STRING_MULTI));
      assertEquals(VALUE_STRING_MULTI_1, rep.getJobEntryAttributeString(idJobentry, 1, ATTR_STRING_MULTI));
      assertNotNull(rep.loadDatabaseMetaFromJobEntryAttribute(idJobentry, null, ATTR_DB, databases));
    }

    @Override
    public void saveRep(final Repository rep, final IMetaStore metaStore, final ObjectId idJob) throws KettleException {
      rep.saveJobEntryAttribute(idJob, getObjectId(), ATTR_BOOL, VALUE_BOOL);
      rep.saveJobEntryAttribute(idJob, getObjectId(), 0, ATTR_BOOL_MULTI, VALUE_BOOL_MULTI_0);
      rep.saveJobEntryAttribute(idJob, getObjectId(), 1, ATTR_BOOL_MULTI, VALUE_BOOL_MULTI_1);
      rep.saveJobEntryAttribute(idJob, getObjectId(), ATTR_INT, VALUE_INT);
      rep.saveJobEntryAttribute(idJob, getObjectId(), 0, ATTR_INT_MULTI, VALUE_INT_MULTI_0);
      rep.saveJobEntryAttribute(idJob, getObjectId(), 1, ATTR_INT_MULTI, VALUE_INT_MULTI_1);
      rep.saveJobEntryAttribute(idJob, getObjectId(), ATTR_STRING, VALUE_STRING);
      rep.saveJobEntryAttribute(idJob, getObjectId(), 0, ATTR_STRING_MULTI, VALUE_STRING_MULTI_0);
      rep.saveJobEntryAttribute(idJob, getObjectId(), 1, ATTR_STRING_MULTI, VALUE_STRING_MULTI_1);
      rep.saveDatabaseMetaJobEntryAttribute(idJob, getObjectId(), null, ATTR_DB, databaseMeta);
      rep.insertJobEntryDatabase(idJob, getObjectId(), databaseMeta.getObjectId());
    }

    public Result execute(final Result prevResult, final int nr) throws KettleException {
      throw new UnsupportedOperationException();
    }

    public void loadXML(final Node entrynode, final List<DatabaseMeta> databases, final List<SlaveServer> slaveServers,
        final Repository rep, final IMetaStore metaStore) throws KettleXMLException {
      throw new UnsupportedOperationException();
    }

    public void setDatabaseMeta(DatabaseMeta databaseMeta) {
      this.databaseMeta = databaseMeta;
    }

  }

  /**
   * Does assertions on all repository.getStepAttribute* and repository.saveStepAttribute* methods.
   */
  @Step(id="StepAttributeTester", name = "StepAttributeTester", image = "")
  public static class TransStepAttributeTesterTransStep extends BaseStepMeta implements StepMetaInterface,
      EntryAndStepConstants {

    private DatabaseMeta databaseMeta;

    private Condition condition;

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
        RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, 
        VariableSpace space, Repository repository, IMetaStore metaStore) {
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
        TransMeta transMeta, Trans trans) {
      return null;

    }

    public StepDataInterface getStepData() {
      return null;

    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, final IMetaStore metaStore)
        throws KettleXMLException {
    }

    public void readRep(Repository rep, final IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases)
        throws KettleException {
      assertEquals(2, rep.countNrStepAttributes(idStep, ATTR_BOOL_MULTI));
      assertEquals(VALUE_BOOL, rep.getStepAttributeBoolean(idStep, ATTR_BOOL));
      assertEquals(VALUE_BOOL_MULTI_0, rep.getStepAttributeBoolean(idStep, 0, ATTR_BOOL_MULTI));
      assertEquals(VALUE_BOOL_MULTI_1, rep.getStepAttributeBoolean(idStep, 1, ATTR_BOOL_MULTI));
      assertEquals(VALUE_BOOL_NOEXIST_DEF, rep.getStepAttributeBoolean(idStep, 0, ATTR_BOOL_NOEXIST,
          VALUE_BOOL_NOEXIST_DEF));
      assertEquals(VALUE_INT, rep.getStepAttributeInteger(idStep, ATTR_INT));
      assertEquals(VALUE_INT_MULTI_0, rep.getStepAttributeInteger(idStep, 0, ATTR_INT_MULTI));
      assertEquals(VALUE_INT_MULTI_1, rep.getStepAttributeInteger(idStep, 1, ATTR_INT_MULTI));
      assertEquals(VALUE_STRING, rep.getStepAttributeString(idStep, ATTR_STRING));
      assertEquals(VALUE_STRING_MULTI_0, rep.getStepAttributeString(idStep, 0, ATTR_STRING_MULTI));
      assertEquals(VALUE_STRING_MULTI_1, rep.getStepAttributeString(idStep, 1, ATTR_STRING_MULTI));
      assertNotNull(rep.loadDatabaseMetaFromStepAttribute(idStep, ATTR_DB, databases));
      assertNotNull(rep.loadConditionFromStepAttribute(idStep, ATTR_COND));
    }

    public void saveRep(Repository rep, final IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep) throws KettleException {
      rep.saveStepAttribute(idTransformation, idStep, ATTR_BOOL, VALUE_BOOL);
      rep.saveStepAttribute(idTransformation, idStep, 0, ATTR_BOOL_MULTI, VALUE_BOOL_MULTI_0);
      rep.saveStepAttribute(idTransformation, idStep, 1, ATTR_BOOL_MULTI, VALUE_BOOL_MULTI_1);
      rep.saveStepAttribute(idTransformation, idStep, ATTR_INT, VALUE_INT);
      rep.saveStepAttribute(idTransformation, idStep, 0, ATTR_INT_MULTI, VALUE_INT_MULTI_0);
      rep.saveStepAttribute(idTransformation, idStep, 1, ATTR_INT_MULTI, VALUE_INT_MULTI_1);
      rep.saveStepAttribute(idTransformation, idStep, ATTR_STRING, VALUE_STRING);
      rep.saveStepAttribute(idTransformation, idStep, 0, ATTR_STRING_MULTI, VALUE_STRING_MULTI_0);
      rep.saveStepAttribute(idTransformation, idStep, 1, ATTR_STRING_MULTI, VALUE_STRING_MULTI_1);
      rep.saveDatabaseMetaStepAttribute(idTransformation, idStep, ATTR_DB, databaseMeta);
      rep.insertStepDatabase(idTransformation, idStep, databaseMeta.getObjectId());
      rep.saveConditionStepAttribute(idTransformation, idStep, ATTR_COND, condition);
    }

    public void setDefault() {
    }

    public void setDatabaseMeta(final DatabaseMeta databaseMeta) {
      this.databaseMeta = databaseMeta;
    }

    public void setCondition(final Condition condition) {
      this.condition = condition;
    }

  }
}
