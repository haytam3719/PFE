import com.example.project.prototype.PaymentApiClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private const val BASE_URL = "https://payment-gatewayapi.onrender.com"

    // Define a custom trust manager that trusts all certificates
    private val trustAllCertificates = arrayOf<X509TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {
        }

        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
            return emptyArray()
        }
    })

    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, trustAllCertificates, null)
    }

    // Create an OkHttpClient that trusts all certificates
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .sslSocketFactory(sslContext.socketFactory, trustAllCertificates[0])
        .hostnameVerifier { _, _ -> true }
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val paymentApiClient: PaymentApiClient by lazy {
        retrofit.create(PaymentApiClient::class.java)
    }
}
