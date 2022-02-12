package fr.isen.cesari.androiderestaurant


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import fr.isen.cesari.androiderestaurant.databinding.ActivityCommandBinding
import fr.isen.cesari.androiderestaurant.model.DishBasket
import fr.isen.cesari.androiderestaurant.model.ListBasket
import org.json.JSONObject
import java.io.File

class CommandActivity : MenuActivity() {
    private lateinit var binding : ActivityCommandBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCommandBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userid = this.getSharedPreferences(getString(R.string.spFileName), Context.MODE_PRIVATE).getInt(getString(R.string.spUserId), 0)

        if(userid == 0) {
            Toast.makeText(this, getString(R.string.warningUserLogout), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            newCommandRequest(userid)
        }
    }

    private fun newCommandRequest(userId : Int) {
        val url = "http://test.api.catering.bluecodegames.com/user/order"

        val params = HashMap<String, Any>()
        params["id_shop"] = 1
        params["id_user"] = userId
        params["msg"] = recupBasketFile().toString()
        val jsonObject = JSONObject(params as Map<*, *>)

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            {
                Toast.makeText(this, getString(R.string.successCommand), Toast.LENGTH_SHORT).show()
                binding.commandText.text = getString(R.string.successCommandTxt)
                deleteBasketData()
            }, {
                Log.e("API", it.toString())
                Toast.makeText(this, getString(R.string.APIfailure), Toast.LENGTH_SHORT).show()
                binding.commandText.text = getString(R.string.failureCommandTxt)
            }
        )

        request.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 1f)

        Volley.newRequestQueue(this).add(request)
    }

    private fun recupBasketFile() : List<DishBasket> {
        val file = File(cacheDir.absolutePath + "/basket.json")
        var dishesBasket: List<DishBasket> = ArrayList()

        if (file.exists()) {
            dishesBasket = Gson().fromJson(file.readText(), ListBasket::class.java).data
        }

        return dishesBasket
    }

    private fun deleteBasketData() {
        File(cacheDir.absolutePath + "/basket.json").delete()
        this.getSharedPreferences(getString(R.string.spFileName), Context.MODE_PRIVATE).edit().remove(getString(R.string.spTotalPrice)).apply()
        this.getSharedPreferences(getString(R.string.spFileName), Context.MODE_PRIVATE).edit().remove(getString(R.string.spTotalQuantity)).apply()
    }
}