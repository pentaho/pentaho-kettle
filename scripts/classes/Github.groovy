@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.21')

@Grab(group='com.google.code.gson', module='gson', version='2.6.2')
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.3')
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import groovy.util.logging.Slf4j;

/*
 * see: https://developer.github.com/v3/
 */

@Slf4j
public class Github {

  // REQUIRED PROPERTIES:
  static String GITHUB_API_TOKEN  = System.getProperty("GITHUB_API_TOKEN");  // aka "personal access token" in your github settings


  public static JsonObject runJSONCommand( String requestType, String jsonCommandString, JsonObject requestJsonObject ) {
    String url = "https://api.github.com" + jsonCommandString;
    HttpClient client = new DefaultHttpClient();
    HttpResponse response = null;
    if ( requestType.equals("GET") ) {
      HttpGet request = new HttpGet(url);
      request.addHeader("Authorization", "token ${GITHUB_API_TOKEN}")
      response = client.execute(request);
    }
    if ( requestType.equals("POST") ) {
      HttpPost request = new HttpPost(url);
      request.addHeader("Authorization", "token ${GITHUB_API_TOKEN}")
      HttpEntity httpEntity = new StringEntity(requestJsonObject.toString());
      request.setEntity(httpEntity);
      response = client.execute(request);
    }
    if ( requestType.equals("PUT") ) {
      HttpPut request = new HttpPut(url);
      request.addHeader("Authorization", "token ${GITHUB_API_TOKEN}")
      HttpEntity httpEntity = new StringEntity(requestJsonObject.toString());
      request.setEntity(httpEntity);
      response = client.execute(request);
    }
    if ( requestType.equals("DELETE") ) {
      HttpDelete request = new HttpDelete(url);
      request.addHeader("Authorization", "token ${GITHUB_API_TOKEN}")
      response = client.execute(request);
    }
    StatusLine status = response.getStatusLine();
    HttpEntity responseEntity = response.getEntity();

    // HTTPDelete doesn't return a response entity on success
    String content = "";
    if (responseEntity) {
      content = EntityUtils.toString(responseEntity);
    }

    if (status.getStatusCode() > 204 ) {
      log.warn("HTTPCODE " + status.getStatusCode() + " from " + url);
      log.warn( content );
    }

    JsonObject responseJsonObject = null;
    if (content.length() > 0) {
      JsonElement jsonElement = new JsonParser().parse(content);
      // If an object, return it
      if (jsonElement instanceof JsonObject) {
        responseJsonObject = jsonElement.getAsJsonObject();
      }
      // If an array, return the first item
      else if (jsonElement instanceof JsonArray) {
        JsonArray array = jsonElement.getAsJsonArray()
        responseJsonObject = array.get(0)
      }
    }
    return responseJsonObject;
  }

  public static ArrayList<String> getOrgRepos( String org, String page ) {
    ArrayList<String> reposArrayList = new ArrayList<String>();
    JsonObject responseJsonObject = Github.runJSONCommand( "GET", "/orgs/" + org + "/repos?per_page=100&page=" + page, null );
    JsonArray jsonArray = responseJsonObject.getAsJsonArray();
    log.info(org + " org repos page " + page + ": " + jsonArray.size());
    Iterator<JsonElement> repoJsonElementIterator = jsonArray.iterator();
    while (repoJsonElementIterator.hasNext()) {
      JsonElement repoJsonElement = repoJsonElementIterator.next();
      JsonObject repoJsonObject = repoJsonElement.getAsJsonObject();
      JsonPrimitive repoNameJsonPrimitive = repoJsonObject.getAsJsonPrimitive("name");
      reposArrayList.add(org + "/" + repoNameJsonPrimitive.getAsString());
    }
    return reposArrayList;
  }


  public static ArrayList<String> getRepoBranches( String repo ) {
    ArrayList<String> branchesArrayList = new ArrayList<String>();
    JsonObject responseJsonObject = Github.runJSONCommand( "GET", "/repos/" + repo + "/branches?per_page=100", null );
    JsonArray jsonArray = responseJsonObject.getAsJsonArray();
    log.info(repo + " repo branches: " + jsonArray.size());
    Iterator<JsonElement> branchesJsonElementIterator = jsonArray.iterator();
    while (branchesJsonElementIterator.hasNext()) {
      JsonElement branchJsonElement = branchesJsonElementIterator.next();
      JsonObject branchJsonObject = branchJsonElement.getAsJsonObject();
      JsonPrimitive branchNameJsonPrimitive = branchJsonObject.getAsJsonPrimitive("name");
      branchesArrayList.add(branchNameJsonPrimitive.getAsString());
    }
    return branchesArrayList;
  }


  public static boolean isProjectPrivate( String org, String projectName ) {
    JsonObject responseJsonObject = Github.runJSONCommand( "GET", "/repos/" + org + "/" + projectName, null );
    JsonPrimitive privateJsonPrimitive = responseJsonObject.getAsJsonPrimitive("private");
    if (privateJsonPrimitive.getAsString().toLowerCase().equals("true")) {
      log.debug(org + "/" + projectName + " is PRIVATE");
      return true;
    } else {
      log.debug(org + "/" + projectName + " is PUBLIC");
      return false;
    }
  }


  public static boolean doesRemoteBranchExist( String org, String projectName, String branch ) {
    JsonObject responseJsonObject = Github.runJSONCommand( "GET", "/repos/" + org + "/" + projectName + "/git/refs/heads/" + branch, null );
    JsonPrimitive refJsonPrimitive = responseJsonObject.getAsJsonPrimitive("ref");
    if ( refJsonPrimitive != null && refJsonPrimitive.getAsString().equals("refs/heads/" + branch) ) {
      return true;
    }
    log.warn( "branch " + branch + " of " + org + "/" + projectName + " not found" );
    return false;
  }


  public static boolean doesRemoteTagExist( String org, String projectName, String tag ) {
    JsonObject responseJsonObject = Github.runJSONCommand( "GET", "/repos/" + org + "/" + projectName + "/git/refs/tags/" + tag, null );
    JsonPrimitive refJsonPrimitive = responseJsonObject.getAsJsonPrimitive("ref");
    if ( refJsonPrimitive != null && refJsonPrimitive.getAsString().equals("refs/tags/" + tag) ) {
      return true;
    }
    log.warn( "tag " + tag + " of " + org + "/" + projectName + " not found" );
    return false;
  }


  public static boolean doesRemoteBranchOrTagExist( String org, String projectName, String branchTagName ) {
    if ( Github.doesRemoteBranchExist( org, projectName, branchTagName ) ) {
      return true;
    }
    if ( Github.doesRemoteTagExist( org, projectName, branchTagName ) ) {
      return true;
    }
    return false;
  }


  public static String getHeadSHA( String org, String projectName, String branch ) {
    JsonObject responseJsonObject = Github.runJSONCommand( "GET", "/repos/${org}/${projectName}/git/refs/heads/${branch}", null );
    String headSHA = responseJsonObject.getAsJsonObject("object").getAsJsonPrimitive("sha").getAsString();
    log.debug( "HEAD SHA of ${org}/${projectName}/${branch} reported as ${headSHA}" );
    return headSHA;
  }

  public static String getLastSHAForDate( String org, String projectName, String branch, Date date ) {
    def dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
    def startDate = new Date(0).format(dateFormat) // beginning of time
    def untilDate = date.format(dateFormat)
    JsonObject responseJsonObject = Github.runJSONCommand( "GET", "/repos/${org}/${projectName}/commits?sha=${branch}&since=${startDate}&until=${untilDate}&per_page=1&page=1", null );
    String sha = responseJsonObject.getAsJsonPrimitive("sha").getAsString();
    log.debug( "Last SHA of ${org}/${projectName}/${branch} previous to ${untilDate} reported as ${sha}" );
    return sha;
  }

  public static String createPullRequest( String org,
                                          String projectName,
                                          String baseBranch,
                                          String featureForkBranch,
                                          String title,
                                          String comments ) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("base",  baseBranch);
    jsonObject.addProperty("head",  featureForkBranch);
    jsonObject.addProperty("title", title);
    jsonObject.addProperty("body",  comments);
    String jsonCommandString = "/repos/" + org + "/" + projectName + "/pulls";
    log.info( "creating pull request with: \n" + jsonObject.toString() );
    JsonObject responseJsonObject = Github.runJSONCommand( "POST", jsonCommandString, jsonObject );
    JsonPrimitive numberJsonPrimitive = responseJsonObject.getAsJsonPrimitive("number");
    if ( numberJsonPrimitive == null ) {
      JsonArray errorsJsonArray = responseJsonObject.getAsJsonArray("errors");
      if ( errorsJsonArray != null ) {
        Iterator<JsonElement> errorsJsonElementIterator = errorsJsonArray.iterator();
        while (errorsJsonElementIterator.hasNext()) {
          JsonElement errorsJsonElement = errorsJsonElementIterator.next();
          JsonObject errorsJsonObject = errorsJsonElement.getAsJsonObject();
          JsonPrimitive messageJsonPrimitive = errorsJsonObject.getAsJsonPrimitive("message");
          if ( messageJsonPrimitive != null ) {
            String errorMessage = messageJsonPrimitive.getAsString();
            log.warn( errorMessage );
            return null;
          }
        }
      }
    }
    String pullRequestNumber = numberJsonPrimitive.getAsString();
    log.info( "pull request " + pullRequestNumber + " created on " + org + ":" + projectName );
    return pullRequestNumber;
  }


  public static void mergePullRequest(    String org,
                                          String projectName,
                                          String pullRequestNumber,
                                          String mergeCommitTitle,
                                          String mergeCommitMessage,
                                          String requiredHeadSHA ) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("commit_title",    mergeCommitTitle);
    jsonObject.addProperty("commit_message",  mergeCommitMessage);
    jsonObject.addProperty("sha",             requiredHeadSHA);
    jsonObject.addProperty("merge_method",    "merge");
    String jsonCommandString = "/repos/" + org + "/" + projectName + "/pulls/" + pullRequestNumber + "/merge";
    log.info( "merging pull request with: \n" + jsonObject.toString() );
    JsonObject responseJsonObject = Github.runJSONCommand( "PUT", jsonCommandString, jsonObject );
    String mergedResponse = responseJsonObject.getAsJsonPrimitive("merged").getAsString();
    log.info( "merged = " + mergedResponse );
  }

  public static void createBranch(String org, String projectName, String branchName, String newBranchName) {
    def headSHA = getHeadSHA(org, projectName, branchName)
    if (!headSHA) {
      println "Unknown HEAD for project (${projectName}) and branch (${branchName})"
      return
    }

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("ref",  "refs/heads/${newBranchName}");
    jsonObject.addProperty("sha",  "${headSHA}");
    String jsonCommandString = "/repos/${org}/${projectName}/git/refs";
    log.debug( "creating branch with: " + jsonObject.toString() );
    JsonObject responseJsonObject = Github.runJSONCommand( "POST", jsonCommandString, jsonObject );
    log.info( "branch created: ${newBranchName}" );
  }

  public static void createTag(String org, String projectName, String branchName, String newTagName) {
    def headSHA = getHeadSHA(org, projectName, branchName)
    if (!headSHA) {
      println "Unknown HEAD for project (${projectName}) and branch (${branchName})"
      return
    }

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("ref",  "refs/tags/${newTagName}");
    jsonObject.addProperty("sha",  "${headSHA}");
    String jsonCommandString = "/repos/${org}/${projectName}/git/refs";
    log.debug( "creating tag with: " + jsonObject.toString() );
    JsonObject responseJsonObject = Github.runJSONCommand( "POST", jsonCommandString, jsonObject );
    log.info( "tag created: ${newTagName}" );
  }

  public static void deleteBranch(String org, String projectName, String branchName) {
    log.debug( "deleting branch: ${branchName}" );
    JsonObject responseJsonObject = Github.runJSONCommand( "DELETE", "/repos/${org}/${projectName}/git/refs/heads/${branchName}", null );
    log.info( "branch deleted: ${branchName}" );
  }

  public static void deleteTag(String org, String projectName, String tagName) {
    log.debug( "deleting tag: ${tagName}" );
    JsonObject responseJsonObject = Github.runJSONCommand( "DELETE", "/repos/${org}/${projectName}/git/refs/tags/${tagName}", null );
    log.info( "tag deleted: ${tagName}" );
  }

}
