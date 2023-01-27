package com.example.restaurantsapp

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class RestaurantsViewModel(private val stateHandle: SavedStateHandle) : ViewModel() {

    private var restInterface: RestaurantsApiService
    val state = mutableStateOf(emptyList<Restaurant>())
    private lateinit var restaurantsCall: Call<List<Restaurant>>

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(
                "https://restaurantsapp-cb673-default-rtdb.firebaseio.com/"
            ).addConverterFactory(
                GsonConverterFactory.create()
            ).build()

        restInterface = retrofit.create(
            RestaurantsApiService::class.java
        )
        getRestaurants()
    }


    private fun getRestaurants() {
        restaurantsCall = restInterface.getRestaurants()

        restaurantsCall.enqueue(
            object : Callback<List<Restaurant>> {
                override fun onResponse(
                    call: Call<List<Restaurant>>,
                    response: Response<List<Restaurant>>
                ) {
                    response.body()
                        ?.let { restaurants -> state.value = restaurants.restoreSelections() }
                }

                override fun onFailure(call: Call<List<Restaurant>>, t: Throwable) {
                    t.printStackTrace()
                }

            }
        )
    }


    fun toggleFavorite(id: Int) {
        val restaurants = state.value.toMutableList()
        val itemIndex = restaurants.indexOfFirst { it.id == id }
        val item = restaurants[itemIndex]
        restaurants[itemIndex] = item.copy(isFavorite = !item.isFavorite)
        storeSelection(restaurants[itemIndex])
        state.value = restaurants
    }

    private fun storeSelection(restaurant: Restaurant) {
        val saveToggled = stateHandle.get<List<Int>?>(FAVORITES).orEmpty().toMutableList()

        if (restaurant.isFavorite) saveToggled.add(restaurant.id) else saveToggled.remove(restaurant.id)
        stateHandle[FAVORITES] = saveToggled
    }

    private fun List<Restaurant>.restoreSelections(): List<Restaurant> {
        //TODO: Fix restoring favorites items. Favorites Do not restore after system sending process death
        var st = stateHandle.get<List<Int>>(FAVORITES)
        Log.d("Started restoring", "Doing now: $st")
        stateHandle.get<List<Int>>(FAVORITES)?.let { selectedIds ->
            val restaurantsMap = this.associateBy { it.id }
            selectedIds.forEach { id ->
                restaurantsMap[id]?.isFavorite = true
            }
            return restaurantsMap.values.toList()
        }
        return this
    }

    override fun onCleared() {
        super.onCleared()
        restaurantsCall.cancel()
    }

    companion object {
        const val FAVORITES = "favorites"
    }
}