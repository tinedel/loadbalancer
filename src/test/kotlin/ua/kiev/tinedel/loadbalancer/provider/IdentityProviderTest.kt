package ua.kiev.tinedel.loadbalancer.provider

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class IdentityProviderTest {

    @Test
    fun `when getting value from identity provider the id is returned`() {
        val provider = IdentityProvider("someIdentity")

        assertEquals("someIdentity", provider.get())
    }
}
