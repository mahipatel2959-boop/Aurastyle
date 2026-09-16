package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProfessionalStylist
import com.example.data.model.SubscriptionTier
import com.example.ui.FashionViewModel
import com.example.ui.components.StylistCard
import com.example.ui.components.SubscriptionTierCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylistSubscriptionScreen(
    viewModel: FashionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val bookings by viewModel.stylistBookings.collectAsState()

    var selectedSessionType by remember { mutableStateOf("1-on-1 Lookbook Consultation") }
    var selectedDate by remember { mutableStateOf("Tomorrow, 3:00 PM") }
    var consultationNotes by remember { mutableStateOf("") }

    val currentTier = remember(userProfile.subscriptionTierName) {
        try {
            SubscriptionTier.valueOf(userProfile.subscriptionTierName)
        } catch (e: Exception) {
            SubscriptionTier.STYLE_PRO
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("vip_stylists_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = GoldDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VIP ATELIER & STYLISTS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldDark,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Certified Human Stylists & Premium Subscriptions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldPrimary,
                    contentColor = NoirObsidian
                ) {
                    Text(
                        text = currentTier.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Active Subscription Status Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NoirDarkCard),
                border = BorderStroke(1.dp, GoldPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Membership Plan",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldLight
                            )
                            Text(
                                text = currentTier.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = CashmereSilk
                            )
                        }

                        Button(
                            onClick = { viewModel.showSubscriptionDialog(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = NoirObsidian
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change Plan", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Includes: Unlimited 3D virtual try-ons, real-time multi-store price comparison alerts, and personal fashion architect access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CashmereBorder
                    )
                }
            }
        }

        // Booked Consultations History (if any)
        if (bookings.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Your Scheduled Consultations (${bookings.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    bookings.forEach { booking ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldLight.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, GoldPrimary)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${booking.sessionType} with ${booking.stylistName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${booking.dateText} (${booking.timeSlot})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SageLightGreen,
                                    contentColor = SageSavingsGreen
                                ) {
                                    Text(
                                        text = booking.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Certified Professional Stylists Directory
        item {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
                Text(
                    text = "Certified Haute Couture Stylists",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Book 1-on-1 private video sessions, bespoke seasonal lookbooks, and wardrobe detox",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(viewModel.stylists) { stylist ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                StylistCard(
                    stylist = stylist,
                    onBookClick = { viewModel.showBookingDialog(stylist) }
                )
            }
        }

        // Subscription Plans Comparison Section
        item {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)) {
                Text(
                    text = "Membership Plans & Tiers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose the styling level tailored to your wardrobe aspirations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(SubscriptionTier.values()) { tier ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                SubscriptionTierCard(
                    tier = tier,
                    isCurrent = tier == currentTier,
                    onSelect = { viewModel.upgradeSubscription(tier) }
                )
            }
        }
    }

    // Book Stylist Dialog
    if (uiState.showBookingDialog && uiState.selectedStylist != null) {
        val stylist = uiState.selectedStylist!!
        val sessionTypes = listOf(
            "1-on-1 Lookbook Consultation",
            "12-Season Color Undertone Analysis",
            "Special Event & Gala Dressing",
            "Capsule Wardrobe Architecture"
        )
        val dates = listOf("Tomorrow, 2:00 PM", "Tomorrow, 5:30 PM", "Saturday, 11:00 AM", "Sunday, 4:00 PM")

        AlertDialog(
            onDismissRequest = { viewModel.showBookingDialog(null) },
            title = {
                Text(
                    text = "Book Session with ${stylist.name}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Rate: $${stylist.hourlyRate.toInt()} / 45-min consultation",
                        style = MaterialTheme.typography.titleSmall,
                        color = GoldDark,
                        fontWeight = FontWeight.Bold
                    )

                    Text(text = "Select Consultation Type:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    sessionTypes.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSessionType = type }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedSessionType == type,
                                onClick = { selectedSessionType = type },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                            )
                            Text(text = type, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Select Time Slot:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    dates.forEach { dt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDate = dt }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedDate == dt,
                                onClick = { selectedDate = dt },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                            )
                            Text(text = dt, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = consultationNotes,
                        onValueChange = { consultationNotes = it },
                        placeholder = { Text("Add styling goals (e.g. need outfits for European tour)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.bookStylist(stylist, selectedSessionType, selectedDate, "Confirmed", consultationNotes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NoirObsidian)
                ) {
                    Text("Confirm Booking ($${stylist.hourlyRate.toInt()})", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showBookingDialog(null) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Subscription Dialog
    if (uiState.showSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showSubscriptionDialog(false) },
            title = { Text("Choose Your AuraStyle Tier", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubscriptionTier.values().forEach { tier ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.upgradeSubscription(tier) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (tier == currentTier) GoldLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (tier == currentTier) BorderStroke(1.5.dp, GoldPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = tier.title, fontWeight = FontWeight.Bold)
                                    Text(text = tier.priceMonthly, style = MaterialTheme.typography.bodySmall, color = GoldDark)
                                }
                                if (tier == currentTier) {
                                    Text(text = "CURRENT", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = GoldDark)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showSubscriptionDialog(false) }) {
                    Text("Close")
                }
            }
        )
    }
}
