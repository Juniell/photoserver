import io.ktor.http.*
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.io.FileFilter


private const val DIR_UNSENT = "Unsent"

object BotClient {
    private lateinit var serverUrl: String
    private lateinit var unsentDirPath: String

    private lateinit var retrofit: Retrofit
    private lateinit var api: BotService

    private var sendUnsentPhotos = false
    private var unsentPhotos = listOf<File>()

    fun initApi(serverUrl: String): BotService {
        this.serverUrl = serverUrl

        retrofit = Retrofit.Builder()
            .baseUrl(this.serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(BotService::class.java)

        return api
    }

    fun initOutputDir(outputDir: String) {
        unsentDirPath = outputDir + File.separator + DIR_UNSENT
        val unsentDir = File(unsentDirPath)
        if (!unsentDir.exists() || !unsentDir.isDirectory)
            unsentDir.mkdir()

        unsentPhotos = readUnsentDir()
    }

    fun checkUrlInit() = this::serverUrl.isInitialized

    fun checkDirInit() = this::unsentDirPath.isInitialized

    fun getApi() = api

    fun sendPhoto(photo: File) {
        runBlocking {
            coroutineScope {
                launch {
                    sendPhoto(
                        photo = photo,
                        onResponse = { response ->
                            if (response.code() == HttpStatusCode.OK.value) {
                                if (!sendUnsentPhotos && unsentPhotos.isNotEmpty())
                                    sendUnsentFiles()
                            } else
                                copyFileToUnsentDir(photo)
                        }
                    )
                }
            }
        }
    }

    private fun sendPhoto(
        photo: File,
        onResponse: (response: Response<ResponseBody>) -> Unit = { },
        onFailure: () -> Unit = { },
    ) {
        val part = photoToMultipart(photo)
        val call = api.sendPhoto(Settings.botServerPhrase, part)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onResponse(response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                copyFileToUnsentDir(photo)
                onFailure()
            }
        })
    }

    private fun sendUnsentFiles() {
        if (sendUnsentPhotos)
            return

        sendUnsentPhotos = true
        unsentPhotos = readUnsentDir()

        if (unsentPhotos.isEmpty()) {
            sendUnsentPhotos = false
            return
        }

        val list = unsentPhotos.toTypedArray().toMutableList()


        runBlocking {
            coroutineScope {
                for (file in unsentPhotos) {
                    launch {

                        val onFailure = {
                            unsentPhotos = readUnsentDir()
                            sendUnsentPhotos = false
                            this@coroutineScope.cancel()
                        }

                        sendPhoto(
                            photo = file,
                            onResponse = { response ->
                                if (response.code() == HttpStatusCode.OK.value) {
                                    file.delete()
                                    list.remove(file)

                                    // Если это был последний неотправленный файл, отмечаем, что закончили
                                    if (list.isEmpty()) {
                                        unsentPhotos = readUnsentDir()
                                        sendUnsentPhotos = false
                                    }
                                } else {
                                    onFailure()
                                }
                            },
                            onFailure = onFailure
                        )
                    }
                }
            }
        }
    }

    private fun photoToMultipart(file: File) = MultipartBody.Part.createFormData(
        "pic",
        file.name,
        RequestBody.create(
            MediaType.parse("image/*"),
            file.readBytes()
        )
    )

    private fun readUnsentDir() =
        File(unsentDirPath).listFiles(FileFilter { it.extension == "jpg" })?.toList() ?: emptyList()

    private fun copyFileToUnsentDir(file: File) {
        val newPath = unsentDirPath + File.separator + file.name
        file.copyTo(File(newPath))
        unsentPhotos = readUnsentDir()
    }
}


interface BotService {
    @Multipart
    @POST("photo")
    fun sendPhoto(
        @Query("phrase") phrase: String,
        @Part photo: MultipartBody.Part,
    ): Call<ResponseBody>

    @POST("check")
    fun checkConnect(
        @Query("phrase") phrase: String,
    ): Call<ResponseBody>

    @GET("settings")
    fun getSettings(
        @Query("phrase") phrase: String,
    ): Call<BotSettings>
}

data class BotSettings(
    val vkId: Int,
    val tgmId: String,
    val photoLife: Int
)