package com.example.httpmethodshttpurlconnectionexample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.httpmethodshttpurlconnectionexample.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.postButton.setOnClickListener { postMethod() }

        binding.getButton.setOnClickListener { getMethod() }

        binding.putButton.setOnClickListener { putMethod() }

        binding.deleteButton.setOnClickListener { deleteMethod() }

    }

    private fun postMethod() {

        // Uncomment the one you want to test, and comment the others

        rawJSON()

        // urlEncoded()

        // formData()

    }

    private fun rawJSON() {

        // Create JSON using JSONObject
        val jsonObject = JSONObject()
        jsonObject.put("name", "Jack")
        jsonObject.put("salary", "3540")
        jsonObject.put("age", "23")

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("http://dummy.restapiexample.com/api/v1/create")
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.setRequestProperty("Content-Type", "application/json") // The format of the content we're sending to the server
            httpURLConnection.setRequestProperty("Accept", "application/json") // The format of response we want to get from the server
            httpURLConnection.doInput = true
            httpURLConnection.doOutput = true

            // Send the JSON we created
            val outputStreamWriter = OutputStreamWriter(httpURLConnection.outputStream)
            outputStreamWriter.write(jsonObjectString)
            outputStreamWriter.flush()

            // Check if the connection is successful
            val responseCode = httpURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = httpURLConnection.inputStream.bufferedReader()
                        .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response))
                    Log.d("Pretty Printed JSON :", prettyJson)

                    // Open DetailsActivity with the results
                    val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                    intent.putExtra("json_results", prettyJson)
                    this@MainActivity.startActivity(intent)
                }
            } else {
                Log.e("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }
        }
    }

    private fun urlEncoded() {

        // Add URL parameters
        val uriBuilder = Uri.Builder()
                .appendQueryParameter("name", "Jack")
                .appendQueryParameter("salary", "8054")
                .appendQueryParameter("age", "45")
                .build()

        val params = uriBuilder.toString().replace("?", "")  // Remove the "?" from the beginning of the parameters ?name=Jack&salary=8054&age=45
        val postData = params.toByteArray(StandardCharsets.UTF_8)

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("https://postman-echo.com/post")
            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded") // The format of the content we're sending to the server
            httpsURLConnection.setRequestProperty("Accept", "application/json") // The format of response we want to get from the server
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true
            val dataOutputStream = DataOutputStream(httpsURLConnection.outputStream)
            dataOutputStream.write(postData)

            // Check if the connection is successful
            val responseCode = httpsURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = httpsURLConnection.inputStream.bufferedReader()
                        .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response))
                    Log.d("Pretty Printed JSON :", prettyJson)

                    // Open DetailsActivity with the results
                    val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                    intent.putExtra("json_results", prettyJson)
                    this@MainActivity.startActivity(intent)
                }
            } else {
                Log.e("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }
        }
    }

    private fun formData() {

        // List of all MIME Types you can upload: https://www.freeformatter.com/mime-types-list.html

        // Get file from assets folder
        val file = getFileFromAssets(this@MainActivity, "lorem_ipsum.txt")

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("https://httpbin.org/post")

            val boundary = "Boundary-${System.currentTimeMillis()}"

            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.addRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=$boundary"
            )
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            val outputStreamToRequestBody = httpsURLConnection.outputStream
            val httpRequestBodyWriter = BufferedWriter(OutputStreamWriter(outputStreamToRequestBody))

            // Add the email in the post data
            httpRequestBodyWriter.write("\n\n--$boundary\n")
            httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"email\"")
            httpRequestBodyWriter.write("\n\n")
            httpRequestBodyWriter.write("jack@email.com")


            // Add the part to describe the file
            httpRequestBodyWriter.write("\n--$boundary\n")
            httpRequestBodyWriter.write("Content-Disposition: form-data;"
                    + "name=\"upload_file.txt\";"
                    + "filename=\"" + file.name + "\""
                    + "\nContent-Type: text/plain\n\n")
            httpRequestBodyWriter.flush()

            // Write the file
            val inputStreamToFile = FileInputStream(file)
            var bytesRead: Int
            val dataBuffer = ByteArray(1024)
            while (inputStreamToFile.read(dataBuffer).also { bytesRead = it } != -1) {
                outputStreamToRequestBody.write(dataBuffer, 0, bytesRead)
            }
            outputStreamToRequestBody.flush()

            // End of the multipart request
            httpRequestBodyWriter.write("\n--$boundary--\n")
            httpRequestBodyWriter.flush()

            // Close the streams
            outputStreamToRequestBody.close()
            httpRequestBodyWriter.close()

            // Check if the connection is successful
            val responseCode = httpsURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = httpsURLConnection.inputStream.bufferedReader()
                        .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response))
                    Log.d("Pretty Printed JSON :", prettyJson)

                    // Open DetailsActivity with the results
                    val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                    intent.putExtra("json_results", prettyJson)
                    this@MainActivity.startActivity(intent)
                }
            } else {
                Log.e("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }
        }
    }

    private fun getMethod() {
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("http://dummy.restapiexample.com/api/v1/employees")
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.setRequestProperty("Accept", "application/json") // The format of response we want to get from the server
            httpURLConnection.requestMethod = "GET"
            httpURLConnection.doInput = true
            httpURLConnection.doOutput = false

            // Check if the connection is successful
            val responseCode = httpURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = httpURLConnection.inputStream.bufferedReader()
                        .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response))
                    Log.d("Pretty Printed JSON :", prettyJson)

                    // Open DetailsActivity with the results
                    val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                    intent.putExtra("json_results", prettyJson)
                    this@MainActivity.startActivity(intent)

                }
            } else {
                Log.e("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }
        }
    }

    private fun putMethod() {
        // Create JSON using JSONObject
        val jsonObject = JSONObject()
        jsonObject.put("name", "John")
        jsonObject.put("job", "Android Developer")

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("https://reqres.in/api/users/2")
            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.requestMethod = "PUT"
            httpsURLConnection.setRequestProperty("Content-Type", "application/json") // The format of the content we're sending to the server
            httpsURLConnection.setRequestProperty("Accept", "application/json") // The format of response we want to get from the server
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false
            val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
            outputStreamWriter.write(jsonObjectString)
            outputStreamWriter.flush()

            // Check if the connection is successful
            val responseCode = httpsURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = httpsURLConnection.inputStream.bufferedReader()
                        .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response))
                    Log.d("Pretty Printed JSON :", prettyJson)

                    // Open DetailsActivity with the results
                    val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                    intent.putExtra("json_results", prettyJson)
                    this@MainActivity.startActivity(intent)

                }
            } else {
                Log.e("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }
        }
    }

    private fun deleteMethod() {
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL("https://my-json-server.typicode.com/typicode/demo/posts/1")
            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.requestMethod = "DELETE"
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false

            // Check if the connection is successful
            val responseCode = httpsURLConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = httpsURLConnection.inputStream.bufferedReader()
                        .use { it.readText() }  // defaults to UTF-8
                withContext(Dispatchers.Main) {

                    // Convert raw JSON to pretty JSON using GSON library
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response))
                    Log.d("Pretty Printed JSON :", prettyJson)

                    // Open DetailsActivity with the results
                    val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                    intent.putExtra("json_results", prettyJson)
                    this@MainActivity.startActivity(intent)

                }
            } else {
                Log.e("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }
        }
    }

    private fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName).also {
        if (!it.exists()) {
            it.outputStream().use { cache ->
                context.assets.open(fileName).use { inputStream ->
                    inputStream.copyTo(cache)
                }
            }
        }
    }
}