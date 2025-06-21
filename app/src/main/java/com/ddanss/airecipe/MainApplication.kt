package com.ddanss.airecipe

import android.app.Application
import androidx.room.Room
import com.ddanss.airecipe.room.AppDatabase

class MainApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "database-airecipe"
        ).build()
    }
}