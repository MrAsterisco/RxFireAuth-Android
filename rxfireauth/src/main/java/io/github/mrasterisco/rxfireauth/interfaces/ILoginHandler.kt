package io.github.mrasterisco.rxfireauth.interfaces

import android.content.Intent

interface ILoginHandler {

    fun handle(data: Intent): Boolean

}