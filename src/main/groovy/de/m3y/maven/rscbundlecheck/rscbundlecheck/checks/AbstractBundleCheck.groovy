package de.m3y.maven.rscbundlecheck.rscbundlecheck.checks

import de.m3y.maven.rscbundlecheck.rscbundlecheck.Bundle
import de.m3y.maven.rscbundlecheck.rscbundlecheck.Issue
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
