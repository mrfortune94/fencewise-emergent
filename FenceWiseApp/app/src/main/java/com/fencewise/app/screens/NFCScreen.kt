package com.fencewise.app.screens

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.nio.charset.Charset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCScreen(
    nfcTag: Tag?,
    onClearTag: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    
    var nfcEnabled by remember { mutableStateOf(nfcAdapter?.isEnabled ?: false) }
    var tagInfo by remember { mutableStateOf("") }
    var ndefMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var writeText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var showWriteDialog by remember { mutableStateOf(false) }
    
    // Update NFC enabled status
    LaunchedEffect(Unit) {
        nfcEnabled = nfcAdapter?.isEnabled ?: false
    }
    
    // Process NFC tag when it changes
    LaunchedEffect(nfcTag) {
        nfcTag?.let { tag ->
            tagInfo = buildString {
                append("Tag ID: ${tag.id.toHexString()}\n")
                append("Tech List: ${tag.techList.joinToString(", ") { it.substringAfterLast('.') }}\n")
            }
            
            // Try to read NDEF data
            val ndef = Ndef.get(tag)
            ndef?.let {
                try {
                    ndef.connect()
                    val ndefMessage = ndef.ndefMessage
                    ndefMessage?.let { msg ->
                        val messages = msg.records.mapNotNull { record ->
                            when (record.tnf) {
                                NdefRecord.TNF_WELL_KNOWN -> {
                                    if (record.type.contentEquals(NdefRecord.RTD_TEXT)) {
                                        parseTextRecord(record)
                                    } else {
                                        "Well-known type: ${record.type.toHexString()}"
                                    }
                                }
                                NdefRecord.TNF_MIME_MEDIA -> {
                                    "MIME: ${String(record.type)}"
                                }
                                NdefRecord.TNF_ABSOLUTE_URI -> {
                                    "URI: ${String(record.payload)}"
                                }
                                else -> "Unknown type"
                            }
                        }
                        ndefMessages = messages
                    }
                    ndef.close()
                } catch (e: Exception) {
                    statusMessage = "Error reading tag: ${e.message}"
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "NFC Reader/Writer",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // NFC Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (nfcEnabled) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (nfcEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (nfcEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "NFC Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (nfcEnabled) "NFC is enabled and ready" else "NFC is disabled - Enable in settings",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Instructions Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How to Use",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Hold an NFC tag near your device to read it\n" +
                           "• Tap 'Write to Tag' to write custom data\n" +
                           "• NFC must be enabled in device settings",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showWriteDialog = true },
                modifier = Modifier.weight(1f),
                enabled = nfcEnabled
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Write to Tag")
            }
            
            OutlinedButton(
                onClick = {
                    onClearTag()
                    tagInfo = ""
                    ndefMessages = emptyList()
                    statusMessage = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Message
        if (statusMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Tag Information Card
        if (tagInfo.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tag Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tagInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // NDEF Messages Card
        if (ndefMessages.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NDEF Messages",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ndefMessages.forEachIndexed { index, message ->
                        Text(
                            text = "${index + 1}. $message",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (index < ndefMessages.size - 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
        
        // Show placeholder when no tag is scanned
        if (tagInfo.isEmpty() && ndefMessages.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (nfcEnabled) "Hold an NFC tag near your device" else "Enable NFC to get started",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
    
    // Write Dialog
    if (showWriteDialog) {
        AlertDialog(
            onDismissRequest = { showWriteDialog = false },
            title = { Text("Write to NFC Tag") },
            text = {
                Column {
                    Text("Enter text to write to the NFC tag:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = writeText,
                        onValueChange = { writeText = it },
                        label = { Text("Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        nfcTag?.let { tag ->
                            val success = writeNfcTag(tag, writeText)
                            if (success) {
                                statusMessage = "Successfully wrote to tag!"
                                writeText = ""
                            } else {
                                statusMessage = "Failed to write to tag"
                            }
                        } ?: run {
                            statusMessage = "No tag detected. Hold a tag near the device and try again."
                        }
                        showWriteDialog = false
                    }
                ) {
                    Text("Write")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWriteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper function to parse text record
private fun parseTextRecord(record: NdefRecord): String {
    val payload = record.payload
    if (payload.isEmpty()) return ""
    
    val textEncoding = if ((payload[0].toInt() and 128) == 0) "UTF-8" else "UTF-16"
    val languageCodeLength = payload[0].toInt() and 0x3F // Lower 6 bits for language code length
    
    return try {
        String(
            payload,
            languageCodeLength + 1,
            payload.size - languageCodeLength - 1,
            Charset.forName(textEncoding)
        )
    } catch (e: Exception) {
        "Error parsing text: ${e.message}"
    }
}

// Helper function to convert byte array to hex string
private fun ByteArray.toHexString(): String =
    joinToString("") { "%02X".format(it) }

// Function to write to NFC tag
private fun writeNfcTag(tag: Tag, text: String): Boolean {
    return try {
        val ndefMessage = createTextRecord(text)
        
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            ndef.connect()
            ndef.writeNdefMessage(ndefMessage)
            ndef.close()
            true
        } else {
            // Try to format the tag if it's not NDEF formatted
            val format = NdefFormatable.get(tag)
            if (format != null) {
                format.connect()
                format.format(ndefMessage)
                format.close()
                true
            } else {
                false
            }
        }
    } catch (e: Exception) {
        false
    }
}

// Function to create NDEF text record
private fun createTextRecord(text: String): NdefMessage {
    val language = "en"
    val textBytes = text.toByteArray()
    val languageBytes = language.toByteArray(Charset.forName("US-ASCII"))
    val textLength = textBytes.size
    val languageLength = languageBytes.size
    val payload = ByteArray(1 + languageLength + textLength)
    
    // Status byte: bit 7 = 0 for UTF-8, bits 0-5 = language code length
    payload[0] = (languageLength and 0x3F).toByte()
    System.arraycopy(languageBytes, 0, payload, 1, languageLength)
    System.arraycopy(textBytes, 0, payload, 1 + languageLength, textLength)
    
    val record = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    return NdefMessage(arrayOf(record))
}
