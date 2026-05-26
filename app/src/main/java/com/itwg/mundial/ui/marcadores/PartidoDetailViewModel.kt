package com.itwg.mundial.ui.marcadores

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.itwg.mundial.data.mapper.formatPartidoDateTime
import com.itwg.mundial.data.model.MarcadorDetailResponse
import com.itwg.mundial.data.model.PartidoMarcadorUsuarioDto
import com.itwg.mundial.data.repository.MarcadoresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PartidoDetailUiState(
    val isLoading: Boolean = true,
    val detail: PartidoDetailUi? = null,
    val errorMessage: String? = null,
)

class PartidoDetailViewModel(
    private val partidoId: Long,
    private val userId: Long,
    private val repository: MarcadoresRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartidoDetailUiState())
    val uiState: StateFlow<PartidoDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.loadMarcadorDetail(partidoId, userId).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = response.toPartidoDetailUi(),
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
}

private fun MarcadorDetailResponse.toPartidoDetailUi(): PartidoDetailUi {
    val home = partido.pais1
    val away = partido.pais2
    val grupo = home?.grupo?.takeIf { it.isNotBlank() } ?: away?.grupo?.takeIf { it.isNotBlank() }
    return PartidoDetailUi(
        id = partido.id,
        dateTime = formatPartidoDateTime(partido.fecha, partido.hora),
        faseNombre = partido.fase.nombre,
        grupoLabel = grupo?.let { "Grupo $it" },
        homeTeam = home?.nombre ?: "Por definir",
        awayTeam = away?.nombre ?: "Por definir",
        homeFlagUrl = home?.bandera,
        awayFlagUrl = away?.bandera,
        isFinished = partido.finalizado,
        finalHomeScore = if (partido.finalizado) partido.marcadorPais1 else null,
        finalAwayScore = if (partido.finalizado) partido.marcadorPais2 else null,
        usuarios = usuarios
            .map { it.toPartidoUsuarioMarcadorUi() }
            .sortedWith(
                compareByDescending<PartidoUsuarioMarcadorUi> { it.ganado }
                    .thenBy { it.name },
            ),
    )
}

private fun PartidoMarcadorUsuarioDto.toPartidoUsuarioMarcadorUi() = PartidoUsuarioMarcadorUi(
    id = id,
    name = name,
    email = email,
    marcadorHome = marcadorPais1,
    marcadorAway = marcadorPais2,
    ganado = ganado,
    hasPrediction = marcadorPais1 != null && marcadorPais2 != null,
)

class PartidoDetailViewModelFactory(
    private val partidoId: Long,
    private val userId: Long,
    private val repository: MarcadoresRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartidoDetailViewModel::class.java)) {
            return PartidoDetailViewModel(
                partidoId = partidoId,
                userId = userId,
                repository = repository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
