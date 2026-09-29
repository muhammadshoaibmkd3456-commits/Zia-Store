package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CartItem
import com.example.data.model.StoreProfile
import com.example.ui.theme.ZiaCashGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WhatsAppBillingDialog(
    profile: StoreProfile,
    cartItems: List<CartItem>,
    discountAmount: Double,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var customerPhone by remember { mutableStateOf("") }
    val subtotal = cartItems.sumOf { it.lineTotal }
    val netTotal = (subtotal - discountAmount).coerceAtLeast(0.0)

    fun sendViaWhatsApp(phone: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.append("🧾 *${profile.storeName}*\n")
        sb.append("_${profile.tagline}_\n")
        if (profile.address.isNotEmpty()) sb.append("📍 ${profile.address}\n")
        if (profile.phone.isNotEmpty()) sb.append("📞 ${profile.phone}\n\n")

        sb.append("📅 *Tareekh:* $dateStr\n")
        sb.append("👤 *Grahak:* Walk-in (${phone.ifEmpty { "Cash" }})\n")
        sb.append("----------------------------\n")
        for (item in cartItems) {
            sb.append("• ${item.product.name}\n")
            sb.append("  ${item.qty.toInt()} x Rs. ${item.customPrice.toInt()} = *Rs. ${item.lineTotal.toInt()}*\n")
        }
        sb.append("----------------------------\n")
        sb.append("Subtotal: Rs. ${subtotal.toInt()}\n")
        if (discountAmount > 0) {
            sb.append("Discount: -Rs. ${discountAmount.toInt()}\n")
        }
        sb.append("💰 *TOTAL BILL: Rs. ${netTotal.toInt()}*\n")
        sb.append("✅ *Status: Full Paid (Cash)*\n\n")

        if (profile.paymentDetails.isNotEmpty()) {
            sb.append("💳 *Online Payment:* ${profile.paymentDetails}\n\n")
        }
        sb.append("🙏 *Shukriya! Phir zaroor aayein.*\n")
        sb.append("_Powered by ZiaStore_")

        val message = sb.toString()

        try {
            val formattedNum = phone.replace("-", "").replace(" ", "").trim()
            val intent = if (formattedNum.isNotEmpty()) {
                val internationalPhone = if (formattedNum.startsWith("0")) {
                    "92" + formattedNum.substring(1)
                } else {
                    formattedNum
                }
                Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$internationalPhone&text=${Uri.encode(message)}"))
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                }
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // General share fallback
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(fallback, "Send Bill"))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WhatsApp Cash Billing",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Customer ka WhatsApp number darj karein. Sale save hogi aur direct bill send hoga.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Customer WhatsApp No.") },
                    placeholder = { Text("e.g. 0300-1234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("whatsapp_phone_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = ZiaCashGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bill Amount:")
                        Text(
                            "Rs. ${netTotal.toInt()}",
                            fontWeight = FontWeight.Bold,
                            color = ZiaCashGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        sendViaWhatsApp(customerPhone)
                        onConfirm(customerPhone)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZiaCashGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("complete_send_whatsapp_button")
                ) {
                    Text("Complete & Send WhatsApp", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
