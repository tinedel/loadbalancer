package ua.kiev.tinedel.loadbalancer.provider

class DelayedIdentityProvider(id: String, private val millis: Long) : IdentityProvider(id) {

    override fun get(): String {
        Thread.sleep(millis)
        return super.get()
    }
}

class FaultyProvider(id: String) : IdentityProvider(id) {
    override fun check() = false
}
