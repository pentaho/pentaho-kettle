@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.21')

@Grab(group='org.eclipse.jgit', module='org.eclipse.jgit', version='4.5.3.201708160445-r')
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

@Grab(group='commons-io', module='commons-io', version='2.4')
import org.apache.commons.io.FileUtils;

import groovy.util.logging.Slf4j;

/*
 * see: http://download.eclipse.org/jgit/site/4.5.3.201708160445-r/apidocs/index.html
 */

@Slf4j
public class LocalGit {

  // REQUIRED PROPERTIES:
  static String GITHUB_USERNAME   = System.getProperty("GITHUB_USERNAME");
  static String GITHUB_PASSWORD   = System.getProperty("GITHUB_PASSWORD");
  static String GITHUB_USER_EMAIL = System.getProperty("GITHUB_USER_EMAIL");

  
  public static void clone(String uri, String branch, boolean bare, File checkoutDir) {
    LocalGit.clone("origin", uri, branch, bare, checkoutDir);
  }
  
  
  public static void clone(String remoteName, String uri, String branch, boolean bare, File checkoutDir) {
    if (checkoutDir.exists()) {
      FileUtils.deleteDirectory(checkoutDir);
    }
    
    checkoutDir.mkdir();
    log.info( "cloning " + branch + " of " + uri );
    
    CloneCommand cloneCommand = Git.cloneRepository();
    cloneCommand.setURI(uri);
    cloneCommand.setBare(bare);
    if (bare) {
      File checkoutGitDir = new File(checkoutDir.getPath() + "/.git");
      checkoutGitDir.mkdir();
      cloneCommand.setDirectory(checkoutGitDir);
    } else {
      cloneCommand.setDirectory(checkoutDir);
    }
    cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_USERNAME, GITHUB_PASSWORD));
    cloneCommand.setCloneAllBranches(false);
    cloneCommand.setBranch(branch);
    cloneCommand.setRemote(remoteName);
    cloneCommand.call();
  }
  
  
  public static void commit(String localWorkingGit, String commitMessage) {
    Repository localRepo = new FileRepository(localWorkingGit);
    Git workingGit = new Git(localRepo);
    StatusCommand statusCommand = workingGit.status();
    Status status = statusCommand.call();
    Set<String> modifiedFiles = status.getModified();
    
    for (String modifiedFile : modifiedFiles) {
      log.info("modified file: " + modifiedFile);
    }
    
    if (modifiedFiles.size() > 0) {
      CommitCommand commitCommand = workingGit.commit();
      commitCommand.setCommitter(GITHUB_USERNAME, GITHUB_USER_EMAIL);
      commitCommand.setAll(true);
      commitCommand.setMessage(commitMessage);
      log.info("committing modifications in " + localWorkingGit + " ...");
      commitCommand.call();
    }
  }
  
  
  public static void pushCommits(String localWorkingGit) { 
    LocalGit.pushCommits( localWorkingGit, "origin", false );
  }
  
  public static void pushCommits(String localWorkingGit, String remoteName) { 
    LocalGit.pushCommits( localWorkingGit, remoteName, "origin" );
  }
  
  public static void pushCommits(String localWorkingGit, String remoteName, boolean pushForcefully ) {
    Repository localRepo = new FileRepository(localWorkingGit);
    Git workingGit = new Git(localRepo);
    PushCommand pushCommand = workingGit.push();
    pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_USERNAME, GITHUB_PASSWORD));
    pushCommand.setForce( pushForcefully );
    pushCommand.setRemote( remoteName );
    pushCommand.setPushAll();
    log.info("pushing commits on " + localWorkingGit + " to " + remoteName + " ...");
    Iterator<PushResult> pushResults = pushCommand.call().iterator();
    while (pushResults.hasNext()) {
      PushResult pushResult = pushResults.next();
      if ((pushResult != null) && (pushResult.getMessages() != null)) {
        log.info(pushResult.getMessages());
      }
      Collection<RemoteRefUpdate> remoteRefUpdates = pushResult.getRemoteUpdates();
      for (RemoteRefUpdate remoteRefUpdate : remoteRefUpdates) {
        if ((remoteRefUpdate != null) && (remoteRefUpdate.getMessage() != null)) {
          log.info(remoteRefUpdate.getMessage());
        }
      }
    }
  }
  
  
  public static void pushBranchDeletion(String localWorkingGit, String branchToDelete) {
    Repository localRepo = new FileRepository(localWorkingGit);
    Git workingGit = new Git(localRepo);
    
    RefSpec refSpec = new RefSpec();
    refSpec = refSpec.setSource( null );
    refSpec = refSpec.setDestination( "refs/heads/" + branchToDelete );
    PushCommand pushCommand = workingGit.push();
    pushCommand.setRefSpecs(refSpec);
    pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_USERNAME, GITHUB_PASSWORD));
    log.info("deleting remote branch " + branchToDelete + " on " + localWorkingGit + " ...");
    Iterator<PushResult> pushResults = pushCommand.call().iterator();
    while (pushResults.hasNext()) {
      PushResult pushResult = pushResults.next();
      if ((pushResult != null) && (pushResult.getMessages() != null)) {
        log.info(pushResult.getMessages());
      }
      Collection<RemoteRefUpdate> remoteRefUpdates = pushResult.getRemoteUpdates();
      for (RemoteRefUpdate remoteRefUpdate : remoteRefUpdates) {
        if ((remoteRefUpdate != null) && (remoteRefUpdate.getMessage() != null)) {
          log.info(remoteRefUpdate.getMessage());
        }
      }
    }
  }
  
  
  public static void createBranch( String localWorkingGit, String currentBranch, String newBranch ) {
    Repository localRepo = new FileRepository(localWorkingGit);
    Git workingGit = new Git(localRepo);
    CheckoutCommand checkoutCommand = workingGit.checkout();
    checkoutCommand.setCreateBranch(true);
    checkoutCommand.setStartPoint( "refs/heads/" + currentBranch );
    checkoutCommand.setName( newBranch );
    log.info( "creating branch " + newBranch + " from " + currentBranch + " on " + localWorkingGit + " ..." );
    checkoutCommand.call();
  }
  
  
  public static void addRemote( String localWorkingGit, String remoteName, String remoteUri ) {
    Repository localRepo = new FileRepository(localWorkingGit);    
    RemoteAddCommand remoteAddCommand = new RemoteAddCommand( localRepo );
    remoteAddCommand.setName( remoteName );
    remoteAddCommand.setUri( new URIish(remoteUri) );
    log.info( "adding remote " + remoteName + " " + remoteUri + " ..." );
    remoteAddCommand.call();
  }  
  
}









