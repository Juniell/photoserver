import Info.bot_need
import Info.bot_server_address
import Info.dir_input
import Info.dir_output
import Info.dir_stickers
import Info.email_address
import Info.email_need
import Info.email_password
import Info.paper_size
import Info.photo_copies_num
import Info.photo_frame_need
import Info.photo_frame_path
import Info.photo_life_time
import Info.print_need
import Info.printer_name
import Info.telegram_bot_name
import Info.text_welcome
import Info.vk_group_id
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import javax.print.PrintService
import javax.print.attribute.standard.MediaSizeName

object Settings {
    var textWelcome: MutableState<String> = mutableStateOf("")
    var dirInput: MutableState<String> = mutableStateOf("")
    var dirOutput: MutableState<String> = mutableStateOf("")
    var dirStickers: MutableState<String> = mutableStateOf("")

    var emailNeed: MutableState<Boolean> = mutableStateOf(true)
    var emailAddress: MutableState<String> = mutableStateOf("")
    var emailPassword: MutableState<String> = mutableStateOf("")

    var botNeed: MutableState<Boolean> = mutableStateOf(true)
    var botServerAddress: MutableState<String> = mutableStateOf("")
    var vkGroupId: MutableState<Int> = mutableStateOf(-1)
    var telegramBotName: MutableState<String> = mutableStateOf("")
    var photoLifeTime: MutableState<Int> = mutableStateOf(-1)

    var printNeed: MutableState<Boolean> = mutableStateOf(true)
    var printerName: MutableState<String> = mutableStateOf("")
    var paperSize: MutableState<String> = mutableStateOf("")
    var frameNeed: MutableState<Boolean> = mutableStateOf(true)
    var photoFramePath: MutableState<String> = mutableStateOf("")
    var photoCopiesNum: MutableState<Int> = mutableStateOf(1)

    lateinit var selectedPhoto: File
    var printer: PrintService? = null
    var paper: MediaSizeName? = null
    var vkGroupChange = false


    /**
     * Возвращает true, если удалось считать настройки из БД. Иначе возвращает false.
     */
    fun readDatabase(): Boolean {
        Database.connect("jdbc:sqlite:info.db", driver = "org.sqlite.JDBC")

        transaction {
            SchemaUtils.create(Info) // if not exist
            commit()
        }

        try {
            transaction {
                val row = Info.selectAll().single()

                textWelcome.value = row[text_welcome]
                dirInput.value = row[dir_input]
                dirOutput.value = row[dir_output]
                dirStickers.value = row[dir_stickers]

                emailNeed.value = row[email_need]
                emailAddress.value = row[email_address]
                emailPassword.value = row[email_password]

                botNeed.value = row[bot_need]
                botServerAddress.value = row[bot_server_address]
                vkGroupId.value = row[vk_group_id]
                telegramBotName.value = row[telegram_bot_name]
                photoLifeTime.value = row[photo_life_time]

                printNeed.value = row[print_need]
                printerName.value = row[printer_name]
                paperSize.value = row[paper_size]
                frameNeed.value = row[photo_frame_need]
                photoFramePath.value = row[photo_frame_path]
                photoCopiesNum.value = row[photo_copies_num]
            }
            return true
        } catch (e: NoSuchElementException) {
            return false
        }
    }

    fun writeDatabase(/*info: InfoSettings,*/ needInsert: Boolean) {
//        saveNewSettings(info)

        transaction {
            if (needInsert)
                Info.insert {
                    it[text_welcome] = textWelcome.value
                    it[dir_input] = dirInput.value
                    it[dir_output] = dirOutput.value
                    it[dir_stickers] = dirStickers.value

                    it[email_need] = emailNeed.value
                    it[email_address] = emailAddress.value
                    it[email_password] = emailPassword.value

                    it[bot_need] = botNeed.value
                    it[bot_server_address] = botServerAddress.value
                    it[vk_group_id] = vkGroupId.value
                    it[telegram_bot_name] = telegramBotName.value
                    it[photo_life_time] = photoLifeTime.value

                    it[print_need] = printNeed.value
                    it[printer_name] = printerName.value
                    it[paper_size] = paperSize.value
                    it[photo_frame_need] = frameNeed.value
                    it[photo_frame_path] = photoFramePath.value
                    it[photo_copies_num] = photoCopiesNum.value
                }
            else
                Info.update {
                    it[text_welcome] = textWelcome.value
                    it[dir_input] = dirInput.value
                    it[dir_output] = dirOutput.value
                    it[dir_stickers] = dirStickers.value

                    it[email_need] = emailNeed.value
                    it[email_address] = emailAddress.value
                    it[email_password] = emailPassword.value

                    it[bot_need] = botNeed.value
                    it[bot_server_address] = botServerAddress.value
                    it[vk_group_id] = vkGroupId.value
                    it[telegram_bot_name] = telegramBotName.value
                    it[photo_life_time] = photoLifeTime.value

                    it[print_need] = printNeed.value
                    it[printer_name] = printerName.value
                    it[paper_size] = paperSize.value
                    it[photo_frame_need] = frameNeed.value
                    it[photo_frame_path] = photoFramePath.value
                    it[photo_copies_num] = photoCopiesNum.value
                }
            commit()
        }
    }
}