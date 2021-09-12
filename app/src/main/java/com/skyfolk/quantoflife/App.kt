package com.skyfolk.quantoflife

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.skyfolk.quantoflife.db.*
import com.skyfolk.quantoflife.import.ImportInteractor
import com.skyfolk.quantoflife.ui.now.NowViewModel
import com.skyfolk.quantoflife.settings.SettingsInteractor
import com.skyfolk.quantoflife.ui.onboarding.OnBoardingViewModel
import com.skyfolk.quantoflife.ui.settings.SettingsViewModel
import com.skyfolk.quantoflife.ui.feeds.FeedsViewModel
import com.skyfolk.quantoflife.ui.statistic.StatisticViewModel
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
            single<IGoalStorageInteractor> { GoalStorageInteractor(get()) }
            single { SettingsInteractor(androidContext())}
            single<IDateTimeRepository> { DateTimeRepository() }
            single { ImportInteractor(get(), get(), get(), androidContext().resources.openRawResource(R.raw.qol_base))}
        }

        val viewModelModule = module {
            viewModel { NowViewModel(get(), get(), get(), get(), get(), get()) }
            viewModel { SettingsViewModel( get(), get(), get(), get())}
            viewModel { FeedsViewModel(get(), get(), get(), get()) }
            viewModel { StatisticViewModel(get(), get(), get(), get()) }
            viewModel { OnBoardingViewModel(get()) }
        }

        startKoin {
            androidContext(this@App)
            androidLogger()
            modules(listOf(storageModule, viewModelModule))
        }

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
    }
}


