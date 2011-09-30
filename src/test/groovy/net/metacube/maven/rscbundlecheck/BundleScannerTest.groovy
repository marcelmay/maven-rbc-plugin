package net.metacube.maven.rscbundlecheck

import org.apache.maven.model.FileSet
import org.testng.annotations.Test

class BundleScannerTest {
  @Test
  void testScan() {
    def fs = new FileSet()
    fs.setDirectory '../src/it/report-goal/src/main/resources'
    fs.addInclude '**/*.properties'

    def scanner = new BundleScanner()
    scanner.scan fs

    // Bundles
    assert scanner.size() == 6
    assert scanner.bundles.size() == 6
    assert scanner.bundles.basename == [
            'net/metacube/maven/maven-rbc-plugin/test/test1',
            'net/metacube/maven/maven-rbc-plugin/test/test2',
            'net/metacube/maven/maven-rbc-plugin/foo/more',
            'net/metacube/maven/maven-rbc-plugin/foo/bar/some',
            'net/metacube/maven/maven-rbc-plugin/test3',
            'net/metacube/maven/maven-rbc-plugin/test4']

    // Locales
    assert scanner.locales.size() == 3
    assert scanner.locales.containsAll(['de','fr','it'])

    assert scanner.bundles.locales.flatten().unique() == ['de', 'it', 'fr']
  }
}
