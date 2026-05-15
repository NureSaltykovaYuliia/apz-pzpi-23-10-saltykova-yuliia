package com.example.mydogspace.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    private const val DEFAULT_URL = "https://yuliiasaltykova-mydogspace.hf.space"

    val BASE_URL: String get() = SessionManager.serverUrl ?: DEFAULT_URL

    fun getImageUrl(relativeUrl: String?): String? {
        if (relativeUrl.isNullOrBlank()) return null
        if (relativeUrl.startsWith("http")) return relativeUrl
        return BASE_URL + relativeUrl.trimStart('/')
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = SessionManager.token

        val requestBuilder = originalRequest.newBuilder()
            .header("Bypass-Tunnel-Reminder", "true")
            .header("Connection", "close")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")

        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        try {
            val response = chain.proceed(request)

            // Якщо сервер повернув 401 (Unauthorized), очищуємо токен
            if (response.code == 401) {
                SessionManager.token = null
            }

            response
        } catch (e: Exception) {
            // Перехоплюємо всі помилки (включаючи IllegalStateException: state 0 від зламаного сокета) 
            // і перетворюємо на IOException, щоб OkHttp не вбивав фоновий потік додатку.
            if (e is java.io.IOException) {
                throw e
            } else {
                throw java.io.IOException("OkHttp exception intercepted: ${e.message}", e)
            }
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .retryOnConnectionFailure(false) // Fix for IllegalStateException: state: 0 crash
        .build()

    // Зберігаємо Retrofit окремо, щоб перестворювати при зміні URL
    @Volatile private var _retrofit: Retrofit? = null
    @Volatile private var _lastUrl: String? = null

    private fun getRetrofit(): Retrofit {
        val currentUrl = BASE_URL
        if (_retrofit == null || _lastUrl != currentUrl) {
            synchronized(this) {
                if (_retrofit == null || _lastUrl != currentUrl) {
                    _retrofit = Retrofit.Builder()
                        .baseUrl(currentUrl)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .client(client)
                        .build()
                    _lastUrl = currentUrl
                    _apiService = _retrofit!!.create(ApiService::class.java)
                }
            }
        }
        return _retrofit!!
    }

    @Volatile private var _apiService: ApiService? = null

    val apiService: ApiService
        get() {
            getRetrofit()
            return _apiService!!
        }

    /** Викликати після зміни URL у SessionManager */
    fun resetClient() {
        synchronized(this) {
            _retrofit = null
            _apiService = null
            _lastUrl = null
        }
    }
}

object SessionManager {
    private const val PREFS_NAME = "MyDogSpacePrefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_SERVER_URL = "server_url"

    private var prefs: android.content.SharedPreferences? = null

    // Стан для Compose (щоб UI бачив зміни токена)
    val tokenState = androidx.compose.runtime.mutableStateOf<String?>(null)

    fun init(context: android.content.Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        tokenState.value = prefs?.getString(KEY_TOKEN, null)
    }

    var token: String?
        get() = tokenState.value
        set(value) {
            tokenState.value = value
            prefs?.edit()?.putString(KEY_TOKEN, value)?.apply()
        }

    var userId: Int?
        get() = if (prefs?.contains(KEY_USER_ID) == true) prefs?.getInt(KEY_USER_ID, -1) else null
        set(value) {
            if (value != null) {
                prefs?.edit()?.putInt(KEY_USER_ID, value)?.apply()
            } else {
                prefs?.edit()?.remove(KEY_USER_ID)?.apply()
            }
        }

    var serverUrl: String?
        get() = prefs?.getString(KEY_SERVER_URL, null)
        set(value) {
            if (value != null) {
                prefs?.edit()?.putString(KEY_SERVER_URL, value)?.apply()
            } else {
                prefs?.edit()?.remove(KEY_SERVER_URL)?.apply()
            }
        }
}

