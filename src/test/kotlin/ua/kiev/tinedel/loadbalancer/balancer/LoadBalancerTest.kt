package ua.kiev.tinedel.loadbalancer.balancer

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider
import ua.kiev.tinedel.loadbalancer.provider.Provider

internal class LoadBalancerTest {

    lateinit var balancingStrategy: BalancingStrategy

    @BeforeEach
    fun initMocks() {
        balancingStrategy = mock()
    }

    @Test
    fun `when registering list of providers less or equal to 10 load balancer is created`() {
        assertDoesNotThrow { LoadBalancer(listOf(IdentityProvider("1")), balancingStrategy).use { } }
    }

    private val excessiveList = (1..11).map { IdentityProvider(it.toString()) }.toList()
    private val fiveProvidersList = (1..5).map { IdentityProvider(it.toString()) }.toList()

    @Test
    fun `when registering list of providers more than 10 exception is thrown`() {
        assertThrows<BalancerException> {
            LoadBalancer(
                excessiveList,
                balancingStrategy
            ).use { }
        }
    }

    @Test
    fun `when trying to get from load balancer balancing strategy is invoked`() {
        whenever(balancingStrategy.pickOne(fiveProvidersList)).thenReturn(fiveProvidersList[2])

        val loadBalancer = LoadBalancer(fiveProvidersList, balancingStrategy)

        loadBalancer.use {
            assertEquals(loadBalancer.get(), "3")
        }

        verify(balancingStrategy).pickOne(fiveProvidersList)
        verifyNoMoreInteractions(balancingStrategy)
    }

    @Test
    fun `when load balancing empty providers list exception must be thrown`() {
        val loadBalancer = LoadBalancer(listOf(), balancingStrategy)

        loadBalancer.use {
            assertThrows<BalancerException> { loadBalancer.get() }
        }

        verifyZeroInteractions(balancingStrategy)
    }

    @Test
    fun `when excluding provider it should not be presented to balancing strategy`() {
        whenever(balancingStrategy.pickOne(any())).thenReturn(fiveProvidersList[2])

        val loadBalancer = LoadBalancer(fiveProvidersList, balancingStrategy)

        loadBalancer.use {
            loadBalancer.exclude(fiveProvidersList[1])
            loadBalancer.get()
        }

        val providersListCapture = argumentCaptor<List<Provider>>()

        verify(balancingStrategy).pickOne(providersListCapture.capture())
        verifyNoMoreInteractions(balancingStrategy)

        val actualProvidersList = providersListCapture.firstValue
        assertEquals(4, actualProvidersList.size)
        assertFalse(actualProvidersList.contains(fiveProvidersList[1]))
    }

    @Test
    fun `when added provider it should be presented to balancing strategy`() {
        whenever(balancingStrategy.pickOne(any())).thenReturn(fiveProvidersList[2])

        val loadBalancer = LoadBalancer(fiveProvidersList, balancingStrategy)
        val provider = IdentityProvider("6")

        loadBalancer.use {
            loadBalancer.include(provider)
            loadBalancer.get()
        }

        val providersListCapture = argumentCaptor<List<Provider>>()

        verify(balancingStrategy).pickOne(providersListCapture.capture())
        verifyNoMoreInteractions(balancingStrategy)

        val actualProvidersList = providersListCapture.firstValue
        assertEquals(6, actualProvidersList.size)
        assertTrue(actualProvidersList.contains(provider))
    }
}
