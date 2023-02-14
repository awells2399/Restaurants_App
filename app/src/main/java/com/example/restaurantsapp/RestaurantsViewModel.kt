package com.example.restaurantsapp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.UnknownHostException


class RestaurantsViewModel() : ViewModel() {

    private var restInterface: RestaurantsApiService
    private var restaurantDao = RestaurantsDb.getDaoInstance(RestaurantsApplication.getAppContext())
    val state = mutableStateOf(emptyList<Restaurant>())
    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
    }

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
        viewModelScope.launch(errorHandler) {
            val restaurants = getAllRestaurants()
        }
    }

    private suspend fun getAllRestaurants(): List<Restaurant> {
        return withContext(Dispatchers.IO) {
            try {
                refreshCache()
            } catch (e: Exception) {
                when (e) {
                    is UnknownHostException,
                    is ConnectException,
                    is HttpException -> {
                        if (restaurantDao.getAll().isEmpty())
                            throw Exception(
                                "Something went wrong. " + "We have no data"
                            )
                    }
                    else -> throw e
                }
            }
            return@withContext restaurantDao.getAll()
        }
    }


    fun toggleFavorite(id: Int, oldValue: Boolean) {

        viewModelScope.launch(errorHandler) {
            val updatedRestaurants = toggleFavoriteRestaurant(id, oldValue)
            state.value = updatedRestaurants
        }

    }


    private suspend fun toggleFavoriteRestaurant(id: Int, oldValue: Boolean) =
        withContext(Dispatchers.IO) {
            restaurantDao.update(
                PartialRestaurant(id = id, isFavorite = !oldValue)
            )
            restaurantDao.getAll()
        }

    private suspend fun refreshCache() {
        val remoteRestaurants = restInterface.getRestaurants()
        val favoriteRestaurants = restaurantDao.getAllFavorited()

        restaurantDao.addAll(remoteRestaurants)
        restaurantDao.updateAll(
            favoriteRestaurants.map {
                PartialRestaurant(it.id, true)
            }
        )
    }


}