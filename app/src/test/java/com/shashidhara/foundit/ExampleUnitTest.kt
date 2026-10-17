package com.shashidhara.foundit

import androidx.compose.ui.test.junit4.createComposeRule
import com.shashidhara.foundit.MainActivity
import com.shashidhara.foundit.ui.FoundITApp
import com.shashidhara.foundit.ui.screens.AccountScreen
import com.shashidhara.foundit.ui.screens.HomeScreen
import com.shashidhara.foundit.ui.theme.FoundITTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}