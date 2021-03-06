package com.znggis.sampleservicebookingapp.repo

import com.google.common.truth.Truth
import com.znggis.sampleservicebookingapp.di.PostExecutionThread
import com.znggis.sampleservicebookingapp.repo.remote.ActionResult
import com.znggis.sampleservicebookingapp.repo.remote.api.HOME_PAGE_JSON
import com.znggis.sampleservicebookingapp.repo.remote.api.HomeApiImpl
import com.znggis.sampleservicebookingapp.repo.remote.base.RetrofitCreator
import com.znggis.sampleservicebookingapp.repo.remote.mapper.HomeDetailMapper
import com.znggis.sampleservicebookingapp.repo.remote.service.HomeService
import com.znggis.sampleservicebookingapp.util.MockResponseFileReader
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.net.HttpURLConnection


@ExperimentalCoroutinesApi
class HomeRepositoryTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var homeRepository: HomeRepository

    private val coroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val home = mockWebServer.url("")
        val homeService =
            RetrofitCreator(home.toUrl().toString()).build().create(HomeService::class.java)
        val apiHome = HomeApiImpl(homeService, HomeDetailMapper())

        val executionThread = object : PostExecutionThread {
            override val main: CoroutineDispatcher
                get() = coroutineDispatcher
            override val io: CoroutineDispatcher
                get() = coroutineDispatcher
            override val default: CoroutineDispatcher
                get() = coroutineDispatcher

        }
        homeRepository = HomeRepositoryImpl(apiHome, executionThread)
    }

    @Test
    fun `Given request data, When Status 404, Then check if error throw from Network Layer`() =
        runBlocking {

            val response = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
            mockWebServer.enqueue(response)

            val result = homeRepository.getHomeDetail().first()
            Truth.assertThat(result).isInstanceOf(ActionResult.Loading::class.java)

            val result2 = homeRepository.getHomeDetail().drop(1).first()
            Truth.assertThat(result2).isInstanceOf(ActionResult.Error::class.java)

        }

    @Test
    fun `Given request data, When Status 200-Ok, Then check if data is in Network Layer`() =
        runBlocking {
            val response = MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(MockResponseFileReader(HOME_PAGE_JSON).content)
            mockWebServer.enqueue(response)


            val result = homeRepository.getHomeDetail().first()
            Truth.assertThat(result).isInstanceOf(ActionResult.Loading::class.java)

            val result2 = homeRepository.getHomeDetail().drop(1).first()
            Truth.assertThat(result2).isInstanceOf(ActionResult.Success::class.java)

        }

    @After
    fun onFinished() {
        mockWebServer.shutdown()
    }
}
