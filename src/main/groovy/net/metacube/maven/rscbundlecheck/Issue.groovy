package net.metacube.maven.rscbundlecheck

/**
 * Holds a check issue.
 *
 * @author mm
 */

public class Issue {
  String check
  String description
  String source
  String sourceIdx

  public String toString() {
    return 'Issue{' +
            'check=\'' + check + '\'' +
            ', description=\'' + description + '\'' +
            ', source=\'' + source + '\'' +
            (sourceIdx?', sourceIdx=\'' + source + '\'':'') +
            '}';
  }
}