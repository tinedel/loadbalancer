package ua.kiev.tinedel.loadbalancer.balancer

import ua.kiev.tinedel.loadbalancer.provider.Provider

interface BalancingStrategy {
    fun pickOne(providers: List<Provider>): Provider
}

class RandomBalancingStrategy : BalancingStrategy {
    override fun pickOne(providers: List<Provider>) = providers.random()
}
