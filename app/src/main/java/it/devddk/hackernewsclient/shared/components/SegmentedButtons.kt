package it.devddk.hackernewsclient.shared.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

/**
 * Implements SegmentedButtons with Material3 style guide since Jetpack Compose doesn't have
 * a SegmentedButtons component yet.
 *
 * It is missing some use cases like:
 * - Disabled buttons
 * - Customizable icons
 * - Icons on unselected buttons
 *
 * Unless needed they won't be implemented waiting for an official Jetpack Compose component.
 *
 * @param modifier Modifier for the whole component
 * @param state The state of the component, holding the selected index, or null
 * @param choices The list of choices to display
 * @param onValueChange The callback to call when the value changes (optional)
 */
@Composable
fun SegmentedButtons(
    modifier: Modifier = Modifier,
    state: MutableState<Int?>,
    choices: List<String>,
    onValueChange: (Int) -> Unit = {},
) {
    val selectedValue = state.value

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 100))
            .background(MaterialTheme.colorScheme.background)
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(percent = 100)
            )
    ) {
        choices.forEachIndexed { i, choice ->
            val containerColor =
                if (selectedValue == i) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.background

            val contentColor =
                if (selectedValue == i) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onBackground

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                shape = RectangleShape,
                onClick = { state.value = i; onValueChange(i) },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(if (selectedValue == i) 1.0f else 0.8f),
            ) {
                if (selectedValue == i) {
                    Icon(
                        Icons.Filled.Check,
                        "Selected $choice",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    choice,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(
                        horizontal = if (selectedValue == i) 8.dp else 0.dp
                    ),
                )
            }

            // put a divider after the buttons, except the last one
            if (i != choices.size - 1) {
                Divider(
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
            }
        }
    }
}
