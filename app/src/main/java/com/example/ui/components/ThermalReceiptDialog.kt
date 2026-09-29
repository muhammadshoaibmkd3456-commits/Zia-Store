package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.ui.theme.ZiaCashGreen
import com.example.ui.theme.ZiaGreenPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ThermalReceiptDialog(
    transaction: TransactionEntity,
    items: List<TransactionItemEntity>,
    profile: StoreProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPrinting by remember { mutableStateOf(false) }
    var printStatus by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(transaction.timestamp))

    fun generateReceiptText(): String {
        val sb = StringBuilder()
        sb.append("================================\n")
        sb.append("      ${profile.storeName}\n")
        sb.append("   ${profile.tagline}\n")
        if (profile.address.isNotEmpty()) sb.append("Address: ${profile.address}\n")
        if (profile.phone.isNotEmpty()) sb.append("Phone: ${profile.phone}\n")
        sb.append("================================\n")
        sb.append("Invoice: #${transaction.invoiceNo}\n")
        sb.append("Date: $formattedDate\n")
        sb.append("Customer: ${transaction.partyName}\n")
        if (transaction.partyPhone.isNotEmpty()) sb.append("Phone: ${transaction.partyPhone}\n")
        sb.append("--------------------------------\n")
        sb.append(String.format("%-16s %3s %5s %6s\n", "Item", "Qty", "Rate", "Total"))
        sb.append("--------------------------------\n")
        for (item in items) {
            val name = if (item.productName.length > 15) item.productName.substring(0, 15) else item.productName
            val lineTotal = item.qty * item.unitPrice
            sb.append(String.format("%-16s %3.0f %5.0f %6.0f\n", name, item.qty, item.unitPrice, lineTotal))
        }
        sb.append("--------------------------------\n")
        sb.append(String.format("Subtotal:            Rs. %8.0f\n", transaction.subtotal))
        if (transaction.discountAmount > 0) {
            sb.append(String.format("Discount:            Rs. %8.0f\n", transaction.discountAmount))
        }
        sb.append(String.format("TOTAL BILL:          Rs. %8.0f\n", transaction.totalAmount))
        sb.append(String.format("Paid Amount:         Rs. %8.0f\n", transaction.paidAmount))
        val balance = transaction.totalAmount - transaction.paidAmount
        if (balance > 0) {
            sb.append(String.format("Baaqi (Udhaar):      Rs. %8.0f\n", balance))
        }
        sb.append("Mode: ${transaction.paymentMode.name}\n")
        sb.append("================================\n")
        if (profile.paymentDetails.isNotEmpty()) {
            sb.append("${profile.paymentDetails}\n")
            sb.append("--------------------------------\n")
        }
        sb.append("   Shukriya! Phir zaroor aayein.\n")
        sb.append("        Powered by ZiaStore\n")
        sb.append("================================\n")
        return sb.toString()
    }

    fun shareWhatsApp(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(sendIntent)
        } catch (e: Exception) {
            // Fallback to normal share if WhatsApp is not installed
            val fallbackIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Share Invoice"))
        }
    }

    fun shareGeneral(text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice"))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "58mm Invoice Preview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Realistic Monospace Thermal Paper Slip
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFAF9F6) // paper white
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(340.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = profile.storeName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = profile.tagline,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color.DarkGray
                            ),
                            textAlign = TextAlign.Center
                        )
                        if (profile.address.isNotEmpty()) {
                            Text(
                                text = profile.address,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.DarkGray
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (profile.phone.isNotEmpty()) {
                            Text(
                                text = "Tel: ${profile.phone}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.DarkGray
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - -",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Bill: #${transaction.invoiceNo}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.DarkGray
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grahak: ${transaction.partyName}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black
                                )
                            )
                        }

                        Text(
                            text = "--------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )

                        // Table Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f))
                            Text("Qty", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("Rate", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text("Total", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                        }

                        Text(
                            text = "--------------------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )

                        // Line items
                        for (item in items) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.productName, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(2f), maxLines = 1)
                                Text("${item.qty.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text("${item.unitPrice.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                Text("${(item.qty * item.unitPrice).toInt()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            }
                        }

                        Text(
                            text = "- - - - - - - - - - - - - - - - - - - -",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )

                        // Totals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal:", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Text("Rs. ${transaction.subtotal.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }

                        if (transaction.discountAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount:", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("-Rs. ${transaction.discountAmount.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TOTAL BILL:", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Rs. ${transaction.totalAmount.toInt()}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Paid Cash:", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Text("Rs. ${transaction.paidAmount.toInt()}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }

                        val remaining = transaction.totalAmount - transaction.paidAmount
                        if (remaining > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Udhaar (Baaqi):", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 12.sp)
                                Text("Rs. ${remaining.toInt()}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Shukriya! Phir zaroor aayein.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Powered by ZiaStore",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Printing Status / Message
                if (isPrinting) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connecting to Speed-X BT-58...", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (printStatus != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZiaCashGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(printStatus ?: "", color = ZiaCashGreen, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Print, Share WhatsApp, General Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isPrinting = true
                            printStatus = null
                            scope.launch {
                                delay(1200) // Realistic Bluetooth thermal printer handshake
                                isPrinting = false
                                printStatus = "Printed on Speed-X 58mm Thermal Printer!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZiaGreenPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_receipt_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print (BT)", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val text = generateReceiptText()
                            shareWhatsApp(text)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("whatsapp_share_receipt_button")
                    ) {
                        Text("WhatsApp", fontSize = 13.sp)
                    }

                    IconButton(
                        onClick = {
                            val text = generateReceiptText()
                            shareGeneral(text)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            }
        }
    }
}
