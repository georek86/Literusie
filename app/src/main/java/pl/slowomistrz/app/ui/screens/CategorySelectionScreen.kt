package pl.slowomistrz.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slowomistrz.app.ui.components.TvInteractiveButton
import pl.slowomistrz.app.ui.theme.BackgroundDark
import pl.slowomistrz.app.ui.theme.PrimaryBlue
import pl.slowomistrz.app.ui.theme.TextPrimary
import pl.slowomistrz.app.ui.theme.TextSecondary

@Composable
fun CategorySelectionScreen(
    categories: List<String>,
    onSelectCategory: (String) -> Unit,
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
                    text = "Wybierz kategorię słówek",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(1.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { category ->
                    TvInteractiveButton(
                        text = category.replaceFirstChar { it.uppercase() },
                        onClick = { onSelectCategory(category) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
