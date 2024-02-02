@GrabResolver(name='pentaho', root='https://one.hitachivantara.com/artifactory/pnt-mvn', m2Compatible='true')
@Grapes([
  @Grab(group = 'org.jfrog.buildinfo', module = 'build-info-api', version = '2.39.8'),
  @Grab(group = 'com.squareup.okhttp3', module = 'okhttp', version = '4.4.1')
])

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.Route
import org.jfrog.build.api.Agent
import org.jfrog.build.api.Build
import org.jfrog.build.api.builder.ArtifactBuilder
import org.jfrog.build.api.builder.BuildInfoBuilder
import org.jfrog.build.api.builder.ModuleBuilder

import java.text.SimpleDateFormat

class Artifactory {
  @Delegate CLI cli
  def parser = new JsonSlurper()
  def jsonOutput = new JsonGenerator.Options()
    .excludeNulls()
    .build()
  static final MediaType JSON = MediaType.parse('application/json; charset=utf-8')
  static final MediaType TEXT = MediaType.parse('text/plain; charset=utf-8')
  SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Build.STARTED_FORMAT)

  HttpUrl baseUrl
  OkHttpClient http

  Artifactory(CLI cli) {
    this.cli = cli
    validate()
    this.baseUrl = HttpUrl.get(rtURL)
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

  void validate() {
    if (!rtURL) printError('Artifactory url missing')
    if (!(rtUsername && rtPassword) && !rtApiToken)
      printError('Artifactory authentication missing. Provide a username and password, or an Api token.')
    if (!buildName) printError('Missing build name')
    if (!buildNumber) printError('Missing build number')
  }

  List<Map> searchArtifacts(List<String> filenames) {
    String repo = ''//baseUrl.pathSegments().last()
    def sb = "" << "items.find({"
    if (repo) sb << '"repo": "' << repo << '", '
    sb << '"type": "file", '
    sb << '"$or": ['
    int lastIdx = filenames.size() - 1
    filenames.eachWithIndex { filename, idx ->
      sb << '{"name": "' << filename << '"}'
      if (idx < lastIdx) sb << ', '
    }
    sb << ']'
    sb << '})'
    sb << '.include("repo", "path", "name", "actual_md5", "actual_sha1")'

    aql(sb.toString(), TEXT)?.results ?: []
  }

  def aql(String query, MediaType type) {
    def url = baseUrl.newBuilder('api/search/aql').build()
    safeRequest(new Request.Builder().url(url)
        .post(RequestBody.create(query, type))
        .build()
    )
  }

  void sendBuildInfo(List<Map> artifacts) {
    if (artifacts.empty) {
      printError('No artifacts found, nothing to be done!')
    }
    def fileExtension = { String name -> name.substring(name.lastIndexOf('.') + 1) }.memoize()
    BuildInfoBuilder builder = new BuildInfoBuilder()
      .name(buildName)
      .number(buildNumber)
      .started(simpleDateFormat.format(new Date()))

    ModuleBuilder moduleBuilder = new ModuleBuilder()
      .id("$buildName-$buildNumber")

    ArtifactBuilder artifactBuilder = new ArtifactBuilder('artifactBuilder')
    artifacts.each { Map<String, String> metadata ->
      patchArtifactProperties(metadata)
      moduleBuilder.addArtifact(
        artifactBuilder.name(metadata.name)
          .type(fileExtension(metadata.name))
          .md5(metadata.actual_md5)
          .sha1(metadata.actual_sha1)
          .build()
      )
    }
    Build build = builder.addModule(moduleBuilder.build()).agent(new Agent(name)).build()

    if (dryRun) {
      println 'Build Info JSON:'
      println JsonOutput.prettyPrint(jsonOutput.toJson(build))
      println '=== end build info ==='
    } else {
      def url = baseUrl.newBuilder('api/build').build()
      httpRequest(new Request.Builder().url(url)
        .put(RequestBody.create(jsonOutput.toJson(build), JSON))
        .build())
      println "Build Info submitted"
    }
  }

  void patchArtifactProperties(artifact) {
    def props = loadArtifactProperties(artifact)
    def buildinfo = mergeProperties(props, [
      'build.name': [buildName] as Set,
      'build.number': [buildNumber] as Set,
    ])
    def patch = ['props': buildinfo]

    if (dryRun) {
      println "=== ${artifact.name} patch ==="
      println JsonOutput.prettyPrint(jsonOutput.toJson(patch))
      println '=== end File Item patch ==='
    } else {
      def url = baseUrl.newBuilder('api/metadata')
        .addPathSegments(artifact.repo)
        .addPathSegments(artifact.path)
        .addPathSegments(artifact.name)
        .build()
      httpRequest(new Request.Builder().url(url)
        .patch(RequestBody.create(jsonOutput.toJson(patch), JSON))
        .build())
    }
  }

  def mergeProperties(Map ori, Map patch) {
    patch.each { k, v ->
      def value = ori[(k)]
      if (value) patch.merge(k, value, { current, values -> current + values })
    }
    return patch
  }

  Map loadArtifactProperties(artifact) {
    def url = baseUrl.newBuilder('api/storage')
      .addPathSegments(artifact.repo)
      .addPathSegments(artifact.path)
      .addPathSegments(artifact.name)
      .addQueryParameter('properties', null)
      .build()

    def result = safeRequest(new Request.Builder().url(url).build())
    if (result.errors) {
      return [:]
    }
    return result['properties']
  }

  void testConnection() throws IOException {
    def url = baseUrl.newBuilder('api/system/version').build()
    httpRequest(new Request.Builder().url(url).build())
  }

  def safeRequest(Request request) { return httpRequest(request, true) }
  def httpRequest(Request request, boolean safeCall = false) throws IOException {
    http.newCall(request).execute().withCloseable { Response response ->
      if (response.isSuccessful() || safeCall) {
        String body = response.body().string()
        return body ? parser.parseText(body) : body
      }
      throw new IOException("$response")
    }
  }
}
