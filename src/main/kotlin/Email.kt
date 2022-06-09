import java.io.File
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


object Email {
    fun sendEmail(
        to: String,
        photo: File,
        text: String = "Ваше фото",
        label: String = "Это ваше фото",
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        val from = Settings.emailAddress

        val host = "smtp.gmail.com"

        val properties = System.getProperties()
        properties["mail.smtp.ssl.protocols"] = "TLSv1.2"
        properties["mail.smtp.auth"] = true
        properties["mail.smtp.starttls.enable"] = "true"
        properties["mail.smtp.host"] = host
        properties["mail.smtp.port"] = "587"

        val session = Session.getDefaultInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(Settings.emailAddress, Settings.emailPassword)
            }
        })
//        session.debug = true
        try {
            val message = MimeMessage(session)

            message.setFrom(InternetAddress(from))

            message.addRecipient(Message.RecipientType.TO, InternetAddress(to))
            message.subject = label

            val bodyText = MimeBodyPart()
            bodyText.setText(text)

            val bodyFile = MimeBodyPart()
            bodyFile.attachFile(photo)

            val multipart = MimeMultipart()
            multipart.addBodyPart(bodyText)
            multipart.addBodyPart(bodyFile)

            message.setContent(multipart)

            Transport.send(message)

            onSuccess()
        } catch (msgEx: MessagingException) {
            msgEx.printStackTrace()
            onFail()
        }
    }
}