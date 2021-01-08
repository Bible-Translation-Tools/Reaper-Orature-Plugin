package org.bibletranslationtools.oratureplugin

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import org.bibletranslationtools.oratureplugin.launcher.PluginLauncher
import java.io.File

class App : CliktCommand(treatUnknownOptionsAsArgs = true) {

    private val wav: String by option(help = "Full path of the wav file to launch")
        .required()
        .validate {
            val file = File(it)
            if (!file.exists()) {
                fail("File: $it does not exist")
            }
            if (file.extension != "wav") {
                fail("File $it is not a wav file")
            }
        }

    private val arguments by argument().multiple()

    override fun run() {
        if (arguments.isNotEmpty()) {
            println("The following arguments are not required. Thus ignored.")
            arguments.forEach(::println)
        }

        val launcher = PluginLauncher(wav)

        launcher.launch()
            .doFinally {
                launcher.cleanup()
            }
            .subscribe()
    }
}

fun main(args: Array<String>) {
    App().main(args)
}
