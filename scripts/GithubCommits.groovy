/*
 * example execution:
 *
 * groovy -cp scripts/groovy/classes \
 *  -DGITHUB_API_TOKEN=<your token> \
 *  -DGITHUB_PROJECTS_CSV_PATH=../../github-projects.csv \
 *  -DSTART_DATE=2018-01-17T18:54:00 \
 *  -DEND_DATE=2018-01-18T18:54:00 \
 *  -DMAX_NUMBER_OF_COMMITS=100 \
 *  scripts/groovy/GithubCommits.groovy
 */
//import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat

class GithubCommits {

  static String GITHUB_API_TOKEN      = System.getProperty("GITHUB_API_TOKEN")
  static String START_DATE            = System.getProperty("START_DATE")
  static String END_DATE              = System.getProperty("END_DATE")
  static String MAX_NUMBER_OF_COMMITS = System.getProperty("MAX_NUMBER_OF_COMMITS")

  public static void commits(String startDate, String endDate, Integer maxNumberOfCommits) {
    println ""
    println "Start Date:            ${startDate}"
    println "End Date:              ${endDate}"
    println "Max Number of Commits: ${maxNumberOfCommits}\n"

    def noChangesList = []

    // Stat variables
    def totalCommits = 0
    def numRespositoriesChanged = 0
    def numMergedPullRequests = 0

    List<GithubProject> githubProjects = GithubProject.parseGithubProjectsCsv();
    for (GithubProject githubProject : githubProjects) {

      if ( !githubProject.getProjectType().equals("RELEASE") ) {
        continue;
      }

      def organization = githubProject.getOrg()
      def repository = githubProject.getName()
      def branch = githubProject.getBranch()
      def auth = GITHUB_API_TOKEN

      def apiURL = "https://api.github.com/repos/${organization}/${repository}/commits?sha=${branch}&since=${startDate}&until=${endDate}&per_page=${maxNumberOfCommits}"
      //println apiURL

      JsonSlurper json = new JsonSlurper()
      def commits = json.parse(apiURL.toURL().newReader(requestProperties: ["Authorization": "token ${auth}".toString(), "Accept": "application/json"]))
      if (commits) {
        def num = 1
        numRespositoriesChanged++
        println "======== ${repository} - ${branch} ======="
        commits.each {
          SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
          format.setTimeZone(TimeZone.getTimeZone("GMT"));
          Date date = format.parse(it.commit.author.date);
          def timestamp = date.format("MMM dd, yyyy HH:mm z")

          println "<--- ${num} --->"
          println "${it.commit.author.name} <${it.commit.author.email}> - ${timestamp}"
          println "${it.html_url}"
          println ""
          println "${it.commit.message}"
          println ""
          num++
          if (it.commit.message.contains("Merge pull request")) {
            numMergedPullRequests++
          }
        }
        totalCommits += num
      }
      else {
        noChangesList.add("${repository} - ${branch}")
      }
    }

    println "Number of repositories with commits: ${numRespositoriesChanged}"
    println "Number of commits across all repositories: ${totalCommits}"
    println "Number of merged pull requests: ${numMergedPullRequests}"
    println ""
    println "Repositories with no changes: ${noChangesList.size()}"
    noChangesList.each {
      println "  ${it}"
    }
  }

  public static void main( String[] args )
  {
    if (!GITHUB_API_TOKEN || (GITHUB_API_TOKEN.length() == 0)) {
      println "ERROR: You must specify a GITHUB_API_TOKEN."
      return
    }

    def numberOfCommits = 100
    if (MAX_NUMBER_OF_COMMITS) {
      numberOfCommits = Integer.parseInt(MAX_NUMBER_OF_COMMITS)
    }

    // If no start date was specified, use yesterday
    String startDate = ""
    if (START_DATE) {
      startDate = START_DATE
    }
    else {
      def today = new Date()
      def yesterday = today - 1
      startDate = yesterday.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    }

    // If no start date was specified, use yesterday
    String endDate = ""
    if (END_DATE) {
      endDate = END_DATE;
    }
    else {
      def today = new Date()
      endDate = today.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    }

    commits(startDate, endDate, numberOfCommits)
  }
}
