package com.siddhantdrk.restaurantApp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.siddhantdrk.restaurantApp.Adapters.OfferListAdapter;
import com.siddhantdrk.restaurantApp.models.Coupon;

import java.util.List;


public class OfferListFragment extends Fragment {

    private RecyclerView rvitem;


    private List<Coupon> coupons;

    public OfferListFragment(List<Coupon> couponList)
    {
        this.coupons=couponList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_offer_list, container, false);
        rvitem=view.findViewById(R.id.All_offers_container);
        OfferListAdapter offerListAdapter=new OfferListAdapter(coupons,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvitem.setAdapter(offerListAdapter);
        rvitem.setLayoutManager(layoutManager);
        return  view;

    }





}