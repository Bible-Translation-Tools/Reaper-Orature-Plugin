package org.bibletranslationtools.oratureplugin.providers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bibletranslationtools.oratureplugin.launcher.config.ParsedPluginData
import java.io.File

class ExecutableProvider {

    private val osName = System.getProperty("os.name").toUpperCase()

    fun executable(): String? {
        val pluginFileStream = javaClass.getResourceAsStream("/config.yaml")
        val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val parsedPlugin: ParsedPluginData = mapper.readValue(pluginFileStream)
        return selectExecutable(parsedPlugin)
    }

    private fun selectExecutable(parsedAudioPlugin: ParsedPluginData): String? {
        val options = when {
            osName.contains("WIN") -> parsedAudioPlugin.executable.windows
            osName.contains("MAC") -> parsedAudioPlugin.executable.macos
            else -> parsedAudioPlugin.executable.linux
        }?.map {
            insertArguments(it)
        }
        return options?.let {
            selectValid(it)
        }
    }

    private fun insertArguments(filename: String): String {
        return filename.replace("\${user.name}", System.getProperty("user.name"))
    }

    private fun selectValid(paths: List<String>): String? {
        return paths.map { File(it) }.firstOrNull {
            it.exists() && it.canExecute()
        }?.absolutePath
    }
}