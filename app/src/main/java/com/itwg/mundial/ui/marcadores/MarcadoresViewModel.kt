package com.itwg.mundial.ui.marcadores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.itwg.mundial.data.mapper.toMatchPrediction
import com.itwg.mundial.data.model.MarcadoresFaceDto
import com.itwg.mundial.data.model.PartidoDto
import com.itwg.mundial.data.repository.MarcadoresRepository
import com.itwg.mundial.model.MatchPrediction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MarcadoresUiState(
    val isLoading: Boolean = true,
    val faces: List<MarcadoresFaceUi> = emptyList(),
    val selectedFaceId: Long? = null,
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

    private var cachedFaces = emptyList<MarcadoresFaceUi>()
    private var apiGrupos: List<String>? = null

    fun loadMarcadores() {
        viewModelScope.launch {
            val previousFaceId = _uiState.value.selectedFaceId
            val previousGroup = _uiState.value.selectedGroup
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.loadMarcadores(userId).fold(
                onSuccess = { data ->
                    apiGrupos = data.grupos
                    cachedFaces = data.faces.map { it.toMarcadoresFaceUi(data.grupos) }
                    val selectedFaceId = previousFaceId?.takeIf { id ->
                        cachedFaces.any { it.id == id }
                    } ?: cachedFaces.firstOrNull()?.id
                    applyFaceSelection(
                        faceId = selectedFaceId,
                        preferredGroup = previousGroup,
                    )
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
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

    fun selectFace(faceId: Long) {
        applyFaceSelection(faceId = faceId, preferredGroup = null)
    }

    fun selectGroup(group: String) {
        val face = cachedFaces.find { it.id == _uiState.value.selectedFaceId } ?: return
        _uiState.update {
            it.copy(
                selectedGroup = group,
                matches = repository.matchesForGroup(face.partidos, group),
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
                    cachedFaces = cachedFaces.map { face ->
                        face.copy(
                            partidos = face.partidos.map { partido ->
                                if (partido.id == partidoIdLong) {
                                    partido.copy(
                                        marcadorUsuarioPais1 = home,
                                        marcadorUsuarioPais2 = away,
                                    )
                                } else {
                                    partido
                                }
                            },
                        )
                    }
                    applyFaceSelection(
                        faceId = _uiState.value.selectedFaceId,
                        preferredGroup = _uiState.value.selectedGroup,
                    )
                    _uiState.update { it.copy(isSavingMarcador = false, saveMarcadorError = null) }
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

    private fun applyFaceSelection(faceId: Long?, preferredGroup: String?) {
        val face = cachedFaces.find { it.id == faceId }
        if (face == null) {
            _uiState.update {
                it.copy(
                    faces = cachedFaces,
                    selectedFaceId = null,
                    groups = emptyList(),
                    selectedGroup = null,
                    matches = emptyList(),
                    initialScores = emptyMap(),
                )
            }
            return
        }

        val groups = face.groups
        val selectedGroup = if (face.isGroupPhase) {
            preferredGroup?.takeIf { it in groups } ?: groups.firstOrNull()
        } else {
            null
        }
        val matches = if (face.isGroupPhase && selectedGroup != null) {
            repository.matchesForGroup(face.partidos, selectedGroup)
        } else {
            face.partidos.map { it.toMatchPrediction() }
        }

        _uiState.update {
            it.copy(
                faces = cachedFaces,
                selectedFaceId = face.id,
                groups = groups,
                selectedGroup = selectedGroup,
                matches = matches,
                initialScores = buildInitialScores(face.partidos),
            )
        }
    }

    private fun MarcadoresFaceDto.toMarcadoresFaceUi(apiGrupos: List<String>?) = MarcadoresFaceUi(
        id = id,
        faseId = faseId,
        faseNombre = faseNombre,
        valor = valor,
        partidos = partidos,
        groups = repository.groupsForFace(this, apiGrupos),
    )

    private fun buildInitialScores(
        partidos: List<PartidoDto>,
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
