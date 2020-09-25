package ua.kiev.tinedel.loadbalancer.provider

class DelayedIdentityProvider(id: String, private val millis: Long) : IdentityProvider(id) {

    override fun get(): String {
        Thread.sleep(millis)
        return super.get()
    }
}

class FaultyProvider(id: String) : IdentityProvider(id) {

    private var failed = true
    override fun check() = !failed

    fun fail() {
        failed = true
    }

    fun restore() {
        failed = false
    }
}
