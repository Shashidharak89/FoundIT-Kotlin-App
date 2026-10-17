package com.shashidhara.foundit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Lightweight, fast-loading vector icons for FoundIT.
 * Eliminates 30,000+ classes from material-icons-extended to prevent multidex and runtime crashes.
 */
object FoundITIcons {
    val Home: ImageVector = Icons.Default.Home
    val Menu: ImageVector = Icons.Default.Menu
    val Close: ImageVector = Icons.Default.Close
    val Account: ImageVector = Icons.Default.AccountCircle
    val Search: ImageVector = Icons.Default.Search
    val Add: ImageVector = Icons.Default.Add
    val Edit: ImageVector = Icons.Default.Edit
    val Lock: ImageVector = Icons.Default.Lock
    val Notifications: ImageVector = Icons.Default.Notifications
    val ChevronRight: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val Check: ImageVector = Icons.Default.Check

    val Radar: ImageVector by lazy {
        ImageVector.Builder(
            name = "Radar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 12f)
                lineTo(19f, 5f)
            }
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.8f
            ) {
                // outer circle
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, true, true, 3f, 12f)
                arcTo(9f, 9f, 0f, true, true, 21f, 12f)
                // middle circle
                moveTo(17f, 12f)
                arcTo(5f, 5f, 0f, true, true, 7f, 12f)
                arcTo(5f, 5f, 0f, true, true, 17f, 12f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(13.5f, 12f)
                arcTo(1.5f, 1.5f, 0f, true, true, 10.5f, 12f)
                arcTo(1.5f, 1.5f, 0f, true, true, 13.5f, 12f)
            }
        }.build()
    }

    val Shield: ImageVector by lazy {
        ImageVector.Builder(
            name = "Shield",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 2f)
                lineTo(4f, 5f)
                verticalLineTo(11f)
                curveTo(4f, 16.55f, 7.41f, 21.74f, 12f, 23f)
                curveTo(16.59f, 21.74f, 20f, 16.55f, 20f, 11f)
                verticalLineTo(5f)
                lineTo(12f, 2f)
                close()
            }
        }.build()
    }

    val Location: ImageVector by lazy {
        ImageVector.Builder(
            name = "Location",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 2f)
                curveTo(8.13f, 2f, 5f, 5.13f, 5f, 9f)
                curveTo(5f, 14.25f, 12f, 22f, 12f, 22f)
                curveTo(12f, 22f, 19f, 14.25f, 19f, 9f)
                curveTo(19f, 5.13f, 15.87f, 2f, 12f, 2f)
                close()
                moveTo(12f, 11.5f)
                curveTo(10.62f, 11.5f, 9.5f, 10.38f, 9.5f, 9f)
                curveTo(9.5f, 7.62f, 10.62f, 6.5f, 12f, 6.5f)
                curveTo(13.38f, 6.5f, 14.5f, 7.62f, 14.5f, 9f)
                curveTo(14.5f, 10.38f, 13.38f, 11.5f, 12f, 11.5f)
                close()
            }
        }.build()
    }

    val Phone: ImageVector by lazy {
        ImageVector.Builder(
            name = "Phone",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(6.62f, 10.79f)
                curveTo(8.06f, 13.62f, 10.38f, 15.94f, 13.21f, 17.38f)
                lineTo(15.41f, 15.18f)
                curveTo(15.69f, 14.9f, 16.08f, 14.82f, 16.43f, 14.93f)
                curveTo(17.55f, 15.3f, 18.75f, 15.5f, 20f, 15.5f)
                curveTo(20.55f, 15.5f, 21f, 15.95f, 21f, 16.5f)
                verticalLineTo(20f)
                curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
                curveTo(10.61f, 21f, 3f, 13.39f, 3f, 4f)
                curveTo(3f, 3.45f, 3.45f, 3f, 4f, 3f)
                horizontalLineTo(7.5f)
                curveTo(8.05f, 3f, 8.5f, 3.45f, 8.5f, 4f)
                curveTo(8.5f, 5.25f, 8.7f, 6.45f, 9.07f, 7.57f)
                curveTo(9.18f, 7.92f, 9.1f, 8.31f, 8.82f, 8.59f)
                lineTo(6.62f, 10.79f)
                close()
            }
        }.build()
    }

    val Email: ImageVector by lazy {
        ImageVector.Builder(
            name = "Email",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(20f, 4f)
                horizontalLineTo(4f)
                curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
                lineTo(2f, 18f)
                curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                horizontalLineTo(20f)
                curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                verticalLineTo(6f)
                curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
                close()
                moveTo(20f, 8f)
                lineTo(12f, 13f)
                lineTo(4f, 8f)
                verticalLineTo(6f)
                lineTo(12f, 11f)
                lineTo(20f, 6f)
                verticalLineTo(8f)
                close()
            }
        }.build()
    }

    val Devices: ImageVector by lazy {
        ImageVector.Builder(
            name = "Devices",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(4f, 6f)
                horizontalLineTo(20f)
                verticalLineTo(16f)
                horizontalLineTo(4f)
                close()
                moveTo(2f, 18f)
                horizontalLineTo(22f)
                verticalLineTo(20f)
                horizontalLineTo(2f)
                close()
            }
        }.build()
    }

    val Wallet: ImageVector by lazy {
        ImageVector.Builder(
            name = "Wallet",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(21f, 7.28f)
                verticalLineTo(5f)
                curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
                horizontalLineTo(5f)
                curveTo(3.89f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineTo(19f)
                curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                verticalLineTo(16.72f)
                curveTo(21.59f, 16.37f, 22f, 15.74f, 22f, 15f)
                verticalLineTo(9f)
                curveTo(22f, 8.26f, 21.59f, 7.63f, 21f, 7.28f)
                close()
                moveTo(20f, 15f)
                horizontalLineTo(16f)
                verticalLineTo(9f)
                horizontalLineTo(20f)
                verticalLineTo(15f)
                close()
            }
        }.build()
    }

    val Key: ImageVector by lazy {
        ImageVector.Builder(
            name = "Key",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12.65f, 10f)
                curveTo(11.83f, 7.67f, 9.61f, 6f, 7f, 6f)
                curveTo(3.69f, 6f, 1f, 8.69f, 1f, 12f)
                curveTo(1f, 15.31f, 3.69f, 18f, 7f, 18f)
                curveTo(9.61f, 18f, 11.83f, 16.33f, 12.65f, 14f)
                horizontalLineTo(17f)
                verticalLineTo(18f)
                horizontalLineTo(21f)
                verticalLineTo(14f)
                horizontalLineTo(23f)
                verticalLineTo(10f)
                horizontalLineTo(12.65f)
                close()
                moveTo(7f, 14f)
                curveTo(5.9f, 14f, 5f, 13.1f, 5f, 12f)
                curveTo(5f, 10.9f, 5.9f, 10f, 7f, 10f)
                curveTo(8.1f, 10f, 9f, 10.9f, 9f, 12f)
                curveTo(9f, 13.1f, 8.1f, 14f, 7f, 14f)
                close()
            }
        }.build()
    }

    val Schedule: ImageVector by lazy {
        ImageVector.Builder(
            name = "Schedule",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(11.99f, 2f)
                curveTo(6.47f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.47f, 22f, 11.99f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 11.99f, 2f)
                close()
                moveTo(12f, 20f)
                curveTo(7.58f, 20f, 4f, 16.42f, 4f, 12f)
                curveTo(4f, 7.58f, 7.58f, 4f, 12f, 4f)
                curveTo(16.42f, 4f, 20f, 7.58f, 20f, 12f)
                curveTo(20f, 16.42f, 16.42f, 20f, 12f, 20f)
                close()
                moveTo(12.5f, 7f)
                horizontalLineTo(11f)
                verticalLineTo(13f)
                lineTo(16.25f, 16.15f)
                lineTo(17f, 14.92f)
                lineTo(12.5f, 12.25f)
                verticalLineTo(7f)
                close()
            }
        }.build()
    }

    val Google: ImageVector by lazy {
        ImageVector.Builder(
            name = "Google",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(21.35f, 11.1f)
                horizontalLineTo(12f)
                verticalLineTo(14.8f)
                horizontalLineTo(17.39f)
                curveTo(16.63f, 16.89f, 14.59f, 18.4f, 12f, 18.4f)
                curveTo(8.46f, 18.4f, 5.6f, 15.54f, 5.6f, 12f)
                curveTo(5.6f, 8.46f, 8.46f, 5.6f, 12f, 5.6f)
                curveTo(13.62f, 5.6f, 15.09f, 6.19f, 16.23f, 7.16f)
                lineTo(19.06f, 4.33f)
                curveTo(17.21f, 2.61f, 14.77f, 1.6f, 12f, 1.6f)
                curveTo(6.26f, 1.6f, 1.6f, 6.26f, 1.6f, 12f)
                curveTo(1.6f, 17.74f, 6.26f, 22.4f, 12f, 22.4f)
                curveTo(17.74f, 22.4f, 22.18f, 18.31f, 22.18f, 12.28f)
                curveTo(22.18f, 11.75f, 22.11f, 11.42f, 21.99f, 11.1f)
                horizontalLineTo(21.35f)
                close()
            }
        }.build()
    }

    val SignOut: ImageVector by lazy {
        ImageVector.Builder(
            name = "SignOut",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(17f, 7f)
                lineTo(15.59f, 8.41f)
                lineTo(17.17f, 10f)
                horizontalLineTo(9f)
                verticalLineTo(12f)
                horizontalLineTo(17.17f)
                lineTo(15.59f, 13.59f)
                lineTo(17f, 15f)
                lineTo(21f, 11f)
                lineTo(17f, 7f)
                close()
                moveTo(4f, 5f)
                horizontalLineTo(12f)
                verticalLineTo(3f)
                horizontalLineTo(4f)
                curveTo(2.9f, 3f, 2f, 3.9f, 2f, 5f)
                verticalLineTo(19f)
                curveTo(2f, 20.1f, 2.9f, 21f, 4f, 21f)
                horizontalLineTo(12f)
                verticalLineTo(19f)
                horizontalLineTo(4f)
                verticalLineTo(5f)
                close()
            }
        }.build()
    }
}

