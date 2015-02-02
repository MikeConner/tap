package co.tapdatapp.tapandroid.localdata;

import android.database.sqlite.SQLiteDatabase;

interface SingleTable {
  void onCreate(SQLiteDatabase db);
  void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
