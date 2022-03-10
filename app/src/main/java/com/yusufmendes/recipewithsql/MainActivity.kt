package com.yusufmendes.recipewithsql

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.startActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.recycler_row.*
import kotlin.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val foodNameList = ArrayList<String>()
        val foodIdList = ArrayList<Int>()

        var arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,foodNameList)
        recyclerView.adapter = arrayAdapter

        //verileri alma islemi
        try {
            val database = this.openOrCreateDatabase("Foods", MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM foods",null)
            val foodNameIndex = cursor.getColumnIndex("foodName")
            val foodIdIndex = cursor.getColumnIndex("id")


            while (cursor.moveToNext()){

                //database den alinan veriler olusturulan dizilere eklendi
                foodNameList.add(cursor.getString(foodNameIndex))
                foodIdList.add(cursor.getInt(foodIdIndex))

            }

            arrayAdapter.notifyDataSetChanged()
            cursor.close()

        }catch (e : Exception){
            e.printStackTrace()
        }

        //yemek adina tiklaninca yapilacak islemler

       recyclerView.onItemClickListener = AdapterView.OnItemClickListener{parent, view, position, id ->

           val intent = Intent(this,AddFoodActivity::class.java)
           intent.putExtra("info","old")
           intent.putExtra("id",foodIdList[position])
           startActivity(intent)

       }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        //menu baglama islemi
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_food_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //menu tiklaninca yapilacak islemler
        if(item.itemId == R.id.addFood){
            val intent = Intent(this@MainActivity,AddFoodActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

}