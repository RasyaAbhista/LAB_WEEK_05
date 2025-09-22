package com.example.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.Button
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

    // Button reference
    private val nextButton: Button by lazy {
        findViewById(R.id.btn_next)
    }

    // GlideLoader instance
    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Panggil API saat pertama kali
        getCatImageResponse()

        // Refresh gambar kucing kalau button ditekan
        nextButton.setOnClickListener {
            getCatImageResponse()
        }
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
                    val firstImage = image?.firstOrNull()

                    // Ambil URL gambar
                    val imageUrl = firstImage?.imageUrl.orEmpty()

                    // Ambil nama breed, kalau kosong → Unknown
                    val breedName = if (!firstImage?.breeds.isNullOrEmpty()) {
                        firstImage?.breeds?.firstOrNull()?.name ?: "Unknown"
                    } else {
                        "Unknown"
                    }

                    // Tampilkan breed name ke TextView
                    apiResponseView.text =
                        getString(R.string.image_placeholder, breedName)

                    // Tampilkan gambar kucing ke ImageView pakai Glide
                    if (imageUrl.isNotBlank()) {
                        imageLoader.loadImage(imageUrl, imageResultView)
                    }
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
