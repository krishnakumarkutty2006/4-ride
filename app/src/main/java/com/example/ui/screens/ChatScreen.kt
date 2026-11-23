package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessageEntity
import com.example.data.model.RideBookingEntity
import com.example.data.model.RideStatus
import com.example.data.model.UserRole
import com.example.ui.components.MaskedCallDialog
import com.example.ui.components.StatusPill
import com.example.ui.components.launchDialer
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.BrandOrangeSoft
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val QUICK_CHAT_MESSAGES = listOf(
    "I'm coming.",
    "Where are you?",
    "I'm waiting.",
    "I'm at pickup point."
)

@Composable
fun MatchedBookingsChatTab(
    bookings: List<RideBookingEntity>,
    currentRole: UserRole,
    currentUserName: String,
    selectedBookingId: String?,
    onSelectBooking: (String?) -> Unit,
    observeChat: (String) -> Flow<List<ChatMessageEntity>>,
    onSendMessage: (bookingId: String, message: String) -> Unit
) {
    // Chat available ONLY for matched bookings (accepted or later, not requested/unmatched)
    val matchedBookings = remember(bookings) {
        bookings.filter {
            it.status != RideStatus.REQUESTED.name && it.status != RideStatus.CANCELLED.name
        }
    }

    val activeBooking = matchedBookings.firstOrNull { it.id == selectedBookingId } ?: matchedBookings.firstOrNull()

    if (activeBooking == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = CircleShape, color = BrandOrangeSoft, modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = BrandOrange,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Text("No Matched Ride Chats Yet", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Real-time chat unlocks automatically once a booking is accepted between passenger and driver.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
    ) {
        if (matchedBookings.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                matchedBookings.forEach { b ->
                    val isSelected = b.id == activeBooking.id
                    val peer = if (currentRole == UserRole.PASSENGER) b.driverName else b.passengerName
                    Surface(
                        modifier = Modifier.clickable { onSelectBooking(b.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ObsidianBlack else PureWhite,
                        border = BorderStroke(1.dp, if (isSelected) ObsidianBlack else CardBorder)
                    ) {
                        Text(
                            text = "$peer • ₹${b.agreedFare.toInt()}",
                            color = if (isSelected) PureWhite else ObsidianBlack,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        BookingChatThread(
            booking = activeBooking,
            currentRole = currentRole,
            currentUserName = currentUserName,
            observeChat = observeChat,
            onSendMessage = onSendMessage,
            onBack = null
        )
    }
}

@Composable
fun BookingChatThread(
    booking: RideBookingEntity,
    currentRole: UserRole,
    currentUserName: String,
    observeChat: (String) -> Flow<List<ChatMessageEntity>>,
    onSendMessage: (bookingId: String, message: String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val messages by observeChat(booking.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var messageText by remember { mutableStateOf("") }
    var showCallDialog by remember { mutableStateOf(false) }

    val peerName = if (currentRole == UserRole.PASSENGER) booking.driverName else booking.passengerName
    val peerPhone = if (currentRole == UserRole.PASSENGER) booking.driverPhone else booking.passengerPhone
    val rideStatus = RideStatus.entries.firstOrNull { it.name == booking.status } ?: RideStatus.ACCEPTED

    if (showCallDialog) {
        MaskedCallDialog(
            recipientName = peerName,
            phone = peerPhone,
            onLaunchDialer = { launchDialer(context, peerPhone) },
            onDismiss = { showCallDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
    ) {
        // Peer Header
        Surface(
            color = PureWhite,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(peerName, style = MaterialTheme.typography.titleMedium)
                        StatusPill(rideStatus)
                    }
                    Text(
                        text = "${booking.pickupName} → ${booking.dropName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                Surface(
                    modifier = Modifier
                        .testTag("chat_call_button")
                        .clickable { showCallDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = BrandOrangeSoft
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = BrandOrange, modifier = Modifier.size(16.dp))
                        Text("Call", color = BrandOrange, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = "Ride matched! Coordinate pickup details safely with $peerName using text or quick replies below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
            items(messages, key = { it.id }) { msg ->
                val isMine = msg.senderRole == currentRole.name
                val timeStr = remember(msg.timestamp) {
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMine) 16.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 16.dp
                        ),
                        color = if (isMine) ObsidianBlack else PureWhite,
                        border = if (isMine) null else BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = msg.message,
                                color = if (isMine) PureWhite else ObsidianBlack,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = timeStr,
                                color = if (isMine) PureWhite.copy(alpha = 0.65f) else TextSecondary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // Quick Messages Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QUICK_CHAT_MESSAGES.forEach { quick ->
                Surface(
                    modifier = Modifier.clickable {
                        onSendMessage(booking.id, quick)
                    },
                    shape = RoundedCornerShape(999.dp),
                    color = SurfaceMuted,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = quick,
                        style = MaterialTheme.typography.labelMedium,
                        color = ObsidianBlack,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Composer
        Surface(color = PureWhite, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Message $peerName...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(24.dp)
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandOrange)
                        .testTag("send_chat_button")
                        .clickable {
                            if (messageText.isNotBlank()) {
                                onSendMessage(booking.id, messageText)
                                messageText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
