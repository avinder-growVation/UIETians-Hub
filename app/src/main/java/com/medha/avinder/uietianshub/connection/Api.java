package com.medha.avinder.uietianshub.connection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.medha.avinder.uietianshub.models.Committee;
import com.medha.avinder.uietianshub.models.Gallery;
import com.medha.avinder.uietianshub.models.QuestionPaper;
import com.medha.avinder.uietianshub.models.Syllabus;
import com.medha.avinder.uietianshub.models.Timeline;
import com.medha.avinder.uietianshub.models.User;
import com.medha.avinder.uietianshub.utils.Tools;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Api {

    @GET("/App Version.json")
    Call<Long> getAppVersion();

    @GET("/Content Versions.json")
    Call<Map<String, String>> getContentVersions();

    @GET("/Syllabus/{branch_sem}.json")
    Call<Syllabus> getSyllabus(@Path("branch_sem") String branch_sem);

    @GET("/Syllabus/{branch_sem}/subjects.json")
    Call<String> getSubjects(@Path("branch_sem") String branch_sem);

    @GET("/Question Papers/{title}.json")
    Call<JsonArray> getQuestionPapers(@Path("title") String title);

    @GET("/Committees.json")
    Call<JsonArray> getCommittees();

    @GET("/Faculty.json")
    Call<JsonArray> getFaculty();

    @GET("/Workshops.json")
    Call<JsonArray> getWorkshops();

    @GET("/Workshop Images/{name}.json")
    Call<JsonArray> getWorkshopImages(@Path("name") String name);

    @GET("/Gallery.json")
    Call<JsonArray> getGallery();

    @GET("/Notices.json")
    Call<JsonArray> getNotifications();

    @POST("/Uploads/Committees.json")
    Call<Committee> insertCommittee(@Body Committee committee);

    @POST("/Uploads/Question Papers.json")
    Call<QuestionPaper> insertQuestionPaper(@Body QuestionPaper questionPaper);

    @POST("/Uploads/Gallery.json")
    Call<Gallery> insertGallery(@Body Gallery gallery);

    @POST("/Uploads/Notifications.json")
    Call<Timeline> insertNotification(@Body Timeline timeline);

    @PUT("/Locations/{email}.json")
    Call<Boolean> setLocation(@Path("email") String email, @Body Map<String, String> map);

    @PUT("/Users/{email}.json")
    Call<User> insertUser(@Path("email") String email, @Body User user);

    @GET("/Users/{email}.json")
    Call<User> getUser(@Path("email") String email);

    @PUT("/Users/{email}/token.json")
    Call<Boolean> setToken(@Path("email") String email, @Body String s2);

    @GET("/Users/masteruhcom/token.json")
    Call<String> getMasterToken();


    @Headers({"Authorization: key=AAAA9i9Z6Xw:APA91bEZl1DcDrVqAm7PJ_OIL0yXxg-X2RCS0N_BYftlLXLpkdiJpBMHHivXvOtV_4PPMGOugMS3fE43-Ond1DqpAJ3XEaZB8v9Yf8XhjN_N7aLI6nmXv_p55GST4frSNgWeo5d8Tk4F",
            "Content-Type:application/json"})
    @POST("fcm/send")
    Call<ResponseBody> sendDataNotification(@Body Tools.RequestDataMessage requestDataNotification);

    @GET("/Banner Image.json")
    Call<String> getBannerImage();

    @GET("/Users.json")
    Call<JsonObject> getUsers();

}
