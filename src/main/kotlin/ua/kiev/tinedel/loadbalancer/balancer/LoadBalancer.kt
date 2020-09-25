package ua.kiev.tinedel.loadbalancer.balancer

import kotlinx.coroutines.*
import ua.kiev.tinedel.loadbalancer.provider.Provider
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

const val MAX_PROVIDERS = 10

/**
 * Load balancer over list of *providers*
 *
 * This class delegates [get] call to a [Provider.get] selected by provided [BalancingStrategy]
 * In case if there are limits on amount of simultaneous requests underlying providers can handle
 * [RequestThrottler] can be used to not overload [Provider]s even more.
 *
 * Load balancer monitors health of the providers by calling [Provider.check] method, disabling provider until
 * 2 consequent calls to check are successful
 *
 * @property providers initial list of providers to which calls could be dispatched
 * @property balancingStrategy strategy used to balance requests
 * @property requestThrottler throttler which allows to limit amount of simultaneous requests in flight
 * @property heartBeatTime how often to call [Provider.check]
 */
class LoadBalancer(
    providers: List<Provider>,
    private val balancingStrategy: BalancingStrategy,
    private val requestThrottler: RequestThrottler = NoThrottling(),
    private val heartBeatTime: Long = 30_000
) : AutoCloseable {

    data class ProviderState(val enabled: Boolean = true, val successCount: Int = 0)

    private val providers: MutableMap<Provider, ProviderState>

    private val lock = ReentrantReadWriteLock()
    private val providersContext = Executors.newFixedThreadPool(MAX_PROVIDERS).asCoroutineDispatcher()

    private val heartBeatScope = CoroutineScope(providersContext)

    init {
        if (providers.size > MAX_PROVIDERS) {
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

    /**
     * Async interface exposed to clients to facilitate asynchronous use of this component
     * [get()] is executed on dedicated providers context
     * WARNING: can be canceled [supervisorScope] is advised
     */
    fun getAsync(scope: CoroutineScope): Deferred<String> {

        return scope.async(providersContext) {
            get()
        }
    }

    /**
     * Calls underlying [providers] get according to [balancingStrategy] with requests limited by [requestThrottler]
     * If all providers are disabled or providers list is empty exception is thrown
     * If there are more requests at the same time then allowed by [requestThrottler] exception is thrown
     */
    fun get() = lock.read {
        val enabledProviders = providers.filterValues { it.enabled }.keys.toList()
        if (enabledProviders.isEmpty()) {
            throw BalancerException("No providers registered")
        }

        requestThrottler.throttle(enabledProviders) {
            balancingStrategy.pickOne(it).get()
        }
    }

    /**
     * Excludes provider from the balancer.
     *
     * @param provider to be excluded. If not present nothing happens
     */
    fun exclude(provider: Provider) = lock.write {
        providers.remove(provider)
    }

    /**
     * Includes new provider to the balancer
     *
     * @param provider to include. If there is already a provider which is equal to the one supplied nothing happens
     */
    fun include(provider: Provider) = lock.read {
        if (providers.size >= MAX_PROVIDERS) {
            throw BalancerException("Too many providers. Limit the number of providers to $MAX_PROVIDERS")
        } else {
            lock.write {
                providers.put(provider, ProviderState())
            }
        }
    }

    override fun close() {
        heartBeatScope.cancel()
    }
}

