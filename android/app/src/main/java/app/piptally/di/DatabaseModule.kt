package app.piptally.di

import android.content.Context
import androidx.room.Room
import app.piptally.data.local.AppDatabase
import app.piptally.data.local.dao.GameDao
import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.data.local.dao.PlayerDao
import app.piptally.data.local.dao.RoundDao
import app.piptally.data.local.dao.RoundScoreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

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
}
