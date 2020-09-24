package ua.kiev.tinedel.loadbalancer.provider

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class IdentityProviderTest {

    @Test
    fun identityProvider_returnsStoredIdentity() {
        val provider = IdentityProvider("someIdentity")

        assertEquals("someIdentity", provider.get())
    }
}
