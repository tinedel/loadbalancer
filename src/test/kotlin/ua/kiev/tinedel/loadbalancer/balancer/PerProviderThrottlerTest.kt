package ua.kiev.tinedel.loadbalancer.balancer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ua.kiev.tinedel.loadbalancer.provider.IdentityProvider

internal class PerProviderThrottlerTest {

    @Test
    fun `throttler should be successfully created with positive limit`() {
        assertDoesNotThrow { PerProviderThrottler(10) }
    }

    @Test
    fun `throttler should throw if invalid limit`() {
        assertThrows<RuntimeException> { PerProviderThrottler(0) }
        assertThrows<RuntimeException> { PerProviderThrottler(-10) }
    }

    @Test
    fun `no exceptions if below limit`() {
        val throttler = PerProviderThrottler(1)

        assertDoesNotThrow {
            throttler.throttle(listOf(IdentityProvider("1"))) {

            }
        }
    }

    @Test
    fun `exception if over limit`() {
        val throttler = PerProviderThrottler(1)

        assertThrows<BalancerException> {
            runBlocking(Dispatchers.Default) {
                val j1 = launch {
                    throttler.throttle(listOf(IdentityProvider("1"))) {
                        Thread.sleep(100)
                    }
                }

                val j2 = launch {
                    throttler.throttle(listOf(IdentityProvider("1"))) {
                        Thread.sleep(100)
                    }
                }

                j1.join()
                j2.join()
            }
        }
    }
}
