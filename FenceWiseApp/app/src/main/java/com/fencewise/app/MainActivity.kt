package com.fencewise.app

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fencewise.app.navigation.Screen
import com.fencewise.app.screens.*
import com.fencewise.app.ui.theme.FenceWiseTheme

class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private val nfcTagState = mutableStateOf<Tag?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        // Create pending intent for NFC
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_MUTABLE
        )
        
        // Handle NFC intent if launched from NFC tag
        handleNfcIntent(intent)
        
        enableEdgeToEdge()
        setContent {
            FenceWiseTheme {
                FenceWiseApp(nfcTagState.value) {
                    nfcTagState.value = null
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Enable foreground dispatch for NFC
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            null,
            null
        )
    }
    
    override fun onPause() {
        super.onPause()
        // Disable foreground dispatch
        nfcAdapter?.disableForegroundDispatch(this)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }
    
    private fun handleNfcIntent(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                nfcTagState.value = it
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun FenceWiseApp(
    nfcTag: Tag?,
    onClearNfcTag: () -> Unit
) {
    val navController = rememberNavController()
    var isAuthenticated by remember { mutableStateOf(false) }

    if (!isAuthenticated) {
        // Show auth screen
        AuthScreen(onAuthSuccess = { isAuthenticated = true })
    } else {
        // Show main app with bottom navigation
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(onNfcClick = {
                        navController.navigate(Screen.NFC.route)
                    })
                }
                composable(Screen.Jobs.route) {
                    JobsScreen()
                }
                composable(Screen.Timesheets.route) {
                    TimesheetsScreen()
                }
                composable(Screen.NFC.route) {
                    NFCScreen()
                }
                composable(Screen.Chat.route) {
                    ChatScreen()
                }
                composable(Screen.NFC.route) {
                    NFCScreen(
                        nfcTag = nfcTag,
                        onClearTag = onClearNfcTag
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Dashboard", Screen.Dashboard.route, Icons.Default.Dashboard),
        BottomNavItem("Jobs", Screen.Jobs.route, Icons.Default.Work),
        BottomNavItem("NFC", Screen.NFC.route, Icons.Default.Nfc),
        BottomNavItem("Chat", Screen.Chat.route, Icons.Default.Chat)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
