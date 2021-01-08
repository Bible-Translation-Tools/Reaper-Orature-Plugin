package org.bibletranslationtools.oratureplugin.launcher

import io.reactivex.Completable
import org.bibletranslationtools.oratureplugin.providers.DirectoryProvider
import org.bibletranslationtools.oratureplugin.providers.ExecutableProvider
import org.bibletranslationtools.oratureplugin.launcher.config.UnsupportedPlatformException
import java.io.File

class PluginLauncher(private val wav: String) {

    private val directoryProvider = DirectoryProvider()
    private val executableProvider = ExecutableProvider()

    fun launch(): Completable {
        return Completable.fromCallable {
            val executable = executableProvider.executable()
            val args = buildBinArguments()
            val processArgs = listOf(
                executable ?: throw UnsupportedPlatformException(),
                *args
            )
            runProcess(processArgs)
        }
    }

    fun cleanup() {
        val audioFile = File(wav)
        val parentDir = audioFile.parentFile

        parentDir.walk().forEach {
            if (it.isFile && it.extension != "wav") {
                it.delete()
            }
            if (it.isDirectory && it.name == "tmp") {
                it.deleteRecursively()
            }
        }
    }

    private fun buildBinArguments(): Array<String> {
        val mediaFile = directoryProvider.getAppDataDirectory().resolve("media.log")
        mediaFile.writeText(wav)

        val audioFile = File(wav)
        val project = audioFile.parentFile.resolve("${audioFile.nameWithoutExtension}.rpp")
        val template = templateFile()

        return arrayOf(template.absolutePath, "-saveas", project.canonicalPath)
    }

    private fun templateFile(): File {
        val templateStream = javaClass.getResourceAsStream("/template.rpp")

        val templateFile = createTempFile("template", ".rpp")
        templateFile.deleteOnExit()

        templateStream.use { input ->
            templateFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return templateFile
    }

    private fun runProcess(processArgs: List<String>) {
        val processBuilder = ProcessBuilder(processArgs)
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        process.outputStream.close()
        while (process.inputStream.read() >= 0) {
        }
        process.waitFor()
    }
}