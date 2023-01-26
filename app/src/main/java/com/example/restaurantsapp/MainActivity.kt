package com.example.restaurantsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable

import androidx.compose.ui.tooling.preview.Preview
import com.example.restaurantsapp.ui.theme.RestaurantsAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestaurantsAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background)
                {
                    RestaurantsScreen()
                }
            }
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    RestaurantsAppTheme {
        RestaurantsScreen()
    }
}