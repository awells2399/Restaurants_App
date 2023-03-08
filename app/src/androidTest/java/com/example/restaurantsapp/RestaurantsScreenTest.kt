package com.example.restaurantsapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.restaurantsapp.restaurants.DummyContent
import com.example.restaurantsapp.restaurants.presentation.Description
import com.example.restaurantsapp.restaurants.presentation.list.RestaurantsScreen
import com.example.restaurantsapp.restaurants.presentation.list.RestaurantsScreenState
import com.example.restaurantsapp.ui.theme.RestaurantsAppTheme
import org.junit.Rule
import org.junit.Test

class RestaurantsScreenTest {
    @get:Rule
    val testRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun initialState_isRendered() {
        testRule.setContent {
            RestaurantsAppTheme {
                RestaurantsScreen(state = RestaurantsScreenState(
                    restaurants = emptyList(),
                    isLoading = true
                ),
                    onItemClick = {},
                    onFavoriteClick = { _: Int, _: Boolean -> })
            }
        }
        testRule.onNodeWithContentDescription(Description.RESTAURANTS_LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun stateWithContext_isRendered() {
        val restaurants = DummyContent.getDomainRestaurants()
        testRule.setContent {
            RestaurantsAppTheme {
                RestaurantsScreen(
                    state = RestaurantsScreenState(
                        restaurants = restaurants,
                        isLoading = false
                    ), onItemClick = {}, onFavoriteClick = { _: Int, _: Boolean -> })

            }
        }
        testRule.onNodeWithText(restaurants[0].title).assertIsDisplayed()
        testRule.onNodeWithText(restaurants[0].description).assertIsDisplayed()
        testRule.onNodeWithContentDescription(Description.RESTAURANTS_LOADING).assertDoesNotExist()
    }

    @Test
    fun stateWithError_isRendered() {
        testRule.setContent {
            RestaurantsAppTheme {
                RestaurantsScreen(
                    state = RestaurantsScreenState(
                        restaurants = listOf(),
                        isLoading = false,
                        error = "Test Error state"
                    ), onItemClick = {}, onFavoriteClick = { _: Int, _: Boolean -> })

            }
        }

        testRule.onNodeWithText("Test Error state").assertIsDisplayed()
        testRule.onNodeWithContentDescription(Description.RESTAURANTS_LOADING).assertDoesNotExist()
    }

    @Test
    fun stateWithContext_ClickOnItem_isRegistered() {
        val restaurants = DummyContent.getDomainRestaurants()
        val targetRestaurant = restaurants[0]

        testRule.setContent {
            RestaurantsAppTheme {
                RestaurantsScreen(
                    state = RestaurantsScreenState(
                        restaurants = restaurants,
                        isLoading = false
                    ),
                    onItemClick = { id -> assert(id == targetRestaurant.id) },
                    onFavoriteClick = { _: Int, _: Boolean -> })

            }
        }
        testRule.onNodeWithText(targetRestaurant.title).performClick()
    }
}
