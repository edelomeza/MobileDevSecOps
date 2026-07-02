package com.example.mobiledevsecops.security.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobiledevsecops.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class CertificatePinningTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun network_security_config_existe_y_tiene_estructura_correcta() {
        val parser = context.resources.getXml(R.xml.network_security_config)
        assertNotNull("network_security_config debería existir", parser)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "network-security-config") {
                parser.close()
                return
            }
            eventType = parser.next()
        }
        fail("Elemento raíz network-security-config no encontrado")
        parser.close()
    }

    @Test
    fun network_security_config_tiene_domain_configs() {
        val parser = context.resources.getXml(R.xml.network_security_config)
        var count = 0
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "domain-config") {
                count++
            }
            eventType = parser.next()
        }
        assertTrue("Debería tener al menos un domain-config", count >= 1)
        parser.close()
    }

    @Test
    fun certificate_pinning_tiene_al_menos_un_pin() {
        val parser = context.resources.getXml(R.xml.network_security_config)
        var count = 0
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "pin") {
                count++
            }
            eventType = parser.next()
        }
        assertTrue("Debería tener al menos un certificate pin", count >= 1)
        parser.close()
    }

    @Test
    fun certificate_pin_tiene_digest_sha256() {
        val parser = context.resources.getXml(R.xml.network_security_config)
        var eventType = parser.eventType
        var pinCount = 0
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "pin") {
                pinCount++
                val digest = parser.getAttributeValue(null, "digest")
                assertEquals("SHA-256", digest)
                eventType = parser.next()
                if (eventType == XmlPullParser.TEXT) {
                    assertFalse("El hash del pin no debe estar vacío", parser.text.isBlank())
                }
            }
            eventType = parser.next()
        }
        assertTrue("Debería tener al menos un pin", pinCount >= 1)
        parser.close()
    }

    @Test
    fun certificate_pinning_tiene_fecha_de_expiracion() {
        val parser = context.resources.getXml(R.xml.network_security_config)
        var eventType = parser.eventType
        var pinSetCount = 0
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "pin-set") {
                pinSetCount++
                val expiration = parser.getAttributeValue(null, "expiration")
                assertNotNull("pin-set debe tener expiration", expiration)
                assertFalse("expiration no debe estar vacío", expiration!!.isBlank())
            }
            eventType = parser.next()
        }
        assertTrue("Debería tener al menos un pin-set", pinSetCount >= 1)
        parser.close()
    }

    @Test
    fun certificate_pinning_tiene_dos_pines_para_rotacion_segura() {
        val parser = context.resources.getXml(R.xml.network_security_config)
        var eventType = parser.eventType
        var inPinSet = false
        var currentPinCount = 0
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "pin-set") {
                        inPinSet = true
                        currentPinCount = 0
                    } else if (inPinSet && parser.name == "pin") {
                        currentPinCount++
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "pin-set" && inPinSet) {
                        assertEquals(
                            "Debe tener exactamente 2 pins para permitir rotación segura sin downtime",
                            2, currentPinCount
                        )
                        inPinSet = false
                    }
                }
            }
            eventType = parser.next()
        }
        parser.close()
    }

    @Test
    fun cleartext_traffic_no_permitido_para_dominio_de_produccion() {
        val parser = context.resources.getXml(R.xml.network_security_config)
        var eventType = parser.eventType
        var inDomainConfig = false
        var cleartextValue: String? = null
        val productionDomains = mutableListOf<String>()
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "domain-config" -> {
                            inDomainConfig = true
                            productionDomains.clear()
                            cleartextValue = parser.getAttributeValue(null, "cleartextTrafficPermitted")
                        }
                        "domain" -> {
                            eventType = parser.next()
                            if (eventType == XmlPullParser.TEXT && parser.text.contains("runasp.net")) {
                                productionDomains.add(parser.text)
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "domain-config" && inDomainConfig) {
                        if (productionDomains.isNotEmpty()) {
                            assertEquals(
                                "cleartextTrafficPermitted debe ser false para dominio producción",
                                "false", cleartextValue
                            )
                        }
                        inDomainConfig = false
                    }
                }
            }
            eventType = parser.next()
        }
        parser.close()
    }
}
