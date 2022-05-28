import io.ktor.http.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File


// todo: получать ссылки на чаты в социальных сетях от сервера
class BotServer {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://bot.fbear.ru/photo_back/photoserver/")    //todo: Вынести в настройки
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: BotService = retrofit.create(BotService::class.java)


    // todo: держать где-то фотки, которые не удалось отправить
    fun sendPhoto(photo: File): Boolean {

//        val requestBody = RequestBody.create(MediaType.parse("*/*"), photo);
//        val fileToUpload = MultipartBody.Part.createFormData("file", photo.name, requestBody)
////        val filename = RequestBody.create(MediaType.parse("text/plain"), photo.getName());
//
//        val call = api.sendPhoto(fileToUpload)
//        call.execute()

        val part = MultipartBody.Part.createFormData(
            "pic",
            photo.name,
            RequestBody.create(
                MediaType.parse("image/*"),
                photo.readBytes()
            )
        )
        // todo: при успешном восстановлении соединения повторно отправлять фотки, которые не отправились

        val call = api.sendPhoto(part)
        val result = call.execute() //failed here when timed out    //todo

        return result.code() == HttpStatusCode.OK.value
    }
}

private interface BotService {
    @Multipart
    @POST("photo")
    fun sendPhoto(
        @Part photo: MultipartBody.Part,
    ): Call<ResponseBody>
}

