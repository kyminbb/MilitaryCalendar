package com.kyminbb.militarycalendar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.commit451.addendum.threetenabp.toLocalDate
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton
import java.io.IOException
import java.util.*
import android.widget.AdapterView
import android.widget.ArrayAdapter

class SettingActivity : AppCompatActivity() {

    companion object {
        const val SELECT_PICTURE = 1
        const val REQUEST_READ_EXTERNAL_STORAGE = 1000
    }

    // Initialize today's date.
    private val today = Calendar.getInstance().toLocalDate()
    private val todayYear = today.year
    private val todayMonth = today.monthValue
    private val todayDay = today.dayOfMonth

    // Initialize the user info.
    var userInfo = User()

    val prefs by lazy {getSharedPreferences("prefs", Context.MODE_PRIVATE)} //shared preference 객체, Activity 초기화한 이후에 사용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the timezone information.
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_setting)

        // Initialize the view.
        init()

        // Load the user info if there exists.
        loadData()

        // Set the spinner for affiliation.
        var affiliations = arrayOf("육군/의경", "해군/해양의무경찰", "공군", "해병", "사회복무요원", "의무소방")
        inputAffiliation.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, affiliations)
        inputAffiliation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //affiliations.get(position) is the string of user's affiliation
                userInfo.affiliation = affiliations.get(position) //setAffiliation() deleted
            }
        }


        // Update profile image.
        buttonProfileImage.setOnClickListener {
            setProfileImage()
        }
/*
        // Change the ETS date only when automatically added date is inaccurate.
        inputEnlistDate.setOnClickListener {
            setEnlistDate()
            setEndDate()
        }
*/
        // Update the affiliation.
        inputEndDate.setOnClickListener {
            setEndDate()
        }

        // Update the promotion dates.
        inputPromotionDate.setOnClickListener {
            setPromotionDates()
        }

        // Complete the info update and save.
        buttonComplete.setOnClickListener {
            saveData()
            // Transition to the main page.
            startActivity<MainActivity>()
        }

        // Initialize the user info and the activity.
        buttonInit.setOnClickListener {
            init()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Start gallery intent if permission is granted.
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                startGalleryIntent()
                /*if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryIntent()
                }*/
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) try {
            // Update the profile image with the loaded image file.
            val mImageUri = intent!!.data
            Glide.with(this).load(mImageUri).into(buttonProfileImage)

            // Get persistent Uri permission so that it will be allowed to reload when you restart the application.
            userInfo.profileImage = mImageUri!!.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun init() {
        // Initialize button texts to today's date.
        inputEnlistDate.text = "${todayYear}/${todayMonth}/${todayDay}"

        // Calculate inputEndDate and inputPromoteDate by given input(inputEndlistDate, affilitaion)
        inputEndDate.text = "전역일"
        inputPromotionDate.text = "진급일"
    }

    // Load the user info from SharedPreferences.
    private fun loadData() {
        //val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE) //필드에 선언함
        val firstStart = prefs.getBoolean("firstStart", true)
        // Load if the application is not first-time executed.
        if (!firstStart) {
            inputEnlistDate.text = prefs.getString(userInfo.affiliation, "")
        }
    }

    // Save the user info to SharedPreferences.
    private fun saveData() {
        //val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE) //필드에 선언함
        prefs.edit().putBoolean("firstStart", false).apply()
        prefs.edit().putString("affiliation", userInfo.affiliation).apply()
    }

    private fun setProfileImage() {
        // First check whether permission to read gallery is granted.
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                // Request permission to user.
                alert("사진 정보를 얻으려면 외부 저장소 권한이 필수로 필요합니다", "권한이 필요한 이유") {
                    yesButton {
                        ActivityCompat.requestPermissions(
                            this@SettingActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE
                        )
                    }
                    noButton { }
                }.show()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        } else {
            // Start gallery intent if permission is granted.
            startGalleryIntent()
        }
    }

    private fun startGalleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image"), SELECT_PICTURE)
    }
/*
    private fun setEnlistDate() {
        // Use SpinnerDatePicker to select the enlist date.
        // https://github.com/drawers/SpinnerDatePicker
        val inputDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            userInfo.promotionDates[Dates.ENLIST.ordinal] = LocalDate.of(year, month + 1, day)
            inputEnlist.text = formatDate(userInfo.promotionDates[Dates.ENLIST.ordinal])
            setEndDate()
        }

        val dialog = SpinnerDatePickerDialogBuilder()
            .context(this)
            .callback(inputDateSetListener)
            .spinnerTheme(R.style.NumberPickerStyle)
            .showTitle(true)
            .showDaySpinner(true)
        if (userInfo.promotionDates.isEmpty()) {
            dialog.defaultDate(todayYear, todayMonth, todayDay)
        } else {
            dialog.defaultDate(
                userInfo.promotionDates[Dates.ENLIST.ordinal].year,
                userInfo.promotionDates[Dates.ENLIST.ordinal].monthValue - 1,
                userInfo.promotionDates[Dates.ENLIST.ordinal].dayOfMonth
            )
        }
        dialog.maxDate(todayYear + 4, 11, 31)
            .minDate(todayYear - 5, 0, 1)
            .build().show()
    }
*/
    private fun setEndDate() {
    }

    private fun setPromotionDates() {}
/*
    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("YYYY/MM/dd"))
    }*/
}