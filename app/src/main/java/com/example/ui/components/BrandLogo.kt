package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

@Composable
fun FourRideBrandLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp,
    showSubtitle: Boolean = false,
    darkText: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_four_ride_logo),
            contentDescription = "4 Ride Logo",
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
        Box {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = BrandOrange, fontWeight = FontWeight.Bold)) {
                        append("4 ")
                    }
                    withStyle(
                        SpanStyle(
                            color = if (darkText) ObsidianBlack else PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Ride")
                    }
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp)
            )
            if (showSubtitle) {
                Text(
                    text = "FAST • FAIR • SAFE",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.2.sp),
                    color = BrandOrange,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .background(BrandOrange.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
