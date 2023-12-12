package pangtourista.project.OperationRetrofitApi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("email_registration.php")
    Call<Users> performEmailRegistration(
            @Query("user_name") String user_name,
            @Query("user_email") String user_email,
            @Query("user_sex") String user_sex,
            @Query("user_photo") String user_photo,
            @Query("user_password") String user_password
    );

    @GET("email_login.php")
    Call<Users> performEmailLogin(
            @Query("user_email_or_phone_number") String user_email_or_phone_number,
            @Query("user_password") String user_password
    );

}
