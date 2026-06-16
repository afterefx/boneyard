package app.boneyard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import app.boneyard.data.local.dao.GameDao
import app.boneyard.data.local.dao.GamePlayerDao
import app.boneyard.data.local.dao.PlayerDao
import app.boneyard.data.local.dao.RoundDao
import app.boneyard.data.local.dao.RoundScoreDao
import app.boneyard.data.local.dao.TemplateDao
import app.boneyard.data.local.entity.GameEntity
import app.boneyard.data.local.entity.GamePlayerEntity
import app.boneyard.data.local.entity.PlayerEntity
import app.boneyard.data.local.entity.RoundEntity
import app.boneyard.data.local.entity.RoundScoreEntity
import app.boneyard.data.local.entity.TemplateEntity
import app.boneyard.data.local.entity.TemplatePlayerEntity

@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        RoundEntity::class,
        RoundScoreEntity::class,
        TemplateEntity::class,
        TemplatePlayerEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun gamePlayerDao(): GamePlayerDao
    abstract fun roundDao(): RoundDao
    abstract fun roundScoreDao(): RoundScoreDao
    abstract fun templateDao(): TemplateDao

    companion object {
        const val DATABASE_NAME = "boneyard.db"
    }
}
