package net.metacube.maven.rscbundlecheck

import org.apache.maven.model.FileSet
import org.codehaus.plexus.util.FileUtils

/**
 * Scans for bundles.
 *
 * @author mm
 */

class BundleScanner {
  Map<String, Bundle> bundles = new HashMap()
  Set locales = new HashSet()
  String dir

  Collection<Bundle> scan(FileSet pFileSet) {
    // Build a scanner
    def dirFile = new File(pFileSet.directory)
    dir = dirFile.getAbsolutePath()
    if (dirFile.exists() && dirFile.isDirectory()) {
      FileUtils.getFiles(dirFile, pFileSet.includes.join(','), pFileSet.excludes.join(',')).each { processFile(it) }
    }
    return bundles.values()
  }

  void processFile(File pFile) {
    String rf = pFile.getAbsolutePath().substring(dir.length() + 1)
    String fp = FileUtils.removeExtension(rf)
    String nfp = fp
    String fn = FileUtils.removeExtension(pFile.getName())
    int i = fn.lastIndexOf('_')
    if (i == fn.length() - 3) {
      nfp = fp.substring(0, fp.length() - 3) // Strip locale plus underscore
      locales.add(fn.substring(fn.length() - 2)) // Extract locale
    }
    Bundle b = bundles.get(nfp)
    if (null == b) {
      b = new Bundle(basename: nfp)
      bundles.put(nfp, b)
    }
    b.files.add(rf)
  }

  Collection<Bundle> getBundles() {
    return bundles.values()
  }

  Bundle getBundle(String pBundleName) {
    return bundles.get(pBundleName)
  }

  int size() {
    return bundles.size()
  }
}
