package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessOsDao {

    // --- MULTI-TENANT ORGANIZATIONS & WORKSPACES ---
    @Query("SELECT * FROM organizations ORDER BY id ASC")
    fun observeOrganizations(): Flow<List<OrganizationEntity>>

    @Query("SELECT * FROM organizations ORDER BY id ASC")
    suspend fun getOrganizationsList(): List<OrganizationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(org: OrganizationEntity): Long

    @Query("SELECT * FROM workspaces WHERE organizationId = :orgId ORDER BY isDefault DESC, id ASC")
    fun observeWorkspaces(orgId: Long): Flow<List<WorkspaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: WorkspaceEntity): Long

    @Query("SELECT * FROM memberships WHERE organizationId = :orgId ORDER BY id ASC")
    fun observeMemberships(orgId: Long): Flow<List<UserRoleMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(member: UserRoleMemberEntity): Long

    // --- FINANCES: ACCOUNTS & TRANSACTIONS ---
    @Query("SELECT * FROM accounts WHERE organizationId = :orgId ORDER BY ohadaCode ASC")
    fun observeAccounts(orgId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE organizationId = :orgId ORDER BY ohadaCode ASC")
    suspend fun getAccountsOnce(orgId: Long): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("SELECT * FROM transactions WHERE organizationId = :orgId ORDER BY timestamp DESC")
    fun observeTransactions(orgId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE organizationId = :orgId ORDER BY timestamp DESC")
    suspend fun getTransactionsOnce(orgId: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(tx: TransactionEntity)

    // --- ACCOUNTING (OHADA) ---
    @Query("SELECT * FROM accounting_entries WHERE organizationId = :orgId ORDER BY entryDate DESC")
    fun observeAccountingEntries(orgId: Long): Flow<List<AccountingEntryEntity>>

    @Query("SELECT * FROM accounting_entries WHERE organizationId = :orgId ORDER BY entryDate DESC")
    suspend fun getAccountingEntriesOnce(orgId: Long): List<AccountingEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountingEntry(entry: AccountingEntryEntity): Long

    @Update
    suspend fun updateAccountingEntry(entry: AccountingEntryEntity)

    // --- CRM, PROSPECTS, CLIENTS & SALES ---
    @Query("SELECT * FROM crm_contacts WHERE organizationId = :orgId ORDER BY createdAt DESC")
    fun observeContacts(orgId: Long): Flow<List<CrmContactEntity>>

    @Query("SELECT * FROM crm_contacts WHERE organizationId = :orgId ORDER BY createdAt DESC")
    suspend fun getContactsOnce(orgId: Long): List<CrmContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: CrmContactEntity): Long

    @Update
    suspend fun updateContact(contact: CrmContactEntity)

    @Query("SELECT * FROM commercial_docs WHERE organizationId = :orgId ORDER BY createdAt DESC")
    fun observeCommercialDocs(orgId: Long): Flow<List<CommercialDocEntity>>

    @Query("SELECT * FROM commercial_docs WHERE organizationId = :orgId ORDER BY createdAt DESC")
    suspend fun getCommercialDocsOnce(orgId: Long): List<CommercialDocEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommercialDoc(doc: CommercialDocEntity): Long

    @Update
    suspend fun updateCommercialDoc(doc: CommercialDocEntity)

    // --- PRODUCTS, SERVICES & INVENTORY ---
    @Query("SELECT * FROM inventory_items WHERE organizationId = :orgId ORDER BY name ASC")
    fun observeProducts(orgId: Long): Flow<List<ProductStockEntity>>

    @Query("SELECT * FROM inventory_items WHERE organizationId = :orgId ORDER BY name ASC")
    suspend fun getProductsOnce(orgId: Long): List<ProductStockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductStockEntity): Long

    @Update
    suspend fun updateProduct(product: ProductStockEntity)

    @Query("SELECT * FROM stock_movements WHERE organizationId = :orgId ORDER BY timestamp DESC")
    fun observeStockMovements(orgId: Long): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity): Long

    // --- HR, PROJECTS & TASKS ---
    @Query("SELECT * FROM employees WHERE organizationId = :orgId ORDER BY fullName ASC")
    fun observeEmployees(orgId: Long): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Query("SELECT * FROM projects_tasks WHERE organizationId = :orgId ORDER BY dueDate ASC")
    fun observeProjectTasks(orgId: Long): Flow<List<ProjectTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectTask(task: ProjectTaskEntity): Long

    @Update
    suspend fun updateProjectTask(task: ProjectTaskEntity)

    // --- DOCUMENTS VAULT ---
    @Query("SELECT * FROM documents WHERE organizationId = :orgId AND isArchived = 0 ORDER BY createdAt DESC")
    fun observeDocuments(orgId: Long): Flow<List<DocumentVaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentVaultEntity): Long

    @Update
    suspend fun updateDocument(doc: DocumentVaultEntity)

    // --- WORKFLOW AUTOMATIONS ---
    @Query("SELECT * FROM workflows WHERE organizationId = :orgId ORDER BY id ASC")
    fun observeWorkflows(orgId: Long): Flow<List<AutomationWorkflowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: AutomationWorkflowEntity): Long

    @Update
    suspend fun updateWorkflow(workflow: AutomationWorkflowEntity)

    // --- SECURITY & AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs WHERE organizationId = :orgId ORDER BY timestamp DESC LIMIT 200")
    fun observeAuditLogs(orgId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE organizationId = :orgId ORDER BY timestamp DESC")
    suspend fun getAuditLogsOnce(orgId: Long): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long
}
