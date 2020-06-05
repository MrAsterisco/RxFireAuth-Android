package io.github.mrasterisco.rxfireauthexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.mrasterisco.rxfireauth.impl.UserManager
import io.github.mrasterisco.rxfireauth.interfaces.IUserManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val userManager: IUserManager = UserManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }

}
