package app.piptally.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.piptally.data.local.dao.GameDao
import app.piptally.data.local.dao.GamePlayerDao
import app.piptally.data.local.dao.PlayerDao
import app.piptally.data.local.dao.RoundDao
import app.piptally.data.local.dao.RoundScoreDao
import app.piptally.data.local.entity.GameEntity
import app.piptally.data.local.entity.GamePlayerEntity
import app.piptally.data.local.entity.PlayerEntity
import app.piptally.data.local.entity.RoundEntity
import app.piptally.data.local.entity.RoundScoreEntity

@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        RoundEntity::class,
        RoundScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun gamePlayerDao(): GamePlayerDao
    abstract fun roundDao(): RoundDao
    abstract fun roundScoreDao(): RoundScoreDao

    companion object {
        const val DATABASE_NAME = "domino_tracker.db"
    }
}
