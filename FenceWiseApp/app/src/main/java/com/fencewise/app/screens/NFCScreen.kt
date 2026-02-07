package com.fencewise.app.screens

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.*
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fencewise.app.data.NFCTag
import com.fencewise.app.data.NFCTagStorage
import java.util.*

@Composable
fun NFCScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val storage = remember { NFCTagStorage(context) }
    
    var savedTags by remember { mutableStateOf(storage.loadTags()) }
    var isScanning by remember { mutableStateOf(false) }
    var scanMessage by remember { mutableStateOf("") }
    var activeTagId by remember { mutableStateOf<String?>(storage.getActiveTag()?.id) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var tagToRename by remember { mutableStateOf<NFCTag?>(null) }
    
    // Check if NFC is supported and enabled
    val nfcSupported = nfcAdapter != null
    val nfcEnabled = nfcAdapter?.isEnabled == true
    
    // Handle NFC intent when tag is detected
    DisposableEffect(activity) {
        if (activity != null && nfcAdapter != null && nfcEnabled) {
            val pendingIntent = PendingIntent.getActivity(
                activity,
                0,
                Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
            )
            
            val intentFilters = arrayOf(
                IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
            )
            
            val techLists = arrayOf(
                arrayOf(NfcA::class.java.name),
                arrayOf(NfcB::class.java.name),
                arrayOf(NfcF::class.java.name),
                arrayOf(NfcV::class.java.name),
                arrayOf(IsoDep::class.java.name),
                arrayOf(Ndef::class.java.name),
                arrayOf(NdefFormatable::class.java.name),
                arrayOf(MifareClassic::class.java.name),
                arrayOf(MifareUltralight::class.java.name)
            )
            
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFilters, techLists)
        }
        
        onDispose {
            if (activity != null && nfcAdapter != null) {
                nfcAdapter.disableForegroundDispatch(activity)
            }
        }
    }
    
    // Handle NFC tag when scanned
    LaunchedEffect(activity?.intent) {
        activity?.intent?.let { intent ->
            if (isScanning && (
                intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
                intent.action == NfcAdapter.ACTION_TECH_DISCOVERED ||
                intent.action == NfcAdapter.ACTION_TAG_DISCOVERED
            )) {
                val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                tag?.let {
                    val nfcTag = readNFCTag(it)
                    storage.saveTag(nfcTag)
                    savedTags = storage.loadTags()
                    scanMessage = "Tag saved: ${nfcTag.name}"
                    isScanning = false
                    
                    // Clear the intent to avoid re-reading
                    intent.action = ""
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NFC Tag Manager") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // NFC Status Card
            if (!nfcSupported) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "NFC not supported on this device",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else if (!nfcEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "NFC is disabled",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                            }
                        ) {
                            Text("Enable NFC")
                        }
                    }
                }
            }
            
            // Scan Button
            if (nfcEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    border = if (isScanning) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                isScanning = !isScanning
                                scanMessage = if (isScanning) "Hold phone near NFC tag..." else ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isScanning) 
                                    MaterialTheme.colorScheme.secondary 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                if (isScanning) Icons.Default.Stop else Icons.Default.Nfc,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isScanning) "Cancel Scanning" else "Scan NFC Tag")
                        }
                        
                        if (scanMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                scanMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            
            // Saved Tags Section
            Text(
                "Saved Tags (${savedTags.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (savedTags.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tags saved yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(savedTags) { tag ->
                        NFCTagCard(
                            tag = tag,
                            isActive = tag.id == activeTagId,
                            onEmulate = {
                                if (activeTagId == tag.id) {
                                    storage.setActiveTag(null)
                                    activeTagId = null
                                } else {
                                    storage.setActiveTag(tag.id)
                                    activeTagId = tag.id
                                }
                            },
                            onRename = {
                                tagToRename = tag
                                showRenameDialog = true
                            },
                            onDelete = {
                                storage.deleteTag(tag.id)
                                savedTags = storage.loadTags()
                                if (activeTagId == tag.id) {
                                    activeTagId = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Rename Dialog
    if (showRenameDialog && tagToRename != null) {
        var newName by remember { mutableStateOf(tagToRename?.name ?: "") }
        
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Tag") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Tag Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            storage.updateTagName(tagToRename?.id ?: "", newName)
                            savedTags = storage.loadTags()
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NFCTagCard(
    tag: NFCTag,
    isActive: Boolean,
    onEmulate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    tag.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "EMULATING",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "UID: ${tag.uid}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Tech: ${tag.technology}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Data: ${tag.dataHex.take(32)}${if (tag.dataHex.length > 32) "..." else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Size: ${tag.data.size} bytes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEmulate,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isActive) "Stop" else "Emulate")
                }
                
                IconButton(
                    onClick = onRename
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename")
                }
                
                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun readNFCTag(tag: Tag): NFCTag {
    val tagId = tag.id
    val uid = tagId.toHexString()
    val techList = tag.techList.joinToString(", ") { it.substringAfterLast('.') }
    
    // Try to read data from the tag
    var data = ByteArray(0)
    
    // Try NDEF first
    tag.techList.find { it.contains("Ndef") }?.let {
        try {
            val ndef = Ndef.get(tag)
            ndef?.connect()
            val ndefMessage = ndef?.ndefMessage
            ndefMessage?.let { msg ->
                data = msg.toByteArray()
            }
            ndef?.close()
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    // If no NDEF data, use tag ID as data
    if (data.isEmpty()) {
        data = tagId
    }
    
    val dataHex = data.toHexString()
    val timestamp = System.currentTimeMillis()
    val name = "NFC Tag ${Date(timestamp).toString().substring(4, 16)}"
    val id = UUID.randomUUID().toString()
    
    return NFCTag(
        id = id,
        name = name,
        uid = uid,
        technology = techList,
        data = data,
        dataHex = dataHex,
        timestamp = timestamp
    )
}

private fun ByteArray.toHexString(): String {
    return joinToString("") { "%02X".format(it) }
}
