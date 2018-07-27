package com.phappytech.library;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.phappytech.library.annotations.PrimaryKey;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import org.chalup.microorm.annotations.Column;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Paramveer Singh
 *
 * @see <a href="https://github.com/ranaparamveer/table-creator-android">Table Creator</a>
 */
public class TableCreator {
    private static HashMap<String, String> COLUMN_FIELDS = new HashMap<>();

    /**
     * Create table in opened database from model
     *
     * @param sqLiteDatabase writable non-null database and currently opened
     * @param tableName      table name to create
     * @param mClass         model class to use as a reference for column names
     *                       The column names are determined from annotations attached
     * @param <T>            template of class to be used
     * @return true if table already exists or is successfully created
     * false otherwise
     */
    public <T> boolean createTable(@NonNull SQLiteDatabase sqLiteDatabase, @NonNull String tableName,
                                   @NonNull Class<T> mClass) {
        if (!sqLiteDatabase.isOpen() || (sqLiteDatabase.isOpen() && sqLiteDatabase.isReadOnly()))
            return false;
        if (isTableExists(sqLiteDatabase, tableName))
            return true;
        createColumnsMap(mClass);
        try {
            StringBuilder commandBuilder = new StringBuilder("CREATE TABLE "
                    + tableName + " ( ");
            String previousSymbol = "";
            for (Map.Entry<String, String> entry : COLUMN_FIELDS.entrySet()) {
                commandBuilder.append(previousSymbol).append(entry.getKey()).append(" ").append(entry.getValue());
                previousSymbol = ", ";
            }
            String command = commandBuilder.append(")").toString();
            sqLiteDatabase.execSQL(command);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Create table in opened database from model
     *
     * @param sqLiteDatabase       writable non-null database and currently opened
     * @param tableName            table name to create
     * @param columnNamesWithTypes String array of column names with their types.
     *                             eg: new String[]{"ZNAME TEXT","ZID INTEGER"}
     * @return true if table already exists or is successfully created
     * false otherwise
     */
    public boolean createTable(@NonNull SQLiteDatabase sqLiteDatabase, @NonNull String tableName,
                               @NonNull String[] columnNamesWithTypes) {
        if (!sqLiteDatabase.isOpen() || (sqLiteDatabase.isOpen() && sqLiteDatabase.isReadOnly()))
            return false;
        if (isTableExists(sqLiteDatabase, tableName))
            return true;
        try {
            StringBuilder commandBuilder = new StringBuilder("CREATE TABLE "
                    + tableName + " ( ");
            String previousSymbol = "";
            for (String string : columnNamesWithTypes) {
                commandBuilder.append(previousSymbol).append(string);
                previousSymbol = ", ";
            }
            String command = commandBuilder.append(")").toString();
            sqLiteDatabase.execSQL(command);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean copyTableFromDB(@NonNull SQLiteDatabase fromDB, @NonNull SQLiteDatabase toDb, @NonNull String tableName) {
        Cursor fromCursor = fromDB.query(tableName, null, null, null, null, null, null);

        if (!toDb.isOpen() || (toDb.isOpen() && toDb.isReadOnly()) || fromCursor == null || fromCursor.getColumnCount() == 0)
            return false;
        if (!isTableExists(toDb, tableName)) {
            fromCursor.moveToFirst();
            createTable(toDb, tableName, createColumnNames(fromCursor));
        } else {
            toDb.delete(tableName, null, null);
        }
        try {
            toDb.insert(tableName, null, cursorRowToContentValues(fromCursor));
            fromCursor.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean copyTableFromDB(@NonNull Context context, @NonNull String fromDBName,
                                   @NonNull String toDbName, @NonNull String tableName) {
        SQLiteDatabase fromDb = null, toDb = null;
        try {
            SQLiteAssetHelper sqLiteAssetHelper=new SQLiteAssetHelper(context,fromDBName,null,1);
            SQLiteAssetHelper sqLiteAssetHelperTo=new SQLiteAssetHelper(context,toDbName,null,1);
            fromDb=sqLiteAssetHelper.getReadableDatabase();
            toDb=sqLiteAssetHelperTo.getWritableDatabase();
            if (fromDb == null || toDb == null)
                return false;
            Cursor fromCursor = fromDb.query(tableName, null, null, null, null, null, null);

            if (!toDb.isOpen() || (toDb.isOpen() && toDb.isReadOnly()) || fromCursor == null || fromCursor.getColumnCount() == 0)
                return false;
            if (!isTableExists(toDb, tableName)) {
                fromCursor.moveToFirst();
                createTable(toDb, tableName, createColumnNames(fromCursor));
            } else {
                toDb.delete(tableName, null, null);
            }
            try {
                toDb.insert(tableName, null, cursorRowToContentValues(fromCursor));
                fromCursor.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                fromCursor.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fromDb != null && fromDb.isOpen())
                fromDb.close();
            if (toDb != null && toDb.isOpen())
                toDb.close();
        }
    }

    private String getDatabasePath(@NonNull Context context, @Nullable String storageDirectory, @NonNull String name) {
        String mDatabasePath;
        if (storageDirectory != null) {
            mDatabasePath = storageDirectory;
        } else {
            mDatabasePath = context.getApplicationInfo().dataDir + "/databases";
        }
        return mDatabasePath + "/" + name;
    }

    private String[] createColumnNames(Cursor cursor) {
        String[] strings = new String[cursor.getColumnCount()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = cursor.getColumnName(i) + " " + getTypeText(cursor.getType(i));
        }

        return strings;
    }

    private String getTypeText(int type) {
        switch (type) {
            case Cursor.FIELD_TYPE_INTEGER:
                return "INTEGER";
            case Cursor.FIELD_TYPE_FLOAT:
                return "REAL";
            case Cursor.FIELD_TYPE_STRING:
                return "TEXT";
            case Cursor.FIELD_TYPE_BLOB:
                return "BLOB";
            default:
                return "TEXT";
        }
    }

    private <T> void createColumnsMap(Class<T> mClass) {
        if (!COLUMN_FIELDS.isEmpty())
            COLUMN_FIELDS.clear();
        boolean primaryKeyFound = false;
        for (Field field : Fields.allFieldsIncludingPrivateAndSuper(mClass)) {
            field.setAccessible(true);
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                if (field.getType().isPrimitive() && columnAnnotation.treatNullAsDefault()) {
                    throw new IllegalArgumentException("Cannot set treatNullAsDefault on primitive members");
                }
                if (columnAnnotation.treatNullAsDefault() && columnAnnotation.readonly()) {
                    throw new IllegalArgumentException("It doesn't make sense to set treatNullAsDefault on readonly column");
                }

                PrimaryKey primaryKeyAnnotation = field.getAnnotation(PrimaryKey.class);
                String primaryKeyString = "";
                if (primaryKeyAnnotation != null && (TYPE_ADAPTERS.get(field.getType()).equals("INTEGER"))) {
                    if (primaryKeyFound)
                        throw new RuntimeException("Multiple Primary Keys found in column model");
                    primaryKeyString = " PRIMARY KEY AUTOINCREMENT";
                    primaryKeyFound = true;
                }

                COLUMN_FIELDS.put(columnAnnotation.value(), TYPE_ADAPTERS.get(field.getType()) + primaryKeyString);

            }


        }
    }

    private static final ImmutableMap<Class<?>, String> TYPE_ADAPTERS;

    static {

        Map<Class<?>, String> typeAdapters = Maps.newHashMap();

        typeAdapters.put(short.class, "INTEGER");
        typeAdapters.put(int.class, "INTEGER");
        typeAdapters.put(long.class, "INTEGER");
        typeAdapters.put(boolean.class, "INTEGER DEFAULT 0");
        typeAdapters.put(float.class, "REAL");
        typeAdapters.put(double.class, "REAL");

        typeAdapters.put(Short.class, "INTEGER");
        typeAdapters.put(Integer.class, "INTEGER");
        typeAdapters.put(Long.class, "INTEGER");
        typeAdapters.put(Boolean.class, "INTEGER DEFAULT 0");
        typeAdapters.put(Float.class, "REAL");
        typeAdapters.put(Double.class, "REAL");

        typeAdapters.put(String.class, "TEXT");

        TYPE_ADAPTERS = ImmutableMap.copyOf(typeAdapters);
    }

    public boolean isTableExists(SQLiteDatabase sqLiteDatabase, String tableName) {

        Cursor cursor = sqLiteDatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public static ContentValues cursorRowToContentValues(Cursor cursor) {
        ContentValues values = new ContentValues();
        String[] columns = cursor.getColumnNames();
        int length = columns.length;
        for (int i = 0; i < length; i++) {
            switch (cursor.getType(i)) {
                case Cursor.FIELD_TYPE_NULL:
                    values.putNull(columns[i]);
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    values.put(columns[i], cursor.getLong(i));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    values.put(columns[i], cursor.getDouble(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    values.put(columns[i], cursor.getString(i));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    values.put(columns[i], cursor.getBlob(i));
                    break;
            }
        }
        return values;
    }
}
