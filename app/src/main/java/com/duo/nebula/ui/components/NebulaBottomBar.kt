package com.duo.nebula.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.duo.nebula.navigation.NebulaDestinations

data class NebulaTab(val route: String, val label: String)

private val tabs = listOf(
    NebulaTab(NebulaDestinations.FEED, "Feed"),
    NebulaTab(NebulaDestinations.CREATE_PUBLICATION, "Publicar"),
    NebulaTab(NebulaDestinations.PROFILE, "Perfil")
)

@Composable
fun NebulaBottomBar(currentRoute: String?, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { tab ->
            val isSelected = currentRoute == tab.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.route) },
                icon = {
                    // Ícones outline quando a aba não está selecionada, preenchidos
                    // quando está — mais nítido e moderno que o AccountCircle antigo.
                    Icon(
                        imageVector = when (tab.route) {
                            NebulaDestinations.FEED -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                            NebulaDestinations.CREATE_PUBLICATION -> Icons.Filled.AddCircle
                            else -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                        },
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
