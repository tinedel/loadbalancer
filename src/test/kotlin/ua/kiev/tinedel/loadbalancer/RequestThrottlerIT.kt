package ua.kiev.tinedel.loadbalancer

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ua.kiev.tinedel.loadbalancer.balancer.BalancerException
import ua.kiev.tinedel.loadbalancer.balancer.LoadBalancer
import ua.kiev.tinedel.loadbalancer.balancer.PerProviderThrottler
import ua.kiev.tinedel.loadbalancer.balancer.RoundRobinBalancingStrategy
import ua.kiev.tinedel.loadbalancer.provider.DelayedIdentityProvider
import kotlin.system.measureTimeMillis

internal class RequestThrottlerIT {

    @Test
    fun `no exceptions if below limit`() {
        val loadBalancer = LoadBalancer(
            listOf(
                DelayedIdentityProvider("1", 1000L),
                DelayedIdentityProvider("2", 1000L),
                DelayedIdentityProvider("3", 1000L)
            ),
            RoundRobinBalancingStrategy(),
            PerProviderThrottler(3)
        )

        loadBalancer.use {
            val runningTime = measureTimeMillis {
                val res = runBlocking {
                    val p1 = loadBalancer.getAsync(this)
                    val p2 = loadBalancer.getAsync(this)
                    val p3 = loadBalancer.getAsync(this)

                    listOf(p1, p2, p3).awaitAll()
                }

                Assertions.assertEquals(3, res.size)
                Assertions.assertTrue(res.contains("1"))
                Assertions.assertTrue(res.contains("2"))
                Assertions.assertTrue(res.contains("3"))
            }

            Assertions.assertTrue(runningTime < 2000)
        }

    }

    @Test
    fun `throws exceptions if above limit for the one which is above limit`() {
        val loadBalancer = LoadBalancer(
            listOf(
                DelayedIdentityProvider("1", 200L),
                DelayedIdentityProvider("2", 1000L),
                DelayedIdentityProvider("3", 1000L)
            ),
            RoundRobinBalancingStrategy(),
            PerProviderThrottler(1)
        )

        loadBalancer.use {
            val res = runBlocking {
                supervisorScope {
                    val p1 = loadBalancer.getAsync(this)
                    val p2 = loadBalancer.getAsync(this)
                    val p3 = loadBalancer.getAsync(this)
                    delay(10) // to ensure p4 is throwing
                    val p4 = loadBalancer.getAsync(this)

                    delay(310)

                    val p5 = loadBalancer.getAsync(this)
                    assertThrows<BalancerException> { p4.await() }

                    listOf(p1, p2, p3, p5).awaitAll()
                }
            }

            Assertions.assertEquals(4, res.size)
            Assertions.assertTrue(res.contains("1"))
            Assertions.assertTrue(res.contains("2"))
            Assertions.assertTrue(res.contains("3"))
        }
    }
}
