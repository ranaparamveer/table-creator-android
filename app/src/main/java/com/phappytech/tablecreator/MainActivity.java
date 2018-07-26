package com.phappytech.tablecreator;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;

import com.phappytech.library.TableCreator;

import org.chalup.microorm.MicroOrm;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseHelper openHelper = new DatabaseHelper(this,"test.db");
        SQLiteDatabase sqLiteDatabase = openHelper.getWritableDatabase();
        new TableCreator().createTable(sqLiteDatabase, "MyTable", MyModel.class);
    }
}
