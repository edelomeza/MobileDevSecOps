package com.example.mockapi.data

import com.example.mockapi.model.EmpEmpleadoDto
import java.util.UUID

data class Empleado(
    val id: Int,
    var nombre: String,
    var aPaterno: String?,
    var aMaterno: String?,
    var curp: String?,
    var idTipoEmpleado: Int?,
    val rowVersion: String = UUID.randomUUID().toString()
) {
    fun toDto() = EmpEmpleadoDto(
        id = id,
        strNombre = nombre,
        strAPaterno = aPaterno,
        strAMaterno = aMaterno,
        strCURP = curp,
        idEmpCatTipoEmpleado = idTipoEmpleado,
        rowVersion = rowVersion
    )
}

class EmpleadoDatabase {
    private val empleados = mutableListOf(
        Empleado(1, "Juan", "Perez", "Lopez", "HEXA010101HDFLNN01", 1),
        Empleado(2, "Maria", "Garcia", "Hernandez", "MAGX020202MDFLNN02", 2),
        Empleado(3, "Carlos", "Lopez", "Martinez", "CALX030303HDFLNN03", 1),
        Empleado(4, "Ana", "Martinez", "Rodriguez", "ANMX040404MDFLNN04", 3),
        Empleado(5, "Pedro", "Rodriguez", "Sanchez", "PERX050505HDFLNN05", 1),
        Empleado(6, "Laura", "Hernandez", "Ramirez", "LAHX060606MDFLNN06", 2),
        Empleado(7, "Miguel", "Sanchez", "Torres", "MISX070707HDFLNN07", 3),
        Empleado(8, "Sofia", "Ramirez", "Flores", "SORX080808MDFLNN08", 1),
        Empleado(9, "Diego", "Torres", "Cruz", "DITX090909HDFLNN09", 2),
        Empleado(10, "Valeria", "Flores", "Ortiz", "VAFX101010MDFLNN10", 3),
        Empleado(11, "Alejandro", "Cruz", "Reyes", "ALCX111111HDFLNN11", 1),
        Empleado(12, "Camila", "Ortiz", "Morales", "CAOX121212MDFLNN12", 2),
        Empleado(13, "Fernando", "Reyes", "Castillo", "FERX131313HDFLNN13", 3),
        Empleado(14, "Isabella", "Morales", "Gomez", "ISMX141414MDFLNN14", 1),
        Empleado(15, "Gabriel", "Castillo", "Vargas", "GACX151515HDFLNN15", 2),
        Empleado(16, "Luciana", "Gomez", "Rios", "LUGX161616MDFLNN16", 3),
        Empleado(17, "Mateo", "Vargas", "Navarro", "MAVX171717HDFLNN17", 1),
        Empleado(18, "Emilia", "Rios", "Medina", "EMRX181818MDFLNN18", 2),
        Empleado(19, "Santiago", "Navarro", "Paredes", "SANX191919HDFLNN19", 3),
        Empleado(20, "Catalina", "Medina", "Vega", "CAMX202020MDFLNN20", 1)
    )

    private var nextId = 21

    fun count(): Int = empleados.size

    fun list(page: Int, pageSize: Int): List<Empleado> {
        val from = (page - 1) * pageSize
        return empleados.drop(from).take(pageSize)
    }

    fun search(texto: String?, idTipoEmpleado: Int?, page: Int, pageSize: Int): List<Empleado> {
        var filtered = empleados

        if (!texto.isNullOrBlank()) {
            val searchLower = texto.lowercase()
            filtered = filtered.filter {
                it.nombre.lowercase().contains(searchLower) ||
                (it.aPaterno?.lowercase()?.contains(searchLower) == true) ||
                (it.aMaterno?.lowercase()?.contains(searchLower) == true)
            }
        }

        if (idTipoEmpleado != null && idTipoEmpleado > 0) {
            filtered = filtered.filter { it.idTipoEmpleado == idTipoEmpleado }
        }

        val from = (page - 1) * pageSize
        return filtered.drop(from).take(pageSize)
    }

    fun searchCount(texto: String?, idTipoEmpleado: Int?): Int {
        var filtered = empleados
        if (!texto.isNullOrBlank()) {
            val searchLower = texto.lowercase()
            filtered = filtered.filter {
                it.nombre.lowercase().contains(searchLower) ||
                (it.aPaterno?.lowercase()?.contains(searchLower) == true) ||
                (it.aMaterno?.lowercase()?.contains(searchLower) == true)
            }
        }
        if (idTipoEmpleado != null && idTipoEmpleado > 0) {
            filtered = filtered.filter { it.idTipoEmpleado == idTipoEmpleado }
        }
        return filtered.size
    }

    fun getById(id: Int): Empleado? = empleados.find { it.id == id }

    fun create(nombre: String, aPaterno: String?, aMaterno: String?, curp: String?, idTipoEmpleado: Int?): Empleado {
        val empleado = Empleado(
            id = nextId++,
            nombre = nombre,
            aPaterno = aPaterno,
            aMaterno = aMaterno,
            curp = curp,
            idTipoEmpleado = idTipoEmpleado
        )
        empleados.add(empleado)
        return empleado
    }

    fun update(id: Int, nombre: String, aPaterno: String?, aMaterno: String?, curp: String?, idTipoEmpleado: Int?, rowVersion: String): Empleado? {
        val index = empleados.indexOfFirst { it.id == id }
        if (index == -1) return null
        val emp = empleados[index]
        empleados[index] = emp.copy(
            nombre = nombre,
            aPaterno = aPaterno,
            aMaterno = aMaterno,
            curp = curp,
            idTipoEmpleado = idTipoEmpleado
        )
        return empleados[index]
    }

    fun delete(id: Int, rowVersion: String): Boolean {
        return empleados.removeIf { it.id == id }
    }
}
