package net.metacube.maven.rscbundlecheck

import net.metacube.maven.rscbundlecheck.checks.AbstractBundleCheck
import org.apache.maven.plugin.logging.Log

/**
 * Checks a bundle
 *
 * @author mm
 */

public class BundleChecker {
  Log log
  List<AbstractBundleCheck> checks = new ArrayList<AbstractBundleCheck>()

  void add(AbstractBundleCheck pCheck) {
    checks.add(pCheck)
  }

  List<Issue> check(Bundle pBundle, List<Issue> pIssues) {
    for (AbstractBundleCheck check: checks) {
      log.info('Running check ' + check.name)
      pIssues.addAll(check.check(pBundle))
    }
    return pIssues
  }
}