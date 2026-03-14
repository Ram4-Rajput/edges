package com.edges.notificationassistant

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edges.notificationassistant.ui.theme.EDGESTheme

class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EDGESTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WelcomeScreen(
                        onRequestPermissions = { requestPermissions() },
                        onOpenNotificationSettings = { openNotificationListenerSettings() }
                    )
                }
            }
        }
    }
    
    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        permissionLauncher.launch(permissions.toTypedArray())
    }
    
    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}

@Composable
fun WelcomeScreen(
    onRequestPermissions: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EDGES",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "AI-Powered Meeting Assistant",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "EDGES monitors your notifications and automatically detects meeting requests using AI. " +
                    "It can schedule meetings in your calendar with one tap.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onRequestPermissions,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Grant Calendar Permissions")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onOpenNotificationSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Enable Notification Access")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Setup Instructions:\n" +
                    "1. Grant calendar permissions\n" +
                    "2. Enable notification access\n" +
                    "3. EDGES will start monitoring automatically",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
