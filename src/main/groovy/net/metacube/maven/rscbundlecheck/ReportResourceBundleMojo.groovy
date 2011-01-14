package net.metacube.maven.rscbundlecheck

import org.apache.maven.doxia.siterenderer.Renderer
import org.apache.maven.project.MavenProject
import org.apache.maven.reporting.AbstractMavenReport

/**
 * Creates a report for the check results.
 *
 * @author mm
 * @goal rbc-report
 * @requiresDependencyResolution compile
 * @threadSafe
 * @phase site
 */
public class ReportResourceBundleMojo extends AbstractMavenReport {
  @Delegate ResourceBundleMojoDelegate resourceBundleMojoDelegate = new ResourceBundleMojoDelegate()

  /**
   * @component
   * @required
   * @readonly
   */
  private Renderer siteRenderer
  /**
   * The directory where the report will be generated
   *
   * @parameter expression="${project.reporting.outputDirectory}"
   * @required
   * @readonly
   */
  private File outputDirectory

  protected void executeReport(Locale pLocale) {
    Map<Bundle, List<Issue>> bundleIssues = new TreeMap(executeChecks())
    ResourceBundle res = getBundle(pLocale)

    doHeading(res)

    doSummarySection(bundleIssues, res)

    doBundleSummarySection(bundleIssues, res)

    doBundleDetailSection(bundleIssues, res)

    doFooter()
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
    sink.text pResBundle.getString('report.rbc.bundle.detail.entry')+' '+pBundle.basename
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
        sink.link('#'+anchorName(b.basename))
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
    sink.text 'maven-rbc-plugin'
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
    return outputDirectory.getAbsolutePath()
  }

  protected Renderer getSiteRenderer() {
    return siteRenderer
  }

  public String getOutputName() {
    return 'rbc'
  }

  public String getName(Locale locale) {
    return getBundle(locale).getString('report.rbc.name')
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
}
