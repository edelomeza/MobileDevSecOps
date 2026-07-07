package com.example.mobiledevsecops.domain.repository

import com.example.mobiledevsecops.domain.model.UsuarioPage

interface UsuarioRepository {
    suspend fun getUsuarios(page: Int, pageSize: Int = 10): UsuarioPage
    suspend fun buscarUsuarios(texto: String, page: Int, pageSize: Int = 10): UsuarioPage
    suspend fun crearUsuario(strNombre: String, strPWD: String, strCorreoElectronico: String)
    suspend fun actualizarUsuario(id: Int, strNombre: String, strPWD: String, strCorreoElectronico: String, rowVersion: String)
    suspend fun eliminarUsuario(id: Int, rowVersion: String)
}
