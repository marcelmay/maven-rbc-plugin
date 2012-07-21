package de.m3y.maven.rscbundlecheck.rscbundlecheck

import org.apache.maven.model.FileSet
import org.codehaus.plexus.util.FileUtils
import java.util.regex.Pattern
import java.util.regex.Matcher
import org.apache.maven.plugin.logging.Log

/**
 * Scans for bundles.
 *
 * @author mm
 */

class BundleScanner {
  Map<String, Bundle> bundles = new HashMap()
  Set locales = new HashSet()
  String dir
  Log log
  File rootDir

  Collection<Bundle> scan(FileSet pFileSet) {
    // Build a scanner
    // Prefix rootDir only if not absolute
    def dirFile = pFileSet.directory.charAt(0)==File.separatorChar ? new File(pFileSet.directory) : new File(rootDir, pFileSet.directory)
    dir = dirFile.getAbsolutePath()
    if(log?.isDebugEnabled()) {
      log.debug("Scanning directory "+dir+' using includes=['+pFileSet.includes.join(', ')+'] and excludes=['+pFileSet.excludes.join(', ')+']')
    }
    if (dirFile.exists() && dirFile.isDirectory()) {
      FileUtils.getFiles(dirFile, pFileSet.includes.join(','), pFileSet.excludes.join(',')).each { processFile(it) }
    } else {
      log?.warn('Directory '+dirFile+' does not exist or is no directory')
    }
    return bundles.values()
  }

  def static final LANG_COUNTRY_PATTERN = Pattern.compile('.*_(([\\p{Alpha}]{2})_([\\p{Alpha}]{2}))$')
  def static final LANG_PATTERN = Pattern.compile('.*_([\\p{Alpha}]{2})$')

  void processFile(File pFile) {
    String relativeResourcePath = pFile.getAbsolutePath().substring(dir.length() + 1)
    String fp = FileUtils.removeExtension(relativeResourcePath)

    String locale = null
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
      fp = fp.substring(0,fp.length()-locale.length()-1)
    }

    Bundle b = bundles.get(fp)
    if (null == b) {
      String path = pFile.getCanonicalPath() // Make location relative
      b = new Bundle(basename: fp, location: new File(path.substring(0, path.size() - fp.size() - '.properties'.size())))
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
