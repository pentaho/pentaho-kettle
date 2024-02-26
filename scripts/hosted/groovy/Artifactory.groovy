@GrabResolver(name='pentaho', root='https://repo.orl.eng.hitachivantara.com/artifactory/pnt-mvn', m2Compatible='true')
@Grapes([
    @Grab(group = 'com.squareup.okhttp3', module = 'okhttp', version = '4.4.1')
])

import groovy.json.JsonSlurper

import okhttp3.HttpUrl
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.Route

class Artifactory {
  @Delegate CLI cli

  HttpUrl baseUrl
  OkHttpClient http

  def parser = new JsonSlurper()

  static final MediaType TEXT = MediaType.parse('text/plain; charset=utf-8')

  Artifactory(CLI cli) {
    this.cli = cli
    this.baseUrl = HttpUrl.get(rtURL.endsWith('/') ? rtURL.substring(0, rtURL.length() - 1) : rtURL)
    this.http = new OkHttpClient.Builder()
        .authenticator({ Route route, Response response ->
          if (response.request().header("Authorization") != null) {
            return null // Give up, we've already attempted to authenticate.
          }
          def request = response.request().newBuilder()
          if (rtApiToken) {
            request.header('X-JFrog-Art-Api', rtApiToken)
          } else {
            request.header('Authorization', Credentials.basic(rtUsername, rtPassword))
          }
          return request.build()
        })
        .addInterceptor({ Interceptor.Chain chain ->
          // workaround for Artifactory 6x because it doesn't add a challenge for authentication
          Request request = chain.request()
          def authenticatedRequest = request.newBuilder()
          if (rtApiToken) {
            authenticatedRequest.header('X-JFrog-Art-Api', rtApiToken)
          } else {
            authenticatedRequest.header('Authorization', Credentials.basic(rtUsername, rtPassword))
          }
          return chain.proceed(authenticatedRequest.build())
        })
        .build()
  }

  String getRepoAqlPart(List<String> inputRepoLst){
    List<String> outReposLst = []
    for (String repo : inputRepoLst) {
      outReposLst.add('{"repo": {"\$match":"' + repo + '"}}')
    }
    return '"$or": [' << outReposLst.join(',') << ']'
  }

  String getRepoAql(){
    List<String> releaseRepos = rtReleaseRepos?.split(',')*.trim() //  ['pntpub-maven-rc', 'pntpub-maven-release']
    List<String> qatRepos = rtQatRepos?.split(',')*.trim() //['pntpub-maven-dev']

    if (rtRepo in qatRepos) {
      //repo = '"repo": {"\$match":"pntpub-maven-dev"}'
      return getRepoAqlPart(qatRepos)
    } else if (rtRepo in releaseRepos) {
      //repo = '"$or": [ {"repo": {"\$match":"pntpub-maven-rc"}}, {"repo": {"\$match":"pntpub-maven-release"}}]'
      return getRepoAqlPart(releaseRepos)
    }
    return ''
  }

  List<Map> searchArtifacts(
      List<String> filenames,
      final String pathMatch,
      String pathNotMatch = null,
      String sortingDirection = "\$asc",
      Integer limit = null,
      Boolean processRepo = Boolean.FALSE
  ) {
    String repo = ''
    if (processRepo.booleanValue() && rtRepo) {
      repo = getRepoAql()
    }

    def sb = "" << "items.find({"
    if (repo) sb << repo << ', '
    sb << '"type": "file",'
    if (pathMatch) sb << '"path": {"\$match":"*/' << pathMatch << '"},'
    if (pathNotMatch) sb << '"path": {"\$nmatch":"*/' << pathNotMatch << '"},'
    sb << '"$or": ['
    int lastIdx = filenames.size() - 1
    filenames.eachWithIndex { filename, idx ->
      filename = filename.replace("-dist", "*")
      sb << '{"name": {"\$match":"' << filename << '"}}'
      if (idx < lastIdx) sb << ', '
    }
    sb << ']'
    sb << '})'
    sb << '.include("repo", "path", "name", "actual_md5", "actual_sha1", "sha256", "size", "created")'
    sb << '.sort({"' << sortingDirection << '" : ["created"]})'
    if (limit) sb << '.limit(' << limit << ')'

    Map aql = aql(sb.toString(), TEXT)
    aql?.results as List<Map> ?: []
  }

  List<Map> addArtifactsSignedURLs(List<Map> results) {
    return results.collect {
      it.put('signedURL',
          getArtifactSignedURL("${it.repo}/${it.path}/${it.name}"))
      return it
    }
  }

  String getArtifactSignedURL(final String filePath) {
    def url = baseUrl.newBuilder('api/signed/url').build()
    String data = '{ "repo_path": "' << filePath << '", "valid_for_secs":172800 }' //2 days
    MediaType mediaType = MediaType.parse("application/json");
    RequestBody body = RequestBody.create(data, mediaType);

    httpRequest(new Request.Builder().url(url).post(body).build(), true, false)
  }

  def aql(String query, MediaType type) {
    def url = baseUrl.newBuilder('api/search/aql').build()

    safeRequest(new Request.Builder().url(url)
        .post(RequestBody.create(query, type))
        .build()
    )
  }

  def safeRequest(Request request) { return httpRequest(request, true) }

  def httpRequest(Request request, boolean safeCall = false, boolean jsonResponse = true) throws IOException {
    http.newCall(request).execute().withCloseable { Response response ->
      if (response.isSuccessful() || safeCall) {
        String body = response.body().string()
        if (!response.isSuccessful()) {
          printError(body)
        }
        return body && jsonResponse ? parser.parseText(body) : body
      }
      throw new IOException("$response")
    }
  }
}
