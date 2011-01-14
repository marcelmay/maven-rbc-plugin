package net.metacube.maven.rscbundlecheck

import org.codehaus.gmaven.mojo.GroovyMojo

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
  @Delegate ResourceBundleMojoDelegate resourceBundleMojoDelegate = new ResourceBundleMojoDelegate()

  public void execute() {
    processCheckResults(executeChecks())
  }

  def processCheckResults(Map<Bundle, List<Issue>> pBundleIssues) {
    log.info 'Found ' + pBundleIssues.collect {it.value}.flatten().size + ' issue(s) in ' + pBundleIssues.size() + ' bundle(s)'
    pBundleIssues.each {
      if (it.value.isEmpty()) {
        if (log.isDebugEnabled()) {
          log.debug 'Bundle ' + it.key.basename + ' is fine'
        }
      }
      else {
        log.warn 'Bundle ' + it.key.basename + ' has ' + it.value.size() + 'issue(s):'
        it.value.each {issue -> log.warn(' - ' + issue.description)
        }
      }
    }
  }

}
