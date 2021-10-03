package pl.pilichm.guesscomposer

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val mComposersData: ArrayList<Composer> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadComposersData()
    }

    /**
     * Function downloading information about top 20 film music composers from imdb.com
     * */
    private fun downloadComposersData(){
        ComposerDataDownloaderAsyncTask().execute()
    }

    /**
     * Async task class for downloading data as text html of imdb.com page.
     * Downloads whole page html, and then extracts composer names, surnames and url to images.
     * */
    private inner class ComposerDataDownloaderAsyncTask: AsyncTask<Void, Void, Int>(){
        override fun doInBackground(vararg params: Void?): Int {
            try {
                val url = URL(DATA_URL)
                val connection = url.openConnection() as HttpURLConnection

                if (connection.responseCode!=HttpURLConnection.HTTP_OK){
                    Log.e("CONNECTION_ERR", "Connection error: ${connection.responseCode}")
                    return 1
                } else {
                    /**
                     * Download page and extract composers data.
                     * */
                    val page = Jsoup.connect(DATA_URL).get()
                    page.select("img").forEach {
                        if (it.attr("src").toString().contains("/images/M/")) {
                            mComposersData.add(Composer(
                                it.attr("alt").toString(),
                                it.attr("src").toString()
                            ))
                        }
                    }

                   Log.i("DOWNLOADER", "Downloaded ${mComposersData.size} composers.")
                }
            } catch (e: Exception){
                Log.e("EXCEPTION", e.message.toString())
                return 1
            }
            return 0
        }

        /***
         * Sets up first question with answers.
         */
        override fun onPostExecute(result: Int?) {
            DownloadImageAsyncTask().execute(mComposersData[0].imageUrl)
            val correctPosition = (1..4).random()
            for (position in 1..4){
                val currentView = layoutMain.findViewWithTag<TextView>(position.toString())
                    if (correctPosition==position){
                        currentView.text = mComposersData[0].name
                    } else {
                        val index = (0..mComposersData.size).random()
                        currentView.text = mComposersData[index].name
                    }
                }
            }
        }

    /**
     * Async task for downloading composer image.
     * Also sets 4 answers, minimum one correct, other 3 are random.
     * */
    private inner class DownloadImageAsyncTask: AsyncTask<String, Void, Drawable>(){
        override fun doInBackground(vararg urls: String?): Drawable {
            val url = URL(urls[0])
            try {
                val inputStream = url.openStream()
                val bufferedStream = BufferedInputStream(inputStream)
                val bitmap = BitmapFactory.decodeStream(bufferedStream)
                inputStream?.close()
                bufferedStream?.close()
                return BitmapDrawable(bitmap)
            } catch (e: java.lang.Exception) {
                Log.e("ERROR", "Error while downloading image - is null!")
            }

            return ShapeDrawable(OvalShape())
        }

        override fun onPostExecute(image: Drawable?) {
            if (image!=null){
                celebrityImageView.setImageDrawable(image)
            } else {
                Log.e("ERROR", "Error while setting image - is null!")
            }
        }
    }

    companion object {
        const val DATA_URL = "https://www.imdb.com/list/ls053620576/"
    }
}