package com.itwg.mundial.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.itwg.mundial.data.mapper.resolveGroups
import com.itwg.mundial.data.mapper.toHomeMatchUi
import com.itwg.mundial.data.model.HomeFaceDto
import com.itwg.mundial.data.model.HomeUsuarioDto
import com.itwg.mundial.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val userId: Long,
    val userName: String? = null,
    val unidadId: Long? = null,
    val faces: List<HomeFaceUi> = emptyList(),
    val viewMode: HomeViewMode = HomeViewMode.MY_MATCHES,
    val totalGanado: Double = 0.0,
    val totalPartidos: Int = 0,
    val partidosPendientes: Int = 0,
    val errorMessage: String? = null,
) {
    val hasUnitRanking: Boolean
        get() = faces.any { it.usuarios.isNotEmpty() }
}

class HomeViewModel(
    private val repository: HomeRepository,
    private val userId: Long,
    initialUserName: String?,
    initialUnidadId: Long?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userId = userId,
            userName = initialUserName,
            unidadId = initialUnidadId,
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.loadHome(userId).fold(
                onSuccess = { data ->
                    val faces = data.faces.map { it.toHomeFaceUi(data.grupos) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            faces = faces,
                            totalGanado = data.totalGanadoSum,
                            totalPartidos = data.totalPartidos,
                            partidosPendientes = data.partidosPendientes,
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

    fun setViewMode(mode: HomeViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }
}

private fun HomeUsuarioDto.toHomeUsuarioUi() = HomeUsuarioUi(
    id = id,
    name = name,
    email = email,
    totalGanado = totalGanado,
    dineroGanado = totalGanado,
)

private fun HomeFaceDto.toHomeFaceUi(apiGrupos: List<String>?): HomeFaceUi {
    val pendientes = partidos.count { partido ->
        !partido.finalizado &&
            partido.marcadorUsuarioPais1 == null &&
            partido.marcadorUsuarioPais2 == null
    }
    val groups = if (faseId == FASE_GRUPOS_ID) {
        resolveGroups(apiGrupos, partidos)
    } else {
        emptyList()
    }
    return HomeFaceUi(
        id = id,
        faseId = faseId,
        faseNombre = faseNombre,
        valor = valor,
        totalGanado = totalGanado,
        partidosPendientes = pendientes,
        matches = partidos.map { it.toHomeMatchUi() },
        usuarios = usuarios
            .map { it.toHomeUsuarioUi() }
            .sortedByDescending { it.dineroGanado },
        groups = groups,
    )
}

class HomeViewModelFactory(
    private val repository: HomeRepository,
    private val userId: Long,
    private val initialUserName: String?,
    private val initialUnidadId: Long?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                repository = repository,
                userId = userId,
                initialUserName = initialUserName,
                initialUnidadId = initialUnidadId,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
