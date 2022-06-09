import kotlinx.coroutines.*
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.*
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission
import org.apache.ftpserver.usermanager.impl.TransferRatePermission
import org.apache.ftpserver.usermanager.impl.WritePermission
import java.io.File


private const val PORT = 2121

object FtpServer {
    private val workDirPath = File("").absolutePath + File.separator + "ftp"
    private val userFile = File(workDirPath + File.separator + "users.properties")
    private val serverFactory = FtpServerFactory()
    private val factory = ListenerFactory()
    private val userManagerFactory = PropertiesUserManagerFactory()
    private lateinit var server: FtpServer
    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    init {
        initNewServer()
    }

    private fun initNewServer() {
        val workDir = File(workDirPath)
        if (!workDir.exists() || !workDir.isDirectory)
            workDir.mkdir()

        if (userFile.exists())      // Очищаем список пользователей
            userFile.delete()
        userFile.createNewFile()

        factory.port = PORT
        serverFactory.addListener("default", factory.createListener())

        userManagerFactory.file = userFile
        userManagerFactory.passwordEncryptor = SaltedPasswordEncryptor()

        val ftpLets: MutableMap<String?, Ftplet?> = HashMap()
        ftpLets["ftpService"] = object : DefaultFtplet() {
            override fun onUploadEnd(session: FtpSession?, request: FtpRequest?): FtpletResult {
                coroutineScope.launch {
                    val file = session?.fileSystemView?.getFile(request?.argument)
                    if (file != null && file.doesExist() && file.isFile && isImageFile(file.name)) {
                        val absolutePath = session.user.homeDirectory + file.absolutePath
                        val imageFile = File(absolutePath)
                        // Создаём мини версию и после этого перемещаем в рабочую директорию
                        createMini(imageFile)
                        imageFile.copyTo(File(Settings.dirInput))
                        imageFile.delete()
                    }
                }
                return FtpletResult.DEFAULT
            }
        }
        serverFactory.ftplets = ftpLets
        server = serverFactory.createServer()
    }

    fun start() {
        if (isWorks())
            return

        deleteAllUser()
        addUser(Settings.ftpUserLogin, Settings.ftpUserPassword)

        if (server.isSuspended)
            server.resume()
        else
            server.start()
    }

    fun stop() = server.suspend()

    fun isWorks() = !server.isStopped && !server.isSuspended

    fun getPort() = PORT

    private fun deleteAllUser() {
        val userManager = userManagerFactory.createUserManager()
        val usernameList = userManager.allUserNames
        usernameList.forEach {
            userManagerFactory.createUserManager().delete(it)
        }
        serverFactory.userManager = userManager
    }

    private fun addUser(name: String, password: String): String? {
        val user = BaseUser()
        user.enabled = true
        user.name = name
        user.password = password

        val userHomeDir = File(workDirPath + File.separator + "photos")
        if (!userHomeDir.exists() || !userHomeDir.isDirectory)
            userHomeDir.mkdir()

        user.homeDirectory = userHomeDir.absolutePath

        val authorities: MutableList<Authority> = ArrayList()
        authorities.add(WritePermission())
        authorities.add(ConcurrentLoginPermission(0, 0))    // без ограничений на авторизации
        authorities.add(TransferRatePermission(0, 0))   // без ограничений на скорость

        user.authorities = authorities
        val um = userManagerFactory.createUserManager()

        val userName = try {
            um.save(user)
            user.name
        } catch (e1: FtpException) {
            e1.printStackTrace()
            null
        }
        serverFactory.userManager = um
        return userName
    }
}