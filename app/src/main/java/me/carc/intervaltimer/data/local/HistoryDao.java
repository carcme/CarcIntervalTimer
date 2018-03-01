package me.carc.intervaltimer.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Store workout history - History Data Access Object (DAO)
 * Created by bamptonm on 03/10/2017.
 */

@Dao
public interface HistoryDao {

    @Query("SELECT * FROM HistoryItem")
    List<HistoryItem> getAllEntries();

    @Query("SELECT * FROM HistoryItem WHERE keyID LIKE :id LIMIT 1")
    HistoryItem findByIndex(long id);

    @Insert
    void insertAll(List<HistoryItem> entries);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HistoryItem entry);

    @Update
    void update(HistoryItem entry);

    @Delete
    void delete(HistoryItem entry);

    @Query("DELETE FROM HistoryItem")
    void nukeTable();
}
