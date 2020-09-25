package ua.kiev.tinedel.loadbalancer

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ua.kiev.tinedel.loadbalancer.balancer.BalancerException
import ua.kiev.tinedel.loadbalancer.balancer.LoadBalancer
import ua.kiev.tinedel.loadbalancer.balancer.RoundRobinBalancingStrategy
import ua.kiev.tinedel.loadbalancer.provider.DelayedIdentityProvider
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider
import kotlin.system.measureTimeMillis

internal class RoundRobinStrategyIT {

    private val providersList = listOf(
        IdentityProvider("1"),
        IdentityProvider("2"),
        IdentityProvider("3")
    )

    @Test
    fun `when using round robin strategy providers responds in turns`() {
        val loadBalancer = LoadBalancer(
            providersList,
            RoundRobinBalancingStrategy()
        )

        assertEquals(listOf("1", "2", "3", "1"), (1..4).map { loadBalancer.get() }.toList())
    }

    @Test
    fun `concurrent execution allowed in round robin`() {
        val loadBalancer = LoadBalancer(
            listOf(
                DelayedIdentityProvider("1", 1000L),
                DelayedIdentityProvider("2", 1000L),
                DelayedIdentityProvider("3", 1000L)
            ),
            RoundRobinBalancingStrategy()
        )

        val runningTime = measureTimeMillis {
            val res = runBlocking {
                coroutineScope {
                    val p1 = loadBalancer.getAsync(this)
                    val p2 = loadBalancer.getAsync(this)
                    val p3 = loadBalancer.getAsync(this)

                    listOf(p1, p2, p3).awaitAll()
                }
            }

            assertEquals(3, res.size)
            assertTrue(res.contains("1"))
            assertTrue(res.contains("2"))
            assertTrue(res.contains("3"))
        }

        assertTrue(runningTime < 2000)
    }

    @Test
    fun `when provider is included after load balancer creation it is called`() {
        val loadBalancer = LoadBalancer(
            providersList,
            RoundRobinBalancingStrategy()
        )

        val res = mutableListOf(loadBalancer.get(), loadBalancer.get())

        loadBalancer.include(IdentityProvider("4"))

        res.add(loadBalancer.get())
        res.add(loadBalancer.get())

        assertEquals(listOf("1", "2", "3", "4"), res)
    }

    @Test
    fun `when provider is excluded after load balancer creation it is not called`() {
        val p2 = providersList[1]
        val loadBalancer = LoadBalancer(
            providersList,
            RoundRobinBalancingStrategy()
        )

        val res = mutableListOf(loadBalancer.get(), loadBalancer.get())

        loadBalancer.exclude(p2)

        res.add(loadBalancer.get())
        res.add(loadBalancer.get())
        res.add(loadBalancer.get())

        // as round robin implementation is not stable regarding providers' list changes the pattern is a bit weird
        assertEquals(listOf("1", "2", "1", "3", "1"), res)
    }

    @Test
    fun `when all providers are excluded load balancer's get should fail`() {
        val loadBalancer = LoadBalancer(
            providersList,
            RoundRobinBalancingStrategy()
        )

        providersList.forEach { loadBalancer.exclude(it) }

        assertThrows<BalancerException> { loadBalancer.get() }
    }
}
