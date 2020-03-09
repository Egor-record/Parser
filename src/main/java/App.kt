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


    val chrome : String = "/Library/Chrome/chromedriver" // Path to Chrome Browser
    val siteURL : String = "https://1xstavka.ru/live/Football/"  // URL of the site we want to parse
    val apiURL : String = "http://localhost:8080/api/v1/games/update" // URL of Server API
    val isHeadless : Boolean = true // Use headless mode - no UI for browser

    /**
     * Method to open browser ? headless regime
     */
    fun openBrowser(isHeadLess : Boolean) : WebDriver {
        System.setProperty("webdriver.chrome.driver", chrome)
        return ChromeDriver(ChromeOptions().setHeadless(isHeadLess))
    }

    /**
     * Open web page; Siteurl - url to webpage
     */
    fun getUrl(browser : WebDriver) {
        return browser.get(siteURL)
    }

    /**
     * Array List of Games convert to JSON
     */
    fun convertToJson(games: ArrayList<Game>) : String? {
        return Gson().toJson(games)
    }

    /**
     * Method to send json - string to Back-end API
     */
    fun sendToAPI(games : String?) {
        /*
            Each HTTP Post Request in new thread
         */
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

    /**
     * Method which parse webpage
     */
    fun parse() {
        val browser = openBrowser(isHeadless)

        getUrl(browser)

        /**
         * Each 10 seconds parse the page. The page changes its value using AJAX, so we don't need to GET it each time
         */
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

             val games : ArrayList<Game> = ArrayList()

             try {
                /*
                    Find elements by class name
                 */
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
                /**
                 * Send JSON to API
                 */
                sendToAPI(convertToJson(games))

            }
        }, 0, 10000)

    }



    parse()
}