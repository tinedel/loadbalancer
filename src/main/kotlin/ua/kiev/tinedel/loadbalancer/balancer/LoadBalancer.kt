package ua.kiev.tinedel.loadbalancer.balancer

import ua.kiev.tinedel.loadbalancer.provider.Provider

val MAX_PROVIDERS = 10

class LoadBalancer(providers: List<Provider>) {
    private val providers: List<Provider>

    init {
        if (providers.size >= MAX_PROVIDERS) {
            throw BalancerException("Too many providers. Limit the number of providers to $MAX_PROVIDERS")
        }

        // list is immutable so no need to copy at this stage
        this.providers = providers
    }

    fun get() {
        if (providers.size == 0) {
            throw BalancerException("No providers registered")
        }

        // step 2 - we care only about initializing list of providers so delegate to first provider
        providers[0].get()
    }
}
