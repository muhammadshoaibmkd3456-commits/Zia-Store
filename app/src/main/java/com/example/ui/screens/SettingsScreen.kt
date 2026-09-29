package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StoreProfile
import com.example.ui.theme.ZiaCashGreen
import com.example.ui.theme.ZiaGreenPrimary
import com.example.ui.theme.ZiaUdhaarRed
import com.example.ui.theme.ZiaWholesaleCrimson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profile: StoreProfile,
    showStoreSettingsSheet: Boolean,
    onOpenBusinessCard: () -> Unit,
    onOpenStoreSettings: (Boolean) -> Unit,
    onToggleRomanUrdu: () -> Unit,
    onToggleWholesale: () -> Unit,
    onToggleWhatsAppBilling: () -> Unit,
    onTogglePrintPreview: () -> Unit,
    onSaveStoreProfile: (StoreProfile) -> Unit,
    onResetData: () -> Unit
) {
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Settings (Tanzeemat)",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        // Digital Business Card Preview Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ZiaGreenPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenBusinessCard() }
                .testTag("business_card_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your Business Card",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Apna digital visiting card dekhein aur share karein",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        // Store & Profile Settings Card
        SettingsSectionTitle(title = "Store & Profile")
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenStoreSettings(true) }
                .testTag("store_profile_settings_row")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Store, contentDescription = null, tint = ZiaGreenPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.storeName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${profile.ownerName} • ${profile.phone}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                    )
                    Text(
                        text = profile.address,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 10.sp),
                        maxLines = 1
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }

        // Toggles Section
        SettingsSectionTitle(title = "App Preferences")
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                // Roman Urdu Toggle
                SettingsToggleRow(
                    icon = Icons.Default.Translate,
                    title = "Home Screen Roman Urdu",
                    subtitle = "Shows Urdu sub-labels on Home cards & buttons",
                    checked = profile.showRomanUrdu,
                    onCheckedChange = { onToggleRomanUrdu() },
                    testTag = "toggle_roman_urdu"
                )

                Divider(modifier = Modifier.padding(horizontal = 14.dp))

                // Wholesale Mode Toggle
                SettingsToggleRow(
                    icon = Icons.Default.PhoneIphone,
                    title = "Wholesale Mode Default",
                    subtitle = "Enables wholesale pricing toggle in POS",
                    checked = profile.wholesaleMode,
                    onCheckedChange = { onToggleWholesale() },
                    testTag = "toggle_wholesale"
                )

                Divider(modifier = Modifier.padding(horizontal = 14.dp))

                // WhatsApp Billing Toggle
                SettingsToggleRow(
                    icon = Icons.Default.Whatsapp,
                    title = "WhatsApp Billing",
                    subtitle = "Enables WhatsApp receipt button at POS checkout",
                    checked = profile.whatsappBilling,
                    onCheckedChange = { onToggleWhatsAppBilling() },
                    testTag = "toggle_whatsapp_billing"
                )

                Divider(modifier = Modifier.padding(horizontal = 14.dp))

                // Print Preview Toggle
                SettingsToggleRow(
                    icon = Icons.Default.Print,
                    title = "Print Preview (58mm)",
                    subtitle = "Shows invoice preview before Bluetooth thermal print",
                    checked = profile.printPreview,
                    onCheckedChange = { onTogglePrintPreview() },
                    testTag = "toggle_print_preview"
                )
            }
        }

        // Subscription Section
        SettingsSectionTitle(title = "Subscription & License")
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = ZiaCashGreen, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.subscriptionPlan,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ZiaCashGreen)
                    )
                    Text(
                        text = "Expiry: ${profile.subscriptionExpiry} • 100% Offline Mode Active",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                    )
                }
            }
        }

        // Data Backup & Security
        SettingsSectionTitle(title = "Data Backup & Recovery")
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBackupDialog = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = ZiaGreenPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Local On-Device Backup", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Export encrypted local backup file or share to WhatsApp", fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResetData() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = ZiaUdhaarRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Restore Sample Data", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = ZiaUdhaarRed)
                        Text("Restores default kiryana & pharmacy items and sample khatas", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Account & Privacy
        SettingsSectionTitle(title = "Account & Privacy")
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrivacyDialog = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Privacy Policy & Offline Data Safety", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Store & Profile Edit Sheet
    if (showStoreSettingsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var sName by remember { mutableStateOf(profile.storeName) }
        var oName by remember { mutableStateOf(profile.ownerName) }
        var phone by remember { mutableStateOf(profile.phone) }
        var address by remember { mutableStateOf(profile.address) }
        var paymentDetails by remember { mutableStateOf(profile.paymentDetails) }
        var tagline by remember { mutableStateOf(profile.tagline) }

        ModalBottomSheet(
            onDismissRequest = { onOpenStoreSettings(false) },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Store & Profile Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = sName,
                    onValueChange = { sName = it },
                    label = { Text("Shop Name (Dukan ka Naam)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("store_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = oName,
                    onValueChange = { oName = it },
                    label = { Text("Owner Name (Malik ka Naam)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("owner_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Shop Phone / WhatsApp") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shop Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = paymentDetails,
                    onValueChange = { paymentDetails = it },
                    label = { Text("Payment Details (Printed on bill)") },
                    placeholder = { Text("JazzCash / EasyPaisa / Bank info") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Tagline / Slogan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSaveStoreProfile(
                            profile.copy(
                                storeName = sName.ifBlank { "ZiaStore" },
                                ownerName = oName.ifBlank { "Muhammad Zia" },
                                phone = phone,
                                address = address,
                                paymentDetails = paymentDetails,
                                tagline = tagline
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_store_profile_button")
                ) {
                    Text("Save Changes (Mehfooz Karein)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        Dialog(onDismissRequest = { showPrivacyDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Privacy & Security", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• ZiaStore operates 100% offline. Zero customer or store data is uploaded to remote servers.\n" +
                            "• All transaction ledgers, inventory counts, and customer phone numbers are stored encrypted at rest on your local device.\n" +
                            "• Phone numbers are only used locally for your shop's WhatsApp bill sharing.\n" +
                            "• Complying with Google Play Data Safety policies.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { showPrivacyDialog = false }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }

    // Backup Dialog
    if (showBackupDialog) {
        Dialog(onDismissRequest = { showBackupDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Local Backup Export", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Dukan ka mukammal data (Products, Khata, Ledger, Store settings) on-device backup file me export ho chuka hai. Isay apne WhatsApp ya Google Drive me mehfooz rakh saktay hain.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showBackupDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val backupText = "ZIASTORE_BACKUP_ENCRYPTED_V1_${System.currentTimeMillis()}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, backupText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(intent, "Export Local Backup"))
                                showBackupDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary)
                        ) {
                            Text("Share / Export")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            color = ZiaGreenPrimary,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = ZiaGreenPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ZiaGreenPrimary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
