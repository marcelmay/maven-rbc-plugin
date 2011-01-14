package net.metacube.maven.rscbundlecheck.checks

import net.metacube.maven.rscbundlecheck.Bundle
import net.metacube.maven.rscbundlecheck.Issue
import org.apache.maven.plugin.logging.Log

/**
 * Base for all checks.
 *
 * @author mm
 */
abstract class AbstractBundleCheck {
  Log log
  String name
  abstract public List<Issue> check(Bundle pBundle)
}
