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
    val isSavingMarcador: Boolean = false,
    val saveMarcadorError: String? = null,
)

class MarcadoresViewModel(
    private val userId: Long,
    private val unidadId: Long?,
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

    fun clearSaveMarcadorError() {
        _uiState.update { it.copy(saveMarcadorError = null) }
    }

    fun saveMarcador(
        partidoId: String,
        homeScore: String,
        awayScore: String,
        onSuccess: () -> Unit,
    ) {
        val home = homeScore.toIntOrNull()
        val away = awayScore.toIntOrNull()
        if (home == null || away == null) {
            _uiState.update {
                it.copy(saveMarcadorError = "Ingresa ambos marcadores.")
            }
            return
        }

        val partidoIdLong = partidoId.toLongOrNull()
        if (partidoIdLong == null) {
            _uiState.update {
                it.copy(saveMarcadorError = "Partido no válido.")
            }
            return
        }

        val unidadIdValue = unidadId
        if (unidadIdValue == null) {
            _uiState.update {
                it.copy(saveMarcadorError = "No se encontró la unidad del usuario.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSavingMarcador = true, saveMarcadorError = null)
            }
            repository.updateMarcador(
                userId = userId,
                unidadId = unidadIdValue,
                partidoId = partidoIdLong,
                marcadorPais1 = home,
                marcadorPais2 = away,
            ).fold(
                onSuccess = {
                    cachedPartidos = cachedPartidos.map { partido ->
                        if (partido.id == partidoIdLong) {
                            partido.copy(
                                marcadorUsuarioPais1 = home,
                                marcadorUsuarioPais2 = away,
                            )
                        } else {
                            partido
                        }
                    }
                    _uiState.update { state ->
                        val group = state.selectedGroup
                        state.copy(
                            isSavingMarcador = false,
                            saveMarcadorError = null,
                            matches = group?.let { g ->
                                repository.matchesForGroup(cachedPartidos, g)
                            } ?: state.matches,
                            initialScores = buildInitialScores(cachedPartidos),
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSavingMarcador = false,
                            saveMarcadorError = error.message,
                        )
                    }
                },
            )
        }
    }

    private fun buildInitialScores(
        partidos: List<com.itwg.mundial.data.model.PartidoDto>,
    ): Map<String, Pair<String, String>> =
        partidos.associate { partido ->
            partido.id.toString() to Pair(
                partido.marcadorUsuarioPais1?.toString() ?: "",
                partido.marcadorUsuarioPais2?.toString() ?: "",
            )
        }
}

class MarcadoresViewModelFactory(
    private val userId: Long,
    private val unidadId: Long?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarcadoresViewModel::class.java)) {
            return MarcadoresViewModel(userId, unidadId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
