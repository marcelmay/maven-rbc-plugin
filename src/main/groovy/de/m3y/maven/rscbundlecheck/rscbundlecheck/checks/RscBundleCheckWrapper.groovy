package de.m3y.maven.rscbundlecheck.rscbundlecheck.checks

import de.m3y.maven.rscbundlecheck.rscbundlecheck.Bundle
import de.m3y.maven.rscbundlecheck.rscbundlecheck.Issue
import org.apache.maven.model.FileSet
import org.apache.maven.plugin.MojoExecutionException
import org.dyndns.fichtner.rsccheck.ant.AntRscCheckContext
import org.dyndns.fichtner.rsccheck.ant.types.Check
import org.dyndns.fichtner.rsccheck.ant.types.Check.CheckName
import org.dyndns.fichtner.rsccheck.ant.types.CheckHolder
import org.dyndns.fichtner.rsccheck.ant.types.Checks
import org.dyndns.fichtner.rsccheck.engine.*

/**
 * Checks using RSCBC.
 *
 * @author mm
 *
 * Note: A lot of groovy-unlike-code here was extracted from org.dyndns.fichtner.rsccheck.ant.RscBundleCheckTask
 *       So its pretty ugly, hacked code.
 */
public class RscBundleCheckWrapper extends AbstractBundleCheck {
  FileSet fileset
  /** Location of fileset directory, typically the project or module location */
  File location
  /** sort result (line numbers)      */
  boolean sortResult = true
  /** Flag if this task should be verbose or not      */
  boolean verbose
  /** Flag if this task stop the build process if an error was found      */
  boolean failOnError = true
  List enabledChecks
  List disabledChecks

  private final List<Checks> checks = new ArrayList<Checks>()

  def RscBundleCheckWrapper() {
    name = 'RSC Bundle Check'
  }

  RscBundleCheckWrapper init() {
    checks.clear()

    // Copy-and-modify fileset for location, if not absolut
    if (fileset.directory.charAt(0)!=File.separatorChar) {
      FileSet orgFileSet = fileset
      fileset = new FileSet()
      fileset.directory = new File(location, orgFileSet.directory)
      fileset.setIncludes(orgFileSet.getIncludes())
      fileset.setExcludes(orgFileSet.getExcludes())
    }

    checks.add(createChecks())
    log.info('Enabled checks: ' + checks)
    this
  }

  List<Issue> check(Bundle pBundle) {
    if (pBundle.files.isEmpty()) {
      throw new MojoExecutionException('No resourcebundle matches the pattern - bundle ' + pBundle + ' is empty')
    }
    final List<RscBundleReader> readers = pBundle.files.collect {
      new RscBundleReaderFile(new File(fileset.directory + File.separator + it))
    }
    Context context = new Context(
            rscBundleReaders: readers,
            visitors: createVisitors(AntRscCheckContext.getVisitorFactory()))
    log.debug('Enabled checks: ' + context.visitors)
    final List<ErrorEntry> result = Collections.emptyList()
    try {
      result = new RscBundleCheck(context).execute()
    } catch (final Exception e) {
      failOnError ? {throw new MojoExecutionException(e.getMessage()) } : log.warn(e.getMessage())
    }

    if (!result.isEmpty()) {
      if(sortResult) {
        result = result.sort {a, b ->
          a.getEntry() == null ? (b.getEntry() == null ? 0 : -1) : a.getEntry().getLineOfKey() - (b.getEntry() == null ? 0 : b.getEntry().getLineOfKey())
        }
      }
      if (failOnError) {
        throw new MojoExecutionException('The check found ' + result.size() + ' errors in the resource files')
      } else {
        log.warn 'The check found ' + result.size() + ' error(s) in the resource file(s)'
      }
    }
    result.collect { ErrorEntry e ->
      String source = e.getBundleReader().getIdentifier()
      String location = pBundle.location
      if (!source?.isEmpty() && !location?.isEmpty() && source.startsWith(location)) {
        source = source.substring(location.size()+1)
      }
      new Issue(check: e.getVisitor().getName(),
                description: e.getMessage(),
                source: source,
                sourceIdx: e.getEntry())}
  }

  private Collection<Visitor> createVisitors(VisitorFactory visitorFactory) {
    if (checks.isEmpty()) {
      final Checks defVisitors = new Checks()
      visitorFactory.getDefaultVisitors().each {
        defVisitors.addInclude(new Check(checkName: new CheckName(it.getName())))
      }
      checks.add(defVisitors)
    }
    checks.collect{ extractVisitorsFromSet(it, visitorFactory) }.flatten()
  }

  private Collection<Visitor> extractVisitorsFromSet(final Checks visitorSet,
                                                     final VisitorFactory visitorFactory) {
    final List<Visitor> result = new ArrayList<Visitor>()
    for (final CheckHolder checkHolder: visitorSet.getData()) {
      checkHolder.getVisitors(result)
    }
    result
  }

  Checks createChecks() {
    final Checks defVisitors = new Checks()
    enabledChecks?.each {
      defVisitors.addInclude new Check(checkName: new CheckName(it))
    }
    disabledChecks?.each {
      defVisitors.addExclude new Check(checkName: new CheckName(it))
    }
    defVisitors
  }
}