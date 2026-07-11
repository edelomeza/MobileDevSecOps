package com.example.mobiledevsecops.shared.fixture

import com.example.mobiledevsecops.domain.model.Empleado
import com.example.mobiledevsecops.domain.model.EmpleadoPage
import com.example.mobiledevsecops.data.remote.dto.EmpleadoDto
import com.example.mobiledevsecops.data.remote.dto.EmpleadoListResponse

object EmpleadoFixtures {

    val empleado = Empleado(
        id = 1,
        strNombre = "Juan",
        strAPaterno = "Perez",
        strAMaterno = "Lopez",
        strCURP = "HEXA010101HDFLNN01",
        idEmpCatTipoEmpleado = 1,
        rowVersion = "AAAAAAAAB9E="
    )

    val empleadoDto = EmpleadoDto(
        id = 1,
        strNombre = "Juan",
        strAPaterno = "Perez",
        strAMaterno = "Lopez",
        strCURP = "HEXA010101HDFLNN01",
        idEmpCatTipoEmpleado = 1,
        rowVersion = "AAAAAAAAB9E="
    )

    val empleadoPage = EmpleadoPage(
        items = listOf(empleado),
        totalCount = 1,
        pageNumber = 1,
        totalPages = 1
    )

    val empleadoListResponse = EmpleadoListResponse(
        items = listOf(empleadoDto),
        totalCount = 1,
        pageNumber = 1,
        pageSize = 10,
        totalPages = 1
    )

    val empleadosMultiPage = (1..15).map { i ->
        Empleado(
            id = i,
            strNombre = "Empleado $i",
            strAPaterno = "APaterno $i",
            strAMaterno = "AMaterno $i",
            strCURP = "CURP${i.toString().padStart(17, '0')}",
            idEmpCatTipoEmpleado = if (i % 2 == 0) 1 else 2,
            rowVersion = "AAAAAAAAB9E="
        )
    }
}
