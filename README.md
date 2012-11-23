# ProjectLocationUpdater

This Eclipse plugin adds a property page to update the project location by editing the `.location` file in your workspace.

## Usecase

Correct the locations for projects moved in the file system without using the Eclipse refactoring `move` tool.
Alternatively such projects could be deleted and reimported into the workspace. During this process some other project settings are altered which may not be intended.

## Background

Some projects depend on external build tools. In consequence they require the workspace configuration separated from the project files.
If such a project has been imported into the workspace and is not located in a subfolder of the workspace, the project locations are stored in the runtime data as absolute path. There is no built in mechanism to change that path.
This property page addresses this issue and allows you to change the location path of the closed project.

## Installation

Install from the Eclipse Marketplace
    http://marketplace.eclipse.org/node/539811
or from the update site
    http://www.gensthaler.de/eclipse/

## Usage

- Open the properties of the closed project.
- Select `Resource Location Updater` page.
- Update the project location path.
- Reopen the project to apply the changes.

## Screenshot

!ProjectLocationUpdater_screenshot.png!

## License

ProjectLocationUpdater is released under the Eclipse Puglic License 1.0. http://www.eclipse.org/legal/epl-v10.html

## Related websites

- [Stackoverflow: Eclipse change project files location](http://stackoverflow.com/questions/1430836/eclipse-change-project-files-location)
- [Moving a Flex Builder/Eclipse Workspace Without Importing Anything](http://www.joeflash.ca/blog/2008/11/moving-a-fb-workspace-update.html)