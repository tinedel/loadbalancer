package ua.kiev.tinedel.loadbalancer.balancer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider
import java.util.concurrent.atomic.AtomicInteger

internal class RoundRobinBalancingStrategyTest {

    private val ip1 = IdentityProvider("1")
    private val ip2 = IdentityProvider("2")

    private val providers = listOf(ip1, ip2)

    @Test
    fun `when pickOne is called round robin strategy is selecting next`() {
        val strategy = RoundRobinBalancingStrategy()

        assertEquals(ip1, strategy.pickOne(providers))
        assertEquals(ip2, strategy.pickOne(providers))
        assertEquals(ip1, strategy.pickOne(providers))
    }

    @Test
    fun `when pickone was called MAX_INT times`() {
        val strategy = RoundRobinBalancingStrategy(AtomicInteger(Int.MAX_VALUE))

        strategy.pickOne(providers) // last one before overflow
        assertEquals(ip1, strategy.pickOne(providers))
    }
}
