package jiwondev.di_container.application

import android.app.Application
import jiwondev.di_container.container.AppContainer

class App : Application() {
    val appContainer: AppContainer = AppContainer(this)
}