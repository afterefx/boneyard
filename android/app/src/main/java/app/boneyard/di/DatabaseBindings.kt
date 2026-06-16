package app.boneyard.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import app.boneyard.data.local.AppDatabase
import app.boneyard.data.local.dao.GameDao
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.data.local.dao.RoundDao
import app.boneyard.data.local.dao.RoundScoreDao
import app.boneyard.data.local.dao.TemplateDao
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object DatabaseBindings {

    @Provides
    fun provideContext(application: Application): Context = application

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()

    @Provides
    fun provideGameDao(db: AppDatabase): GameDao = db.gameDao()

    @Provides
    fun provideGamePlayerDao(db: AppDatabase): GamePlayerDao = db.gamePlayerDao()

    @Provides
    fun provideRoundDao(db: AppDatabase): RoundDao = db.roundDao()

    @Provides
    fun provideRoundScoreDao(db: AppDatabase): RoundScoreDao = db.roundScoreDao()

    @Provides
    fun provideTemplateDao(db: AppDatabase): TemplateDao = db.templateDao()

    @Provides
    fun provideCircuit(
        presenterFactories: Set<Presenter.Factory>,
        uiFactories: Set<Ui.Factory>,
    ): Circuit = Circuit.Builder()
        .addPresenterFactories(presenterFactories)
        .addUiFactories(uiFactories)
        .build()
}
