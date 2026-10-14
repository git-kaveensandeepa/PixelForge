package com.example.data

import android.content.Context
import com.example.model.Project
import com.example.model.ProjectTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class ProjectSummary(
    val id: String,
    val name: String,
    val updatedAt: Long,
    val artboardCount: Int,
    val elementCount: Int,
    val startArtboardName: String
)

class ProjectRepository(private val context: Context) {

    private val projectsDir: File
        get() = File(context.filesDir, "projects").apply { if (!exists()) mkdirs() }

    suspend fun getProjectSummaries(): List<ProjectSummary> = withContext(Dispatchers.IO) {
        ensureInitialSeed()
        val files = projectsDir.listFiles { _, name -> name.endsWith(".json") && !name.endsWith(".tmp") } ?: emptyArray()
        val list = mutableListOf<ProjectSummary>()

        for (file in files) {
            try {
                val json = file.readText()
                val project = ProjectSerializer.deserializeProject(json)
                val totalElements = project.artboards.sumOf { it.elements.size }
                val startScreen = project.artboards.find { it.isStartArtboard }?.name
                    ?: project.artboards.firstOrNull()?.name ?: "Screen 1"

                list.add(
                    ProjectSummary(
                        id = project.id,
                        name = project.name,
                        updatedAt = project.updatedAt,
                        artboardCount = project.artboards.size,
                        elementCount = totalElements,
                        startArtboardName = startScreen
                    )
                )
            } catch (_: Exception) {
                // If main file corrupted, attempt backup recovery
                val backupFile = File(projectsDir, "${file.nameWithoutExtension}.bak")
                if (backupFile.exists()) {
                    try {
                        val backupJson = backupFile.readText()
                        val project = ProjectSerializer.deserializeProject(backupJson)
                        list.add(
                            ProjectSummary(
                                id = project.id,
                                name = "${project.name} (Recovered)",
                                updatedAt = project.updatedAt,
                                artboardCount = project.artboards.size,
                                elementCount = project.artboards.sumOf { it.elements.size },
                                startArtboardName = project.artboards.firstOrNull()?.name ?: "Screen 1"
                            )
                        )
                    } catch (_: Exception) {}
                }
            }
        }

        list.sortedByDescending { it.updatedAt }
    }

    suspend fun loadProject(id: String): Project? = withContext(Dispatchers.IO) {
        val file = File(projectsDir, "$id.json")
        val backupFile = File(projectsDir, "$id.bak")

        if (file.exists()) {
            try {
                val json = file.readText()
                return@withContext ProjectSerializer.deserializeProject(json)
            } catch (e: Exception) {
                // Main file read failed; fallback to backup
                if (backupFile.exists()) {
                    try {
                        val bakJson = backupFile.readText()
                        return@withContext ProjectSerializer.deserializeProject(bakJson)
                    } catch (_: Exception) {}
                }
            }
        } else if (backupFile.exists()) {
            try {
                val bakJson = backupFile.readText()
                return@withContext ProjectSerializer.deserializeProject(bakJson)
            } catch (_: Exception) {}
        }

        null
    }

    suspend fun saveProject(project: Project): Boolean = withContext(Dispatchers.IO) {
        try {
            val mainFile = File(projectsDir, "${project.id}.json")
            val backupFile = File(projectsDir, "${project.id}.bak")
            val tempFile = File(projectsDir, "${project.id}.tmp")

            val json = ProjectSerializer.serializeProject(project)

            // Atomic save technique: write temp first, copy current to backup, rename temp to main
            tempFile.writeText(json)
            if (mainFile.exists()) {
                mainFile.copyTo(backupFile, overwrite = true)
            }
            if (tempFile.renameTo(mainFile)) {
                true
            } else {
                // Fallback direct write
                mainFile.writeText(json)
                tempFile.delete()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun duplicateProject(id: String): Project? = withContext(Dispatchers.IO) {
        val original = loadProject(id) ?: return@withContext null
        val newId = UUID.randomUUID().toString()
        val duplicated = original.copy(
            id = newId,
            name = "${original.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveProject(duplicated)
        duplicated
    }

    suspend fun deleteProject(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(projectsDir, "$id.json")
            val backup = File(projectsDir, "$id.bak")
            if (backup.exists()) backup.delete()
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun renameProject(id: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        val project = loadProject(id) ?: return@withContext false
        val updated = project.copy(name = newName.trim(), updatedAt = System.currentTimeMillis())
        saveProject(updated)
    }

    suspend fun importProject(jsonString: String): Project = withContext(Dispatchers.IO) {
        val imported = ProjectSerializer.deserializeProject(jsonString)
        val finalProject = imported.copy(
            id = UUID.randomUUID().toString(),
            name = if (imported.name.isBlank()) "Imported Project" else "${imported.name} (Imported)",
            updatedAt = System.currentTimeMillis()
        )
        saveProject(finalProject)
        finalProject
    }

    private fun ensureInitialSeed() {
        val files = projectsDir.listFiles { _, name -> name.endsWith(".json") }
        if (files == null || files.isEmpty()) {
            val authSample = ProjectTemplates.createAuthFlow("Login & Onboarding Demo")
            val ecommerceSample = ProjectTemplates.createEcommerceFlow("Nordic Shop Prototype")
            try {
                val file1 = File(projectsDir, "${authSample.id}.json")
                file1.writeText(ProjectSerializer.serializeProject(authSample))

                val file2 = File(projectsDir, "${ecommerceSample.id}.json")
                file2.writeText(ProjectSerializer.serializeProject(ecommerceSample))
            } catch (_: Exception) {}
        }
    }
}
