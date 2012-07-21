package de.m3y.maven.rscbundlecheck.rscbundlecheck

import de.m3y.maven.rscbundlecheck.rscbundlecheck.checks.IncompleteResourceBundleCheck
import de.m3y.maven.rscbundlecheck.rscbundlecheck.checks.RscBundleCheckWrapper
import org.apache.maven.model.FileSet
import org.apache.maven.project.MavenProject
import org.codehaus.gmaven.mojo.GroovyMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.logging.Log

/**
 * Runs the bundle checks.
 *
 * Check out <a href="http://rscbundlecheck.sourceforge.net/">rscbundlecheck</a>.
 *
 * @author mm
 * @goal check
 * @requiresDependencyResolution compile
 * @threadSafe
 */
public class CheckResourceBundleMojo extends GroovyMojo {
  /**
   * The current project.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project

  /* === Config === */
  /**
   * Sort results when reporting.
   *
   * @parameter default-value='true'
   */
  private boolean sortResult
  /**
   * Be verbose.
   *
   * Automatically enabled when running mvn in debug mode.
   *
   * @parameter default-value='false'
   */
  private boolean verbose
  /**
   * Fail on error, such as missing resource key.
   *
   * @parameter default-value='false'
   */
  private boolean failOnError
  /**
   * Autodetects locales and warns in a resource file for a locale is missing.
   *
   * @parameter default-value='true'
   */
  private boolean warnOnIncompleteBundle
  /**
   * Fileset for scanning.
   *
   * Example:
   * <pre><code>
   * &lt;fileset&gt;
   *   &lt;directory&gt;src/main/resources&lt;/directory&gt;
   *   &lt;includes&gt;
   *     &lt;include&gt;de/m3y/example/project/&#42;&#42;/&#42;.properties&lt;/include&gt;
   *   &lt;/includes&gt;
   * &lt;/fileset&gt;
   * </code></pre>
   * Note: Same rules as for includes apply for excludes
   *
   * @parameter
   */
  private FileSet fileset = new FileSet(directory: 'src/main/resources', includes: ['**/*.properties'])
  /**
   * Checks enabled.
   *
   * By default, all checks are enabled.
   *
   * <pre><code>
   * &lt;enabledChecks&gt;
   *   &lt;param&gt;&#42;&lt;/param&gt;
   * &lt;/enabledChecks&gt;
   * &lt;disabledChecks&gt;
   *   &lt;!-- param&gt;allowed char key check&lt;/param --&gt;
   *   &lt;!-- param&gt;unicode check&lt;/param --&gt;
   *   &lt;param&gt;cross bundle check&lt;/param&gt;
   *   &lt;!-- param&gt;duplicate key check&lt;/param --&gt;
   *   &lt;!-- param&gt;empty key check&lt;/param --&gt;
   *   &lt;!-- param&gt;empty value check&lt;/param --&gt;
   *   &lt;!-- param&gt;messageformat check&lt;/param --&gt;
   *   &lt;!-- param&gt;invalid char check&lt;/param --&gt;
   *   &lt;!-- param&gt;line end check&lt;/param --&gt;
   *   &lt;!-- param&gt;upper lower check&lt;/param --&gt;
   *   &lt;!-- param&gt;invalid char check&lt;/param --&gt;
   * &lt;/disabledChecks&gt;
   * </code></pre>
   *
   * @parameter
   */
  private List enabledChecks = ['*']
  /**
   * Checks disabled.
   *
   * See         {@link #enabledChecks}
   * @parameter
   */
  private List disabledChecks

  public void execute() {
    processCheckResults(executeChecks())
  }

  def processCheckResults(Map<Bundle, List<Issue>> pBundleIssues) {
    int numberOfIssues = pBundleIssues.collect {it.value}.flatten().size
    printIssues(log, pBundleIssues, numberOfIssues)
    handleFailOnError(failOnError, numberOfIssues)
  }

  def static printIssues(Log pLog, Map<Bundle, List<Issue>> pBundleIssues, int pNumberOfIssues) {
    pLog.info 'Found ' + pNumberOfIssues + ' issue(s) in ' + pBundleIssues.size() + ' bundle(s)'
    pBundleIssues.each {
      if (it.value.isEmpty()) {
        if (pLog.isDebugEnabled()) {
          pLog.debug 'Bundle ' + it.key.basename + ' is fine'
        }
      }
      else {
        pLog.warn 'Bundle ' + it.key.basename + ' has ' + it.value.size() + ' issue(s):'
        it.value.each {issue -> pLog.warn(' - ' + issue.description)
        }
      }
    }

  }
  def static handleFailOnError(boolean pFailOnError, int pNumberOfIssues) {
    if(pFailOnError && pNumberOfIssues>0) {
      throw new MojoExecutionException(
              'Found '+pNumberOfIssues+' issue(s). Change \'failOnError\' plugin configuration, or fix reported issues.')
    }
  }

  Map<Bundle, List<Issue>> executeChecks() {
    // Scan for bundles
    BundleScanner bScanner = new BundleScanner(log: log, rootDir: project.basedir)
    bScanner.scan(fileset)
    log.info "Found ${bScanner.size()} bundle(s)"
    if (log.isDebugEnabled()) {
      bScanner.getBundles().each { log.debug it.toString() }
    }

    // Build check configuration
    BundleChecker checker = buildCheckerForConfiguration(bScanner)

    // Run checks
    Map<Bundle, List<Issue>> result = new HashMap()
    bScanner.getBundles().each {
      result.put(it, checker.check(it, new ArrayList<Issue>()))
    }
    result
  }

  BundleChecker buildCheckerForConfiguration(BundleScanner pScanner) {
    BundleChecker bc = new BundleChecker(log: log)
    bc.add(new RscBundleCheckWrapper(log: log,
            fileset: fileset,
            location: pScanner.rootDir,
            sortResult: sortResult,
            verbose: verbose,
            failOnError: false, // Handle failure at mojo level
            enabledChecks: enabledChecks,
            disabledChecks: disabledChecks).init())
    if (warnOnIncompleteBundle) {
      bc.add(new IncompleteResourceBundleCheck(log: log, locales: pScanner.locales))
    }
    return bc
  }
}
