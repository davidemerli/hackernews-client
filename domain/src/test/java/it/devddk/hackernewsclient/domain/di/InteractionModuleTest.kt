package it.devddk.hackernewsclient.domain.di

import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class InteractionModuleTest : KoinTest {
    @Test
    fun interactionModule_check() {
        checkModules {
            modules(interactionModule)
        }
    }
}

