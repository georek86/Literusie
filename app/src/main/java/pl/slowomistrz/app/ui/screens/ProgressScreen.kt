package pl.slowomistrz.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slowomistrz.app.ui.components.TvInteractiveButton
import pl.slowomistrz.app.ui.theme.AccentYellow
import pl.slowomistrz.app.ui.theme.BackgroundDark
import pl.slowomistrz.app.ui.theme.PastelGreen
import pl.slowomistrz.app.ui.theme.PrimaryBlue
import pl.slowomistrz.app.ui.theme.SurfaceCard
import pl.slowomistrz.app.ui.theme.TextPrimary
import pl.slowomistrz.app.ui.theme.TextSecondary

@Composable
fun ProgressScreen(
    masteredCount: Int,
    practicedCount: Int,
    totalWordsCount: Int,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvInteractiveButton(
                    text = "Powrót",
                    icon = Icons.Default.ArrowBack,
                    onClick = onBack
                )

                Text(
                    text = "Postępy w nauce",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(1.dp))
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Karta: Wszystkie słowa w bazie
                ProgressMetricCard(
                    title = "Baza słówek",
                    value = "$totalWordsCount",
                    subtitle = "Dostępne w aplikacji",
                    icon = Icons.Default.School,
                    tint = PrimaryBlue
                )

                // Karta: Ćwiczone słówka
                ProgressMetricCard(
                    title = "Poznane słowa",
                    value = "$practicedCount",
                    subtitle = "Liczba rozpoczętych",
                    icon = Icons.Default.Star,
                    tint = AccentYellow
                )

                // Karta: Opanowane słówka
                ProgressMetricCard(
                    title = "Opanowane słowa",
                    value = "$masteredCount",
                    subtitle = "Zapisane w pamięci",
                    icon = Icons.Default.CheckCircle,
                    tint = PastelGreen
                )
            }
        }
    }
}

@Composable
private fun ProgressMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .width(320.dp)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.height(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = value,
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary
            )
        }
    }
}
