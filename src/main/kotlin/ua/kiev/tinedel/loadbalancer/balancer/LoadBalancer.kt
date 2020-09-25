package ua.kiev.tinedel.loadbalancer.balancer

import kotlinx.coroutines.*
import ua.kiev.tinedel.loadbalancer.provider.Provider
import java.util.concurrent.Executors

const val MAX_PROVIDERS = 10

class LoadBalancer(providers: List<Provider>, private val balancingStrategy: BalancingStrategy) {
    private val providers: List<Provider>

    private val providersContext = Executors.newFixedThreadPool(MAX_PROVIDERS).asCoroutineDispatcher()

    init {
        if (providers.size >= MAX_PROVIDERS) {
            throw BalancerException("Too many providers. Limit the number of providers to $MAX_PROVIDERS")
        }

        // list is immutable so no need to copy at this stage
        this.providers = providers
    }

    fun getAsync(scope: CoroutineScope): Deferred<String> {
        if (providers.isEmpty()) {
            throw BalancerException("No providers registered")
        }

        return scope.async(providersContext) {
            balancingStrategy.pickOne(providers).get()
        }
    }

    fun get() = runBlocking {
        getAsync(this).await()
    }
}
