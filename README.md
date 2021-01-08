# Reaper-Orature-Plugin
Reaper audio plugin for Orature

# Preparation

**Add ReaperOraturePlugin.lua action script**  
   
1. Download "ReaperOraturePlugin.lua" file and put it in a directory of your choice.  
2. Open Reaper, then go to Actions. Select "Show Action List".  
3. Then click "New action" and choose "Load ReaScript". Select "ReaperOraturePlugin.lua" from the location where you saved it.  
   
You may want to set the keyboard shortcut for the script,  

Or you can use SWS/S&M Extension for Reaper (https://www.sws-extension.org/) to run the script automatically on project startup. Windows and MacOS only.  

If you installed SWS/S&M Extension, go to Actions and select "Show Action List". Find "Script: ReaperOraturePlugin.lua", right click and "Copy selected action Command ID". Close. Then go to Extensions and select "Startup actions -> Set global startup action" and enter copied Command ID. That's it! When a project is loaded, the action will start automatically.  

# Set audio plugin in Orature

Open Orature. Go to Audio Plugins and select "New". Enter the name of the plugin (for example Reaper). As for executable, point to reaper-orature-plugin.jar and select "Record", "Edit" checkboxes of your choice.  

Try to make a recording. Reaper application should start. If you configured SWS/S&M Extension, then media will be loaded onto track automatically. Otherwise, use predefined shortcut or run it manually by going to Action List  

When you finish with recording/editing, save the project, confirm overwrite and close Reaper.  

# Background and Problem Introduction

Orature supports third-party (external applications) and first-party (which can be dynamically loaded in window) plugins for editing and recording. 
Third party plugins are launched by calling exec() on the provided third-party binary, and providing cli arguments, namely --wav="{file}" where {file} is the absolute path to a wav file (in the case of recording, this is an empty wav file with a valid header corresponding to 44.1khz 16 bit mono).

Implications of this are that the file passed to the external application serves as the return value of the plugin. In other words, Orature expects that any modifications to the file will overwrite the file passed to the plugin by Orature.

#### The following editors are confirmed to be content with this approach and will happily overwrite the file on save:
 - Adobe Audition
 - Ocenaudio

#### The following editors and Digital Audio Workstations are confirmed NOT compatible with this approach:
 - Audacity
 - Cubase
 - Logic Pro
 - Reaper

Applications that are not compatible tend to share a commonality. Namely, the incompatible software prefers to construct its own audio projects to operate off of, and requires the resulting audio to be exported, rendered, or "bounced."

# Proposed Solution

As a workaround, we propose small adapter applications sit in between Orature and the desired third party application. This adapter will be called by Orature AS the third party plugin (henceforth referred to as the adapter), and provide to it over the cli the wav file to be edited or recorded over via --wav="{file}". The adapter may then need to create and manage a project file corresponding to the specific application it is an adapter for. The association between this project and the original file provided through Orature may be done through some sort of persistent map of wav file to project file/directory, or a small database. Alternatively, the adapter could choose to create a new project from the file every time. Regardless of approach, the adapter should be responsible for launching the third party application, and, on the application's closing, be responsible for rendering the audio to the file Orature provided. Thus, when the adapter is finished, all changes made to the file shall overwrite the file allowing Orature to be naive of the intermediate steps necessary.

#### Note:
As none of this requires Java as a platform, the adapter can theoretically be implemented in any ecosystem or language. However, As Orature can run on any operating system supporting the JVM and JavaFX (which additionally implies both x86 and ARM architectures) which means that choosing NOT to implement the adapter in Java means needing to provide support on all platforms (this can, of course, be restricted to the platforms supported by the third party application in question). Platforms such as Python and Ruby requiring a runtime would preferrably be avoided as it would require bundling these runtimes with the Orature installer as they cannot be assumed to exist on the end user's computer. The following README assumes continuing with the Java/JVM ecosystem.

# Prerequisites

This README provides an example to set up the project contained in this repo using the Intellij IDE and the Gradle build system. Other IDEs can be used, provided the user is familiar with using Gradle.

Intellij Idea: https://www.jetbrains.com/idea/

OpenJDK 11: https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html 

# Setup

First, clone this git repo. 
On opening Intellij, select "Open/Import Project" rather than creating a new one.
Select the build.gradle file found at the root of this repository

Find the run configurations as seen here:

![run-config](https://raw.githubusercontent.com/Bible-Translation-Tools/Orature-Plugin/main/screenshots/edit-run-configurations.png)

Edit the run configuration to provide the program arugments (to provide the wav argument) as seen here:

![edit-config](https://raw.githubusercontent.com/Bible-Translation-Tools/Orature-Plugin/main/screenshots/plugin-program-arguments.png)

# Compiling

The Shadow Jar plugin will produce a jar with all dependencies included and specify the program entrypoint. It is configured through the shadowJar block of the build.gradle file:

```
shadowJar {
    archiveFileName = 'orature-plugin.jar'
    mergeServiceFiles()
    manifest {
        attributes(
            'Main-Class': "org.bibletranslationtools.oratureplugin.MainKt"
        )
    }
}
```

In order to use Java instead of Kotlin, change ```'Main-Class': "org.bibletranslationtools.oratureplugin.MainKt"``` to 
```'Main-Class': "org.bibletranslationtools.oratureplugin.Plugin"```

For your convenience, the Kotlin side has been pre-configured with the Clikt library to parse the wav argument. Treat the "run()" method of the App class as main. It currently prints the provided wav path.

For Java, only a simple Hello World is provided to verify setup.

Build should trigger the jar process (which will be overriden by shadow jar). You can find the output jar in the build/libs directory shown here:

![build-output](https://raw.githubusercontent.com/Bible-Translation-Tools/Orature-Plugin/main/screenshots/build-output.png)

To verify the jar, it can be launched with ```java -jar orature-plugin.jar```
The plugin can be renamed by modifying the shadowJar configuration referenced above.
