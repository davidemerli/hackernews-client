package it.devddk.hackernewsclient.shared.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test


class SegmentedButtonUITest {


    @get:Rule
    val composeTestRule = createComposeRule()

    private fun testSegmentedButton(nButtons: Int) {
        val choices = (0 until nButtons).map { it.toString() }

        composeTestRule.apply {
            setContent {
                val state = mutableStateOf<Int?>(null)
                SegmentedButtons(state = state, choices = choices)
            }

            choices.forEach {
                onNodeWithContentDescription("Selected $it").assertDoesNotExist()
                onNodeWithText(it,
                    substring = true,
                    ignoreCase = true).assertExists("Option $it should be visible")
                    .assertHasClickAction()
            }


            choices.forEach { selected ->
                onNodeWithText(selected,
                    substring = true,
                    ignoreCase = true).assertExists("Option $selected should be visible")
                    .performClick()
                onNodeWithContentDescription("Selected $selected").assertExists("Options $selected should be selected")
                choices.filter { it != selected }.forEach { notSelected ->
                    onNodeWithContentDescription("Selected $notSelected").assertDoesNotExist()
                    onNodeWithText(notSelected,
                        substring = true,
                        ignoreCase = true).assertExists("Option $notSelected should be visible, when $selected is selected")
                        .assertHasClickAction()

                }
            }
        }
    }

    @Test
    fun testSegmentedButton_1_options() {
        testSegmentedButton(1)
    }

    @Test
    fun testSegmentedButton_3_options() {
        testSegmentedButton(3)
    }

    @Test
    fun testSegmentedButton_5_options() {
        testSegmentedButton(5)
    }

    @Test
    fun testSegmentedButton_6_options() {
        testSegmentedButton(6)
    }
}