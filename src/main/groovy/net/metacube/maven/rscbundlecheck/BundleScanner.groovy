package net.metacube.maven.rscbundlecheck

import org.apache.maven.model.FileSet
import org.codehaus.plexus.util.FileUtils
import java.util.regex.Pattern
import java.util.regex.Matcher

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

  def static final LANG_COUNTRY_PATTERN = Pattern.compile('.*_(([\\p{Alpha}]{2})_([\\p{Alpha}]{2}))$')
  def static final LANG_PATTERN = Pattern.compile('.*_([\\p{Alpha}]{2})$')

  void processFile(File pFile) {
    String relativeResourcePath = pFile.getAbsolutePath().substring(dir.length() + 1)
    String fp = FileUtils.removeExtension(relativeResourcePath)
    String fn = FileUtils.removeExtension(pFile.getName())

    String locale
    Matcher matcher = LANG_COUNTRY_PATTERN.matcher(fp)
    if(matcher.matches()) {
      locale = matcher.group(3)
    } else {
      matcher = LANG_PATTERN.matcher(fp)
      if(matcher.matches()) {
        locale = matcher.group(1)
      }
    }

    if(null!=locale) {
      locales.add(locale)
      fn = fn.substring(0,fn.length()-locale.length()-1)
      fp = fp.substring(0,fp.length()-locale.length()-1)
    }

    Bundle b = bundles.get(fp)
    if (null == b) {
      b = new Bundle(basename: fp)
      bundles.put(fp, b)
    }
    if(null!=locale) {
      b.locales.add(locale)
    }
    b.files.add(relativeResourcePath)
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
