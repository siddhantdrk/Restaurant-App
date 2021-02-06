package com.siddhantdrk.restaurantApp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.siddhantdrk.restaurantApp.Adapters.CouponsAdapter;
import com.siddhantdrk.restaurantApp.models.Coupon;

import java.util.List;


public class CouponsFragment extends Fragment {

    private RecyclerView rvitem;

    private List<Coupon> coupons;

    public CouponsFragment(List<Coupon> couponList)
    {
        this.coupons=couponList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_coupons, container, false);

        rvitem=view.findViewById(R.id.All_coupons_container);


        CouponsAdapter couponsAdapter=new CouponsAdapter(coupons,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvitem.setAdapter(couponsAdapter);
        rvitem.setLayoutManager(layoutManager);


        return view;
    }
}