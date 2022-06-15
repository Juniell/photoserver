import Info.bot_need
import Info.bot_server_address
import Info.cameras_need
import Info.dir_input
import Info.dir_output
import Info.dir_smileys
import Info.dir_stickers
import Info.email_address
import Info.email_need
import Info.email_password
import Info.mirrors_need
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fragments.settings.components.ConnectionState
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import javax.print.PrintService
import javax.print.attribute.standard.MediaSizeName

object Settings {
    var textWelcome by mutableStateOf("")
    var dirInput by mutableStateOf("")
    var dirOutput by mutableStateOf("")
    var dirStickers by mutableStateOf("")
    var dirSmileys by mutableStateOf("")

    var emailNeed by mutableStateOf(true)
    var emailAddress by mutableStateOf("")
    var emailPassword by mutableStateOf("")

    var botNeed by mutableStateOf(true)
    var botServerAddress by mutableStateOf("")
    var vkGroupId by mutableStateOf(-1)
    var telegramBotName by mutableStateOf("")
    var photoLifeTime by mutableStateOf(-1)

    var printNeed by mutableStateOf(true)
    var printerName by mutableStateOf("")
    var paperSize by mutableStateOf("")
    var frameNeed by mutableStateOf(true)
    var photoFramePath by mutableStateOf("")
    var photoCopiesNum by mutableStateOf(1)

    var mirrorNeed by mutableStateOf(true)
    var camerasNeed by mutableStateOf(true)

    lateinit var selectedPhoto: File
    lateinit var selectedPhotoMini: File
    var printer: PrintService? = null
    var paper: MediaSizeName? = null
    var botServerPhrase by mutableStateOf("")
    var ftpUserLogin by mutableStateOf("")
    var ftpUserPassword by mutableStateOf("")
    var oldFtpUserLogin = ""
    var oldFtpUserPassword = ""
    var oldVkGroupId = -1
    var botServerState by mutableStateOf(ConnectionState.UNKNOWN)


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

                textWelcome = row[text_welcome]
                dirInput = row[dir_input]
                dirOutput = row[dir_output]
                dirStickers = row[dir_stickers]
                dirSmileys = row[dir_smileys]

                emailNeed = row[email_need]
                emailAddress = row[email_address]
                emailPassword = row[email_password]

                botNeed = row[bot_need]
                botServerAddress = row[bot_server_address]
                vkGroupId = row[vk_group_id]
                telegramBotName = row[telegram_bot_name]
                photoLifeTime = row[photo_life_time]

                printNeed = row[print_need]
                printerName = row[printer_name]
                paperSize = row[paper_size]
                frameNeed = row[photo_frame_need]
                photoFramePath = row[photo_frame_path]
                photoCopiesNum = row[photo_copies_num]

                mirrorNeed = row[mirrors_need]
                camerasNeed = row[cameras_need]
            }
            return true
        } catch (e: NoSuchElementException) {
            return false
        }
    }

    fun writeDatabase(needInsert: Boolean) {
        transaction {
            if (needInsert)
                Info.insert {
                    it[text_welcome] = textWelcome
                    it[dir_input] = dirInput
                    it[dir_output] = dirOutput
                    it[dir_stickers] = dirStickers
                    it[dir_smileys] = dirSmileys

                    it[email_need] = emailNeed
                    it[email_address] = emailAddress
                    it[email_password] = emailPassword

                    it[bot_need] = botNeed
                    it[bot_server_address] = botServerAddress
                    it[vk_group_id] = vkGroupId
                    it[telegram_bot_name] = telegramBotName
                    it[photo_life_time] = photoLifeTime

                    it[print_need] = printNeed
                    it[printer_name] = printerName
                    it[paper_size] = paperSize
                    it[photo_frame_need] = frameNeed
                    it[photo_frame_path] = photoFramePath
                    it[photo_copies_num] = photoCopiesNum

                    it[mirrors_need] = mirrorNeed
                    it[cameras_need] = camerasNeed
                }
            else
                Info.update {
                    it[text_welcome] = textWelcome
                    it[dir_input] = dirInput
                    it[dir_output] = dirOutput
                    it[dir_stickers] = dirStickers
                    it[dir_smileys] = dirSmileys

                    it[email_need] = emailNeed
                    it[email_address] = emailAddress
                    it[email_password] = emailPassword

                    it[bot_need] = botNeed
                    it[bot_server_address] = botServerAddress
                    it[vk_group_id] = vkGroupId
                    it[telegram_bot_name] = telegramBotName
                    it[photo_life_time] = photoLifeTime

                    it[print_need] = printNeed
                    it[printer_name] = printerName
                    it[paper_size] = paperSize
                    it[photo_frame_need] = frameNeed
                    it[photo_frame_path] = photoFramePath
                    it[photo_copies_num] = photoCopiesNum

                    it[mirrors_need] = mirrorNeed
                    it[cameras_need] = camerasNeed
                }
            commit()
        }
    }
}