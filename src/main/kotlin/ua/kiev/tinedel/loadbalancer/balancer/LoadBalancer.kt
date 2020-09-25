package ua.kiev.tinedel.loadbalancer.balancer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import ua.kiev.tinedel.loadbalancer.provider.Provider
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

const val MAX_PROVIDERS = 10

class LoadBalancer(providers: List<Provider>, private val balancingStrategy: BalancingStrategy) {
    private val providers: MutableList<Provider>

    private val lock = ReentrantReadWriteLock()
    private val providersContext = Executors.newFixedThreadPool(MAX_PROVIDERS).asCoroutineDispatcher()

    init {
        if (providers.size >= MAX_PROVIDERS) {
            throw BalancerException("Too many providers. Limit the number of providers to $MAX_PROVIDERS")
        }

        this.providers = providers.toMutableList()
    }

    fun getAsync(scope: CoroutineScope): Deferred<String> {

        return scope.async(providersContext) {
            get()
        }
    }

    fun get() = lock.read {
        if (providers.isEmpty()) {
            throw BalancerException("No providers registered")
        }

        balancingStrategy.pickOne(providers.toList()).get()
    }

    fun exclude(provider: Provider) = lock.write {
        providers.remove(provider)
    }

    fun include(provider: Provider) = lock.write {
        providers.add(provider)
    }
}

