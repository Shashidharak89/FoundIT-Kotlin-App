package com.shashidhara.foundit.data.model

import com.google.firebase.Timestamp

/**
 * Audit-friendly record of physical custody transitions:
 * - RECEIPT: When an item is received into security custody.
 * - RETURN: When an item is verified and returned to its rightful owner.
 */
data class HandoverRecord(
    val id: String = "",
    val postId: String = "",
    val itemTitle: String = "",
    val type: String = TYPE_RECEIPT, // "RECEIPT" or "RETURN"
    val location: String = "",
    val staffName: String = "",
    val staffId: String = "",
    val itemCondition: String = "",
    val accessories: String = "",
    val notes: String = "",
    val referenceNumber: String = "",
    val evidencePhotoUrl: String = "",
    val evidencePhotoPublicId: String = "",
    val verifiedOwnerName: String = "",
    val verifiedOwnerUid: String = "",
    val verificationMethod: String = "",
    val recipientConfirmation: String = "",
    val actorUid: String = "",
    val timestamp: Timestamp? = null,
    val createdAt: Timestamp? = null
) {
    companion object {
        const val TYPE_RECEIPT = "RECEIPT"
        const val TYPE_RETURN = "RETURN"
    }

    val isReceipt: Boolean
        get() = type.equals(TYPE_RECEIPT, ignoreCase = true)

    val isReturn: Boolean
        get() = type.equals(TYPE_RETURN, ignoreCase = true)
}
