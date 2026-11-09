package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AccountEntity
import com.example.data.local.BusinessOsDatabase
import com.example.data.local.OrganizationEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Business OS", appName)
    }

    @Test
    fun `multi-tenant room isolation strictly separates organization accounts`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, BusinessOsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.dao()

        val org1Id = dao.insertOrganization(
            OrganizationEntity(
                name = "Org Alpha SARL",
                slug = "org-alpha",
                legalForm = "SARL",
                taxId = "NIU-001",
                ohadaZone = "OHADA - CEMAC",
                currency = "XAF"
            )
        )
        val org2Id = dao.insertOrganization(
            OrganizationEntity(
                name = "Org Beta SAS",
                slug = "org-beta",
                legalForm = "SAS",
                taxId = "SIRET-002",
                ohadaZone = "IFRS-EU",
                currency = "EUR"
            )
        )

        dao.insertAccount(
            AccountEntity(
                organizationId = org1Id,
                name = "Banque Alpha",
                accountNumber = "CM-001",
                ohadaCode = "5211",
                type = "BANK",
                balanceCents = 5_000_000_00L,
                currency = "XAF"
            )
        )
        dao.insertAccount(
            AccountEntity(
                organizationId = org2Id,
                name = "Banque Beta",
                accountNumber = "FR-002",
                ohadaCode = "5120",
                type = "BANK",
                balanceCents = 25_000_00L,
                currency = "EUR"
            )
        )

        val org1Accounts = dao.getAccountsOnce(org1Id)
        val org2Accounts = dao.getAccountsOnce(org2Id)

        assertEquals(1, org1Accounts.size)
        assertEquals("Banque Alpha", org1Accounts.first().name)
        assertEquals(1, org2Accounts.size)
        assertEquals("Banque Beta", org2Accounts.first().name)
        assertTrue(org1Accounts.none { it.organizationId == org2Id })

        db.close()
    }
}
