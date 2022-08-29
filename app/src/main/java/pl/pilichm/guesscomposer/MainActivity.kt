package pl.pilichm.guesscomposer

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.jsoup.Jsoup
import pl.pilichm.guesscomposer.databinding.ActivityMainBinding
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mComposersData: ArrayList<Composer> = ArrayList()
    private var nextQuestionIndex:Int = 0
    private var mCorrectCount: Int = 0
    private var mGuessedFirstTime: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showProgressView()
        downloadComposersData()
        setUpListeners()
    }

    /**
     * Adds listeners to text views with answers.
     * */
    private fun setUpListeners(): Unit{
        for (position in (1..4)){
            val currentView = binding.layoutMain.findViewWithTag<TextView>(position.toString())
            currentView.setOnClickListener {
                if (nextQuestionIndex<=mComposersData.size){

                    if (nextQuestionIndex==mComposersData.size){
                        Handler(Looper.getMainLooper()).postDelayed(
                            {showEndInfo()}, NEXT_QUESTION_DELAYED
                        )
                    } else {
                        val correctIndex = nextQuestionIndex - 1
                        Log.e("COMP", "${currentView.text} == ${mComposersData[correctIndex].name}")
                        if (currentView.text==mComposersData[correctIndex].name){
                            currentView.setBackgroundColor(
                                ContextCompat.getColor(applicationContext, R.color.color_correct))

                            if (mGuessedFirstTime) {
                                mCorrectCount++
                            }

                            mGuessedFirstTime = true

                            Toast.makeText(
                                applicationContext,
                                resources.getString(R.string.answer_correct),
                                Toast.LENGTH_SHORT).show()

                            Handler(Looper.getMainLooper()).postDelayed({
                                showProgressView()
                                setUpNextQuestion()
                                showQuestionViews()
                            }, NEXT_QUESTION_DELAYED)
                        } else {
                            mGuessedFirstTime = false

                            Toast.makeText(
                                applicationContext,
                                resources.getString(R.string.answer_incorrect),
                                Toast.LENGTH_SHORT).show()
                            currentView.setBackgroundColor(
                                ContextCompat.getColor(applicationContext, R.color.color_incorrect))
                        }
                    }
                }
            }
        }

        /**
         * Adds listener to replay button.
         * */
        binding.imagePlayAgain.setOnClickListener {
            nextQuestionIndex = 0
            mCorrectCount = 0
            mComposersData.shuffle()
            showProgressView()
            setUpNextQuestion()
            showQuestionViews()
        }
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
            mComposersData.shuffle()
            setUpNextQuestion()
            }
        }

    /**
     * Downloads image for next composer, and sets up question and random answers.
     * */
    private fun setUpNextQuestion(){
        clearAnswersBackground()
        DownloadImageAsyncTask().execute(mComposersData[nextQuestionIndex].imageUrl)
        val correctPosition = (1..4).random()
        for (index in 1..4){
            val currentView = binding.layoutMain.findViewWithTag<TextView>(index.toString())
            if (correctPosition==index){
                currentView.text = mComposersData[nextQuestionIndex].name
            } else {
                val randomIndex = (0 until mComposersData.size).random()
                currentView.text = mComposersData[randomIndex].name
            }
        }

        nextQuestionIndex++
        showQuestionViews()
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
                binding.celebrityImageView.setImageDrawable(image)
            } else {
                Log.e("ERROR", "Error while setting image - is null!")
            }
        }
    }

    /**
     * Shows views displaying composers image, question and answers, hides progress bar.
     * */
    private fun showQuestionViews(){
        binding.progressBar.visibility = View.INVISIBLE
        binding.tvEnd.visibility = View.INVISIBLE
        binding.layoutMain.visibility = View.VISIBLE
        binding.imagePlayAgain.visibility = View.INVISIBLE
    }

    /**
     * Hides views displaying composers image, question and answers, shows progress bar.
     * */
    private fun showProgressView(){
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutMain.visibility = View.INVISIBLE
        binding.imagePlayAgain.visibility = View.INVISIBLE
        binding.tvEnd.visibility = View.INVISIBLE
    }

    /**
     * Displays information that app has ended. Hides progress, question and answers views.
     * */
    private fun showEndInfo(){
        binding.progressBar.visibility = View.INVISIBLE
        binding.layoutMain.visibility = View.INVISIBLE
        binding.imagePlayAgain.visibility = View.VISIBLE
        binding.tvEnd.visibility = View.VISIBLE
        binding.tvEnd.text = "Your guessed first time:\n $mCorrectCount times! \n Tap below to play again."
    }

    /**
     * Clears background color of all answer views.
     * */
    private fun clearAnswersBackground(){
        for (position in (1..4)) {
            val currentView = binding.layoutMain.findViewWithTag<TextView>(position.toString())
            currentView.setBackgroundColor(
                ContextCompat.getColor(applicationContext, R.color.answer_button_background_color))
        }
    }

    companion object {
        const val DATA_URL = "https://www.imdb.com/list/ls053620576/"
        const val NEXT_QUESTION_DELAYED = 333L
    }
}