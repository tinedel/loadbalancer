package ua.kiev.tinedel.loadbalancer

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ua.kiev.tinedel.loadbalancer.balancer.LoadBalancer
import ua.kiev.tinedel.loadbalancer.balancer.RoundRobinBalancingStrategy
import ua.kiev.tinedel.loadbalancer.provider.DelayedIdentityProvider
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider
import kotlin.system.measureTimeMillis

internal class RoundRobinStrategyIT {

    @Test
    fun `when using round robin strategy providers responds in turns`() {
        val loadBalancer = LoadBalancer(
            listOf(
                IdentityProvider("1"),
                IdentityProvider("2"),
                IdentityProvider("3")
            ),
            RoundRobinBalancingStrategy()
        )

        assertEquals(listOf("1", "2", "3", "1"), (1..4).map { loadBalancer.get() }.toList())
    }

    @Test
    fun `concurrent execution allowed and round robin`() {
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

}
