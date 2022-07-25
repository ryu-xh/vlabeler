package com.sdercolin.vlabeler.model

import androidx.compose.ui.res.useResource
import com.sdercolin.vlabeler.env.Log
import com.sdercolin.vlabeler.env.isDebug
import com.sdercolin.vlabeler.util.JavaScript
import com.sdercolin.vlabeler.util.ParamMap
import com.sdercolin.vlabeler.util.toFile
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.charset.Charset

sealed class TemplatePluginResult {
    data class Raw(val lines: List<String>) : TemplatePluginResult()
    data class Parsed(val entries: List<FlatEntry>) : TemplatePluginResult()
}

fun runTemplatePlugin(
    plugin: Plugin,
    params: ParamMap,
    inputFiles: List<File>,
    encoding: String,
    sampleNames: List<String>
): TemplatePluginResult {
    val js = JavaScript(
        logHandler = Log.infoFileHandler,
        currentWorkingDirectory = requireNotNull(plugin.directory).absolutePath.toFile()
    )
    val inputTexts = inputFiles.map { it.readText(Charset.forName(encoding)) }
    val resourceTexts = plugin.readResourceFiles()

    js.set("debug", isDebug)
    js.setJson("inputs", inputTexts)
    js.setJson("samples", sampleNames)
    js.setJson("params", params.toJsonObject())
    js.setJson("resources", resourceTexts)

    if (plugin.outputRawEntry.not()) {
        val entryDefCode = useResource("template_entry.js") { String(it.readAllBytes()) }
        js.eval(entryDefCode)
    }

    plugin.scriptFiles.zip(plugin.readScriptTexts()).forEach { (file, source) ->
        Log.debug("Launch script: $file")
        js.exec(file, source)
        Log.debug("Finished script: $file")
    }

    return if (plugin.outputRawEntry) {
        val lines = js.getJson<List<String>>("output")
        Log.info("Plugin execution got raw lines:\n" + lines.joinToString("\n"))
        js.close()
        TemplatePluginResult.Raw(lines)
    } else {
        val entries = js.getJson<List<FlatEntry>>("output")
        Log.info("Plugin execution got entries:\n" + entries.joinToString("\n"))
        js.close()
        TemplatePluginResult.Parsed(entries)
    }
}

@Serializable
data class FlatEntry(
    val sample: String? = null,
    val name: String,
    val start: Float,
    val end: Float,
    val points: List<Float> = listOf(),
    val extras: List<String> = listOf()
) {
    fun toEntry(fallbackSample: String) = Entry(sample ?: fallbackSample, name, start, end, points, extras)
}