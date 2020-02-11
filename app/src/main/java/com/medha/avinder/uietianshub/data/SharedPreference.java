package com.medha.avinder.uietianshub.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    private SharedPreferences sharedPreferences;

    public SharedPreference(Context context) {
        sharedPreferences = context.getSharedPreferences("shared_preference", Context.MODE_PRIVATE);
    }

    public int getRunCount() {
        return sharedPreferences.getInt("run_count", 0);
    }

    public void setRunCount(int count) {
        sharedPreferences.edit().putInt("run_count", count).apply();
    }

    public int getSyllabusVersion() {
        return sharedPreferences.getInt("syllabus_version", 0);
    }

    public void setSyllabusVersion(int version) {
        sharedPreferences.edit().putInt("syllabus_version", version).apply();
    }

    public int getCommitteesVersion() {
        return sharedPreferences.getInt("committees_version", 0);
    }

    public void setCommitteesVersion(int version) {
        sharedPreferences.edit().putInt("committees_version", version).apply();
    }

    public int getFacultyVersion() {
        return sharedPreferences.getInt("faculty_version", 0);
    }

    public void setFacultyVersion(int version) {
        sharedPreferences.edit().putInt("faculty_version", version).apply();
    }

    public int getWorkshopsVersion() {
        return sharedPreferences.getInt("workshops_version", 0);
    }

    public void setWorkshopsVersion(int version) {
        sharedPreferences.edit().putInt("workshops_version", version).apply();
    }

    public String getCurrentSyllabusInCache() {
        return sharedPreferences.getString("syllabus_in_cache", null);
    }

    public void setCurrentSyllabusInCache(String branch) {
        sharedPreferences.edit().putString("syllabus_in_cache", branch).apply();
    }

    public String getCurrentPaperInCache() {
        return sharedPreferences.getString("paper_in_cache", null);
    }

    public void setCurrentPaperInCache(String name) {
        sharedPreferences.edit().putString("paper_in_cache", name).apply();
    }

    public String getCurrentWorkshopInCache() {
        return sharedPreferences.getString("workshop_in_cache", null);
    }

    public void setCurrentWorkshopInCache(String name) {
        sharedPreferences.edit().putString("workshop_in_cache", name).apply();
    }

    public String getCurrentFacultyInCache() {
        return sharedPreferences.getString("faculty_in_cache", null);
    }

    public void setCurrentFacultyInCache(String name) {
        sharedPreferences.edit().putString("faculty_in_cache", name).apply();
    }
}
