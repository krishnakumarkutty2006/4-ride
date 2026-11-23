package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.UserRole
import com.example.data.model.VehicleType
import com.example.ui.components.AvatarImage
import com.example.ui.components.FourRideBrandLogo
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.BrandOrangeSoft
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenBg
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.OtpChallenge

@Composable
fun AuthScreen(
    otpChallenge: OtpChallenge?,
    onRequestOtp: (phone: String, role: UserRole, isDriverRegistration: Boolean) -> Unit,
    onClearOtp: () -> Unit,
    onVerifyPassengerOtp: (otp: String, name: String) -> Unit,
    onVerifyDriverSignIn: (otp: String) -> Unit,
    onVerifyDriverRegister: (
        otp: String,
        name: String,
        vehicleType: VehicleType,
        vehicleModel: String,
        vehiclePlate: String,
        profilePhotoUri: String,
        vehiclePhotoUri: String
    ) -> Unit,
    onVerifyAdminOtp: (otp: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.PASSENGER) }
    var isDriverRegisterMode by remember { mutableStateOf(false) }

    var passengerPhone by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf(VehicleType.CAR) }
    var vehicleModel by remember { mutableStateOf("") }
    var vehiclePlate by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf("") }
    var vehiclePhotoUri by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var adminPhoneInput by remember { mutableStateOf("") }

    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { profilePhotoUri = it.toString() } }

    val vehiclePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { vehiclePhotoUri = it.toString() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Logo Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FourRideBrandLogo(iconSize = 42.dp, showSubtitle = true)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = BrandOrangeSoft
                ) {
                    Text(
                        text = "City Mobility",
                        color = BrandOrange,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Hero Artwork Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_login_hero),
                        contentDescription = "4 Ride city mobility hero",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = ObsidianBlack.copy(alpha = 0.88f)
                    ) {
                        Text(
                            text = "Transparent fares • Verified drivers • Live GPS",
                            color = PureWhite,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Independent Role Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceMuted)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple(UserRole.PASSENGER, "Passenger", Icons.Default.Person),
                    Triple(UserRole.DRIVER, "Driver", Icons.Default.DirectionsCar),
                    Triple(UserRole.ADMIN, "Admin", Icons.Default.AdminPanelSettings)
                ).forEach { (role, label, icon) ->
                    val active = selectedRole == role
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("role_tab_${role.name.lowercase()}")
                            .clickable {
                                selectedRole = role
                                otpInput = ""
                                onClearOtp()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (active) ObsidianBlack else PureWhite.copy(alpha = 0f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (active) BrandOrange else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (active) PureWhite else TextSecondary
                            )
                        }
                    }
                }
            }

            // Simulated SMS OTP Banner when challenge is active
            AnimatedVisibility(visible = otpChallenge != null) {
                otpChallenge?.let { challenge ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SuccessGreenBg,
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = "OTP SMS", tint = SuccessGreen)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "4 Ride Verification Code",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ObsidianBlack
                                )
                                Text(
                                    text = "OTP for ${challenge.phone}: ${challenge.code}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessGreen
                                )
                            }
                            TextButton(onClick = { otpInput = challenge.code }) {
                                Text("Auto-fill", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Role Specific Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedRole) {
                        UserRole.PASSENGER -> {
                            Text("Passenger Login", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                text = "Sign in quickly using just your mobile phone number and OTP verification.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            OutlinedTextField(
                                value = passengerPhone,
                                onValueChange = { passengerPhone = it },
                                label = { Text("Mobile Phone Number") },
                                placeholder = { Text("+91 9811223344") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("passenger_phone_input"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            // Quick fill demo passenger chip
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Quick demo:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Surface(
                                    modifier = Modifier.clickable { passengerPhone = "+919811223344" },
                                    shape = RoundedCornerShape(8.dp),
                                    color = BrandOrangeSoft
                                ) {
                                    Text(
                                        text = "Ananya (+919811223344)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrandOrange,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (otpChallenge?.role == UserRole.PASSENGER) {
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { otpInput = it },
                                    label = { Text("Enter 6-digit OTP") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("passenger_otp_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Button(
                                    onClick = { onVerifyPassengerOtp(otpInput, "") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("verify_passenger_otp_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                                ) {
                                    Text("Verify & Continue", color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onRequestOtp(passengerPhone, UserRole.PASSENGER, false) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("send_passenger_otp_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack)
                                ) {
                                    Text("Send OTP", color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        UserRole.DRIVER -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDriverRegisterMode) "Driver Registration" else "Driver Login",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                TextButton(onClick = {
                                    isDriverRegisterMode = !isDriverRegisterMode
                                    otpInput = ""
                                    onClearOtp()
                                }) {
                                    Text(
                                        text = if (isDriverRegisterMode) "Existing Driver?" else "New Driver? Register",
                                        color = BrandOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isDriverRegisterMode) {
                                OutlinedTextField(
                                    value = driverName,
                                    onValueChange = { driverName = it },
                                    label = { Text("Full Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }

                            OutlinedTextField(
                                value = driverPhone,
                                onValueChange = { driverPhone = it },
                                label = { Text("Driver Mobile Number") },
                                placeholder = { Text("+91 9876543210") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("driver_phone_input"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            if (!isDriverRegisterMode) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Demo driver:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Surface(
                                        modifier = Modifier.clickable { driverPhone = "+919876543210" },
                                        shape = RoundedCornerShape(8.dp),
                                        color = BrandOrangeSoft
                                    ) {
                                        Text(
                                            text = "Arjun (+919876543210)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrandOrange,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            if (isDriverRegisterMode) {
                                Text("Vehicle Category", style = MaterialTheme.typography.labelLarge)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    VehicleType.entries.forEach { vType ->
                                        val selected = vehicleType == vType
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { vehicleType = vType },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (selected) BrandOrangeSoft else SurfaceMuted,
                                            border = BorderStroke(1.dp, if (selected) BrandOrange else CardBorder)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(vType.emoji, style = MaterialTheme.typography.titleLarge)
                                                Text(
                                                    text = vType.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selected) BrandOrange else ObsidianBlack
                                                )
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = vehicleModel,
                                    onValueChange = { vehicleModel = it },
                                    label = { Text("Vehicle Model (e.g. Toyota Innova)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                OutlinedTextField(
                                    value = vehiclePlate,
                                    onValueChange = { vehiclePlate = it },
                                    label = { Text("Vehicle Number Plate") },
                                    placeholder = { Text("KA-01-FR-2026") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                // Photo Picker for Profile & Vehicle photos
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                profilePhotoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        color = SurfaceMuted,
                                        border = BorderStroke(1.dp, CardBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            AvatarImage(profilePhotoUri, "Driver profile photo", size = 34.dp)
                                            Column {
                                                Text("Profile Photo", style = MaterialTheme.typography.labelMedium)
                                                Text(
                                                    text = if (profilePhotoUri.isBlank()) "Pick photo" else "Selected ✓",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = BrandOrange
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                vehiclePhotoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        color = SurfaceMuted,
                                        border = BorderStroke(1.dp, CardBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            AvatarImage(vehiclePhotoUri, "Vehicle photo", size = 34.dp, isVehicle = true)
                                            Column {
                                                Text("Vehicle Photo", style = MaterialTheme.typography.labelMedium)
                                                Text(
                                                    text = if (vehiclePhotoUri.isBlank()) "Pick photo" else "Selected ✓",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = BrandOrange
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (otpChallenge?.role == UserRole.DRIVER) {
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { otpInput = it },
                                    label = { Text("Enter 6-digit OTP") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Button(
                                    onClick = {
                                        if (isDriverRegisterMode) {
                                            onVerifyDriverRegister(
                                                otpInput,
                                                driverName,
                                                vehicleType,
                                                vehicleModel,
                                                vehiclePlate,
                                                profilePhotoUri,
                                                vehiclePhotoUri
                                            )
                                        } else {
                                            onVerifyDriverSignIn(otpInput)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("verify_driver_otp_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                                ) {
                                    Text(
                                        text = if (isDriverRegisterMode) "Verify OTP & Register" else "Verify & Open Driver App",
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onRequestOtp(driverPhone, UserRole.DRIVER, isDriverRegisterMode)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("send_driver_otp_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack)
                                ) {
                                    Text("Send OTP", color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        UserRole.ADMIN -> {
                            Text("App Owner Admin Portal", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                text = "Sign in with the authorized Admin mobile number and OTP verification to manage drivers, rides, and platform pricing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            OutlinedTextField(
                                value = adminPhoneInput,
                                onValueChange = { adminPhoneInput = it },
                                label = { Text("Admin Mobile Number") },
                                placeholder = { Text("6374534661") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_phone_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Admin number:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Surface(
                                    modifier = Modifier.clickable { adminPhoneInput = "6374534661" },
                                    shape = RoundedCornerShape(8.dp),
                                    color = BrandOrangeSoft
                                ) {
                                    Text(
                                        text = "6374534661 (Tap to fill)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrandOrange,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            if (otpChallenge?.role == UserRole.ADMIN) {
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { otpInput = it },
                                    label = { Text("Enter 6-digit OTP") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_otp_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Button(
                                    onClick = { onVerifyAdminOtp(otpInput) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("verify_admin_otp_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                                ) {
                                    Text("Verify OTP & Open Admin", color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onRequestOtp(adminPhoneInput, UserRole.ADMIN, false) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("send_admin_otp_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack)
                                ) {
                                    Text("Send Admin OTP", color = PureWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
