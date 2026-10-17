package com.shashidhara.foundit.data.model

import com.google.firebase.Timestamp

/**
 * Audit log recording privileged administrative operations.
 * Append-only record tracking who changed what, the previous and new status,
 * and the administrative reason.
 */
data class AuditLogEntry(
    val id: String = "",
    val actorUid: String = "",
    val actorName: String = "",
    val action: String = ACTION_STATUS_CHANGE,
    val entityType: String = ENTITY_POST,
    val entityId: String = "",
    val previousStatus: String = "",
    val newStatus: String = "",
    val reason: String = "",
    val metadata: Map<String, Any> = emptyMap(),
    val createdAt: Timestamp? = null
) {
    companion object {
        const val ACTION_STATUS_CHANGE = "STATUS_CHANGE"
        const val ACTION_SECURITY_RECEIPT = "SECURITY_RECEIPT"
        const val ACTION_OWNER_RETURN = "OWNER_RETURN"
        const val ACTION_REQUEST_OVERRIDE = "REQUEST_OVERRIDE"
        const val ACTION_POST_DELETE = "POST_DELETE"

        const val ENTITY_POST = "POST"
        const val ENTITY_REQUEST = "REQUEST"
        const val ENTITY_HANDOVER = "HANDOVER"
        const val ENTITY_USER = "USER"
    }
}
