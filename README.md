Maven Resource Bundle Check Plugin
==================================

Historical note: This plugin used to be called *maven-rbc-plugin* (see maven-rbc-plugin#1 ).

The resource bundle check plugin *rbc-maven-plugin* runs some checks on your resource files using underneath the [ResourceCheck Ant task][rscbundlecheck.sf.net].


What is it good for?
--------------------

* Runs various checks on resource files, such as missing language resources files or missing resource entries
* Can create a Maven report of the check results

Check out the [plugin web site][site] for details.

![Example report][example_report]

[rscbundlecheck.sf.net]: http://rscbundlecheck.sourceforge.net
[site]: http://labs.consol.de/projects/maven/maven-rbc-plugin/
[example_report]: https://github.com/marcelmay/maven-rbc-plugin/raw/master/src/site/resources/example-report-only.png "Example report showing some check issues"

Development
-----------

* Build the plugin

    mvn clean install

  Make sure you got [Maven 3.0.3+][maven_download] or higher.

* Build the site (and the optional example report)

    mvn clean install site -Psite,example-report

    mvn site:deploy -Psite,dist-labs

* Release

    mvn release:prepare

    mvn release:perform

Make sure you got the changes etc for the site updated previous to the release.

[maven_download]: http://maven.apache.org
