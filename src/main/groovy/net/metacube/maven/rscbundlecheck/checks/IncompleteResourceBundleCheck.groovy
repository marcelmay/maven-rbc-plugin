package net.metacube.maven.rscbundlecheck.checks

import net.metacube.maven.rscbundlecheck.Bundle
import net.metacube.maven.rscbundlecheck.Issue

/**
 * Checks if bundle misses any resource file for given locales.
 *
 * @author mm
 */
class IncompleteResourceBundleCheck extends AbstractBundleCheck {

  Set<Locale> locales

  def IncompleteResourceBundleCheck() {
    name = 'Missing resource files check'
  }

  List<Issue> check(Bundle pBundle) {
    List<Issue> issues = new ArrayList<Issue>()
    locales.each {l ->
      if (!pBundle.files.find {
        def m = it =~ '.*_(..)\\.properties$'
        m.matches() && (l == m[0][1])
      }) issues.add(new Issue(
              check: name,
              description: 'Missing resource for bundle ' + pBundle.basename + ' and locale ' + l))
      if (!pBundle.files.find {
        def m = it =~ '.*_(..)\\.properties$'
        !m.matches()
      }) issues.add(new Issue(
              check: name,
              description: 'Missing resource for bundle ' + pBundle.basename + ' and default locale'))
    }
    return issues
  }
}
