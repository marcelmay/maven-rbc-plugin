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
  /** Directory for bundle, eg '/.../src/main/resources' */
  File location

  public String toString() {
    '{basename='+basename+', location='+location+', files='+files+', locales='+locales+'}'
  }

  int compareTo(Object o) {
    if(o instanceof Bundle) {
      basename?basename.compareTo(((Bundle)o).basename): -1
    } else {
      -1
    }
  }

}