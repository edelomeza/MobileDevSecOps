package com.example.mockapi.data

import com.example.mockapi.model.EmpCatTipoEmpleadoDto

data class TipoEmpleado(
    val id: Int,
    val strValor: String,
    val strDescripcion: String
) {
    fun toDto() = EmpCatTipoEmpleadoDto(
        id = id,
        strValor = strValor,
        strDescripcion = strDescripcion
    )
}

class TipoEmpleadoDatabase {
    private val tipos = mutableListOf(
        TipoEmpleado(1, "Desarrollador", "Desarrollador de software"),
        TipoEmpleado(2, "QA", "Control de calidad"),
        TipoEmpleado(3, "Manager", "Gerente de proyecto"),
        TipoEmpleado(4, "DevOps", "Operaciones de desarrollo"),
        TipoEmpleado(5, "Analista", "Analista de sistemas"),
        TipoEmpleado(6, "Soporte", "Soporte técnico")
    )

    fun count(): Int = tipos.size

    fun list(page: Int, pageSize: Int): List<TipoEmpleado> {
        val from = (page - 1) * pageSize
        return tipos.drop(from).take(pageSize)
    }

    fun getById(id: Int): TipoEmpleado? = tipos.find { it.id == id }
}
