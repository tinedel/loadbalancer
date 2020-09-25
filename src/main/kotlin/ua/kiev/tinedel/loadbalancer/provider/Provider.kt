package ua.kiev.tinedel.loadbalancer.provider

interface Provider {
    fun get(): String
}

open class IdentityProvider(private val id: String) : Provider {
    override fun get() = id
}
