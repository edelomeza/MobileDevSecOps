package com.example.mobiledevsecops.domain.repository

import com.example.mobiledevsecops.domain.model.EmpCatTipoEmpleado
import com.example.mobiledevsecops.domain.model.Empleado
import com.example.mobiledevsecops.domain.model.EmpleadoPage

interface EmpleadoRepository {
    suspend fun getEmpleados(page: Int, pageSize: Int = 10): EmpleadoPage
    suspend fun buscarEmpleados(texto: String?, idTipoEmpleado: Int?, page: Int, pageSize: Int = 10): EmpleadoPage
    suspend fun getEmpleadoById(id: Int): Empleado
    suspend fun getTiposEmpleado(): List<EmpCatTipoEmpleado>
    suspend fun crearEmpleado(
        strNombre: String,
        strAPaterno: String?,
        strAMaterno: String?,
        strCURP: String?,
        idEmpCatTipoEmpleado: Int?
    )
    suspend fun actualizarEmpleado(
        id: Int,
        strNombre: String,
        strAPaterno: String?,
        strAMaterno: String?,
        strCURP: String?,
        idEmpCatTipoEmpleado: Int?,
        rowVersion: String
    )
    suspend fun eliminarEmpleado(id: Int, rowVersion: String)
}
