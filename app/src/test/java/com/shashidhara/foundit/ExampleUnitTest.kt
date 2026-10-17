package com.shashidhara.foundit

import androidx.compose.ui.test.junit4.createComposeRule
import com.shashidhara.foundit.MainActivity
import com.shashidhara.foundit.ui.FoundITApp
import com.shashidhara.foundit.ui.screens.AccountScreen
import com.shashidhara.foundit.ui.screens.CreatePostScreen
import com.shashidhara.foundit.ui.screens.HomeScreen
import com.shashidhara.foundit.ui.theme.FoundITTheme
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], packageName = "com.shashidhara.foundit")
class ExampleUnitTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.shashidhara.foundit")
                    .setApiKey("AIzaSyFakeKeyForUnitTests")
                    .setProjectId("foundit-items")
                    .build()
            )
        }
    }

    @Test
    fun testLaunchMainActivity() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull(activity)
    }

    @Test
    fun testHomeScreenRendersWithoutCrash() {
        composeTestRule.setContent {
            FoundITTheme {
                HomeScreen()
            }
        }
    }

    @Test
    fun testAccountScreenRendersWithoutCrash() {
        composeTestRule.setContent {
            FoundITTheme {
                AccountScreen()
            }
        }
    }

    @Test
    fun testFoundITAppFullComposition() {
        composeTestRule.setContent {
            FoundITTheme {
                FoundITApp()
            }
        }
    }

    @Test
    fun testCreatePostScreenRendersWithoutCrash() {
        composeTestRule.setContent {
            FoundITTheme {
                CreatePostScreen(
                    postViewModel = com.shashidhara.foundit.ui.viewmodel.PostViewModel(),
                    currentUser = null,
                    onPostCreated = {}
                )
            }
        }
    }

    @Test
    fun testDashboardScreenRendersWithoutCrash() {
        composeTestRule.setContent {
            FoundITTheme {
                com.shashidhara.foundit.ui.screens.DashboardScreen(
                    currentUser = com.shashidhara.foundit.data.model.UserProfile(
                        uid = "test-uid-123",
                        name = "Test User",
                        email = "test@example.com"
                    ),
                    onNavigateToCreatePost = {},
                    onNavigateToSignIn = {}
                )
            }
        }
    }

    @Test
    fun testPostRequestDeterministicIdAndLifecycle() {
        val postId = "post_abc"
        val requesterUid = "user_xyz"
        val id = com.shashidhara.foundit.data.model.PostRequest.generateId(postId, requesterUid)
        assertEquals("post_abc_user_xyz", id)

        val request = com.shashidhara.foundit.data.model.PostRequest(
            id = id,
            postId = postId,
            requesterUid = requesterUid,
            postOwnerUid = "owner_123",
            message = "I found your item",
            status = com.shashidhara.foundit.data.model.PostRequest.STATUS_PENDING
        )
        assertEquals(true, request.isPending)
        assertEquals(false, request.isAccepted)
    }

    @Test
    fun testRoleBasedSidebarItems() {
        // Normal user should never have AdminPanel in navigation
        val userItems = com.shashidhara.foundit.ui.navigation.Screen.getSidebarItemsForRole("USER")
        val hasAdminForUser = userItems.any { it is com.shashidhara.foundit.ui.navigation.Screen.AdminPanel }
        assertEquals(false, hasAdminForUser)

        val nullRoleItems = com.shashidhara.foundit.ui.navigation.Screen.getSidebarItemsForRole(null)
        val hasAdminForNull = nullRoleItems.any { it is com.shashidhara.foundit.ui.navigation.Screen.AdminPanel }
        assertEquals(false, hasAdminForNull)

        // Only ADMIN should have AdminPanel
        val adminItems = com.shashidhara.foundit.ui.navigation.Screen.getSidebarItemsForRole("ADMIN")
        val hasAdminForAdmin = adminItems.any { it is com.shashidhara.foundit.ui.navigation.Screen.AdminPanel }
        assertEquals(true, hasAdminForAdmin)
    }

    @Test
    fun testCaseLifecycleStateMachine() {
        // Valid forward custody flow
        assertEquals(true, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("OPEN", "PENDING"))
        assertEquals(true, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("OPEN", "AWAITING_SECURITY_HANDOVER"))
        assertEquals(true, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("AWAITING_SECURITY_HANDOVER", "WITH_SECURITY"))
        assertEquals(true, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("WITH_SECURITY", "RETURNED_TO_OWNER"))
        assertEquals(true, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("RETURNED_TO_OWNER", "RESOLVED"))

        // Invalid transitions: cannot return to owner directly from OPEN report without security receipt
        assertEquals(false, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("OPEN", "RETURNED_TO_OWNER"))
        assertEquals(false, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("AWAITING_SECURITY_HANDOVER", "RETURNED_TO_OWNER"))
    }

    @Test
    fun testHandoverRecordAndAuditLogDataModels() {
        val handover = com.shashidhara.foundit.data.model.HandoverRecord(
            id = "handover_1",
            postId = "post_1",
            itemTitle = "Blue Backpack",
            type = com.shashidhara.foundit.data.model.HandoverRecord.TYPE_RECEIPT,
            staffName = "Officer Smith",
            location = "Security Desk A"
        )
        assertEquals(true, handover.isReceipt)
        assertEquals(false, handover.isReturn)

        val auditLog = com.shashidhara.foundit.data.model.AuditLogEntry(
            id = "audit_1",
            actorUid = "admin_uid_123",
            actorName = "Chief Admin",
            action = com.shashidhara.foundit.data.model.AuditLogEntry.ACTION_STATUS_CHANGE,
            entityType = com.shashidhara.foundit.data.model.AuditLogEntry.ENTITY_POST,
            entityId = "post_1",
            previousStatus = "OPEN",
            newStatus = "WITH_SECURITY",
            reason = "Item handed over to security desk"
        )
        assertEquals("admin_uid_123", auditLog.actorUid)
        assertEquals("WITH_SECURITY", auditLog.newStatus)
    }

    @Test
    fun testAdminPanelScreenRendersWithoutCrash() {
        composeTestRule.setContent {
            FoundITTheme {
                com.shashidhara.foundit.ui.screens.AdminPanelScreen(
                    currentUser = com.shashidhara.foundit.data.model.UserProfile(
                        uid = "admin-uid-123",
                        name = "Admin User",
                        email = "admin@foundit.edu",
                        role = "ADMIN"
                    )
                )
            }
        }
    }

    @Test
    fun testOwnershipDeterminationByUid() {
        val post = com.shashidhara.foundit.data.model.PostItem(
            id = "post_999",
            ownerUid = "owner_uid_abc",
            title = "Found Keys",
            type = "FOUND"
        )

        val ownerUser = com.shashidhara.foundit.data.model.UserProfile(
            uid = "owner_uid_abc",
            name = "John Doe",
            email = "john@example.com"
        )

        val otherUser = com.shashidhara.foundit.data.model.UserProfile(
            uid = "other_uid_xyz",
            name = "John Doe", // Even if display name matches, ownership must be strictly UID-based!
            email = "john.another@example.com"
        )

        assertEquals(true, ownerUser.uid == post.ownerUid)
        assertEquals(false, otherUser.uid == post.ownerUid)
    }

    @Test
    fun testHomeScreenPublicGuestRenderingWithoutCrash() {
        // Guest user (currentUser = null) browsing recent posts
        composeTestRule.setContent {
            FoundITTheme {
                com.shashidhara.foundit.ui.screens.HomeScreen(
                    currentUser = null,
                    onNavigateToCreatePost = {},
                    onNavigateToAccount = {}
                )
            }
        }
    }

    @Test
    fun testCaseLifecycleTransitionsAndResolution() {
        // Verify OPEN can transition directly to RESOLVED and CLOSED
        val openTransitions = com.shashidhara.foundit.data.model.CaseLifecycle.getAllowedTransitions("OPEN")
        assertEquals(true, openTransitions.contains("RESOLVED"))
        assertEquals(true, openTransitions.contains("CLOSED"))
        assertEquals(true, openTransitions.contains("PENDING"))

        // Verify PENDING can transition directly to RESOLVED and WITH_SECURITY
        val pendingTransitions = com.shashidhara.foundit.data.model.CaseLifecycle.getAllowedTransitions("PENDING")
        assertEquals(true, pendingTransitions.contains("RESOLVED"))
        assertEquals(true, pendingTransitions.contains("WITH_SECURITY"))

        // Verify Admin Override allows any valid status
        assertEquals(true, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("OPEN", "RESOLVED", isAdmin = false))
        assertEquals(true, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("CLOSED", "OPEN", isAdmin = false))
        assertEquals(true, com.shashidhara.foundit.data.model.CaseLifecycle.isValidTransition("CLOSED", "RESOLVED", isAdmin = true))
    }

    @Test
    fun testPostAutofillValuesIntegrity() {
        val originalPost = com.shashidhara.foundit.data.model.PostItem(
            id = "test_post_101",
            ownerUid = "uid_456",
            type = "FOUND",
            title = "Found Umbrella",
            description = "Blue umbrella near library",
            category = "Other",
            location = "Library 2nd Floor",
            imageUrl = "https://res.cloudinary.com/demo/image/upload/sample.jpg",
            imagePublicId = "sample",
            status = "PENDING"
        )

        // Verify all fields are present and properly populated without loss
        assertEquals("test_post_101", originalPost.id)
        assertEquals("uid_456", originalPost.ownerUid)
        assertEquals("FOUND", originalPost.type)
        assertEquals("Found Umbrella", originalPost.title)
        assertEquals("Blue umbrella near library", originalPost.description)
        assertEquals("Other", originalPost.category)
        assertEquals("Library 2nd Floor", originalPost.location)
        assertEquals("https://res.cloudinary.com/demo/image/upload/sample.jpg", originalPost.imageUrl)
        assertEquals("sample", originalPost.imagePublicId)
        assertEquals("PENDING", originalPost.status)
    }
}