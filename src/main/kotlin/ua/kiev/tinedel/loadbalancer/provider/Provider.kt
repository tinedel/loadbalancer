package ua.kiev.tinedel.loadbalancer.provider

interface Provider {
    fun get(): String
    fun check(): Boolean
}

class ProviderException(message: String) : RuntimeException(message)

open class IdentityProvider(private val id: String) : Provider {
    override fun get() = id
    override fun check() = true
}
