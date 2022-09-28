package com.sdercolin.vlabeler.model

import com.sdercolin.vlabeler.env.Log
import com.sdercolin.vlabeler.env.isDebug
import com.sdercolin.vlabeler.exception.PluginRuntimeException
import com.sdercolin.vlabeler.exception.PluginUnexpectedRuntimeException
import com.sdercolin.vlabeler.ui.string.LocalizedJsonString
import com.sdercolin.vlabeler.util.JavaScript
import com.sdercolin.vlabeler.util.ParamMap
import com.sdercolin.vlabeler.util.Resources
import com.sdercolin.vlabeler.util.execResource
import com.sdercolin.vlabeler.util.parseJson
import com.sdercolin.vlabeler.util.toFile
import kotlinx.serialization.Serializable

fun runMacroPlugin(
    plugin: Plugin,
    params: ParamMap,
    project: Project,
): Pair<Project, LocalizedJsonString?> {
    val js = JavaScript(
        logHandler = Log.infoFileHandler,
        currentWorkingDirectory = requireNotNull(plugin.directory).absolutePath.toFile(),
    )
    val result = runCatching {
        val resourceTexts = plugin.readResourceFiles()

        js.set("debug", isDebug)
        js.setJson("labeler", project.labelerConf)
        js.setJson("entries", project.currentModule.entries)
        js.setJson("params", params.resolve(project, js))
        js.setJson("resources", resourceTexts)

        listOf(
            Resources.classEntryJs,
            Resources.classEditedEntryJs,
            Resources.expectedErrorJs,
            Resources.reportJs,
            Resources.fileJs,
        ).forEach { js.execResource(it) }

        plugin.scriptFiles.zip(plugin.readScriptTexts()).forEach { (file, source) ->
            Log.debug("Launch script: $file")
            js.exec(file, source)
            Log.debug("Finished script: $file")
        }

        val editedEntries = js.getJsonOrNull<List<PluginEditedEntry>>("output")
        if (editedEntries != null) {
            val newCount = editedEntries.count { it.originalIndex == null }
            val editedCount = editedEntries.count {
                if (it.originalIndex == null) {
                    false
                } else {
                    project.currentModule.entries[it.originalIndex] != it.entry
                }
            }
            val removedCount =
                (
                    project.currentModule.entries.indices.toSet() -
                        editedEntries.mapNotNull { it.originalIndex }.toSet()
                    ).size
            Log.info(
                buildString {
                    appendLine("Plugin execution got edited entries:")
                    appendLine("Total: " + editedEntries.size)
                    appendLine("New: $newCount")
                    appendLine("Edited: $editedCount")
                    appendLine("Removed: $removedCount")
                },
            )
        }
        val newProject = if (editedEntries != null) {
            project.updateCurrentModule { copy(entries = editedEntries.map { it.entry }) }.validate()
        } else {
            project
        }
        val report = js.getOrNull<String>("reportText")
        newProject to report?.parseJson<LocalizedJsonString>()
    }.getOrElse {
        val expected = js.getOrNull("expectedError") ?: false
        js.close()
        if (expected) {
            throw PluginRuntimeException(it, it.message?.parseJson())
        } else {
            throw PluginUnexpectedRuntimeException(it)
        }
    }
    js.close()
    return result
}

@Serializable
data class PluginEditedEntry(
    val originalIndex: Int?,
    val entry: Entry,
)
