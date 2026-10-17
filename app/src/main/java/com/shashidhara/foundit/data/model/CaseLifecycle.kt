package com.shashidhara.foundit.data.model

/**
 * Centralized, validated state machine for Lost & Found item custody and case lifecycle.
 * Prevents invalid state transitions (e.g., returning an item before security has confirmed receipt).
 */
object CaseLifecycle {
    const val STATUS_OPEN = "OPEN"
    const val STATUS_PENDING = "PENDING"
    const val STATUS_AWAITING_SECURITY_HANDOVER = "AWAITING_SECURITY_HANDOVER"
    const val STATUS_WITH_SECURITY = "WITH_SECURITY"
    const val STATUS_AWAITING_OWNER_COLLECTION = "AWAITING_OWNER_COLLECTION"
    const val STATUS_RETURNED_TO_OWNER = "RETURNED_TO_OWNER"
    const val STATUS_RESOLVED = "RESOLVED"
    const val STATUS_CLOSED = "CLOSED"

    val ALL_STATUSES = listOf(
        STATUS_OPEN,
        STATUS_PENDING,
        STATUS_AWAITING_SECURITY_HANDOVER,
        STATUS_WITH_SECURITY,
        STATUS_AWAITING_OWNER_COLLECTION,
        STATUS_RETURNED_TO_OWNER,
        STATUS_RESOLVED,
        STATUS_CLOSED
    )

    fun getStatusLabel(status: String): String {
        return when (status.uppercase()) {
            STATUS_OPEN -> "Open Report"
            STATUS_PENDING -> "Claim Accepted / Arranging Handover"
            STATUS_AWAITING_SECURITY_HANDOVER -> "Awaiting Security Handover"
            STATUS_WITH_SECURITY -> "With Security (In Custody)"
            STATUS_AWAITING_OWNER_COLLECTION -> "Awaiting Owner Collection"
            STATUS_RETURNED_TO_OWNER -> "Returned to Owner"
            STATUS_RESOLVED -> "Case Resolved"
            STATUS_CLOSED -> "Case Closed"
            else -> status.replace('_', ' ')
        }
    }

    /**
     * Returns the list of permitted next statuses from the given [currentStatus].
     * Normal progression:
     * OPEN -> PENDING -> AWAITING_SECURITY_HANDOVER -> WITH_SECURITY -> AWAITING_OWNER_COLLECTION -> RETURNED_TO_OWNER -> RESOLVED
     */
    fun getAllowedTransitions(currentStatus: String): List<String> {
        return when (currentStatus.uppercase()) {
            STATUS_OPEN -> listOf(
                STATUS_PENDING,
                STATUS_AWAITING_SECURITY_HANDOVER,
                STATUS_CLOSED
            )
            STATUS_PENDING -> listOf(
                STATUS_AWAITING_SECURITY_HANDOVER,
                STATUS_OPEN,
                STATUS_CLOSED
            )
            STATUS_AWAITING_SECURITY_HANDOVER -> listOf(
                STATUS_WITH_SECURITY,
                STATUS_OPEN,
                STATUS_CLOSED
            )
            STATUS_WITH_SECURITY -> listOf(
                STATUS_AWAITING_OWNER_COLLECTION,
                STATUS_RETURNED_TO_OWNER,
                STATUS_CLOSED
            )
            STATUS_AWAITING_OWNER_COLLECTION -> listOf(
                STATUS_RETURNED_TO_OWNER,
                STATUS_WITH_SECURITY,
                STATUS_CLOSED
            )
            STATUS_RETURNED_TO_OWNER -> listOf(
                STATUS_RESOLVED,
                STATUS_CLOSED
            )
            STATUS_RESOLVED -> listOf(
                STATUS_OPEN // Administrative correction
            )
            STATUS_CLOSED -> listOf(
                STATUS_OPEN // Administrative reopen
            )
            else -> listOf(STATUS_OPEN, STATUS_CLOSED)
        }
    }

    fun isValidTransition(from: String, to: String): Boolean {
        if (from.equals(to, ignoreCase = true)) return true
        return getAllowedTransitions(from).any { it.equals(to, ignoreCase = true) }
    }
}
