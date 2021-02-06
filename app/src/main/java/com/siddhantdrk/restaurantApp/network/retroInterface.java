package com.siddhantdrk.restaurantApp.network;

import com.siddhantdrk.restaurantApp.FoodList.SubItem;
import com.siddhantdrk.restaurantApp.models.Address;
import com.siddhantdrk.restaurantApp.models.AddressResponse;
import com.siddhantdrk.restaurantApp.models.Coupon;
import com.siddhantdrk.restaurantApp.models.FinalOrder;
import com.siddhantdrk.restaurantApp.models.Food_Response;
import com.siddhantdrk.restaurantApp.models.MyOrderResponse;
import com.siddhantdrk.restaurantApp.models.Response;
import com.siddhantdrk.restaurantApp.models.Token;
import com.siddhantdrk.restaurantApp.models.User;
import com.siddhantdrk.restaurantApp.models.banners;

import org.json.JSONObject;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Observable;

public interface retroInterface {

    @GET("banner")
    Observable<List<banners>> BANNERS();

    @GET("coupons")
    Observable<List<Coupon>> Coupons();

    @POST("auth/register")
    Observable<Response> register(@Body User user);

    @POST("auth/register_otp")
    Observable<Response> REGISTER_OTP(@Body User user);

    @POST("auth/number_otp")
    Observable<Response> NUMBER_OTP(@Body User user);

    @POST("auth/login")
    Observable<Response> login();

    @POST("auth/number_login")
    Observable<Response> NUMBER_LOGIN(@Body User user);

    @POST("auth/google_login")
    Observable<Response> GOOGLE_LOGIN(@Body JSONObject jsonObject);

    @POST("auth/facebook_login")
    Observable<Response> FACEBOOK_LOGIN(@Body JSONObject jsonObject);

    @POST("auth/google_otp")
    Observable<Response> GOOGLE_OTP(@Body User user);

    @POST("auth/number_save")
    Observable<Response> SAVE_NUMBER(@Body User user);

    @POST("auth/reset_password_otp")
    Observable<Response> RESET_PASSWORD_OTP(@Body User user);

    @POST("auth/new_password")
    Observable<Response> SET_PASSWORD(@Body User user);

    @POST("auth/profile_details")
    Observable<Response> PROFILE_DETAILS(@Body User user);

    @GET("food")
    Observable<Food_Response> GET_FOOD_LIST(@Query("category") String category);

    @GET("address")
    Observable<AddressResponse> GET_ADDRESS(@Query("mobile") String mobile);

    @POST("address")
    Observable<Response> SAVE_ADDRESS(@Body Address address);

    @POST("get_payment_token")
    Observable<Token> GET_TOKEN(@Query("mid") String mid, @Query("orderId") String orderId, @Query("amount") String amount,@Body FinalOrder finalOrder);

    @POST("offline_order")
    Observable<Response> PLACE_OFFLINE_ORDER(@Body FinalOrder finalOrder);

    @POST("online_order")
    Observable<Response> PLACE_ONLINE_ORDER(@Body FinalOrder finalOrder,@Query("mid") String mid,@Query("orderId") String orderId);

    @DELETE("address")
    Observable<Response> DELETE_ADDRESS(@Query("id") String id,@Query("mobile_no") String mobile_no,@Query("position") int i);

    @GET("my_orders")
    Observable<List<MyOrderResponse>> GET_TRANSACTIONS(@Query("mobile_no") String mobile);

    @GET("my_coupons")
    Observable<List<Coupon>> GET_COUPONS();

    @PUT("cancel_offline_order")
    Observable<Response> CANCEL_OFFLINE_ORDER(@Query("id") String id,@Query("orderId") String orderId);

    @PUT("cancel_online_order")
    Observable<Response> CANCEL_ONLINE_ORDER(@Query("id") String id,@Query("orderId") String orderId, @Query("mid") String mid,
                                                @Query("tid") String tid,@Query("amount") Double amount,@Query("refundId") String refundId);

    @POST("coupon_available")
    Observable<Response>  CHECK_COUPON_AVAILIBILITY(@Query("mobile_no") String mobile_no, @Query("coupon") String coupon,@Query("saving") Double saving,@Query("limit") int limit,@Query("duration") int duration);

    @GET("item_details")
    Observable<SubItem> GET_ITEM_DETAILS(@Query("id") String id);

    @GET("order_details")
    Observable<MyOrderResponse> GET_ORDER_DETAILS(@Query("id") String id);
}