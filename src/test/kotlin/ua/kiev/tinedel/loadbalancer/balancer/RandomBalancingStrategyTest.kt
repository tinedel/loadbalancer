package ua.kiev.tinedel.loadbalancer.balancer

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider

internal class RandomBalancingStrategyTest {

    @Test
    fun `one of the providers present in the list is chosen`() {
        val balancingStrategy = RandomBalancingStrategy()

        val providers = listOf(IdentityProvider("1"), IdentityProvider("2"))
        val provider = balancingStrategy.pickOne(providers)

        assertTrue(providers.contains(provider))
    }
}
