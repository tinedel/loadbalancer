package ua.kiev.tinedel.loadbalancer.balancer

import ua.kiev.tinedel.loadbalancer.provider.Provider
import java.util.concurrent.atomic.AtomicInteger

/**
 * An interface to be implemented to balance requests and choose the provider to execute the request
 */
interface BalancingStrategy {

    /**
     * Returns provider instance to process the request
     */
    fun pickOne(providers: List<Provider>): Provider
}

/**
 * Random strategy
 *
 * Picks random provider from a given list
 */
class RandomBalancingStrategy : BalancingStrategy {
    override fun pickOne(providers: List<Provider>) = providers.random()
}

/**
 * Class tries to evenly distribute requests by round robin.
 * Maintains internal state and chooses next mod N provider where N is a size of the given list
 *
 * Can break round robin behaviour if list is changing the size
 * Works properly under assumption that list rarely changes size.
 */
class RoundRobinBalancingStrategy internal constructor(private val current: AtomicInteger) : BalancingStrategy {

    constructor() : this(AtomicInteger(0))

    override fun pickOne(providers: List<Provider>): Provider {
        return providers[current.getAndUpdate { if (it == Int.MAX_VALUE) 0 else it + 1 } % providers.size]
    }
}
