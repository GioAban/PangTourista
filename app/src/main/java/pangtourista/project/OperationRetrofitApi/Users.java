package pangtourista.project.OperationRetrofitApi;

import com.google.gson.annotations.SerializedName;

public class Users {

    @SerializedName("response")
    private String Response;

    @SerializedName("user_id")
    private String UserId;

    public String getResponse() {
        return Response;
    }

    public String getUserId() {
        return UserId;
    }
}
