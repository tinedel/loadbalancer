package ua.kiev.tinedel.loadbalancer.balancer

import ua.kiev.tinedel.loadbalancer.provider.Provider
import java.util.concurrent.atomic.AtomicInteger

/**
 * Request throttlers need to implement this interface to be able to control throughput of the component
 */
interface RequestThrottler {
    fun <T> throttle(providers: List<Provider>, block: (List<Provider>) -> T): T
}

/**
 * Class represent no restrictions throttling
 */
class NoThrottling : RequestThrottler {
    override fun <T> throttle(providers: List<Provider>, block: (List<Provider>) -> T): T {
        return block(providers)
    }
}

/**
 * Class keeps track of requests in flight and allows to decline the request if amount of them
 * is more then allowed on average per active provider
 */
class PerProviderThrottler(private val perProviderLimit: Int) : RequestThrottler {

    init {
        require(perProviderLimit > 0) { "Amount of requests allowed per provider should be more than 0" }
    }

    private val currentInFlight = AtomicInteger(0)

    override fun <T> throttle(providers: List<Provider>, block: (List<Provider>) -> T): T {
        try {
            if (currentInFlight.incrementAndGet() > perProviderLimit * providers.size) {
                throw BalancerException("Cluster overloaded. Try again later")
            }

            return block(providers)
        } finally {
            currentInFlight.decrementAndGet()
        }
    }
}
