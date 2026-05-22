package com.itwg.mundial.ui.marcadores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.itwg.mundial.data.repository.MarcadoresRepository
import com.itwg.mundial.model.MatchPrediction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MarcadoresUiState(
    val isLoading: Boolean = true,
    val groups: List<String> = emptyList(),
    val selectedGroup: String? = null,
    val matches: List<MatchPrediction> = emptyList(),
    val initialScores: Map<String, Pair<String, String>> = emptyMap(),
    val errorMessage: String? = null,
)

class MarcadoresViewModel(
    private val userId: Long,
    private val repository: MarcadoresRepository = MarcadoresRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarcadoresUiState())
    val uiState: StateFlow<MarcadoresUiState> = _uiState.asStateFlow()

    private var cachedPartidos = emptyList<com.itwg.mundial.data.model.PartidoDto>()

    fun loadMarcadores() {
        viewModelScope.launch {
            val previousGroup = _uiState.value.selectedGroup
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.loadMarcadores(userId).fold(
                onSuccess = { data ->
                    cachedPartidos = data.partidos
                    val selected = previousGroup?.takeIf { it in data.groups }
                        ?: data.groups.firstOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            groups = data.groups,
                            selectedGroup = selected,
                            matches = selected?.let { g ->
                                repository.matchesForGroup(data.partidos, g)
                            } ?: emptyList(),
                            initialScores = buildInitialScores(data.partidos),
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                },
            )
        }
    }

    fun selectGroup(group: String) {
        _uiState.update {
            it.copy(
                selectedGroup = group,
                matches = repository.matchesForGroup(cachedPartidos, group),
            )
        }
    }

    private fun buildInitialScores(
        partidos: List<com.itwg.mundial.data.model.PartidoDto>,
    ): Map<String, Pair<String, String>> =
        partidos
            .filter { !it.finalizado }
            .associate { partido ->
                partido.id.toString() to Pair(
                    partido.marcadorPais1?.toString() ?: "",
                    partido.marcadorPais2?.toString() ?: "",
                )
            }
}

class MarcadoresViewModelFactory(
    private val userId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarcadoresViewModel::class.java)) {
            return MarcadoresViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
