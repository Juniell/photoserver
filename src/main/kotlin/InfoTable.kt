import org.jetbrains.exposed.sql.Table

object Info: Table() {
    val textWelcome = varchar("text_welcome", 500)
    val dirInput = varchar("dir_input", 200)
    val dirOutput = varchar("dir_output", 200)
    val dirStickers = varchar("dir_stickers", 200)

    val botServerAddress = varchar("bot_server_address", 200)
    val vkGroupId = integer("vk_group_id")
    val telegramBotName = varchar("telegram_bot_name", 50)
    val photoLifeTime = integer("photo_life_time")

    val printerName = varchar("printer_name", 50)
    val paperSize = varchar("paper_size", 50)
    val photoFramePath = varchar("photo_frame_path", 50).nullable()
    val photoCopiesNum = integer("photo_copies_num")

}

data class InfoSettings(
    val textWelcome: String?,
    val dirInput: String?,
    val dirOutput: String?,
    val dirStickers: String?,

    val botServerAddress: String?,
    val vkGroupId: Int?,
    val telegramBotName: String?,
    val photoLifeTime: Int?,

    val printerName: String?,
    val paperSize: String?,
    val photoFramePath: String?,
    val photoCopiesNum: Int?
)