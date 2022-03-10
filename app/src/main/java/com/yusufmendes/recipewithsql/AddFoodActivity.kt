package com.yusufmendes.recipewithsql

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.internal.ManufacturerUtils
import kotlinx.android.synthetic.main.activity_add_food.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.jar.Manifest

class AddFoodActivity : AppCompatActivity() {

    var selectImage : Uri? = null
    var selectBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)


        val intent = intent
        val info = intent.getStringExtra("info")

        if (info.equals("new")){

            foodNameEditText.setText("")
            recipeEditText.setText("")
            save.visibility = View.VISIBLE

            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.select_image)
            imageView.setImageBitmap(selectedImageBackground)

        }else{

            save.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)

            val database = this.openOrCreateDatabase("Foods", Context.MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM foods WHERE id = ?", arrayOf(selectedId.toString()))

            val foodNameIx = cursor.getColumnIndex("foodName")
            val recipeIx = cursor.getColumnIndex("recipe")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){

                foodNameEditText.setText(cursor.getString(foodNameIx))
                recipeEditText.setText(cursor.getString(recipeIx))


                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                imageView.setImageBitmap(bitmap)

            }
            cursor.close()
        }
    }

    fun save(view: View){

        //SQLite'a kaydetme islemleri

        var foodName = foodNameEditText.text.toString()
        var foodRecipe = recipeEditText.text.toString()

        // secilen bitmap bos mu degil mi kontrol edildi
        if (selectBitmap != null){

            val shortBitmap = shrinkTheImage(selectBitmap!!,300)

            //bitmap'i veriye donusturme
            val outputStream = ByteArrayOutputStream()
            shortBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            //verileri kaydetme islemleri(SQL)
            try {

                val database = this.openOrCreateDatabase("Foods", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS foods(id INTEGER PRIMARY KEY, foodName VARCHAR,recipe TEXT, image BLOB)")
                val sqlString = "INSERT INTO foods (foodName,recipe,image) VALUES (?,?,?)"
                //baglama islemi
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,foodName)
                statement.bindString(2,foodRecipe)
                statement.bindBlob(3,byteArray)
                //baglama islemlerini sql de calistirma islemi
                statement.execute()

            }catch (e : Exception){
                e.printStackTrace()
            }

            //intent olayi
            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

            finish()
        }
    }

    fun selectImage(view : View){

        //kullanıcı bu izne daha önce onay vermis mi kontrolu
        if(ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //izin verilmedi izin istenilmesi gerekli

            //izin isteme(istenilecek izin,1(istenilen bir deger yazilabilir))
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }else{
            //izin verildi

            //galeriye gitme islemleri
            val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent,2)
        }
    }

    //istenilen izinler verilince yapilacak sonuclar(android icinde bulunan hazır fonksiyon)

    override fun onRequestPermissionsResult(
        requestCode: Int, //istek kodu
        permissions: Array<out String>, //dizi icindeki izin
        grantResults: IntArray //verilen sonuclar
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //requsetcode 1 ise izin verilmistir
        if (requestCode == 1){

            //grandResult.size > 0 geriye birsey dondu mu kontrolu icin kullanildi
            //grandResult[0] sonuclar izin verildiyse
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //if sarti saglandi ise kullanici izin vermis demektir

                //galeriye gitme islemleri
                val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent,2)
            }
        }
    }

    //galeriye gidilince yapilacaklar

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            //secilen gorselin konumu
            selectImage = data.data

            try {

                applicationContext?.let {
                    //secilen gorseli bitmap cevirme
                    if (selectImage != null){
                        if (Build.VERSION.SDK_INT >= 28 ){
                            val source = ImageDecoder.createSource(it.contentResolver,selectImage!!)
                            selectBitmap = ImageDecoder.decodeBitmap(source)
                            //secilen gorseli imageViewe atama
                            imageView.setImageBitmap(selectBitmap)
                        }
                        else{
                            selectBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,selectImage)
                            imageView.setImageBitmap(selectBitmap)
                        }
                    }
                }

            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //gorsellerin boyutlarini kucultme islemi
    fun shrinkTheImage(selectUserBitmap : Bitmap , maxSize : Int) : Bitmap{

        var width = selectUserBitmap.width
        var height = selectUserBitmap.height

        //bitmap orani hesaplama islemi
        var bitmapRatio : Double = width.toDouble() / height.toDouble()

        //gorselin yatay mi dikey mi oldugunu kontrol etme
        if (bitmapRatio > 1){
            //gorsel yatay

            width = maxSize
            val shortenHeight = width / bitmapRatio
            height = shortenHeight.toInt()

        }else{
            //gorsel dikey
            height = maxSize
            val shortenWidth = height * bitmapRatio
            width = shortenWidth.toInt()

        }
        return Bitmap.createScaledBitmap(selectUserBitmap,width,height,true)
    }

}