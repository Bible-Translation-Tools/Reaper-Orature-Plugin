package org.bibletranslationtools.oratureplugin.providers

import java.io.File
import java.nio.file.FileSystems

class DirectoryProvider {

    private val appName = "ReaperOraturePlugin"
    private val pathSeparator = FileSystems.getDefault().separator
    private val userHome = System.getProperty("user.home")
    private val windowsAppData = System.getenv("APPDATA")
    private val osName = System.getProperty("os.name").toUpperCase()

    fun getAppDataDirectory(): File {
        val pathComponents = mutableListOf<String>()

        when {
            osName.contains("WIN") -> pathComponents.add(windowsAppData)
            osName.contains("MAC") -> {
                pathComponents.add(userHome)
                pathComponents.add("Library")
                pathComponents.add("Application Support")
            }
            osName.contains("LINUX") -> {
                pathComponents.add(userHome)
                pathComponents.add(".config")
            }
        }

        pathComponents.add(appName)

        // create the directory if it does not exist
        val pathString = pathComponents.joinToString(pathSeparator)
        val file = File(pathString)
        file.mkdirs()
        return file
    }
}