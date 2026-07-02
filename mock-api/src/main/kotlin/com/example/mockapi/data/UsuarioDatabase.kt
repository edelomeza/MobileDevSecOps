package com.example.mockapi.data

import com.example.mockapi.model.UsuarioDto
import com.example.mockapi.model.UserUpdateRequest
import java.util.UUID

data class Usuario(
    val id: Int,
    var nombre: String,
    var correo: String,
    val rowVersion: String = UUID.randomUUID().toString()
) {
    fun toDto() = UsuarioDto(
        id = id,
        strNombre = nombre,
        strCorreoElectronico = correo,
        rowVersion = rowVersion
    )
}

class UsuarioDatabase {
    private val usuarios = mutableListOf(
        Usuario(1, "Juan Perez", "juan@example.com"),
        Usuario(2, "Maria Garcia", "maria@example.com"),
        Usuario(3, "Carlos Lopez", "carlos@example.com"),
        Usuario(4, "Ana Martinez", "ana@example.com"),
        Usuario(5, "Pedro Rodriguez", "pedro@example.com"),
        Usuario(6, "Laura Hernandez", "laura@example.com"),
        Usuario(7, "Miguel Sanchez", "miguel@example.com"),
        Usuario(8, "Sofia Ramirez", "sofia@example.com"),
        Usuario(9, "Diego Torres", "diego@example.com"),
        Usuario(10, "Valeria Flores", "valeria@example.com"),
        Usuario(11, "Alejandro Cruz", "alejandro@example.com"),
        Usuario(12, "Camila Ortiz", "camila@example.com"),
        Usuario(13, "Fernando Reyes", "fernando@example.com"),
        Usuario(14, "Isabella Morales", "isabella@example.com"),
        Usuario(15, "Gabriel Castillo", "gabriel@example.com"),
        Usuario(16, "Luciana Gomez", "luciana@example.com"),
        Usuario(17, "Mateo Vargas", "mateo@example.com"),
        Usuario(18, "Emilia Rios", "emilia@example.com"),
        Usuario(19, "Santiago Navarro", "santiago@example.com"),
        Usuario(20, "Catalina Medina", "catalina@example.com")
    )

    private var nextId = 21

    fun count(): Int = usuarios.size

    fun list(page: Int, pageSize: Int): List<Usuario> {
        val from = (page - 1) * pageSize
        return usuarios.drop(from).take(pageSize)
    }

    fun create(nombre: String, correo: String): Usuario {
        val usuario = Usuario(id = nextId++, nombre = nombre, correo = correo)
        usuarios.add(usuario)
        return usuario
    }

    fun update(id: Int, request: UserUpdateRequest): Usuario? {
        val index = usuarios.indexOfFirst { it.id == id }
        if (index == -1) return null
        val usuario = usuarios[index]
        usuarios[index] = usuario.copy(
            nombre = request.strNombre,
            correo = request.strCorreoElectronico
        )
        return usuarios[index]
    }

    fun delete(id: Int, rowVersion: String): Boolean {
        return usuarios.removeIf { it.id == id }
    }
}
