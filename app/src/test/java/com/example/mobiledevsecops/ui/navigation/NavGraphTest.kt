package com.example.mobiledevsecops.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavGraphTest {

    @Test
    fun `LOGIN ruta es correcta`() {
        assertEquals("login", Routes.LOGIN)
    }

    @Test
    fun `INDEX ruta es correcta`() {
        assertEquals("index", Routes.INDEX)
    }

    @Test
    fun `USUARIO ruta contiene placeholder page`() {
        assertEquals("usuario/{page}", Routes.USUARIO)
    }

    @Test
    fun `USUARIO_CREAR ruta es correcta`() {
        assertEquals("usuario/crear", Routes.USUARIO_CREAR)
    }

    @Test
    fun `USUARIO_ACTUALIZAR ruta solo tiene id placeholder`() {
        assertTrue(Routes.USUARIO_ACTUALIZAR.contains("{id}"))
        assertFalse(Routes.USUARIO_ACTUALIZAR.contains("{nombre}"))
        assertFalse(Routes.USUARIO_ACTUALIZAR.contains("{correo}"))
        assertFalse(Routes.USUARIO_ACTUALIZAR.contains("{rowVersion}"))
    }

    @Test
    fun `USUARIO_ELIMINAR ruta solo tiene id placeholder`() {
        assertTrue(Routes.USUARIO_ELIMINAR.contains("{id}"))
        assertFalse(Routes.USUARIO_ELIMINAR.contains("{nombre}"))
        assertFalse(Routes.USUARIO_ELIMINAR.contains("{correo}"))
        assertFalse(Routes.USUARIO_ELIMINAR.contains("{rowVersion}"))
    }

    @Test
    fun `navToUsuario genera ruta con page`() {
        assertEquals("usuario/3", Routes.navToUsuario(3))
    }

    @Test
    fun `navToUsuario con page por defecto es 1`() {
        assertEquals("usuario/1", Routes.navToUsuario())
    }

    @Test
    fun `navToActualizar genera ruta solo con id`() {
        val result = Routes.navToActualizar(1)
        assertEquals("usuario/actualizar/1", result)
    }

    @Test
    fun `navToEliminar genera ruta solo con id`() {
        val result = Routes.navToEliminar(10)
        assertEquals("usuario/eliminar/10", result)
    }
}
