package com.example.mobiledevsecops.ui.index

import com.example.mobiledevsecops.domain.usecase.LogoutUseCase
import com.example.mobiledevsecops.shared.fake.FakeAuthRepository
import com.example.mobiledevsecops.shared.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IndexViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private lateinit var fakeRepo: FakeAuthRepository
    private lateinit var viewModel: IndexViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeAuthRepository()
        val logoutUseCase = LogoutUseCase(fakeRepo)
        viewModel = IndexViewModel(logoutUseCase)
    }

    @Test
    fun `estado inicial tiene isLoggingOut en false`() {
        assertEquals(false, viewModel.uiState.value.isLoggingOut)
    }

    @Test
    fun `onLogoutClicked cambia isLoggingOut a true y emite NavigateToLogin`() = runTest {
        val events = mutableListOf<IndexEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onLogoutClicked()

        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isLoggingOut)
        assertTrue(events.any { it is IndexEvent.NavigateToLogin })
        assertEquals(null, fakeRepo.getToken())
        job.cancel()
    }

    @Test
    fun `onNavigateToUsuario emite NavigateToUsuario`() = runTest {
        val events = mutableListOf<IndexEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onNavigateToUsuario()

        advanceUntilIdle()

        assertTrue(events.any { it is IndexEvent.NavigateToUsuario })
        assertFalse(events.any { it is IndexEvent.NavigateToLogin })
        job.cancel()
    }
}
