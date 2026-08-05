package com.example.mobiledevsecops.domain.usecase

import com.example.mobiledevsecops.data.remote.SessionExpiredException
import com.example.mobiledevsecops.domain.model.ProductoAutocomplete
import com.example.mobiledevsecops.domain.repository.VentaRepository

sealed class BuscarProductosAutocompleteResult {
    data class Success(val productos: List<ProductoAutocomplete>) : BuscarProductosAutocompleteResult()
    data object SessionExpired : BuscarProductosAutocompleteResult()
}

class BuscarProductosAutocompleteUseCase(
    private val ventaRepository: VentaRepository
) {
    suspend operator fun invoke(
        texto: String,
        maxResultados: Int = 10
    ): BuscarProductosAutocompleteResult {
        return try {
            val productos = ventaRepository.buscarProductosAutocomplete(texto, maxResultados)
            BuscarProductosAutocompleteResult.Success(productos)
        } catch (e: SessionExpiredException) {
            BuscarProductosAutocompleteResult.SessionExpired
        }
    }
}
