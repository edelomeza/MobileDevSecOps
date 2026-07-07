package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.UsuarioApi
import com.example.mobiledevsecops.data.remote.dto.UserCreateRequest
import com.example.mobiledevsecops.data.remote.dto.UserDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.UserUpdateRequest
import com.example.mobiledevsecops.data.remote.dto.UsuarioListResponse
import com.example.mobiledevsecops.domain.model.Usuario
import com.example.mobiledevsecops.domain.model.UsuarioPage
import com.example.mobiledevsecops.domain.repository.UsuarioRepository

class UsuarioRepositoryImpl(
    private val usuarioApi: UsuarioApi
) : UsuarioRepository {

    override suspend fun getUsuarios(page: Int, pageSize: Int): UsuarioPage {
        val response: UsuarioListResponse = usuarioApi.getUsuarios(page, pageSize)
        return response.toDomain()
    }

    override suspend fun buscarUsuarios(texto: String, page: Int, pageSize: Int): UsuarioPage {
        val response: UsuarioListResponse = usuarioApi.buscarUsuarios(texto, page, pageSize)
        return response.toDomain()
    }

    override suspend fun crearUsuario(strNombre: String, strPWD: String, strCorreoElectronico: String) {
        val request = UserCreateRequest(
            strNombre = strNombre,
            strPWD = strPWD,
            strCorreoElectronico = strCorreoElectronico
        )
        usuarioApi.crearUsuario(request)
    }

    override suspend fun actualizarUsuario(id: Int, strNombre: String, strPWD: String, strCorreoElectronico: String, rowVersion: String) {
        val request = UserUpdateRequest(
            id = id,
            strNombre = strNombre,
            strPWD = strPWD,
            strCorreoElectronico = strCorreoElectronico,
            rowVersion = rowVersion
        )
        usuarioApi.actualizarUsuario(request)
    }

    override suspend fun eliminarUsuario(id: Int, rowVersion: String) {
        val request = UserDeleteRequest(
            id = id,
            rowVersion = rowVersion
        )
        usuarioApi.eliminarUsuario(request)
    }
}

private fun UsuarioListResponse.toDomain(): UsuarioPage = UsuarioPage(
    items = items.map { usuarioDto ->
        Usuario(
            id = usuarioDto.id,
            strNombre = usuarioDto.strNombre,
            strCorreoElectronico = usuarioDto.strCorreoElectronico,
            rowVersion = usuarioDto.rowVersion ?: ""
        )
    },
    totalCount = totalCount,
    pageNumber = pageNumber,
    totalPages = totalPages
)
