package com.medha.avinder.uietianshub.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.medha.avinder.uietianshub.models.Committee;
import com.medha.avinder.uietianshub.models.Faculty;
import com.medha.avinder.uietianshub.models.QuestionPaper;
import com.medha.avinder.uietianshub.models.Syllabus;
import com.medha.avinder.uietianshub.models.Timeline;
import com.medha.avinder.uietianshub.models.User;
import com.medha.avinder.uietianshub.models.Workshop;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "eh_db";

    public static final String TABLE_PROFILE = "profile";
    public static final String TABLE_SYLLABUS = "syllabus";
    public static final String TABLE_Q_PAPERS = "q_papers";
    public static final String TABLE_COMMITTEES = "committees";
    public static final String TABLE_FACULTY = "faculty";
    public static final String TABLE_WORKSHOPS = "workshops";
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String TABLE_WIFI_USERS = "wifi_users";

    private static final String COLUMN_PROFILE_ID = "profile_id";
    private static final String COLUMN_PROFILE_NAME = "profile_name";
    private static final String COLUMN_PROFILE_BRANCH_SEM = "profile_branch_sem";
    private static final String COLUMN_PROFILE_EMAIL = "profile_email";
    private static final String COLUMN_PROFILE_O_EMAIL = "profile_o_email";

    private static final String COLUMN_SYLLABUS_BRANCH_SEM = "syllabus_branch_sem";
    private static final String COLUMN_SYLLABUS_SUBJECTS = "syllabus_subjects";
    private static final String COLUMN_SYLLABUS_PAGES = "syllabus_pages";
    private static final String COLUMN_SYLLABUS_LINK = "syllabus_link";

    private static final String COLUMN_Q_PAPERS_ID = "q_papers_id";
    private static final String COLUMN_Q_PAPERS_TITLE = "q_papers_title";
    private static final String COLUMN_Q_PAPERS_MAJOR_MINOR = "q_papers_major_minor";
    private static final String COLUMN_Q_PAPERS_LINK = "q_papers_link";
    private static final String COLUMN_Q_PAPERS_CREDITS = "q_papers_credits";
    private static final String COLUMN_Q_PAPERS_PDF_IMAGE = "q_papers_pdf_image";
    private static final String COLUMN_Q_PAPERS_TIMESTAMP = "q_papers_timestamp";

    private static final String COLUMN_COMMITTEES_ID = "committees_id";
    private static final String COLUMN_COMMITTEES_NAME = "committees_name";
    private static final String COLUMN_COMMITTEES_DETAILS = "committees_details";
    private static final String COLUMN_COMMITTEES_COVER_IMAGE = "committees_cover_image";
    private static final String COLUMN_COMMITTEES_DETAILS_IMAGE = "committees_details_image";
    private static final String COLUMN_COMMITTEES_CONTACT = "committees_contact";
    private static final String COLUMN_COMMITTEES_ABOUT_TEAM = "committees_about_team";
    private static final String COLUMN_COMMITTEES_WEB_PAGE_LINK = "committees_web_page_link";
    private static final String COLUMN_COMMITTEES_OTHER_INFO = "committees_other_info";

    private static final String COLUMN_FACULTY_ID = "faculty_id";
    private static final String COLUMN_FACULTY_NAME = "faculty_name";
    private static final String COLUMN_FACULTY_BRANCH = "faculty_branch";
    private static final String COLUMN_FACULTY_POSITION = "faculty_position";
    private static final String COLUMN_FACULTY_MAIL = "faculty_mail";
    private static final String COLUMN_FACULTY_CONTACT = "faculty_contact";
    private static final String COLUMN_FACULTY_CV_LINK = "faculty_cv_link";

    private static final String COLUMN_WORKSHOPS_NAME = "workshops_name";
    private static final String COLUMN_WORKSHOPS_CONTENT_LINK = "workshops_content_link";
    private static final String COLUMN_WORKSHOPS_SAMPLE1LINK = "workshops_sample1_link";
    private static final String COLUMN_WORKSHOPS_SAMPLE2LINK = "workshops_sample2_link";
    private static final String COLUMN_WORKSHOPS_LOGO = "workshops_logo";

    private static final String COLUMN_NOTIFICATIONS_ID = "notifications_id";
    private static final String COLUMN_NOTIFICATIONS_TITLE = "notifications_title";
    private static final String COLUMN_NOTIFICATIONS_IMAGE = "notifications_image";
    private static final String COLUMN_NOTIFICATIONS_DETAILS = "notifications_details";
    private static final String COLUMN_NOTIFICATIONS_TIMESTAMP = "notifications_timestamp";

    private static final String COLUMN_WIFI_USER_NAME = "wifi_user_name";
    private static final String COLUMN_WIFI_PASSWORD = "wifi_password";

    public static final String CREATE_TABLE_PROFILE =
            "CREATE TABLE " + TABLE_PROFILE + "("
                    + COLUMN_PROFILE_ID + " INTEGER,"
                    + COLUMN_PROFILE_NAME + " TEXT,"
                    + COLUMN_PROFILE_BRANCH_SEM + " TEXT,"
                    + COLUMN_PROFILE_EMAIL + " TEXT,"
                    + COLUMN_PROFILE_O_EMAIL + " TEXT"
                    + ")";

    public static final String CREATE_TABLE_SYLLABUS =
            "CREATE TABLE " + TABLE_SYLLABUS + "("
                    + COLUMN_SYLLABUS_BRANCH_SEM + " TEXT PRIMARY KEY,"
                    + COLUMN_SYLLABUS_SUBJECTS + " TEXT,"
                    + COLUMN_SYLLABUS_PAGES + " TEXT,"
                    + COLUMN_SYLLABUS_LINK + " TEXT"
                    + ")";

    public static final String CREATE_TABLE_Q_PAPERS =
            "CREATE TABLE " + TABLE_Q_PAPERS + "("
                    + COLUMN_Q_PAPERS_ID + " INTEGER,"
                    + COLUMN_Q_PAPERS_TITLE + " TEXT,"
                    + COLUMN_Q_PAPERS_MAJOR_MINOR + " INTEGER,"
                    + COLUMN_Q_PAPERS_LINK + " TEXT,"
                    + COLUMN_Q_PAPERS_CREDITS + " TEXT,"
                    + COLUMN_Q_PAPERS_PDF_IMAGE + " INTEGER,"
                    + COLUMN_Q_PAPERS_TIMESTAMP + " TEXT"
                    + ")";

    public static final String CREATE_TABLE_COMMITTEES =
            "CREATE TABLE " + TABLE_COMMITTEES + "("
                    + COLUMN_COMMITTEES_ID + " INTEGER,"
                    + COLUMN_COMMITTEES_NAME + " TEXT,"
                    + COLUMN_COMMITTEES_DETAILS + " TEXT,"
                    + COLUMN_COMMITTEES_COVER_IMAGE + " INTEGER,"
                    + COLUMN_COMMITTEES_DETAILS_IMAGE + " TEXT,"
                    + COLUMN_COMMITTEES_CONTACT + " TEXT,"
                    + COLUMN_COMMITTEES_ABOUT_TEAM + " TEXT,"
                    + COLUMN_COMMITTEES_WEB_PAGE_LINK + " TEXT,"
                    + COLUMN_COMMITTEES_OTHER_INFO + " TEXT"
                    + ")";

    public static final String CREATE_TABLE_FACULTY =
            "CREATE TABLE " + TABLE_FACULTY + "("
                    + COLUMN_FACULTY_ID + " INTEGER,"
                    + COLUMN_FACULTY_NAME + " TEXT,"
                    + COLUMN_FACULTY_BRANCH + " TEXT,"
                    + COLUMN_FACULTY_POSITION + " TEXT,"
                    + COLUMN_FACULTY_MAIL + " TEXT,"
                    + COLUMN_FACULTY_CONTACT + " TEXT,"
                    + COLUMN_FACULTY_CV_LINK + " TEXT"
                    + ")";

    public static final String CREATE_TABLE_WORKSHOPS =
            "CREATE TABLE " + TABLE_WORKSHOPS + "("
                    + COLUMN_WORKSHOPS_NAME + " TEXT,"
                    + COLUMN_WORKSHOPS_CONTENT_LINK + " TEXT,"
                    + COLUMN_WORKSHOPS_SAMPLE1LINK + " TEXT,"
                    + COLUMN_WORKSHOPS_SAMPLE2LINK + " TEXT,"
                    + COLUMN_WORKSHOPS_LOGO + " TEXT"
                    + ")";

    public static final String CREATE_TABLE_NOTIFICATIONS =
            "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                    + COLUMN_NOTIFICATIONS_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_NOTIFICATIONS_TITLE + " TEXT,"
                    + COLUMN_NOTIFICATIONS_IMAGE + " TEXT,"
                    + COLUMN_NOTIFICATIONS_DETAILS + " TEXT,"
                    + COLUMN_NOTIFICATIONS_TIMESTAMP + " TEXT"
                    + ")";

    public static final String CREATE_TABLE_WIFI_USERS =
            "CREATE TABLE " + TABLE_WIFI_USERS + "("
                    + COLUMN_WIFI_USER_NAME + " TEXT PRIMARY KEY,"
                    + COLUMN_WIFI_PASSWORD + " TEXT"
                    + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PROFILE);
        db.execSQL(CREATE_TABLE_SYLLABUS);
        db.execSQL(CREATE_TABLE_Q_PAPERS);
        db.execSQL(CREATE_TABLE_COMMITTEES);
        db.execSQL(CREATE_TABLE_FACULTY);
        db.execSQL(CREATE_TABLE_WORKSHOPS);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
        db.execSQL(CREATE_TABLE_WIFI_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYLLABUS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Q_PAPERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMITTEES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FACULTY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKSHOPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIFI_USERS);

        onCreate(db);
    }

    public void insertProfile(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_ID, 1);
        values.put(COLUMN_PROFILE_NAME, user.getName());
        values.put(COLUMN_PROFILE_BRANCH_SEM, user.getBranchSem());
        values.put(COLUMN_PROFILE_EMAIL, user.getEmail());
        values.put(COLUMN_PROFILE_O_EMAIL, user.getoEmail());

        db.insert(TABLE_PROFILE, null, values);
    }

    public User getProfile() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PROFILE,
                new String[]{COLUMN_PROFILE_NAME, COLUMN_PROFILE_BRANCH_SEM, COLUMN_PROFILE_EMAIL, COLUMN_PROFILE_O_EMAIL},
                COLUMN_PROFILE_ID + "=?",
                new String[]{String.valueOf(1)}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();

            User user = new User();
            user.setName(cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_NAME)));
            user.setBranchSem((cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_BRANCH_SEM))));
            user.setEmail((cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_EMAIL))));
            user.setoEmail((cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_O_EMAIL))));
            cursor.close();
            return user;
        }
        return null;
    }

    public void updateProfile(String name, String branchSem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_NAME, name);
        values.put(COLUMN_PROFILE_BRANCH_SEM, branchSem);
        db.update(TABLE_PROFILE, values, COLUMN_PROFILE_ID + " =?", new String[]{String.valueOf(1)});
        db.close();
    }

    public int updateFullProfile(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_NAME, user.getName());
        values.put(COLUMN_PROFILE_EMAIL, user.getEmail());
        values.put(COLUMN_PROFILE_BRANCH_SEM, user.getBranchSem());
        values.put(COLUMN_PROFILE_O_EMAIL, user.getoEmail());

        return db.update(TABLE_PROFILE, values, COLUMN_PROFILE_ID + " =?", new String[]{String.valueOf(1)});
    }

    public void insertSyllabus(Syllabus syllabus) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SYLLABUS_BRANCH_SEM, syllabus.getBranchSem());
        values.put(COLUMN_SYLLABUS_SUBJECTS, syllabus.getSubjects());
        values.put(COLUMN_SYLLABUS_PAGES, syllabus.getPages());
        values.put(COLUMN_SYLLABUS_LINK, syllabus.getLink());

        db.insert(TABLE_SYLLABUS, null, values);
    }

    public Syllabus getSyllabus(String branch_sem) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SYLLABUS,
                new String[]{COLUMN_SYLLABUS_SUBJECTS, COLUMN_SYLLABUS_PAGES, COLUMN_SYLLABUS_LINK},
                COLUMN_SYLLABUS_BRANCH_SEM + "=?",
                new String[] {branch_sem}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            Syllabus syllabus = new Syllabus(branch_sem, cursor.getString(cursor.getColumnIndex(COLUMN_SYLLABUS_SUBJECTS)), cursor.getString(cursor.getColumnIndex(COLUMN_SYLLABUS_PAGES)), cursor.getString(cursor.getColumnIndex(COLUMN_SYLLABUS_LINK)));
            cursor.close();
            return syllabus;
        }
        return null;
    }

    public String getSubjects(String branch_sem) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SYLLABUS,
                new String[]{COLUMN_SYLLABUS_SUBJECTS},
                COLUMN_SYLLABUS_BRANCH_SEM + "=?",
                new String[] {branch_sem}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            String subjects = cursor.getString(cursor.getColumnIndex(COLUMN_SYLLABUS_SUBJECTS));
            cursor.close();
            return subjects;
        }
        return null;
    }

    public int updateSyllabus(Syllabus syllabus) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SYLLABUS_SUBJECTS, syllabus.getSubjects());
        values.put(COLUMN_SYLLABUS_PAGES, syllabus.getPages());
        values.put(COLUMN_SYLLABUS_LINK, syllabus.getLink());

        return db.update(TABLE_SYLLABUS, values, COLUMN_SYLLABUS_BRANCH_SEM + " = ?", new String[]{syllabus.getBranchSem()});
    }

    public void insertQuestionPaper(QuestionPaper questionPaper) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_Q_PAPERS_ID, questionPaper.getId());
        values.put(COLUMN_Q_PAPERS_TITLE, questionPaper.getTitle());
        values.put(COLUMN_Q_PAPERS_LINK, questionPaper.getLink());
        values.put(COLUMN_Q_PAPERS_MAJOR_MINOR, questionPaper.getMajorMinor());
        values.put(COLUMN_Q_PAPERS_CREDITS, questionPaper.getCredits());
        values.put(COLUMN_Q_PAPERS_PDF_IMAGE, questionPaper.getPdfImage());
        values.put(COLUMN_Q_PAPERS_TIMESTAMP, questionPaper.getTimestamp());

        db.insert(TABLE_Q_PAPERS, null, values);
    }

    public ArrayList<QuestionPaper> getQuestionPapers(String title) {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<QuestionPaper> papersList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_Q_PAPERS,
                new String[]{COLUMN_Q_PAPERS_ID, COLUMN_Q_PAPERS_LINK, COLUMN_Q_PAPERS_MAJOR_MINOR, COLUMN_Q_PAPERS_CREDITS, COLUMN_Q_PAPERS_PDF_IMAGE, COLUMN_Q_PAPERS_TIMESTAMP},
                COLUMN_Q_PAPERS_TITLE + "=?",
                new String[] {title}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                QuestionPaper questionPaper = new QuestionPaper(cursor.getInt(cursor.getColumnIndex(COLUMN_Q_PAPERS_ID)), title, cursor.getString(cursor.getColumnIndex(COLUMN_Q_PAPERS_LINK)), cursor.getInt(cursor.getColumnIndex(COLUMN_Q_PAPERS_MAJOR_MINOR)), cursor.getString(cursor.getColumnIndex(COLUMN_Q_PAPERS_CREDITS)), cursor.getInt(cursor.getColumnIndex(COLUMN_Q_PAPERS_PDF_IMAGE)), cursor.getString(cursor.getColumnIndex(COLUMN_Q_PAPERS_TIMESTAMP)));
                papersList.add(questionPaper);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return papersList;
    }

    public int updateQuestionPaper(QuestionPaper questionPaper) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_Q_PAPERS_TITLE, questionPaper.getTitle());
        values.put(COLUMN_Q_PAPERS_LINK, questionPaper.getLink());
        values.put(COLUMN_Q_PAPERS_MAJOR_MINOR, questionPaper.getMajorMinor());
        values.put(COLUMN_Q_PAPERS_CREDITS, questionPaper.getCredits());
        values.put(COLUMN_Q_PAPERS_PDF_IMAGE, questionPaper.getPdfImage());
        values.put(COLUMN_Q_PAPERS_TIMESTAMP, questionPaper.getTimestamp());

        return db.update(TABLE_Q_PAPERS, values, COLUMN_Q_PAPERS_ID + " =? AND " + COLUMN_Q_PAPERS_TITLE + " =?", new String[]{String.valueOf(questionPaper.getId()), questionPaper.getTitle()});
    }

    public void insertCommittee(Committee committee) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_COMMITTEES_ID, committee.getId());
        values.put(COLUMN_COMMITTEES_NAME, committee.getName());
        values.put(COLUMN_COMMITTEES_DETAILS, committee.getDetails());
        values.put(COLUMN_COMMITTEES_COVER_IMAGE, committee.getCoverImage());
        values.put(COLUMN_COMMITTEES_DETAILS_IMAGE, committee.getDetailsImage());
        values.put(COLUMN_COMMITTEES_CONTACT, committee.getContact());
        values.put(COLUMN_COMMITTEES_ABOUT_TEAM, committee.getAboutTeam());
        values.put(COLUMN_COMMITTEES_WEB_PAGE_LINK, committee.getWebPageLink());
        values.put(COLUMN_COMMITTEES_OTHER_INFO, committee.getOtherInfo());

        db.insert(TABLE_COMMITTEES, null, values);
    }

    public Committee getCommittee(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_COMMITTEES,
                new String[]{COLUMN_COMMITTEES_ID, COLUMN_COMMITTEES_DETAILS, COLUMN_COMMITTEES_COVER_IMAGE, COLUMN_COMMITTEES_DETAILS_IMAGE, COLUMN_COMMITTEES_CONTACT, COLUMN_COMMITTEES_ABOUT_TEAM, COLUMN_COMMITTEES_WEB_PAGE_LINK, COLUMN_COMMITTEES_OTHER_INFO},
                COLUMN_COMMITTEES_NAME + "=?",
                new String[] {name}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            Committee committee = new Committee(cursor.getInt(cursor.getColumnIndex(COLUMN_COMMITTEES_ID)), name, cursor.getString(cursor.getColumnIndex(COLUMN_COMMITTEES_DETAILS)), cursor.getString(cursor.getColumnIndex(COLUMN_COMMITTEES_COVER_IMAGE)), cursor.getString(cursor.getColumnIndex(COLUMN_COMMITTEES_DETAILS_IMAGE)), cursor.getString(cursor.getColumnIndex(COLUMN_COMMITTEES_CONTACT)), cursor.getString(cursor.getColumnIndex(COLUMN_COMMITTEES_ABOUT_TEAM)), cursor.getString(cursor.getColumnIndex(COLUMN_COMMITTEES_WEB_PAGE_LINK)), cursor.getString(cursor.getColumnIndex(COLUMN_COMMITTEES_OTHER_INFO)));
            cursor.close();
            return committee;
        }
        return null;
    }

    public ArrayList<Committee> getAllCommittees() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Committee> committeesList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_COMMITTEES,
                new String[]{COLUMN_COMMITTEES_ID, COLUMN_COMMITTEES_NAME, COLUMN_COMMITTEES_COVER_IMAGE},
                null, null, null, null, COLUMN_COMMITTEES_NAME + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                Committee committee = new Committee(cursor.getInt(cursor.getColumnIndex(COLUMN_COMMITTEES_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_COMMITTEES_NAME)), null, cursor.getString(cursor.getColumnIndex(COLUMN_COMMITTEES_COVER_IMAGE)), null, null, null, null, null);
                committeesList.add(committee);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return committeesList;
    }

    public int updateCommittee(Committee committee) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_COMMITTEES_NAME, committee.getName());
        values.put(COLUMN_COMMITTEES_DETAILS, committee.getDetails());
        values.put(COLUMN_COMMITTEES_COVER_IMAGE, committee.getCoverImage());
        values.put(COLUMN_COMMITTEES_DETAILS_IMAGE, committee.getDetailsImage());
        values.put(COLUMN_COMMITTEES_CONTACT, committee.getContact());
        values.put(COLUMN_COMMITTEES_ABOUT_TEAM, committee.getAboutTeam());
        values.put(COLUMN_COMMITTEES_WEB_PAGE_LINK, committee.getWebPageLink());
        values.put(COLUMN_COMMITTEES_OTHER_INFO, committee.getOtherInfo());

        return db.update(TABLE_COMMITTEES, values, COLUMN_COMMITTEES_ID + " = ?",
                new String[]{String.valueOf(committee.getId())});
    }

    public int updateCommitteeCoverImage(int id, String coverPath) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_COMMITTEES_COVER_IMAGE, coverPath);

        return db.update(TABLE_COMMITTEES, values, COLUMN_COMMITTEES_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int updateCommitteeDetailsImage(int id, String detailsPath) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_COMMITTEES_DETAILS_IMAGE, detailsPath);

        return db.update(TABLE_COMMITTEES, values, COLUMN_COMMITTEES_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void insertFaculty(Faculty faculty) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FACULTY_ID, faculty.getId());
        values.put(COLUMN_FACULTY_NAME, faculty.getName());
        values.put(COLUMN_FACULTY_BRANCH, faculty.getBranch());
        values.put(COLUMN_FACULTY_POSITION, faculty.getPosition());
        values.put(COLUMN_FACULTY_MAIL, faculty.getMail());
        values.put(COLUMN_FACULTY_CONTACT, faculty.getContact());
        values.put(COLUMN_FACULTY_CV_LINK, faculty.getCvLink());

        db.insert(TABLE_FACULTY, null, values);
    }

    public ArrayList<Faculty> getBranchFaculty(String branch) {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Faculty> facultyList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_FACULTY,
                null,COLUMN_FACULTY_BRANCH + "=?",
                new String[] {branch}, null, null, COLUMN_FACULTY_NAME + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                Faculty faculty = new Faculty(cursor.getInt(cursor.getColumnIndex(COLUMN_FACULTY_ID)), branch, cursor.getString(cursor.getColumnIndex(COLUMN_FACULTY_NAME)), cursor.getString(cursor.getColumnIndex(COLUMN_FACULTY_POSITION)), cursor.getString(cursor.getColumnIndex(COLUMN_FACULTY_MAIL)), cursor.getString(cursor.getColumnIndex(COLUMN_FACULTY_CONTACT)), cursor.getString(cursor.getColumnIndex(COLUMN_FACULTY_CV_LINK)));
                facultyList.add(faculty);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return facultyList;
    }

    public int updateFaculty(Faculty faculty) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FACULTY_NAME, faculty.getName());
        values.put(COLUMN_FACULTY_BRANCH, faculty.getBranch());
        values.put(COLUMN_FACULTY_POSITION, faculty.getPosition());
        values.put(COLUMN_FACULTY_MAIL, faculty.getMail());
        values.put(COLUMN_FACULTY_CONTACT, faculty.getContact());
        values.put(COLUMN_FACULTY_CV_LINK, faculty.getCvLink());

        return db.update(TABLE_FACULTY, values, COLUMN_FACULTY_ID + " = ?", new String[]{String.valueOf(faculty.getId())});
    }

    public void insertWorkshop(Workshop workshop) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_WORKSHOPS_NAME, workshop.getName());
        values.put(COLUMN_WORKSHOPS_CONTENT_LINK, workshop.getContentLink());
        values.put(COLUMN_WORKSHOPS_SAMPLE1LINK, workshop.getSample1Link());
        values.put(COLUMN_WORKSHOPS_SAMPLE2LINK, workshop.getSample2Link());
        values.put(COLUMN_WORKSHOPS_LOGO, workshop.getLogo());

        db.insert(TABLE_WORKSHOPS, null, values);
    }

    public ArrayList<Workshop> getAllWorkshops() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Workshop> workshopList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_WORKSHOPS, null,null, null, null, null, COLUMN_WORKSHOPS_NAME + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                Workshop workshop = new Workshop(cursor.getString(cursor.getColumnIndex(COLUMN_WORKSHOPS_NAME)), cursor.getString(cursor.getColumnIndex(COLUMN_WORKSHOPS_CONTENT_LINK)), cursor.getString(cursor.getColumnIndex(COLUMN_WORKSHOPS_SAMPLE1LINK)), cursor.getString(cursor.getColumnIndex(COLUMN_WORKSHOPS_SAMPLE2LINK)), cursor.getString(cursor.getColumnIndex(COLUMN_WORKSHOPS_LOGO)));
                workshopList.add(workshop);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return workshopList;
    }

    public int updateWorkshop(Workshop workshop) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_WORKSHOPS_CONTENT_LINK, workshop.getContentLink());
        values.put(COLUMN_WORKSHOPS_SAMPLE1LINK, workshop.getSample1Link());
        values.put(COLUMN_WORKSHOPS_SAMPLE2LINK, workshop.getSample2Link());
        values.put(COLUMN_WORKSHOPS_LOGO, workshop.getLogo());

        return db.update(TABLE_WORKSHOPS, values, COLUMN_WORKSHOPS_NAME + " = ?", new String[]{workshop.getName()});
    }

    public void insertNotification(Timeline timeline) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATIONS_ID, timeline.getId());
        values.put(COLUMN_NOTIFICATIONS_TITLE, timeline.getTitle());
        values.put(COLUMN_NOTIFICATIONS_IMAGE, timeline.getImage());
        values.put(COLUMN_NOTIFICATIONS_DETAILS, timeline.getDetails());
        values.put(COLUMN_NOTIFICATIONS_TIMESTAMP, timeline.getTimestamp());

        db.insert(TABLE_NOTIFICATIONS, null, values);
    }

    public Timeline getNotification(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NOTIFICATIONS, null,COLUMN_NOTIFICATIONS_ID + "=?", new String[] {String.valueOf(id)}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            Timeline timeline = new Timeline(cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_TITLE)),cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_IMAGE)), cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_DETAILS)), cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_TIMESTAMP)), null);
            cursor.close();
            return timeline;
        }
        return null;
    }

    public ArrayList<Timeline> getAllNotifications() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Timeline> timelineList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NOTIFICATIONS, null,null, null, null, null, COLUMN_NOTIFICATIONS_ID + " DESC",null);

        if (cursor.moveToFirst()) {
            do {
                Timeline timeline = new Timeline(cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_TITLE)),cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_IMAGE)), cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_DETAILS)), cursor.getString(cursor.getColumnIndex(COLUMN_NOTIFICATIONS_TIMESTAMP)), null);
                timelineList.add(timeline);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return timelineList;
    }

    public int updateNotification(Timeline timeline) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATIONS_ID, timeline.getId());
        values.put(COLUMN_NOTIFICATIONS_TITLE, timeline.getTitle());
        values.put(COLUMN_NOTIFICATIONS_IMAGE, timeline.getImage());
        values.put(COLUMN_NOTIFICATIONS_DETAILS, timeline.getDetails());
        values.put(COLUMN_NOTIFICATIONS_TIMESTAMP, timeline.getTimestamp());

        return db.update(TABLE_NOTIFICATIONS, values, COLUMN_NOTIFICATIONS_ID + " = ?",
                new String[]{String.valueOf(timeline.getId())});
    }

    public int getProfileCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PROFILE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void insertWifiUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_WIFI_USER_NAME, username);
        values.put(COLUMN_WIFI_PASSWORD, password);

        db.insert(TABLE_WIFI_USERS, null, values);
    }


    public void deleteWifiUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_WIFI_USERS, COLUMN_WIFI_USER_NAME  + " = ?", new String[]{username});
        db.close();
    }

    public int updateWifiUser(String oldUsername, String newUsername, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_WIFI_USER_NAME, newUsername);
        values.put(COLUMN_WIFI_PASSWORD, newPassword);

        return db.update(TABLE_WIFI_USERS, values, COLUMN_WIFI_USER_NAME + " = ?", new String[]{oldUsername});
    }

    public String getPasswordFromUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_WIFI_USERS, null,COLUMN_WIFI_USER_NAME + "=?", new String[] {username}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            String password = cursor.getString(cursor.getColumnIndex(COLUMN_WIFI_PASSWORD));
            cursor.close();
            return password;
        }
        return null;
    }

    public ArrayList<String> getAllWifiUsers() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<String> usersList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_WIFI_USERS, null,null, null, null, null, null,null);

        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndex(COLUMN_WIFI_USER_NAME));
                usersList.add(username);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return usersList;
    }

    public int getRowCount(String table_name) {
        String countQuery = "SELECT  * FROM " + table_name;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public boolean checkTableExistence(String table_name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", table_name});
        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }
}
