package co.ld.codechallenge.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.*
import co.ld.codechallenge.data.DataFactory
import co.ld.codechallenge.model.search.Repo
import co.ld.codechallenge.network.Response
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class GithubViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var githubViewModel: GithubViewModel

    @Before
    fun setUp() {
        githubViewModel = GithubViewModel()
    }

    @Test
    fun `getRepos`() {
        githubViewModel.getRepos(DataFactory.testQuery)
            .observe(
                mockLifecycleOwner(),
                Observer { response -> Assert.assertNotNull(response) })
    }

    @Test
    fun `getReposRx`() {
        LiveDataReactiveStreams.fromPublisher(
            githubViewModel.with(mockLifecycleOwner())
                .getRepoRx(DataFactory.testQuery)
        ).observeForever { response -> Assert.assertNotNull(response) }
    }

    @Test
    fun `with`() {
        Assert.assertNotNull(githubViewModel.with(mockLifecycleOwner()))
    }


    private fun mockLifecycleOwner(): LifecycleOwner {
        val lcOwner = Mockito.mock(LifecycleOwner::class.java)
        val lcRegistry = LifecycleRegistry(lcOwner)
        lcRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        `when`(lcOwner.lifecycle).thenReturn(lcRegistry)
        return lcOwner
    }

}