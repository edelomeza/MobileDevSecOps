package com.example.mobiledevsecops.shared.fake

import com.example.mobiledevsecops.data.remote.ConflictException
import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.EmpCatTipoEmpleado
import com.example.mobiledevsecops.domain.model.Empleado
import com.example.mobiledevsecops.domain.model.EmpleadoPage
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository
import com.example.mobiledevsecops.shared.fixture.EmpleadoFixtures

class FakeEmpleadoRepository : EmpleadoRepository {

    var shouldThrowException = false
    var shouldThrowSessionExpired = false
    var shouldThrowConflict = false
    var exceptionMessage = "Error de conexión"

    private val _empleados = mutableListOf<Empleado>()
    val empleados: List<Empleado> get() = _empleados.toList()

    var currentPage = 1
    var totalPages = 1
    var totalCount = 0

    private val _tiposEmpleado = mutableListOf(
        EmpCatTipoEmpleado(1, "Desarrollador", "Desarrollador de software"),
        EmpCatTipoEmpleado(2, "QA", "Control de calidad")
    )

    fun givenEmpleados(empleados: List<Empleado>) {
        _empleados.clear()
        _empleados.addAll(empleados)
        totalCount = empleados.size
        totalPages = maxOf(1, (empleados.size + 7) / 8)
    }

    override suspend fun getEmpleados(page: Int, pageSize: Int): EmpleadoPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        currentPage = page
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, _empleados.size)
        val items = if (start < _empleados.size) _empleados.subList(start, end) else emptyList()

        return EmpleadoPage(
            items = items,
            totalCount = totalCount,
            pageNumber = page,
            totalPages = totalPages
        )
    }

    override suspend fun buscarEmpleados(texto: String?, idTipoEmpleado: Int?, page: Int, pageSize: Int): EmpleadoPage {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        var filtered: List<Empleado> = _empleados
        if (!texto.isNullOrBlank()) {
            val searchLower = texto.lowercase()
            filtered = filtered.filter {
                it.strNombre.lowercase().contains(searchLower) ||
                (it.strAPaterno?.lowercase()?.contains(searchLower) == true) ||
                (it.strAMaterno?.lowercase()?.contains(searchLower) == true)
            }
        }
        if (idTipoEmpleado != null && idTipoEmpleado > 0) {
            filtered = filtered.filter { it.idEmpCatTipoEmpleado == idTipoEmpleado }
        }

        currentPage = page
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, filtered.size)
        val items = if (start < filtered.size) filtered.subList(start, end) else emptyList()

        return EmpleadoPage(
            items = items,
            totalCount = filtered.size,
            pageNumber = page,
            totalPages = maxOf(1, (filtered.size + pageSize - 1) / pageSize)
        )
    }

    override suspend fun getEmpleadoById(id: Int): Empleado {
        return _empleados.firstOrNull { it.id == id }
            ?: throw Exception("Empleado no encontrado")
    }

    override suspend fun getTiposEmpleado(): List<EmpCatTipoEmpleado> {
        return _tiposEmpleado.toList()
    }

    override suspend fun crearEmpleado(
        strNombre: String,
        strAPaterno: String?,
        strAMaterno: String?,
        strCURP: String?,
        idEmpCatTipoEmpleado: Int?
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        _empleados.add(
            Empleado(
                id = _empleados.size + 1,
                strNombre = strNombre,
                strAPaterno = strAPaterno,
                strAMaterno = strAMaterno,
                strCURP = strCURP,
                idEmpCatTipoEmpleado = idEmpCatTipoEmpleado,
                rowVersion = "AAAAAAAAB9E="
            )
        )
        totalCount = _empleados.size
    }

    override suspend fun actualizarEmpleado(
        id: Int,
        strNombre: String,
        strAPaterno: String?,
        strAMaterno: String?,
        strCURP: String?,
        idEmpCatTipoEmpleado: Int?,
        rowVersion: String
    ) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        val index = _empleados.indexOfFirst { it.id == id }
        if (index != -1) {
            _empleados[index] = _empleados[index].copy(
                strNombre = strNombre,
                strAPaterno = strAPaterno,
                strAMaterno = strAMaterno,
                strCURP = strCURP,
                idEmpCatTipoEmpleado = idEmpCatTipoEmpleado
            )
        }
    }

    override suspend fun eliminarEmpleado(id: Int, rowVersion: String) {
        if (shouldThrowSessionExpired) throw SessionExpiredException()
        if (shouldThrowConflict) throw ConflictException()
        if (shouldThrowException) throw Exception(exceptionMessage)

        _empleados.removeAll { it.id == id }
        totalCount = _empleados.size
    }
}
