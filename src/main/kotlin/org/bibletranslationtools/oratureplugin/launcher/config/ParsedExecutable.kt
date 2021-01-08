package org.bibletranslationtools.oratureplugin.launcher.config

data class ParsedExecutable(
    // nullable since executable might not exist for a platform
    var macos: List<String>?,
    var windows: List<String>?,
    var linux: List<String>?
)