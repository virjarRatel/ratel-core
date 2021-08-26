package com.virjar.ratel.manager.model;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.raizlabs.android.dbflow.sql.migration.UpdateTableMigration;

@Database(version = DbConf.VERSION)
public class DbConf {
    static final int VERSION = 6;


    @Migration(version = 3, database = DbConf.class)
    public static class MigrationVersion3 extends AlterTableMigration<RatelApp> {
        public MigrationVersion3(Class<RatelApp> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            addColumn(SQLiteType.INTEGER, "daemon");
            super.onPreMigrate();
        }
    }


    @Migration(version = 5, database = DbConf.class)
    public static class MigrationVersion5 extends UpdateTableMigration<RatelApp> {

        /**
         * Creates an update migration.
         *
         * @param table The table to update
         */
        public MigrationVersion5(@NonNull Class<RatelApp> table) {
            super(table);
            set(RatelApp_Table.engineVersionCode.eq(0));
            set(RatelApp_Table.engineVersionName.eq("unknown"));
            set(RatelApp_Table.buildTimeStamp.eq(0L));
        }
    }
}
