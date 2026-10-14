package com.example.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.data.ProjectSummary
import com.example.model.Project
import com.example.model.ProjectTemplates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val projects: List<ProjectSummary> = emptyList(),
    val errorMessage: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val summaries = repository.getProjectSummaries()
                _uiState.value = DashboardUiState(isLoading = false, projects = summaries)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState(isLoading = false, errorMessage = e.localizedMessage)
            }
        }
    }

    fun createProject(templateType: String, customName: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val name = customName.ifBlank { "Untitled Project" }
            val project = when (templateType) {
                "AUTH" -> ProjectTemplates.createAuthFlow(name)
                "ECOMMERCE" -> ProjectTemplates.createEcommerceFlow(name)
                else -> ProjectTemplates.createBlankMobile(name)
            }
            repository.saveProject(project)
            loadProjects()
            onCreated(project.id)
        }
    }

    fun renameProject(projectId: String, newName: String) {
        viewModelScope.launch {
            repository.renameProject(projectId, newName)
            loadProjects()
        }
    }

    fun duplicateProject(projectId: String) {
        viewModelScope.launch {
            repository.duplicateProject(projectId)
            loadProjects()
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            loadProjects()
        }
    }

    fun importProjectJson(jsonString: String, onImported: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val imported = repository.importProject(jsonString)
                loadProjects()
                onImported(imported.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to import project: ${e.message}")
            }
        }
    }

    fun loadFullProjectForShare(projectId: String, onReady: (Project) -> Unit) {
        viewModelScope.launch {
            val project = repository.loadProject(projectId)
            if (project != null) {
                onReady(project)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
