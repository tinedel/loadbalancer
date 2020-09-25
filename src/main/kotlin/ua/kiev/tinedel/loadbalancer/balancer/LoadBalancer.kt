package ua.kiev.tinedel.loadbalancer.balancer

import kotlinx.coroutines.*
import ua.kiev.tinedel.loadbalancer.provider.Provider
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

const val MAX_PROVIDERS = 10

class LoadBalancer(
    providers: List<Provider>,
    private val balancingStrategy: BalancingStrategy,
    private val heartBeatTime: Long = 30_000
) : AutoCloseable {

    data class ProviderState(val enabled: Boolean = true, val successCount: Int = 0)

    private val providers: MutableMap<Provider, ProviderState>

    private val lock = ReentrantReadWriteLock()
    private val providersContext = Executors.newFixedThreadPool(MAX_PROVIDERS).asCoroutineDispatcher()

    private val heartBeatScope = CoroutineScope(providersContext)

    init {
        if (providers.size >= MAX_PROVIDERS) {
            throw BalancerException("Too many providers. Limit the number of providers to $MAX_PROVIDERS")
        }

        this.providers = providers.map { it to ProviderState() }.toMap().toMutableMap()

        heartBeatScope.launch {
            while (isActive) {
                checkProviders()
                delay(heartBeatTime)
            }
        }

    }

    private fun checkProviders() = lock.write {
        providers.keys.forEach {
            providers.computeIfPresent(it) { p, state ->

                when {
                    !p.check() -> ProviderState(false, 0)
                    !state.enabled && state.successCount >= 1 -> ProviderState(true, 0)
                    else -> {
                        state.copy(successCount = state.successCount + 1)
                    }
                }
            }
        }
    }

    fun getAsync(scope: CoroutineScope): Deferred<String> {

        return scope.async(providersContext) {
            get()
        }
    }

    fun get() = lock.read {
        val enabledProviders = providers.filterValues { it.enabled }.keys.toList()
        if (enabledProviders.isEmpty()) {
            throw BalancerException("No providers registered")
        }

        balancingStrategy.pickOne(enabledProviders).get()
    }

    fun exclude(provider: Provider) = lock.write {
        providers.remove(provider)
    }

    fun include(provider: Provider) = lock.write {
        providers.put(provider, ProviderState())
    }

    override fun close() {
        heartBeatScope.cancel()
    }
}

