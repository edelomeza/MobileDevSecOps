package com.example.mobiledevsecops.data.repository

import com.example.mobiledevsecops.data.remote.EmpleadoApi
import com.example.mobiledevsecops.data.remote.dto.EmpleadoCreateRequest
import com.example.mobiledevsecops.data.remote.dto.EmpleadoDeleteRequest
import com.example.mobiledevsecops.data.remote.dto.EmpleadoListResponse
import com.example.mobiledevsecops.data.remote.dto.EmpleadoUpdateRequest
import com.example.mobiledevsecops.domain.model.EmpCatTipoEmpleado
import com.example.mobiledevsecops.domain.model.Empleado
import com.example.mobiledevsecops.domain.model.EmpleadoPage
import com.example.mobiledevsecops.domain.repository.EmpleadoRepository

class EmpleadoRepositoryImpl(
    private val empleadoApi: EmpleadoApi
) : EmpleadoRepository {

    override suspend fun getEmpleados(page: Int, pageSize: Int): EmpleadoPage {
        val response = empleadoApi.getEmpleados(page, pageSize)
        return response.toDomain()
    }

    override suspend fun buscarEmpleados(texto: String?, idTipoEmpleado: Int?, page: Int, pageSize: Int): EmpleadoPage {
        val response = empleadoApi.buscarEmpleados(texto, idTipoEmpleado, page, pageSize)
        return response.toDomain()
    }

    override suspend fun getEmpleadoById(id: Int): Empleado {
        val dto = empleadoApi.getEmpleadoById(id)
        return Empleado(
            id = dto.id,
            strNombre = dto.strNombre,
            strAPaterno = dto.strAPaterno,
            strAMaterno = dto.strAMaterno,
            strCURP = dto.strCURP,
            idEmpCatTipoEmpleado = dto.idEmpCatTipoEmpleado,
            rowVersion = dto.rowVersion ?: ""
        )
    }

    override suspend fun getTiposEmpleado(): List<EmpCatTipoEmpleado> {
        val response = empleadoApi.getTiposEmpleado()
        return response.items.map {
            EmpCatTipoEmpleado(
                id = it.id,
                strValor = it.strValor,
                strDescripcion = it.strDescripcion
            )
        }
    }

    override suspend fun crearEmpleado(
        strNombre: String,
        strAPaterno: String?,
        strAMaterno: String?,
        strCURP: String?,
        idEmpCatTipoEmpleado: Int?
    ) {
        empleadoApi.crearEmpleado(
            EmpleadoCreateRequest(
                strNombre = strNombre,
                strAPaterno = strAPaterno,
                strAMaterno = strAMaterno,
                strCURP = strCURP,
                idEmpCatTipoEmpleado = idEmpCatTipoEmpleado
            )
        )
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
        empleadoApi.actualizarEmpleado(
            EmpleadoUpdateRequest(
                id = id,
                strNombre = strNombre,
                strAPaterno = strAPaterno,
                strAMaterno = strAMaterno,
                strCURP = strCURP,
                idEmpCatTipoEmpleado = idEmpCatTipoEmpleado,
                rowVersion = rowVersion
            )
        )
    }

    override suspend fun eliminarEmpleado(id: Int, rowVersion: String) {
        empleadoApi.eliminarEmpleado(
            EmpleadoDeleteRequest(
                id = id,
                rowVersion = rowVersion
            )
        )
    }

    private fun EmpleadoListResponse.toDomain(): EmpleadoPage {
        return EmpleadoPage(
            items = items.map { dto ->
                Empleado(
                    id = dto.id,
                    strNombre = dto.strNombre,
                    strAPaterno = dto.strAPaterno,
                    strAMaterno = dto.strAMaterno,
                    strCURP = dto.strCURP,
                    idEmpCatTipoEmpleado = dto.idEmpCatTipoEmpleado,
                    rowVersion = dto.rowVersion ?: ""
                )
            },
            totalCount = totalCount,
            pageNumber = pageNumber,
            totalPages = totalPages
        )
    }
}
