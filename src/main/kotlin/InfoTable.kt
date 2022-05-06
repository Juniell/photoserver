import org.jetbrains.exposed.sql.Table

object Info: Table() {
    val textWelcome = varchar("text_welcome", 500)
    val printerName = varchar("printer_name", 50)
    val dirInput = varchar("dir_input", 200)
    val dirOutput = varchar("dir_output", 200)
    val dirStickers = varchar("dir_stickers", 200)
}

data class InfoSettings(
    val textWelcome: String?,
    val printerName: String?,
    val dirInput: String?,
    val dirOutput: String?,
    val dirStickers: String?
)