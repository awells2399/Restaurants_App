package com.example.restaurantsapp.restaurants.data

import com.example.restaurantsapp.restaurants.data.di.IoDispatcher
import com.example.restaurantsapp.restaurants.data.local.LocalRestaurant
import com.example.restaurantsapp.restaurants.data.local.PartialLocalRestaurant
import com.example.restaurantsapp.restaurants.data.local.RestaurantsDao
import com.example.restaurantsapp.restaurants.data.remote.RestaurantsApiService
import com.example.restaurantsapp.restaurants.domain.Restaurant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantsRepository @Inject constructor(
    private val restInterface: RestaurantsApiService,
    private val restaurantDao: RestaurantsDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    suspend fun loadRestaurants() {
        return withContext(dispatcher) {
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
        }
    }

    suspend fun getRestaurants(): List<Restaurant> {
        return withContext(dispatcher) {
            return@withContext restaurantDao.getAll().map {
                Restaurant(it.id, it.title, it.description, it.isFavorite)
            }
        }
    }


    suspend fun toggleFavoriteRestaurant(id: Int, value: Boolean) =
        withContext(dispatcher) {
            restaurantDao.update(
                PartialLocalRestaurant(id = id, isFavorite = value)
            )

        }

    private suspend fun refreshCache() {
        val remoteRestaurants = restInterface.getRestaurants()
        val favoriteRestaurants = restaurantDao.getAllFavorited()

        restaurantDao.addAll(remoteRestaurants.map {
            LocalRestaurant(
                it.id,
                it.title,
                it.description,
                false
            )
        })
        restaurantDao.updateAll(
            favoriteRestaurants.map {
                PartialLocalRestaurant(it.id, true)
            }
        )
    }
}