package pangtourista.project.OperationRetrofitApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String BASE_URL = "http://192.168.100.18/pang_tourista/users/";
    //https://pangtourista.website/users/
    private static Retrofit retrofit  = null;
    public static Retrofit getApiClient()
    {
        if(retrofit == null)
        {
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
    return retrofit;
    }
}
