import org.jetbrains.exposed.sql.Table

object Info: Table() {
    val text_welcome = varchar("text_welcome", 500)
    val dir_input = varchar("dir_input", 200)
    val dir_output = varchar("dir_output", 200)
    val dir_stickers = varchar("dir_stickers", 200)

    val email_need = bool("email_need")
    val email_address = varchar("email_address", 200)
    val email_password = varchar("email_password", 200)

    val bot_need = bool("bot_need")
    val bot_server_address = varchar("bot_server_address", 200)
    val vk_group_id = integer("vk_group_id")
    val telegram_bot_name = varchar("telegram_bot_name", 50)
    val photo_life_time = integer("photo_life_time")

    val print_need = bool("print_need")
    val printer_name = varchar("printer_name", 50)
    val paper_size = varchar("paper_size", 50)
    val photo_frame_need = bool("photo_frame_need")
    val photo_frame_path = varchar("photo_frame_path", 50)
    val photo_copies_num = integer("photo_copies_num")

}

//data class InfoSettings(
//    val textWelcome: String?,
//    val dirInput: String?,
//    val dirOutput: String?,
//    val dirStickers: String?,
//
//    val emailNeed: Boolean?,
//    val emailAddress: String?,
//    val emailPassword: String?,
//
//    val botNeed: Boolean?,
//    val botServerAddress: String?,
//    val vkGroupId: Int?,
//    val telegramBotName: String?,
//    val photoLifeTime: Int?,
//
//    val printNeed: Boolean?,
//    val printerName: String?,
//    val paperSize: String?,
//    val frameNeed: Boolean?,
//    val photoFramePath: String?,
//    val photoCopiesNum: Int?
//)