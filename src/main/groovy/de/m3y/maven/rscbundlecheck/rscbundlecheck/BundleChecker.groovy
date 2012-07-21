package de.m3y.maven.rscbundlecheck.rscbundlecheck

import de.m3y.maven.rscbundlecheck.rscbundlecheck.checks.AbstractBundleCheck
import org.apache.maven.plugin.logging.Log

/**
 * Performs configured checks on a bundle.
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
      log.info('Running check ' + check.name + ' on bundle ' + pBundle.basename)
      pIssues.addAll(check.check(pBundle))
    }
    return pIssues
  }
}