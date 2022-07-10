package it.devddk.hackernewsclient.data.repository.feedback

import android.app.Activity
import androidx.test.core.app.ActivityScenario.launch
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import io.mockk.verifyOrder
import it.devddk.hackernewsclient.data.di.databaseModule
import it.devddk.hackernewsclient.data.di.networkingModule
import it.devddk.hackernewsclient.data.di.repositoryModule
import it.devddk.hackernewsclient.data.utils.MockTask
import it.devddk.hackernewsclient.domain.model.feedback.Feedback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.Executor
import kotlin.test.assertTrue

class RepositoryFeedbackTest : KoinTest {

    val dut = FeedbackRepositoryImpl()
    val feedback = Feedback(1020, "bello", 2)

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val instantiateModules = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(repositoryModule, networkingModule, databaseModule)
        Timber.plant(Timber.DebugTree())
    }

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        // Your way to build a Mock here
        mockkClass(clazz)

    }

    @Test
    fun testRepositorySucceeds() = runTest {
        declareMock<DatabaseReference>(named("feedback")) {
            every { push() } answers {
                this@declareMock
            }
            every { setValue(any()) } answers {
                MockTask()
            }
        }

        val mockDbRef = get<DatabaseReference>(named("feedback"))

        val result = dut.sendFeedback(feedback)


        verifyOrder {
            mockDbRef.push()
            mockDbRef.setValue(any())
        }

        assertTrue("Sending feedback should be successful") {
            result.isSuccess
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testRepositoryHandlesFailure() = runTest {
        declareMock<DatabaseReference>(named("feedback")) {
            every { push() } answers {
                this@declareMock
            }
            every { setValue(any()) } answers {
                throw Exception("Failed")
            }
        }

        val mockDbRef = get<DatabaseReference>(named("feedback"))

        val result = dut.sendFeedback(feedback)


        verifyOrder {
            mockDbRef.push()
            mockDbRef.setValue(any())
        }

        assertTrue("Sending feedback should fail") {
            result.isFailure
        }

    }

    @After
    fun tearDown() {
        stopKoin()
    }
}