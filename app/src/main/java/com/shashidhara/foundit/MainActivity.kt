package com.shashidhara.foundit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shashidhara.foundit.ui.FoundITApp
import com.shashidhara.foundit.ui.theme.FoundITTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoundITTheme {
                FoundITApp()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FoundITAppPreview() {
    FoundITTheme {
        FoundITApp()
    }
}