package ua.kiev.tinedel.loadbalancer.balancer

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider

internal class LoadBalancerTest {

    @Test
    fun `when registering list of providers less or equal to 10 load balancer is created`() {
        assertDoesNotThrow { LoadBalancer(listOf(IdentityProvider("1"))) }
    }

    @Test
    fun `when registering list of providers more than 10 exception is thrown`() {
        assertThrows<BalancerException> { LoadBalancer((1..11).map { IdentityProvider(it.toString()) }.toList()) }
    }
}
