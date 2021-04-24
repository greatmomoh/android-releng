package co.ld.codechallenge.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.ld.codechallenge.R
import co.ld.codechallenge.network.NetworkManager
import co.ld.codechallenge.util.EspressoIdlingResource
import com.google.gson.GsonBuilder
import matcher.Matchers.withListSize
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection

@RunWith(AndroidJUnit4::class)
class GithubListFragmentTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var retrofit: Retrofit

    @Before
    fun setUp() {
        EspressoIdlingResource.setDefaultIdlingResource()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource())

        val httpClient = OkHttpClient.Builder()

        mockWebServer = MockWebServer()
        retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(getGsonConverter())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient.build())
            .build()
        NetworkManager.getInstance().setExecutor(retrofit)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource())
        mockWebServer.shutdown()
    }

    @Test
    fun confirm_empty_response_displays_nothing_to_user() {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readFileFromResources("SampleErrorData.json"))

        mockWebServer.enqueue(response)

        launchFragmentInContainer<GithubListFragment>(
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.repo_list)).check(matches(withListSize(0)))
    }

    @Test
    fun confirm_correct_response_displays_data_to_user() {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(readFileFromResources("SampleData.json"))

        mockWebServer.enqueue(response)

        launchFragmentInContainer<GithubListFragment>(
            themeResId = R.style.AppTheme
        )

        onView(withId(R.id.repo_list)).check(matches(withListSize(5)))
    }

    private fun getGsonConverter(): GsonConverterFactory {
        return GsonConverterFactory.create(
            GsonBuilder()
                .enableComplexMapKeySerialization()
                .setLenient()
                .create()
        )
    }

    @Throws(IOException::class)
    private fun readFileFromResources(fileName: String): String {
        var inputStream: InputStream? = null
        try {
            inputStream =
                javaClass.classLoader?.getResourceAsStream(fileName)
            val builder = StringBuilder()
            val reader = BufferedReader(InputStreamReader(inputStream))

            var str: String? = reader.readLine()
            while (str != null) {
                builder.append(str)
                str = reader.readLine()
            }
            return builder.toString()
        } finally {
            inputStream?.close()
        }
    }
}
