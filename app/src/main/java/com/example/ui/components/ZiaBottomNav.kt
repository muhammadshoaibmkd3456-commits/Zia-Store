package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ZiaGreenPrimary
import com.example.ui.theme.ZiaTextSecondary

@Composable
fun ZiaBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    cartItemCount: Int = 0,
    showRomanUrdu: Boolean = true
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab
            BottomNavItem(
                label = if (showRomanUrdu) "Ghar (Home)" else "Home",
                selected = currentTab == "home",
                iconFilled = Icons.Filled.Home,
                iconOutlined = Icons.Outlined.Home,
                onClick = { onTabSelected("home") },
                testTag = "nav_home_tab"
            )

            // Ledgers Tab
            BottomNavItem(
                label = if (showRomanUrdu) "Khata" else "Ledgers",
                selected = currentTab == "ledgers",
                iconFilled = Icons.Filled.MenuBook,
                iconOutlined = Icons.Outlined.MenuBook,
                onClick = { onTabSelected("ledgers") },
                testTag = "nav_ledgers_tab"
            )

            // POS Tab (Raised circular green button with cart icon)
            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .size(60.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(ZiaGreenPrimary)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected("pos") }
                    .testTag("nav_pos_fab"),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ) {
                                Text("$cartItemCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "POS Terminal",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Products Tab
            BottomNavItem(
                label = if (showRomanUrdu) "Maal" else "Products",
                selected = currentTab == "products",
                iconFilled = Icons.Filled.Inventory2,
                iconOutlined = Icons.Outlined.Inventory2,
                onClick = { onTabSelected("products") },
                testTag = "nav_products_tab"
            )

            // Settings Tab
            BottomNavItem(
                label = if (showRomanUrdu) "Settings" else "Settings",
                selected = currentTab == "settings",
                iconFilled = Icons.Filled.Settings,
                iconOutlined = Icons.Outlined.Settings,
                onClick = { onTabSelected("settings") },
                testTag = "nav_settings_tab"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    iconFilled: ImageVector,
    iconOutlined: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = ZiaGreenPrimary
    val inactiveColor = ZiaTextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = if (selected) iconFilled else iconOutlined,
            contentDescription = label,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) activeColor else inactiveColor
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
