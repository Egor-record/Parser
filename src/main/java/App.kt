import com.google.gson.Gson
import okhttp3.*
import org.openqa.selenium.By
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList


fun main() {

    val chrome : String = "/Library/Chrome/chromedriver"
    val siteURL : String = "https://1xstavka.ru/live/Football/"
    val apiURL : String = "http://localhost:8080/api/v1/games/update"
    val isHeadless : Boolean = true

    fun openBrowser(isHeadLess : Boolean) : WebDriver {
        System.setProperty("webdriver.chrome.driver", chrome)
        return ChromeDriver(ChromeOptions().setHeadless(isHeadLess))
    }

    fun getUrl(browser : WebDriver) {
        return browser.get(siteURL)
    }

    fun convertToJson(games: ArrayList<Game>) : String? {
        return Gson().toJson(games)
    }

    fun sendToAPI(games : String?) {
        Thread(Runnable {

            println(games.toString())

            try {

                val client: OkHttpClient = OkHttpClient().newBuilder()
                    .build()
                val mediaType: MediaType? = MediaType.parse("application/json")
                val body =
                    RequestBody.create(mediaType, games.toString())

                val request: Request = Request.Builder()
                    .url(apiURL)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build()
                val response: Response = client.newCall(request).execute()

                 response.close()


            } catch (err : SocketTimeoutException) {
                println(err)
            } catch (err: ConnectException) {
                println(err)
            } finally {

            }


        }).start()

    }

    fun parse() {
        val browser = openBrowser(isHeadless)

        getUrl(browser)

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

             val games : ArrayList<Game> = ArrayList()

             try {

                 val elements = browser.findElements(
                     By.className("c-events-scoreboard")
                 )

                 for (element in elements) {

                     val teamDivs = element.findElements(
                         By.className("c-events__team")
                     )

                     val scoreDivs = element.findElements(
                         By.className("c-events-scoreboard__cell--all")
                     )

                     val game = Game(teamFirst =  teamDivs[0].text,
                                     teamSecond =  teamDivs[1].text,
                                     scoreFirstTeam = scoreDivs[0].text,
                                     scoreSecondTeam = scoreDivs[1].text,
                                     league = "FIFA",
                                    betName = "1xBet"


                         );

                     games.add(game)

                 }


             } catch (e : StaleElementReferenceException) {
                 kotlin.io.println("No Element !")
             }

                sendToAPI(convertToJson(games))

            }
        }, 0, 10000)

    }



    parse()
}