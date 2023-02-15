package com.example.restaurantsapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.UnknownHostException

class RestaurantsRepository {
    private var restInterface: RestaurantsApiService = Retrofit.Builder()
        .baseUrl(
            "https://restaurantsapp-cb673-default-rtdb.firebaseio.com/"
        ).addConverterFactory(
            GsonConverterFactory.create()
        ).build()
        .create(RestaurantsApiService::class.java)

    private var restaurantDao = RestaurantsDb.getDaoInstance(RestaurantsApplication.getAppContext())


    suspend fun getAllRestaurants(): List<Restaurant> {
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


    suspend fun toggleFavoriteRestaurant(id: Int, oldValue: Boolean) =
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