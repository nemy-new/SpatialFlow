package com.codetrio.overdrive.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlaylistEntity::class, PlaylistSongEntity::class, HistoryEventEntity::class, LyricsSyncEntity::class], version = 5, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao
    abstract fun lyricsSyncDao(): LyricsSyncDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "spatialflow_music_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlist_songs ADD COLUMN lufs REAL DEFAULT NULL")
            }
        }
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `lyrics_sync` (`videoId` TEXT NOT NULL, `offsetMs` INTEGER NOT NULL, PRIMARY KEY(`videoId`))")
            }
        }
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_playlist_songs_playlistId` ON `playlist_songs` (`playlistId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_playlist_songs_songId` ON `playlist_songs` (`songId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_history_events_timestamp` ON `history_events` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_history_events_songId` ON `history_events` (`songId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_history_events_hourOfDay` ON `history_events` (`hourOfDay`)")
            }
        }
    }
}
