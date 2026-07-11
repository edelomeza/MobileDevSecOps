package com.example.mobiledevsecops.shared.fixture

import com.example.mobiledevsecops.domain.model.Empleado
import com.example.mobiledevsecops.domain.model.EmpleadoPage

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
