package ua.kiev.tinedel.loadbalancer.balancer

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider

internal class LoadBalancerTest {

    lateinit var balancingStrategy: BalancingStrategy

    @BeforeEach
    fun initMocks() {
        balancingStrategy = mock()
    }

    @Test
    fun `when registering list of providers less or equal to 10 load balancer is created`() {
        assertDoesNotThrow { LoadBalancer(listOf(IdentityProvider("1")), balancingStrategy) }
    }

    private val excessiveList = (1..11).map { IdentityProvider(it.toString()) }.toList()
    private val fiveProvidersList = (1..5).map { IdentityProvider(it.toString()) }.toList()

    @Test
    fun `when registering list of providers more than 10 exception is thrown`() {
        assertThrows<BalancerException> {
            LoadBalancer(
                excessiveList,
                balancingStrategy
            )
        }
    }

    @Test
    fun `when trying to get from load balancer balancing strategy is invoked`() {
        whenever(balancingStrategy.pickOne(fiveProvidersList)).thenReturn(fiveProvidersList[2])

        val loadBalancer = LoadBalancer(fiveProvidersList, balancingStrategy)

        assertEquals(loadBalancer.get(), "3")

        verify(balancingStrategy).pickOne(fiveProvidersList)
        verifyNoMoreInteractions(balancingStrategy)
    }

    @Test
    fun `when load balancing empty providers list exception must be thrown`() {
        val loadBalancer = LoadBalancer(listOf(), balancingStrategy)

        assertThrows<BalancerException> { loadBalancer.get() }

        verifyZeroInteractions(balancingStrategy)
    }
}
