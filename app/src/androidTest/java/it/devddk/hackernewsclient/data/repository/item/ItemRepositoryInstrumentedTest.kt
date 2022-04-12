package it.devddk.hackernewsclient.data.repository.item

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import it.devddk.hackernewsclient.data.di.networkingModule
import it.devddk.hackernewsclient.data.di.repositoryModule
import it.devddk.hackernewsclient.domain.repository.ItemRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.logger.Level
import org.koin.test.KoinTestRule

@RunWith(AndroidJUnit4::class)
class ItemRepositoryInstrumentedTest : KoinComponent {

    val itemRepository: ItemRepository by inject()

    @Before
    fun setupKoin() {
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().context)!!

        loadKoinModules(listOf(repositoryModule, networkingModule))
    }

    @After
    fun tearDownKoin() {
        unloadKoinModules(listOf(repositoryModule, networkingModule))
    }

    @Test
    fun getItemNumberSucceds(): Unit {
        runBlocking {
            val result = itemRepository.getItemById(1)
            result.getOrThrow()
            assertTrue(result.isSuccess)
        }
    }
}