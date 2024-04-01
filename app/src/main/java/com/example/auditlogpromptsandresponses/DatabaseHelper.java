package com.example.auditlogpromptsandresponses;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "audit_responses.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_AUDIT_PROMPT = "audit_prompt";
    private static final String TABLE_RESPONSES = "responses";

    private static final String COLUMN_SEQUENCE_NUMBER = "sequence_number";
    private static final String COLUMN_PROMPT_TIME = "prompt_time";
    private static final String COLUMN_PROMPT = "prompt";
    private static final String COLUMN_RESPONSE_TIME = "response_time";
    private static final String COLUMN_RESPONSE = "response";

    // Table create statements
    private static final String CREATE_TABLE_AUDIT_PROMPT = "CREATE TABLE "
            + TABLE_AUDIT_PROMPT + "(" + COLUMN_SEQUENCE_NUMBER + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PROMPT_TIME + " TEXT,"
            + COLUMN_PROMPT + " TEXT" + ")";

    private static final String CREATE_TABLE_RESPONSES = "CREATE TABLE "
            + TABLE_RESPONSES + "(" + COLUMN_SEQUENCE_NUMBER + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_RESPONSE_TIME + " TEXT,"
            + COLUMN_RESPONSE + " TEXT" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_AUDIT_PROMPT);
        db.execSQL(CREATE_TABLE_RESPONSES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDIT_PROMPT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESPONSES);
        // Create tables again
        onCreate(db);
    }

    // Add new audit prompt
    public void addAuditPrompt(String promptTime, String prompt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROMPT_TIME, promptTime);
        values.put(COLUMN_PROMPT, prompt);
        // Inserting Row
        db.insert(TABLE_AUDIT_PROMPT, null, values);
        db.close();
    }

    // Add new response
    public void addResponse(String responseTime, String response) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESPONSE_TIME, responseTime);
        values.put(COLUMN_RESPONSE, response);
        // Inserting Row
        db.insert(TABLE_RESPONSES, null, values);
        db.close();
    }

    public String getAllAuditPromptLogs() {
        List<String> logs = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_AUDIT_PROMPT;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String log = "ID: " + cursor.getInt(cursor.getColumnIndex(COLUMN_SEQUENCE_NUMBER)) +
                        ", Prompt Time: " + cursor.getString(cursor.getColumnIndex(COLUMN_PROMPT_TIME)) +
                        ", Prompt: " + cursor.getString(cursor.getColumnIndex(COLUMN_PROMPT));
                logs.add(log);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return formatLogs(logs);
    }

    public String getAllResponseLogs() {
        List<String> logs = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_RESPONSES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String log = "ID: " + cursor.getInt(cursor.getColumnIndex(COLUMN_SEQUENCE_NUMBER)) +
                        ", Response Time: " + cursor.getString(cursor.getColumnIndex(COLUMN_RESPONSE_TIME)) +
                        ", Response: " + cursor.getString(cursor.getColumnIndex(COLUMN_RESPONSE));
                logs.add(log);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return formatLogs(logs);
    }

    private String formatLogs(List<String> logs) {
        StringBuilder formattedLogs = new StringBuilder();
        for (String log : logs) {
            formattedLogs.append(log).append("\n");
        }
        return formattedLogs.toString();
    }
}

