[![Build Status](https://drone.io/github.com/gensth/ProjectLocationUpdater/status.png)](https://drone.io/github.com/gensth/ProjectLocationUpdater/latest)

# ProjectLocationUpdater

This Eclipse plugin adds a wizard and a property page to update the project location by editing the `.location` file in your workspace.

## Use case

Correct the locations of projects which were moved in the file system without using the Eclipse refactoring `move` tool. (Alternatively such projects could be deleted and reimported into the workspace. During this process some other project settings are altered which may not be intended.)

The ProjectLocationUpdater also allows to set the path to a project using symlinks without resolving before storing them. (When using the Eclipse import wizard all symlinks would be resolved.)

## Background

Some projects depend on external build tools. In consequence they require the workspace configuration separated from the project files.
If such a project has been imported into the workspace and is not located in a subfolder of the workspace, the project locations are stored in the runtime data as absolute path. There is no built in mechanism to change that path.

The ProjectLocationUpdater addresses this issue and allows you to change the location path of the closed project.

## Installation

Install from
* the Eclipse Marketplace: http://marketplace.eclipse.org/node/539811
* or from the update site: http://www.gensthaler.de/eclipse/

## Usage

* Select one or multiple closed projects
* Do one of the following
  * Menu "Project" -> "Update Project Location(s)"
  * Context menu -> "Update Project Location(s)"
  * Context menu -> "Properties" -> "Project Location Updater"
* Update the project location path of the selected project or the common path of multiple selected projects.
* Reopen the project to apply the changes.

## Screenshot

![screenshot](https://raw.github.com/gensth/ProjectLocationUpdater/master/ProjectLocationUpdater_screenshot.png "ProjectLocationUpdater")

## Building and Releasing

This project builds with `mvn clean verify` which compiles and puts the latest SNAPSHOT to the update site in `update-site/target/repository`.

To create a new release and publish it using the update-site follow these steps:

```
mvn tycho-versions:set-version -DnewVersion=1.1.0
git commit -m "release 1.1.0" -a
git tag release-1.1.0
git push origin release-1.1.0
git push origin master

mvn clean verify
# will generate a full update site containing the new plugin
# in update-site/target/repository/
# deploy this directory to your web server

mvn tycho-versions:set-version -DnewVersion=1.2.0-SNAPSHOT
git commit -m "increase version to 1.2.0-SNAPSHOT" -a
```

## License

ProjectLocationUpdater is released under the Eclipse Public License 1.0. http://www.eclipse.org/legal/epl-v10.html

## Related websites

* [Stackoverflow: Eclipse change project files location](http://stackoverflow.com/questions/1430836/eclipse-change-project-files-location)
* [Moving a Flex Builder/Eclipse Workspace Without Importing Anything](http://www.joeflash.ca/blog/2008/11/moving-a-fb-workspace-update.html)
