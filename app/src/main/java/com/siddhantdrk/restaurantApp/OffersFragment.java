package com.siddhantdrk.restaurantApp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.siddhantdrk.restaurantApp.models.Coupon;
import com.siddhantdrk.restaurantApp.models.Response;
import com.siddhantdrk.restaurantApp.network.networkUtils;
import com.siddhantdrk.restaurantApp.utils.CustomSwipeRefreshLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class OffersFragment extends Fragment {

    ViewPager2 viewPager2;
    private CompositeSubscription mSubscriptions;
    private ProgressDialog progressDialog;
    private LinearLayout internet;
    TabLayout offers_tabs;
    private Button retry;
    private CustomSwipeRefreshLayout customSwipeRefreshLayout;
    private SharedPreferences mSharedPreferences;


    public OffersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_offers, container, false);

        viewPager2 = rootView.findViewById(R.id.offers_viewPager);
        offers_tabs = rootView.findViewById(R.id.offers_tabs);
        internet = rootView.findViewById(R.id.no_internet_container);
        retry = rootView.findViewById(R.id.retry_btn);
        mSubscriptions = new CompositeSubscription();
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        if (mSharedPreferences.getString("offer", null) == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_loading);
            Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
            FETCH_COUPONS();
        }
        else
        {
            Type type=new TypeToken<List<Coupon>>(){}.getType();
            Gson gson=new Gson();

           List<Coupon> couponList=gson.fromJson(mSharedPreferences.getString("offer", null),type);
            viewPager2.setVisibility(View.VISIBLE);
            internet.setVisibility(View.GONE);
            viewPager2.setAdapter(new OfferFragmentStateAdapter(this,couponList));
            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                    offers_tabs, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    if (position == 0) {
                        tab.setText("Restaurant Offers");
                    } else {
                        tab.setText("Payment offers/coupons");
                    }
                }
            }
            );
            tabLayoutMediator.attach();
        }


        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(getContext());
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_loading);
                Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

                FETCH_COUPONS();
            }
        });

        customSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        customSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (customSwipeRefreshLayout.isRefreshing()) {
                    if (isOnline()) {
                        progressDialog = new ProgressDialog(getContext());
                        progressDialog.show();
                        progressDialog.setContentView(R.layout.progress_loading);
                        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                        FETCH_COUPONS();
                        customSwipeRefreshLayout.setRefreshing(false);
                    } else {
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "No response!! please check your\nInternet Connectivity", Toast.LENGTH_SHORT).show();
                                customSwipeRefreshLayout.setRefreshing(false);
                            }
                        }, 1000);
                    }
                }

            }
        });


        return rootView;
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    private void FETCH_COUPONS() {

        mSubscriptions.add(networkUtils.getRetrofit()
                .GET_COUPONS()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(List<Coupon> response) {

        progressDialog.dismiss();
        List<Coupon> newresponse=new ArrayList<>();

        Date d1=new Date();

        for(Coupon x:response)
        { long diff= (d1.getTime()-x.getExpiry().getTime())/1000;
            if(diff<0)
            {
                newresponse.add(x);
            }
        }

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getContext());

        Gson gson = new Gson();

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String banner = gson.toJson(newresponse);
        editor.putString("offer",banner);
        editor.apply();

        viewPager2.setVisibility(View.VISIBLE);
        internet.setVisibility(View.GONE);
        viewPager2.setAdapter(new OfferFragmentStateAdapter(this,newresponse));
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                offers_tabs, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    tab.setText("Restaurant Offers");
                } else {
                    tab.setText("Payment offers/coupons");
                }
            }
        }
        );
        tabLayoutMediator.attach();




    }

    private void handleError(Throwable error) {

        progressDialog.dismiss();

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            viewPager2.setVisibility(View.GONE);
            internet.setVisibility(View.VISIBLE);


        }
    }
}