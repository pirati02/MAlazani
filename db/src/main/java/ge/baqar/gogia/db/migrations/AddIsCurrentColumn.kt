package ge.baqar.gogia.db.migrations

import androidx.sqlite.db.SupportSQLiteDatabase

import androidx.room.migration.Migration




object AddIsCurrentColumn {
    val migration: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Song "
                        + "ADD COLUMN is_current BIT"
            )
        }
    }
}