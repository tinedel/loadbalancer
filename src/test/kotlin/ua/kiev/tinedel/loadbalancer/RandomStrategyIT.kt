package ua.kiev.tinedel.loadbalancer

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ua.kiev.tinedel.loadbalancer.balancer.LoadBalancer
import ua.kiev.tinedel.loadbalancer.balancer.RandomBalancingStrategy
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider

internal class RandomStrategyIT {

    @Test
    fun `when using random strategy one of the providers responds`() {
        val loadBalancer = LoadBalancer(
            listOf(
                IdentityProvider("1"),
                IdentityProvider("2"),
                IdentityProvider("3")
            ),
            RandomBalancingStrategy()
        )

        loadBalancer.use {
            (1..10).map { loadBalancer.get() }.distinct().forEach {
                assertTrue(listOf("1", "2", "3").contains(it))
            }
        }
    }
}
