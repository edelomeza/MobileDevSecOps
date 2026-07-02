package com.example.mobiledevsecops.shared.fixture

import com.example.mobiledevsecops.domain.model.Usuario
import com.example.mobiledevsecops.domain.model.UsuarioPage
import com.example.mobiledevsecops.data.remote.dto.UsuarioDto
import com.example.mobiledevsecops.data.remote.dto.UsuarioListResponse

object UsuarioFixtures {

    val usuario = Usuario(
        id = 1,
        strNombre = "Juan Pérez",
        strCorreoElectronico = "juan@example.com",
        rowVersion = "AAAAAAAAB9E="
    )

    val usuarioDto = UsuarioDto(
        id = 1,
        strNombre = "Juan Pérez",
        strCorreoElectronico = "juan@example.com",
        rowVersion = "AAAAAAAAB9E="
    )

    val usuarioPage = UsuarioPage(
        items = listOf(usuario),
        totalCount = 1,
        pageNumber = 1,
        totalPages = 1
    )

    val usuarioListResponse = UsuarioListResponse(
        items = listOf(usuarioDto),
        totalCount = 1,
        pageNumber = 1,
        pageSize = 10,
        totalPages = 1
    )

    val usuariosMultiPage = (1..15).map { i ->
        Usuario(
            id = i,
            strNombre = "Usuario $i",
            strCorreoElectronico = "usuario$i@example.com",
            rowVersion = "AAAAAAAAB9E="
        )
    }
}
