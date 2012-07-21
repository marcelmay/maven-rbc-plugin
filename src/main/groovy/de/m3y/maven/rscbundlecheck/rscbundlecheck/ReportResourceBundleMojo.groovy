package de.m3y.maven.rscbundlecheck.rscbundlecheck

import de.m3y.maven.rscbundlecheck.rscbundlecheck.checks.IncompleteResourceBundleCheck
import de.m3y.maven.rscbundlecheck.rscbundlecheck.checks.RscBundleCheckWrapper
import org.apache.maven.doxia.siterenderer.Renderer
import org.apache.maven.model.FileSet
import org.apache.maven.project.MavenProject
import org.apache.maven.reporting.AbstractMavenReport
import org.apache.maven.plugin.MojoExecutionException

/**
 * Creates a report for the check results.
 *
 * @author mm
 * @goal report
 * @requiresDependencyResolution compile
 * @threadSafe
 * @phase site
 */
public class ReportResourceBundleMojo extends AbstractMavenReport {

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
  /**
   * Name of the report, as listed in the project reports menu.
   * Useful when having multiple reports.
   *
   * @parameter default-value=''
   */
  private String name

  /**
   * @component
   * @required
   * @readonly
   */
  private Renderer siteRenderer

  protected void executeReport(Locale pLocale) {
    Map<Bundle, List<Issue>> bundleIssues = new TreeMap(executeChecks())
    int numberOfIssues = bundleIssues.collect {it.value}.flatten().size
    log.info 'Found ' + numberOfIssues + ' issue(s) in ' + bundleIssues.size() + ' bundle(s)'
    bundleIssues.each {
      if (it.value.isEmpty()) {
        if (log.isDebugEnabled()) {
          log.debug 'Bundle ' + it.key.basename + ' is fine'
        }
      }
      else {
        log.warn 'Bundle ' + it.key.basename + ' has ' + it.value.size() + ' issue(s):'
        it.value.each {issue -> log.warn(' - ' + issue.description)
        }
      }
    }

    ResourceBundle res = getBundle(pLocale)

    doHeading(res)

    doSummarySection(bundleIssues, res)

    doBundleSummarySection(bundleIssues, res)

    doBundleDetailSection(bundleIssues, res)

    doFooter()

    if(failOnError && numberOfIssues>0) {
      throw new MojoExecutionException(
              'Found '+numberOfIssues+' issue(s). Change \'failOnError\' plugin configuration, or fix reported issues.')
    }
  }

  def doBundleDetailSection(Map<Bundle, List<Issue>> pBundleIssues, ResourceBundle pResBundle) {
    sink.section1()
    sink.sectionTitle1()
    sink.text(pResBundle.getString('report.rbc.bundle.detail.title'))
    sink.sectionTitle1_()

    sink.paragraph()
    sink.text pResBundle.getString('report.rbc.bundle.detail.info')
    sink.paragraph_()

    pBundleIssues.each {Bundle b, List<Issue> issues ->
      if (!issues.isEmpty()) {
        doBundleDetails(b, issues, pResBundle)
      }
    }
    sink.section1_()
  }

  def doBundleDetails(Bundle pBundle, List<Issue> pIssues, ResourceBundle pResBundle) {
    sink.section2()
    sink.sectionTitle2()
    sink.anchor anchorName(pBundle.basename)
    sink.anchor_()
    sink.text(pResBundle.getString('report.rbc.bundle.detail.entry') + ' ' + pBundle.basename
        + ' (' + pIssues.size() + ' '
        + (pIssues.size()>1 ? pResBundle.getString('report.rbc.bundle.detail.issues') : pResBundle.getString('report.rbc.bundle.detail.issue') )
        + ')')
    sink.sectionTitle2_()

    sink.table()
    sink.tableRow()
    tableHeaderCell pResBundle.getString('report.rbc.bundle.detail.check'), '10%'
    tableHeaderCell pResBundle.getString('report.rbc.bundle.detail.description'), '25%'
    tableHeaderCell pResBundle.getString('report.rbc.bundle.detail.source'), '50%'
    tableHeaderCell pResBundle.getString('report.rbc.bundle.detail.source.idx'), '15%'
    sink.tableRow_()

    pIssues.each {Issue issue ->
      sink.tableRow()
      tableCell issue.check
      tableCell issue.description
      tableCell issue.source
      tableCell issue.sourceIdx
      sink.tableRow_()
    }

    sink.table_()

    sink.section2_()
  }

  def doBundleSummarySection(Map<Bundle, List<Issue>> pBundleIssues, ResourceBundle pResBundle) {
    sink.section1()
    sink.sectionTitle1()
    sink.text(pResBundle.getString('report.rbc.bundle.summay.title'))
    sink.sectionTitle1_()

    sink.paragraph()
    sink.table()

    sink.tableRow()
    tableHeaderCell pResBundle.getString('report.rbc.bundle.summary.bundles'), '66%'
    tableHeaderCell pResBundle.getString('report.rbc.bundle.summary.locales'), '22%'
    tableHeaderCell pResBundle.getString('report.rbc.bundle.summary.issues'), '12%'
    sink.tableRow_()

    pBundleIssues.each {Bundle b, List<Issue> issues ->
      sink.tableRow()

      if (issues.isEmpty()) {
        tableCell b.basename
      } else {
        sink.tableCell()
        sink.link('#' + anchorName(b.basename))
        sink.text b.basename
        sink.link_()
        sink.tableCell_()
      }

      tableCell b.locales.join(', ')
      tableCell issues.size().toString()

      sink.tableRow_()
    }

    sink.table_()
    sink.paragraph_()
  }

  def anchorName(String pName) {
    pName.replace(File.separatorChar as char, '_' as char)
  }

  def doSummarySection(Map<Bundle, List<Issue>> pBundleIssues, ResourceBundle pResBundle) {
    sink.section1()
    sink.sectionTitle1()
    sink.text(pResBundle.getString('report.rbc.summay.title'))
    sink.sectionTitle1_()

    sink.paragraph()
    sink.table()

    sink.tableRow()
    tableHeaderCell pResBundle.getString('report.rbc.summary.bundles'), '66%'
    tableHeaderCell pResBundle.getString('report.rbc.summary.locales'), '22%'
    tableHeaderCell pResBundle.getString('report.rbc.summary.issues'), '12%'
    sink.tableRow_()

    sink.tableRow()
    tableCell Integer.toString(pBundleIssues.size())
    tableCell pBundleIssues.keySet().collect {it.locales}.flatten().unique().join(', ')
    tableCell pBundleIssues.values().flatten().size().toString()
    sink.tableRow_()

    sink.table_()
    sink.paragraph_()
  }

  def doFooter() {
    sink.body_()
    sink.flush()
    sink.close()
  }


  def doHeading(ResourceBundle pResBundle) {
    sink.head()
    sink.title()
    sink.text(pResBundle.getString('report.rbc.header'))
    sink.title_()
    sink.head_()

    sink.body()

    sink.section1()
    sink.sectionTitle1()
    sink.text(pResBundle.getString('report.rbc.header'))
    sink.sectionTitle1_()

    sink.paragraph()
    sink.text(pResBundle.getString('report.rbc.link') + ' ')
    sink.link('http://labs.consol.de/projects/maven/maven-rbc-plugin/')
    sink.text 'rbc-maven-plugin'
    sink.link_()
    sink.text('.')

    sink.paragraph_()
    sink.section1_()
  }

  def tableHeaderCell(String pText, String pWidth = null) {
    pWidth ? sink.tableHeaderCell(pWidth) : sink.tableHeaderCell()
    sink.text pText
    sink.tableHeaderCell_()
  }

  def tableCell(String pText) {
    sink.tableCell()
    sink.text pText
    sink.tableCell_()
  }

  protected String getOutputDirectory() {
    return getReportOutputDirectory().getAbsolutePath()
  }

  protected Renderer getSiteRenderer() {
    return siteRenderer
  }

  public String getOutputName() {
    return name?.length()? 'rbc-'+name.toLowerCase().replaceAll('[^a-zA-Z0-9]','_') : 'rbc'
  }

  public String getName(Locale locale) {
    return name?.length()>0? name : getBundle(locale).getString('report.rbc.name')
  }

  public String getDescription(Locale locale) {
    return getBundle(locale).getString('report.rbc.description')
  }

  protected MavenProject getProject() {
    return project
  }

  private ResourceBundle getBundle(Locale locale) {
    return ResourceBundle.getBundle('rbc-report', locale, this.getClass().getClassLoader())
  }

  Map<Bundle, List<Issue>> executeChecks() {
    // Scan for bundles
    BundleScanner bScanner = new BundleScanner(log: log, rootDir: project.basedir)
    bScanner.scan(fileset)
    log.info "Found ${bScanner.size()} bundles"
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
