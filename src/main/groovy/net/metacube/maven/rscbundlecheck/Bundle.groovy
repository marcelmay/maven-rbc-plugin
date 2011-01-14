package net.metacube.maven.rscbundlecheck
/**
 * Holds bundle information.
 *
 * @author mm
 */

public class Bundle implements Comparable {
  /** The bundle name without i18n postfixes and without extension. */
  String basename
  Set<String> files = new HashSet()
  Set<String> locales = new HashSet()

  public String toString() {
    StringBuilder b = new StringBuilder('{basename=')
    b.append(basename).append(', files=<')
    for(f in files) {
      b.append(' ').append(f)
    }
    return b.append(' >}').toString()
  }

  int compareTo(Object o) {
    if(o instanceof Bundle) {
      basename?basename.compareTo(((Bundle)o).basename): -1
    } else {
      -1
    }
  }

}