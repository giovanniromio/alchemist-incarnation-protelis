# Alchemist Incarnation SAPERE

[Protelis][Protelis] incarnation of [Alchemist][Alchemist]. This module allows the user to write Protelis programs and run them within the Alchemist infrastructure.

Support for OpenStreetMap is provided.

## Usage

Incarnation usage should be rather transparent, unless an extension of the incarnation is required for the experiment.

To learn how to write simulations, please refer to the [Domain-Specific Language for writing Protelis simulations][PROTELIS-DSL]. To understand how to simulate, refer to the [Alchemist main project][alchemist-git] instead.

If you need more information on Protelis, refer to [the official site][Protelis].

### Basics

This incarnation allows for running a simulation in which nodes are programmed with Protelis.

It provides Alchemist-specific implementations for ExecutionContext and NetworkManager, a definition for ProtelisNode (that also represents a Protelis DeviceUID), and a set of actions and conditions (ProtelisProgram, SendToNeighbor, ComputationalRoundComplete) that implement all the machinery needed for simulating cycles and message sending between devices.

Devices can host more than one program at a time. Multiple programs may interact by reading writing global variables, through the classic Protelis' ``self.(get|set|has)EnvironmentVariable()``.

The computational round and the message shipment are defined in two different reactions. This means that they can take separate time distributions, in order to model network delays.

### AlchemistExecutionContext: methods provided

* routingDistance

## Build Status
[![Build Status](https://drone.io/github.com/DanySK/alchemist-incarnation-protelis/status.png)](https://drone.io/github.com/DanySK/alchemist-incarnation-protelis/latest)


### Javadocs

Javadocs for latest build is available [here][Javadoc]. Please note that such documentation may be not in sync with the version you are importing in your project, even if you point at the latest release, since such documention is re-generated by our nightly build system and is updated with the latest commit (if the build passes).
The documentation for any specific version of this library is released on Maven Central along with the code and the compiled jar file.


### Downloads

The latest artifacts for this project can be downloaded [here][Jars]. This page includes three artifacts:
* A jar file containing the compiled class files
* A jar file containing the source code
* A jar file containing the generated javadoc

Complete build reports can be downloaded [here][reports]


### Main project
* [Build reports][dashboard]
* [Test summary][test]
* [FindBugs violations][findbugs]
* [PMD violations][pmd]
* [Checkstyle violations][checkstyle]


### Tests
* [FindBugs violations][findbugs-test]
* [PMD violations][pmd-test]
* [Checkstyle violations][checkstyle-test]


## Notes for Developers


### Importing the project
The project has been developed using Eclipse, and can be easily imported in such IDE.


#### Recommended configuration
* Download [the latest Eclipse for Java SE developers][eclipse]. Arch Linux users can use the package extra/eclipse-java, which is rather up-to-date.
* Install the Gradle plug-in
	* In Eclipse, click Help -> Eclipse Marketplace...
	* In the search form enter "gradle", then press Enter
	* One of the retrieved entries should be "Gradle IDE Pack", click Install
	* Follow the instructions, accept the license, wait for Eclipse to download and install the product, accept the installation and restart the IDE.
* Install the FindBugs plug-in
	* In Eclipse, click Help -> Eclipse Marketplace...
	* In the search form enter "findbugs", then press Enter
	* One of the retrieved entries should be "FindBugs Eclipse Plugin", click Install
	* Follow the instructions, accept the license, wait for Eclipse to download and install the product, accept the installation and restart the IDE.
* Install the PMD plug-in
	* **Do not** install eclipse-pmd from the Eclipse Marketplace
	* In Eclipse, click Help -> Install New Software
	* In the text field labelled "Work with:", enter: https://sourceforge.net/projects/pmd/files/pmd-eclipse/update-site-latest/
	* Press Enter
	* PMD for Eclipse 4 will appear in the plugin list. Select it and click Next.
	* Follow the instructions, accept the license, wait for Eclipse to download and install the product, accept the installation and restart the IDE.
* Install the Checkstyle plug-in
	* In Eclipse, click Help -> Eclipse Marketplace...
	* In the search form enter "checkstyle", then press Enter
	* One of the retrieved entries should be "Checkstyle Plug-in" with a written icon whose text is "eclipse-cs", click Install
	* Follow the instructions, accept the license, wait for Eclipse to download and install the product, accept the installation and restart the IDE.


#### Import Procedure
* Open Eclipse
* Click File -> Import -> Git -> Projects from Git -> Next
* Clone URI -> Next
* Paste `git@github.com:DanySK/alchemist-incarnation-protelis.git` as URI -> Next -> Next
* Select the directory where you want to clone the repository. Beware that it **does not** point to the current Eclipse workspace by default
* Next -> Next -> Finish
* The project will appear in your projects list.
* Right click on the project, select Gradle -> Refresh Dependencies. If the option is disabled, do first Gradle -> Enable Dependency Management and then try again.
* Checkstyle, PMD and FindBugs should be pre-configured. **Do not** run Gradle -> Refresh all, because that would delete the automatic invocation of the code checkers.

### Developing the project
Contributions to this project are welcome. Just some rules:
1. Commit often. Do not throw at me pull requests with a single giant commit adding or changing the world. Split it in multiple commits and request a merge to the mainline often.
2. Do not introduce low quality code. All the new code must comply with the checker rules (that are quite strict) and must not introduce any other warning. Resolutions of existing warnings (if any is present) are very welcome instead.


#### Building the project
While developing, you can rely on Eclipse to build the project, it will generally do a very good job.
If you want to generate the artifacts, you can rely on Gradle. Just point a terminal on the project's root and issue

```bash
./gradlew
```

This will trigger the creation of the artifacts the executions of the tests, the generation of the documentation and of the project reports.


#### Release numbers explained
We release often. We are not scared of high version numbers, they are just numbers in the end.
We use a three levels numbering:

* **Update of the minor number**: there are some small changes, and no backwards compatibility is broken. Probably, it is better saying that there is nothing suggesting that any project that depends on this one may have any problem compiling or running. Raise the minor version if there is just a bug fix, or a code improvement, such that no interface, constructor, or non-private member of a class is modified either in syntax or in semantics. Also, no new classes should be provided.
	* Example: switch from 1.2.3 to 1.2.4 
* **Update of the middle number**: there are changes that should not break any backwards compatibility, but the possibility exists. Raise the middle version number if there is a remote probability that projects that depend upon this one may have problems compiling if they update. For instance, if you have added a new class, since a depending project may have already defined it, that is enough to trigger a mid-number change. Also updating the version ranges of a dependency, or adding a new dependency, should cause the mid-number to raise. As for minor numbers, no changes to interfaces, constructors or non-private member of classes are allowed. If mid-number is update, minor number should be reset to 0.
	* Example: switch from 1.2.3 to 1.3.0 
* **Update of the major number**: *non-backwards-compatible change*. If a change in interfaces, constructors, or public member of some class have happened, a new major number should be issued. This is also the case if the semantics of some method has changed. In general, if there is a high probability that projects depending upon this one may experience compile-time or run-time issues if they switch to the new version, then a new major number should be adopted. If the major version number is upgraded, the mid and minor numbers should be reset to 0.
	* Example: switch from 1.2.3 to 2.0.0 


[Alchemist]: http://danysk.github.io/alchemist/
[Protelis]: http://protelis.org/
[PROTELIS-DSL]: https://github.com/DanySK/alchemist-dsl-protelis
[alchemist-git]: https://github.com/DanySK/alchemist
[Javadoc]: http://137.204.107.70/alchemist-build/alchemist-incarnation-protelis/build/docs/javadoc/
[Jars]: https://drone.io/github.com/DanySK/alchemist-incarnation-protelis/files
[reports]: https://drone.io/github.com/DanySK/alchemist-incarnation-protelis/files/build/reports/reports.tar
[dashboard]: http://137.204.107.70/alchemist-build/alchemist-incarnation-protelis/build/reports/buildDashboard/
[test]: http://137.204.107.70/alchemist-build/alchemist-incarnation-protelis/build/reports/tests/
[checkstyle]: http://137.204.107.70/alchemist-build/alchemist-incarnation-protelis/build/reports/checkstyle/main.html
[checkstyle-test]: http://137.204.107.70/alchemist-build/alchemist-incarnation-protelis/build/reports/checkstyle/test.html
[findbugs]: http://137.204.107.70/alchemist-build/alchemist-incarnation-protelis/build/reports/findbugs/main.html
[findbugs-test]: http://137.204.107.70/alchemist-build/alchemist-incarnation-protelis/build/reports/findbugs/test.html
[pmd]: http://137.204.107.70/alchemist-build/alchemist-incarnation-protelis/build/reports/pmd/main.html
[pmd-test]: http://137.204.107.70/alchemist-build/alchemist-incarnation-protelis/build/reports/pmd/test.html
[eclipse]: https://eclipse.org/downloads/
