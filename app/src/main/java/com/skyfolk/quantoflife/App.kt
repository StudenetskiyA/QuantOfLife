package com.skyfolk.quantoflife

import android.app.Application
import android.content.Context
import com.skyfolk.quantoflife.db.DBInteractor
import com.skyfolk.quantoflife.db.EventsStorageInteractor
import com.skyfolk.quantoflife.db.IQuantsStorageInteractor
import com.skyfolk.quantoflife.db.QuantsStorageInteractor
import com.skyfolk.quantoflife.ui.now.NowViewModel
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.ui.onboarding.OnBoardingViewModel
import com.skyfolk.quantoflife.ui.settings.SettingsViewModel
import com.skyfolk.quantoflife.ui.feeds.StatisticViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        val storageModule = module {
            single { DBInteractor(get()) }
            single<IQuantsStorageInteractor> { QuantsStorageInteractor(get()) }
            single{ EventsStorageInteractor(get()) }
            single { SettingsInteractor(androidContext().getSharedPreferences("qol_preferences", Context.MODE_PRIVATE))}
        }

        val viewModelModule = module {
            viewModel { NowViewModel(get(), get(), get()) }
            viewModel { SettingsViewModel(get(), get(), get(), get())}
            viewModel { StatisticViewModel(get(), get(), get()) }
            viewModel { OnBoardingViewModel(get()) }
        }

        startKoin {
            androidContext(this@App)
            androidLogger()
            modules(listOf(storageModule, viewModelModule))
        }
    }
}


