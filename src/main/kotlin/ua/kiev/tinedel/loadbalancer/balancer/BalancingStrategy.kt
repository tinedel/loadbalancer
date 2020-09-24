package ua.kiev.tinedel.loadbalancer.balancer

import ua.kiev.tinedel.loadbalancer.provider.Provider
import java.util.concurrent.atomic.AtomicInteger

interface BalancingStrategy {
    fun pickOne(providers: List<Provider>): Provider
}

class RandomBalancingStrategy : BalancingStrategy {
    override fun pickOne(providers: List<Provider>) = providers.random()
}

class RoundRobinBalancingStrategy internal constructor(private val current: AtomicInteger) : BalancingStrategy {

    constructor() : this(AtomicInteger(0))

    override fun pickOne(providers: List<Provider>): Provider {
        return providers[current.getAndUpdate { if (it == Int.MAX_VALUE) 0 else it + 1 } % providers.size]
    }
}
