# NFC Feature Documentation

## Overview
The FenceWise Android app now includes comprehensive NFC (Near Field Communication) functionality, allowing users to read from and write to NFC tags.

## Features

### 1. NFC Tag Reading
- Automatically detects NFC tags when held near the device
- Displays tag information including:
  - Tag ID in hexadecimal format
  - List of supported technologies
  - NDEF message content (if available)
- Supports multiple NDEF record types:
  - Text records
  - MIME type records
  - URI records

### 2. NFC Tag Writing
- Write custom text messages to NFC tags
- Supports both formatted and unformatted tags
- Automatically formats unformatted tags when writing

### 3. User Interface
- Dedicated NFC screen accessible from:
  - Dashboard card ("NFC Reader")
  - Bottom navigation bar (NFC icon)
- Visual indicators for NFC status:
  - Green indicator when NFC is enabled
  - Red warning when NFC is disabled
- Clear instructions for usage
- Real-time tag detection and display

## Technical Implementation

### Permissions
The app requires the following NFC-related permissions (AndroidManifest.xml):
```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="false" />
```

Note: NFC is marked as not required (`android:required="false"`) so the app can still be installed on devices without NFC hardware.

### Intent Filters
The MainActivity handles the following NFC-related intents:
- `ACTION_NDEF_DISCOVERED` - For NDEF formatted tags with text/plain MIME type
- `ACTION_TAG_DISCOVERED` - For any NFC tag

The activity uses `singleTop` launch mode to handle new NFC intents without creating new activity instances.

### Key Components

#### MainActivity.kt
- Initializes NFC adapter
- Enables foreground dispatch for NFC in `onResume()`
- Disables foreground dispatch in `onPause()`
- Handles NFC intents in `onNewIntent()`
- Manages NFC tag state using Compose state

#### NFCScreen.kt
- Main UI for NFC functionality
- Displays NFC status and instructions
- Shows tag information when detected
- Provides write dialog for writing to tags
- Includes helper functions:
  - `parseTextRecord()` - Parses NDEF text records
  - `writeNfcTag()` - Writes NDEF messages to tags
  - `createTextRecord()` - Creates NDEF text records

## Usage

### Reading NFC Tags
1. Navigate to the NFC screen via Dashboard or bottom navigation
2. Ensure NFC is enabled on the device
3. Hold an NFC tag near the device's NFC antenna
4. Tag information will be displayed automatically

### Writing to NFC Tags
1. Navigate to the NFC screen
2. Tap the "Write to Tag" button
3. Enter the text you want to write
4. Tap "Write"
5. Hold an NFC tag near the device when prompted
6. The app will write the text to the tag

## Supported NFC Technologies
The app supports reading tags with the following technologies:
- NDEF (NFC Data Exchange Format)
- NdefFormatable (for formatting and writing to blank tags)

## Error Handling
- Displays user-friendly error messages for common issues
- Handles tags that don't support NDEF format
- Gracefully handles read/write failures
- Shows clear status indicators for NFC availability

## Testing

### Without Physical NFC Tags
While physical NFC tags are ideal for testing, you can verify the implementation by:
1. Checking that the NFC screen loads without errors
2. Verifying the NFC status indicator (will show disabled if device lacks NFC)
3. Testing the UI interactions (write dialog, clear button)

### With Physical NFC Tags
1. Use NFC-enabled Android device (API 24+)
2. Enable NFC in device settings
3. Test reading various tag types (NDEF formatted, blank tags)
4. Test writing text to tags
5. Verify tag information is displayed correctly

## Future Enhancements
Potential improvements for the NFC feature:
- Support for additional NDEF record types (URIs, vCards, etc.)
- Batch tag reading/writing
- Tag history and favorites
- Integration with job management (e.g., tag jobs/equipment)
- QR code fallback for devices without NFC
