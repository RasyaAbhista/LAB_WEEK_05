package com.example.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_week_05.api.CatApiService
import com.example.lab_week_05.model.ImageData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    // Retrofit instance pakai Moshi
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    // CatApiService instance
    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    // TextView reference
    private val apiResponseView: TextView by lazy {
        findViewById(R.id.api_response)
    }

    // ImageView reference
    private val imageResultView: ImageView by lazy {
        findViewById(R.id.image_result)
    }

    // GlideLoader instance
    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Panggil API
        getCatImageResponse()
    }

    private fun getCatImageResponse() {
        val call = catApiService.searchImages(1, "full")
        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "Failed to get response", t)
            }

            override fun onResponse(
                call: Call<List<ImageData>>,
                response: Response<List<ImageData>>
            ) {
                if (response.isSuccessful) {
                    val image = response.body()
                    val firstImage = image?.firstOrNull()?.imageUrl.orEmpty()

                    if (firstImage.isNotBlank()) {
                        // Load image ke ImageView pakai Glide
                        imageLoader.loadImage(firstImage, imageResultView)
                    } else {
                        Log.d(MAIN_ACTIVITY, "Missing image URL")
                    }

                    // Tampilkan URL juga ke TextView
                    apiResponseView.text =
                        getString(R.string.image_placeholder, firstImage)
                } else {
                    Log.e(
                        MAIN_ACTIVITY,
                        "Failed to get response\n" + response.errorBody()?.string().orEmpty()
                    )
                }
            }
        })
    }

    companion object {
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
    }
}
