## IntelliJ Plugin - Composite Build Helper
A plugin to help manage [composite gradle builds](https://docs.gradle.org/current/userguide/composite_builds.html). This plugin will show as a Tool Window in your IDE.

## Features
- Load all available gradle builds in a directory into a single table view for easy management. 
- View git status information for each build if available.
- Quickly open build directories in your IDE's integrated terminal.
- Select which builds to include and automatically update your `settings.gradle` with the required `includeBuild` statements.
- Easily configure each included build's [dependency substitution settings](https://docs.gradle.org/current/userguide/composite_builds.html#included_build_declaring_substitutions).