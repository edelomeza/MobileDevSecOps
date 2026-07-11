package com.example.mobiledevsecops.shared.fake

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.Usuario
import com.example.mobiledevsecops.domain.model.UsuarioPage
import com.example.mobiledevsecops.domain.repository.UsuarioRepository
import com.example.mobiledevsecops.shared.fixture.UsuarioFixtures

class FakeUsuarioRepository : UsuarioRepository {

    var shouldThrowException = false
    var shouldThrowSessionExpired = false
    var shouldThrowConflict = false
    var exceptionMessage = "Error de conexión"

    private val _usuarios = mutableListOf<Usuario>()
    val usuarios: List<Usuario> get() = _usuarios.toList()

    var currentPage = 1
    var totalPages = 1
    var totalCount = 0

    fun givenUsuarios(usuarios: List<Usuario>) {
        _usuarios.clear()
        _usuarios.addAll(usuarios)
        totalCount = usuarios.size
        totalPages = maxOf(1, (usuarios.size + 7) / 8)
    }

    override suspend fun getUsuarios(page: Int, pageSize: Int): UsuarioPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        currentPage = page
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, _usuarios.size)
        val items = if (start < _usuarios.size) _usuarios.subList(start, end) else emptyList()

        return UsuarioPage(
            items = items,
            totalCount = totalCount,
            pageNumber = page,
            totalPages = totalPages
        )
    }

    override suspend fun buscarUsuarios(texto: String, page: Int, pageSize: Int): UsuarioPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        val filtered = _usuarios.filter {
            it.strNombre.contains(texto, ignoreCase = true) ||
                it.strCorreoElectronico.contains(texto, ignoreCase = true)
        }
        val filteredTotal = filtered.size
        val filteredTotalPages = maxOf(1, (filteredTotal + pageSize - 1) / pageSize)
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, filteredTotal)
        val items = if (start < filteredTotal) filtered.subList(start, end) else emptyList()

        return UsuarioPage(
            items = items,
            totalCount = filteredTotal,
            pageNumber = page,
            totalPages = filteredTotalPages
        )
    }

    override suspend fun crearUsuario(
        strNombre: String,
        strPWD: String,
        strCorreoElectronico: String
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        _usuarios.add(
            Usuario(
                id = _usuarios.size + 1,
                strNombre = strNombre,
                strCorreoElectronico = strCorreoElectronico,
                rowVersion = "AAAAAAAAB9E="
            )
        )
        totalCount = _usuarios.size
    }

    override suspend fun actualizarUsuario(
        id: Int,
        strNombre: String,
        strPWD: String,
        strCorreoElectronico: String,
        rowVersion: String
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        val index = _usuarios.indexOfFirst { it.id == id }
        if (index != -1) {
            _usuarios[index] = _usuarios[index].copy(
                strNombre = strNombre,
                strCorreoElectronico = strCorreoElectronico
            )
        }
    }

    override suspend fun eliminarUsuario(id: Int, rowVersion: String) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        _usuarios.removeAll { it.id == id }
        totalCount = _usuarios.size
    }
}
