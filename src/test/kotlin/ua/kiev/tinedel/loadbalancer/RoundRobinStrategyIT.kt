package ua.kiev.tinedel.loadbalancer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.kiev.tinedel.loadbalancer.balancer.LoadBalancer
import ua.kiev.tinedel.loadbalancer.balancer.RoundRobinBalancingStrategy
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider

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
}
