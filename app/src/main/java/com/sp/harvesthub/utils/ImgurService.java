package com.sp.harvesthub.utils;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImgurService {
    @Headers({
        "Authorization: Client-ID 2c0e07c56d5af3e"  // This is a public client ID for testing
    })
    @Multipart
    @POST("3/image")
    Call<ImageResponse> uploadImage(@Part MultipartBody.Part image);
} 