package io.github.itslanmi.withdrawwise.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.itslanmi.withdrawwise.R

val Poppins =
    FontFamily(
        Font(R.font.poppins_regular, FontWeight.Normal),
        Font(R.font.poppins_medium, FontWeight.Medium),
        Font(R.font.poppins_semibold, FontWeight.SemiBold),
        Font(R.font.poppins_bold, FontWeight.Bold)
    )

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = Poppins,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.em
)

val WithdrawWiseTypography =
    Typography(
        displayLarge = style(52, 60, FontWeight.Bold, -0.02),
        displayMedium = style(44, 52, FontWeight.Bold, -0.02),
        displaySmall = style(36, 44, FontWeight.Bold, -0.015),
        headlineLarge = style(30, 38, FontWeight.Bold, -0.01),
        headlineMedium = style(26, 34, FontWeight.Bold, -0.01),
        headlineSmall = style(22, 30, FontWeight.SemiBold),
        titleLarge = style(20, 28, FontWeight.SemiBold),
        titleMedium = style(16, 24, FontWeight.SemiBold),
        titleSmall = style(14, 20, FontWeight.SemiBold),
        bodyLarge = style(16, 24, FontWeight.Normal),
        bodyMedium = style(14, 21, FontWeight.Normal),
        bodySmall = style(12, 18, FontWeight.Normal),
        labelLarge = style(14, 20, FontWeight.SemiBold),
        labelMedium = style(12, 16, FontWeight.Medium),
        labelSmall = style(11, 16, FontWeight.Medium, 0.02)
    )
