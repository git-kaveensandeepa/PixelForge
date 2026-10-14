package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

object IconRegistry {
    val AVAILABLE_ICONS = listOf(
        "home" to Icons.Default.Home,
        "search" to Icons.Default.Search,
        "settings" to Icons.Default.Settings,
        "favorite" to Icons.Default.Favorite,
        "star" to Icons.Default.Star,
        "person" to Icons.Default.Person,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "notifications" to Icons.Default.Notifications,
        "share" to Icons.Default.Share,
        "menu" to Icons.Default.Menu,
        "check" to Icons.Default.Check,
        "add" to Icons.Default.Add,
        "delete" to Icons.Default.Delete,
        "edit" to Icons.Default.Edit,
        "lock" to Icons.Default.Lock,
        "email" to Icons.Default.Email,
        "phone" to Icons.Default.Phone,
        "info" to Icons.Default.Info,
        "arrow_back" to Icons.AutoMirrored.Filled.ArrowBack,
        "camera" to Icons.Default.CameraAlt,
        "filter" to Icons.Default.FilterList,
        "refresh" to Icons.Default.Refresh,
        "cloud" to Icons.Default.Cloud,
        "visibility" to Icons.Default.Visibility,
        "bookmark" to Icons.Default.Bookmark
    )

    fun getIcon(name: String): ImageVector {
        val normalized = name.lowercase().trim()
        return AVAILABLE_ICONS.find { it.first == normalized }?.second ?: Icons.Default.Star
    }
}
