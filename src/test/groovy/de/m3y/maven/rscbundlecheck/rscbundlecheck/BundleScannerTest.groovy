package de.m3y.maven.rscbundlecheck.rscbundlecheck

import org.apache.maven.model.FileSet
import org.testng.annotations.Test

class BundleScannerTest {
  @Test
  void testScan() {
    def fs = new FileSet()
    fs.setDirectory '../src/it/report-goal/src/main/resources'
    fs.addInclude '**/*.properties'

    def scanner = new BundleScanner(rootDir: new File('.'))
    scanner.scan fs

    // Bundles
    assert scanner.size() == 6
    assert scanner.bundles.size() == 6
    assert scanner.bundles.basename.sort() == [
            'de/m3y/maven/rbc-maven-plugin/test/test1',
            'de/m3y/maven/rbc-maven-plugin/test/test2',
            'de/m3y/maven/rbc-maven-plugin/foo/more',
            'de/m3y/maven/rbc-maven-plugin/foo/bar/some',
            'de/m3y/maven/rbc-maven-plugin/test3',
            'de/m3y/maven/rbc-maven-plugin/test4'].sort()

    // Locales
    assert scanner.locales.size() == 3
    assert scanner.locales.containsAll(['de','fr','it'])

    assert scanner.bundles.locales.flatten().unique() == ['de', 'it', 'fr']
  }
}
