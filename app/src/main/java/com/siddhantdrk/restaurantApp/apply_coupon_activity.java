package com.siddhantdrk.restaurantApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.siddhantdrk.restaurantApp.Adapters.CouponAdapter;
import com.siddhantdrk.restaurantApp.Adapters.finishActivity;
import com.siddhantdrk.restaurantApp.models.Coupon;
import com.siddhantdrk.restaurantApp.models.FoodCart;
import com.siddhantdrk.restaurantApp.models.Response;
import com.siddhantdrk.restaurantApp.network.networkUtils;
import com.siddhantdrk.restaurantApp.utils.constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class apply_coupon_activity extends AppCompatActivity implements finishActivity {

    private CompositeSubscription mSubscriptions;
    private ProgressDialog progressDialog;
    private RecyclerView rvItem, rvitem2;
    private EditText coupon;
    private TextView apply, valid, others;
    private List<Coupon> coupons;
    private SharedPreferences mSharedPreferences;
    private LinearLayout internet;
    private Button retry;
    private ImageView backBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apply_coupon_activity);
        mSubscriptions = new CompositeSubscription();
        rvItem = findViewById(R.id.coupon_list_view);
        rvitem2 = findViewById(R.id.more_coupons_container);
        coupon = findViewById(R.id.enter_edit_txt);
        apply = findViewById(R.id.apply);
        internet=findViewById(R.id.no_internet_container);
        retry=findViewById(R.id.retry_btn);
        valid=findViewById(R.id.valid);
        others=findViewById(R.id.others);

        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(apply_coupon_activity.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_loading);
                Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

                FETCH_COUPONS();
            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_loading);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        FETCH_COUPONS();

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APPLY();
            }
        });
    }



    private void FETCH_COUPONS() {

        mSubscriptions.add(networkUtils.getRetrofit()
                .GET_COUPONS()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(List<Coupon> response) {

        rvItem.setVisibility(View.VISIBLE);
        rvitem2.setVisibility(View.VISIBLE);
        valid.setVisibility(View.VISIBLE);
        others.setVisibility(View.VISIBLE);
        internet.setVisibility(View.GONE);
        List<Coupon> applicable=new ArrayList<>();
        List<Coupon> others=new ArrayList<>();
        List<Coupon> newresponse=new ArrayList<>();

        Date d1=new Date();

        for(Coupon x:response)
        { long diff= (d1.getTime()-x.getExpiry().getTime())/1000;
            if(diff<0)
            {
                newresponse.add(x);
            }
        }
        coupons= newresponse;
        progressDialog.dismiss();
        Gson gson = new Gson();
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        String cart = mSharedPreferences.getString("cart", null);
        Type type = new TypeToken<ArrayList<FoodCart>>() {
        }.getType();
        ArrayList<FoodCart> foodCart;
        foodCart = gson.fromJson(cart, type);

        assert foodCart != null;
        for(Coupon x: newresponse)
        {int flag=0;
            for(FoodCart y:foodCart)
            {
                if(x.getCategory().equals(y.getFood_category()) && !applicable.contains(x))
                {   flag=1;
                    applicable.add(x);

                }
            }
            if(x.getCategory().equals("all"))
            {   flag=1;
                applicable.add(x);
            }

            if(flag==0)
            {
                others.add(x);
            }


        }





        CouponAdapter couponAdapter=new CouponAdapter(applicable,this,this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvItem.setAdapter(couponAdapter);
        rvItem.setLayoutManager(layoutManager);

        CouponAdapter couponAdapter2=new CouponAdapter(others,this,this);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        rvitem2.setAdapter(couponAdapter2);
        rvitem2.setLayoutManager(layoutManager2);


    }

    private void handleError(Throwable error) {

        progressDialog.dismiss();

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            rvItem.setVisibility(View.GONE);
            rvitem2.setVisibility(View.GONE);
            valid.setVisibility(View.GONE);
            others.setVisibility(View.GONE);
            internet.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void ActivityFinish() {
        finish();
    }

    private void APPLY() {

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String s=coupon.getText().toString();
        if(s.isEmpty())
        {
            alertBox("Enter Valid Coupon!");
        }
        else
        {

            int flag=0;
            int total=0;
            Gson gson = new Gson();
            String cart = mSharedPreferences.getString("cart", null);
            Type type = new TypeToken<ArrayList<FoodCart>>() {
            }.getType();
            ArrayList<FoodCart> foodCart;
            foodCart = gson.fromJson(cart, type);
            assert foodCart != null;
            for(FoodCart x:foodCart)
            {
                total+=x.getPrice()*x.getQuantity();
            }

            for(Coupon x:coupons)
            {
                if(x.getCoupon_name().toLowerCase().equals(s.toLowerCase()))
                {
                    flag=1;
                    if(x.getCategory().equals("all"))
                    {
                        if(total<x.getMin_amount())
                        {
                            alertBox("Coupon not Applied! Bill Amount should be above Rs."+x.getMin_amount());
                        }
                        else {
                            double saving = (total * x.getDiscount()) / 100;
                            saving = saving > x.getMax_discount() ? x.getMax_discount() : saving;
                            String coupon_name = x.getCoupon_name();
                            CouponsUsed(saving,coupon_name,x.getLimit(),x.getDuration());

                            break;
                        }

                    }
                    else
                    {int flag2=0;
                        for(FoodCart y:foodCart)
                        {
                            if(y.getFood_category().equals(x.getCategory()))
                            {flag2=1;
                                if((y.getQuantity()*y.getPrice())<x.getMin_amount())
                                {
                                    alertBox("Coupon not Applied! "+x.getCategory()+" Items should Price above Rs."+x.getMin_amount());
                                }
                                else
                                {
                                    double saving=(total*x.getDiscount())/100;
                                    saving=saving>x.getMax_discount()?x.getMax_discount():saving;
                                    String coupon_name=x.getCoupon_name();
                                    CouponsUsed(saving,coupon_name,x.getLimit(),x.getDuration());


                                }

                            }
                        }
                        if(flag2==0)
                        {
                            alertBox("Coupon not Applied! Cart doesn't contain "+x.getCategory()+" Items");
                            break;
                        }
                    }




                }
            }

            if(flag==0)
            {
                alertBox("Coupon is not valid/expired !");
            }


        }
    }


    private void  alertBox(String message)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("oK",null);
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    private void OfferBox(double saving, String coupon)
    {
        LayoutInflater layoutInflater= LayoutInflater.from(this);
        final View offerView=layoutInflater.inflate(R.layout.coupon,null);

        final TextView savings=offerView.findViewById(R.id.saving);
        final TextView offername=offerView.findViewById(R.id.offer);
        String s="₹"+ saving;
        savings.setText(s);
        offername.setText(coupon);


        progressDialog=new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(offerView);

        new CountDownTimer(2000,1000){
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
               Intent output=new Intent();
               setResult(RESULT_OK,output);
               finish();
            }
        }.start();

    }

    private void CouponsUsed(Double saving,String coupon_name,int limit,int duration)
    {

        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_loading);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        mSubscriptions.add(networkUtils.getRetrofit(mSharedPreferences.getString(constants.TOKEN, null))
                .CHECK_COUPON_AVAILIBILITY(mSharedPreferences.getString(constants.PHONE, null),coupon_name,saving,limit,duration)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse2,this::handleError2));
    }

    private void handleResponse2(Response response) {

        progressDialog.dismiss();
        if(response.getAvailable())
        {
            OfferBox(response.getSaving(),response.getCoupon_name());
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("discount", String.valueOf(response.getSaving()));
            editor.putString("coupon_name", String.valueOf(response.getCoupon_name()));
            editor.apply();
        }
        else {
            alertBox(response.getMessage());
        }


    }

    private void handleError2(Throwable error) {

        progressDialog.dismiss();

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
            Log.e("error",error.toString());

        }
    }

}
