package com.example.ledcontroller;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// creates the database
@Database(entities = {ColorPattern.class}, version = 1, exportSchema = false) // makes a database that stores ColorPatterns
public abstract class PatternDatabase extends RoomDatabase {

    public abstract PatternDao patternDao(); // DAO reference

    private static volatile PatternDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    // create executorService to run database operations on a separate group of threads
    static final ExecutorService databaseWriterExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // creates and returns the database
    static PatternDatabase getDatabase(final Context context)
    {
        if (INSTANCE == null)
        {
            // allow only one thread to create the database
            synchronized (PatternDatabase.class)
            {
                if (INSTANCE == null)
                {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), PatternDatabase.class, "pattern_database").addCallback(PatternDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }

    // this method is called when the database is built (when the app opens)
    private static PatternDatabase.Callback PatternDatabaseCallback = new RoomDatabase.Callback()
    {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db)
        {
            super.onOpen(db);

            databaseWriterExecutor.execute(() ->
            {
                /*
                PatternDao dao = INSTANCE.patternDao();
                // interact with the database below
                // delete previous patterns (for testing)
                dao.deleteAll();
                byte[] code = {'s', 0-128, 0-128, 1-128, 0 - 128};
                ColorPattern pattern = new ColorPattern("Red", code);
                dao.insert(pattern);
                code[4] = 96 - 128;
                pattern = new ColorPattern("Green", code);
                dao.insert(pattern);
                code[4] = 160 - 128;
                pattern = new ColorPattern("Blue", code);
                dao.insert(pattern);
                 */
            });
        }
    };
}
