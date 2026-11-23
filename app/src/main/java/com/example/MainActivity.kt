package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.FourRideDatabase
import com.example.data.local.SessionPreferences
import com.example.data.model.UserRole
import com.example.data.repository.FourRideRepository
import com.example.data.repository.LocationService
import com.example.ui.screens.AdminDashboard
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DriverDashboard
import com.example.ui.screens.PassengerDashboard
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.FourRideTheme
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.FourRideViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = FourRideDatabase.getInstance(applicationContext)
        val repository = FourRideRepository(
            dao = database.fourRideDao(),
            sessionPreferences = SessionPreferences(applicationContext)
        )
        val locationService = LocationService(applicationContext)

        setContent {
            FourRideTheme {
                val fourRideViewModel: FourRideViewModel = viewModel(
                    factory = FourRideViewModel.Factory(repository, locationService)
                )
                FourRideAppRoot(
                    viewModel = fourRideViewModel,
                    hasLocationPermission = locationService.hasLocationPermission()
                )
            }
        }
    }
}

@Composable
private fun FourRideAppRoot(
    viewModel: FourRideViewModel,
    hasLocationPermission: Boolean
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val otpChallenge by viewModel.otpChallenge.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        if (granted) {
            viewModel.refreshGpsLocation()
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(bannerMessage) {
        if (bannerMessage != null) {
            delay(3800L)
            viewModel.clearBanner()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!session.isLoggedIn) {
            AuthScreen(
                otpChallenge = otpChallenge,
                onRequestOtp = viewModel::requestOtp,
                onClearOtp = viewModel::clearOtpChallenge,
                onVerifyPassengerOtp = viewModel::verifyPassengerOtp,
                onVerifyDriverSignIn = viewModel::verifyDriverOtpAndSignIn,
                onVerifyDriverRegister = viewModel::verifyDriverOtpAndRegister,
                onVerifyAdminOtp = viewModel::verifyAdminOtp
            )
        } else {
            when (session.role) {
                UserRole.PASSENGER -> PassengerDashboard(viewModel = viewModel)
                UserRole.DRIVER -> DriverDashboard(viewModel = viewModel)
                UserRole.ADMIN -> AdminDashboard(viewModel = viewModel)
                null -> Unit
            }
        }

        AnimatedVisibility(
            visible = bannerMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = ObsidianBlack,
                shadowElevation = 8.dp
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(
                        text = bannerMessage.orEmpty(),
                        color = PureWhite,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::clearBanner) {
                        Text("OK", color = BrandOrange)
                    }
                }
            }
        }
    }
}
