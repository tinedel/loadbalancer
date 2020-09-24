package ua.kiev.tinedel.loadbalancer.provider

interface Provider {
    fun get(): String
}

class IdentityProvider(private val id: String) : Provider {
    override fun get() = id
}
