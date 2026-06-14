package app.piptally.di

import app.piptally.data.repository.GameRepositoryImpl
import app.piptally.data.repository.PlayerRepositoryImpl
import app.piptally.data.repository.RoundRepositoryImpl
import app.piptally.data.repository.RoundScoreRepositoryImpl
import app.piptally.data.repository.StatsRepositoryImpl
import app.piptally.domain.repository.GameRepository
import app.piptally.domain.repository.PlayerRepository
import app.piptally.domain.repository.RoundRepository
import app.piptally.domain.repository.RoundScoreRepository
import app.piptally.domain.repository.StatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindRoundRepository(impl: RoundRepositoryImpl): RoundRepository

    @Binds
    @Singleton
    abstract fun bindRoundScoreRepository(impl: RoundScoreRepositoryImpl): RoundScoreRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: app.piptally.data.repository.SettingsRepositoryImpl): app.piptally.domain.repository.SettingsRepository
}
