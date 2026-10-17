package com.shashidhara.foundit.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.shashidhara.foundit.data.model.UserProfile
import com.shashidhara.foundit.ui.components.FoundITIcons
import com.shashidhara.foundit.ui.components.FoundITVideoPlayer
import com.shashidhara.foundit.ui.theme.AquaFoamBase
import com.shashidhara.foundit.ui.theme.AquaFoamDark
import com.shashidhara.foundit.ui.theme.AquaFoamLight
import com.shashidhara.foundit.ui.theme.AquaFoamLighter
import com.shashidhara.foundit.ui.theme.AquaFoamMedium
import com.shashidhara.foundit.ui.theme.AquaFoamMuted
import com.shashidhara.foundit.ui.theme.CyberTealBase
import com.shashidhara.foundit.ui.theme.CyberTealDark
import com.shashidhara.foundit.ui.theme.CyberTealDarker
import com.shashidhara.foundit.ui.theme.CyberTealDarkest
import com.shashidhara.foundit.ui.theme.CyberTealElevated
import com.shashidhara.foundit.ui.theme.CyberTealLight
import com.shashidhara.foundit.ui.theme.CyberTealMedium
import com.shashidhara.foundit.ui.theme.CyberTealMuted
import com.shashidhara.foundit.ui.theme.CyberTealSoft
import com.shashidhara.foundit.ui.viewmodel.PostViewModel
import com.shashidhara.foundit.ui.viewmodel.SubmissionState

@Composable
fun CreatePostScreen(
    postViewModel: PostViewModel,
    currentUser: UserProfile?,
    onPostCreated: () -> Unit,
    modifier: Modifier = Modifier,
    initialPostType: String = "LOST"
) {
    val context = LocalContext.current

    val title by postViewModel.title.collectAsState()
    val description by postViewModel.description.collectAsState()
    val category by postViewModel.category.collectAsState()
    val location by postViewModel.location.collectAsState()
    val postType by postViewModel.postType.collectAsState()

    val selectedMediaUri by postViewModel.selectedMediaUri.collectAsState()
    val isVideo by postViewModel.isVideo.collectAsState()
    val uploadedMedia by postViewModel.uploadedMedia.collectAsState()
    val uploadProgress by postViewModel.uploadProgress.collectAsState()
    val submissionState by postViewModel.submissionState.collectAsState()

    val categories = remember {
        listOf("Electronics", "Wallets & Cards", "Keys", "Bags", "Documents", "Clothing", "Other")
    }

    LaunchedEffect(initialPostType) {
        if (initialPostType.isNotBlank()) {
            postViewModel.setPostType(initialPostType)
        }
    }

    LaunchedEffect(submissionState) {
        if (submissionState is SubmissionState.Success) {
            Toast.makeText(context, "Post published successfully!", Toast.LENGTH_SHORT).show()
            postViewModel.resetSubmissionState()
            onPostCreated()
        }
    }

    // Android Photo Picker launcher (supporting both images and videos)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val isVid = mimeType.startsWith("video/")
            postViewModel.onMediaSelected(uri, mimeType, isVid)
        }
    }

    val isSubmitting = submissionState is SubmissionState.Uploading || submissionState is SubmissionState.SavingToFirestore

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberTealDarkest),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Top Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(CyberTealDark, CyberTealDarker)
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            colors = listOf(CyberTealBase, AquaFoamBase.copy(alpha = 0.5f))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberTealMuted)
                            .border(1.dp, AquaFoamBase.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FoundITIcons.CloudUpload,
                            contentDescription = null,
                            tint = AquaFoamBase,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CLOUDINARY DIRECT UPLOAD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaFoamLighter,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Report Lost or Found Item",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = AquaFoamLighter
                    )
                    Text(
                        text = "Add photos or videos with location details to notify the community.",
                        fontSize = 12.sp,
                        color = AquaFoamMedium,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Post Type Selector Toggle (LOST vs FOUND)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberTealDark)
                    .border(1.dp, CyberTealElevated, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isLostSelected = postType == "LOST"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLostSelected) CyberTealBase else CyberTealDark)
                        .border(
                            1.dp,
                            if (isLostSelected) AquaFoamBase else CyberTealDark,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { postViewModel.setPostType("LOST") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "I LOST AN ITEM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLostSelected) AquaFoamLighter else CyberTealSoft
                    )
                }

                val isFoundSelected = postType == "FOUND"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFoundSelected) AquaFoamMuted else CyberTealDark)
                        .border(
                            1.dp,
                            if (isFoundSelected) AquaFoamBase else CyberTealDark,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { postViewModel.setPostType("FOUND") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "I FOUND AN ITEM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFoundSelected) AquaFoamLighter else CyberTealSoft
                    )
                }
            }
        }

        // Media Picker Section (Image or Video)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "MEDIA PROOF (IMAGE OR VIDEO)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTealSoft,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            if (selectedMediaUri != null || uploadedMedia != null) {
                // Media Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(CyberTealDark)
                        .border(1.dp, AquaFoamBase.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                ) {
                    val mediaUri = selectedMediaUri
                    val upload = uploadedMedia

                    if (isVideo) {
                        // Playable Video Preview using FoundITVideoPlayer
                        val videoPlayUrl = upload?.secureUrl ?: mediaUri?.toString().orEmpty()
                        FoundITVideoPlayer(
                            videoUrl = videoPlayUrl,
                            modifier = Modifier.fillMaxSize(),
                            autoPlay = false,
                            useController = true
                        )
                    } else {
                        // Image Preview using Coil SubcomposeAsyncImage
                        SubcomposeAsyncImage(
                            model = upload?.secureUrl ?: mediaUri,
                            contentDescription = "Selected Media Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = AquaFoamBase,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        )
                    }

                    // Top Media Type Tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberTealDarkest.copy(alpha = 0.85f))
                            .border(1.dp, AquaFoamBase.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isVideo) FoundITIcons.Video else FoundITIcons.MediaImage,
                                contentDescription = null,
                                tint = AquaFoamBase,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isVideo) "VIDEO SELECTED" else "IMAGE SELECTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaFoamLighter
                            )
                        }
                    }

                    // Remove / Change button
                    if (!isSubmitting) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CyberTealDarkest.copy(alpha = 0.85f))
                                .border(1.dp, CyberTealElevated, CircleShape)
                                .clickable { postViewModel.clearMedia() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FoundITIcons.Close,
                                contentDescription = "Remove Media",
                                tint = AquaFoamBase,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                // Empty State Drop Box to Launch Photo Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(CyberTealDark)
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(CyberTealElevated, AquaFoamBase.copy(alpha = 0.4f))),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable(enabled = !isSubmitting) {
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(CyberTealMuted)
                                .border(1.dp, AquaFoamBase.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FoundITIcons.CloudUpload,
                                contentDescription = "Upload Media",
                                tint = AquaFoamBase,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tap to Select Photo or Video",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaFoamLighter
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "JPG, PNG, MP4, MOV supported • Photo Picker",
                            fontSize = 11.sp,
                            color = CyberTealSoft
                        )
                    }
                }
            }
        }

        // Category Selection Chips
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "CATEGORY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTealSoft,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            val chipsScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipsScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = category == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) CyberTealBase else CyberTealDark)
                            .border(
                                1.dp,
                                if (isSelected) AquaFoamBase else CyberTealElevated,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = !isSubmitting) { postViewModel.setCategory(cat) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AquaFoamLighter else AquaFoamLight
                        )
                    }
                }
            }
        }

        // Item Title Field
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "ITEM TITLE *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTealSoft,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { postViewModel.setTitle(it) },
                placeholder = { Text("e.g., Black Sony Headphones, Blue Wallet", color = AquaFoamDark) },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberTealDark),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AquaFoamBase,
                    unfocusedBorderColor = CyberTealElevated,
                    focusedTextColor = AquaFoamLighter,
                    unfocusedTextColor = AquaFoamLight,
                    cursorColor = AquaFoamBase
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // Location Field
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "LOCATION *",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTealSoft,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            OutlinedTextField(
                value = location,
                onValueChange = { postViewModel.setLocation(it) },
                placeholder = { Text("e.g., Central Station Platform 3, Metro Train", color = AquaFoamDark) },
                leadingIcon = {
                    Icon(
                        imageVector = FoundITIcons.Location,
                        contentDescription = null,
                        tint = AquaFoamBase
                    )
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberTealDark),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AquaFoamBase,
                    unfocusedBorderColor = CyberTealElevated,
                    focusedTextColor = AquaFoamLighter,
                    unfocusedTextColor = AquaFoamLight,
                    cursorColor = AquaFoamBase
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // Description Field
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ADDITIONAL DETAILS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTealSoft,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { postViewModel.setDescription(it) },
                placeholder = { Text("Include unique marks, exact date, or contact instructions...", color = AquaFoamDark) },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberTealDark),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AquaFoamBase,
                    unfocusedBorderColor = CyberTealElevated,
                    focusedTextColor = AquaFoamLighter,
                    unfocusedTextColor = AquaFoamLight,
                    cursorColor = AquaFoamBase
                ),
                shape = RoundedCornerShape(16.dp),
                minLines = 3,
                maxLines = 5
            )
        }

        // Upload Progress Indicator
        item {
            AnimatedVisibility(visible = isSubmitting) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberTealDark)
                        .border(1.dp, AquaFoamBase.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    val progressPercent = (uploadProgress * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (submissionState is SubmissionState.SavingToFirestore) {
                                "Registering with FoundIT Network..."
                            } else {
                                "Uploading ${if (isVideo) "Video" else "Image"} to Cloudinary..."
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AquaFoamLighter
                        )
                        Text(
                            text = if (submissionState is SubmissionState.SavingToFirestore) "Syncing" else "$progressPercent%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaFoamBase
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (submissionState is SubmissionState.SavingToFirestore) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AquaFoamBase,
                            trackColor = CyberTealElevated
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AquaFoamBase,
                            trackColor = CyberTealElevated
                        )
                    }
                }
            }
        }

        // Error Banner with Retry
        item {
            val errorState = submissionState as? SubmissionState.Error
            if (errorState != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberTealDark)
                        .border(1.dp, AquaFoamBase, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SUBMISSION ERROR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaFoamBase,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = FoundITIcons.Close,
                                contentDescription = "Dismiss",
                                tint = AquaFoamLight,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { postViewModel.resetSubmissionState() }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorState.message,
                            fontSize = 12.sp,
                            color = AquaFoamLight,
                            lineHeight = 16.sp
                        )

                        // If Cloudinary succeeded but Firestore failed, offer Retry button to avoid re-upload
                        if (errorState.canRetrySave) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { postViewModel.submitPost(context, currentUser) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberTealBase,
                                    contentColor = AquaFoamLighter
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = FoundITIcons.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = AquaFoamLighter
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Retry Saving Post",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Submission Button
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { postViewModel.submitPost(context, currentUser) },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberTealBase,
                    contentColor = AquaFoamLighter,
                    disabledContainerColor = CyberTealMuted,
                    disabledContentColor = AquaFoamDark
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AquaFoamLighter,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (submissionState is SubmissionState.SavingToFirestore) "Publishing Post..." else "Uploading Media...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = FoundITIcons.Add,
                        contentDescription = null,
                        tint = AquaFoamLighter,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Publish to Community Network",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
