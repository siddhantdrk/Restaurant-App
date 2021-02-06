package com.siddhantdrk.restaurantApp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.siddhantdrk.restaurantApp.FoodList.Cart_Quantity;
import com.siddhantdrk.restaurantApp.FoodList.Item;
import com.siddhantdrk.restaurantApp.FoodList.ItemAdapter;
import com.siddhantdrk.restaurantApp.FoodList.SubItem;
import com.siddhantdrk.restaurantApp.models.FoodCart;
import com.siddhantdrk.restaurantApp.models.Food_Details;
import com.siddhantdrk.restaurantApp.models.Food_Response;
import com.siddhantdrk.restaurantApp.models.Response;
import com.siddhantdrk.restaurantApp.models.subcategory_list;
import com.siddhantdrk.restaurantApp.network.networkUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.siddhantdrk.restaurantApp.utils.constants.BASE;

public class Subcategory_main extends AppCompatActivity implements Cart_Quantity {

    private CompositeSubscription mSubscriptions;
    private Intent intent;
    private TextView heading, update;
    private RecyclerView rvItem;
    private ItemAdapter itemAdapter;
    private ProgressDialog progressDialog;
    private ImageView back_img_btn;
    private TextInputEditText search;
    private List<subcategory_list> list;
    private List<Item> itemList;
    private List<SubItem> subItemList;
    private SharedPreferences mSharedPreferences;
    private ImageView cart_btn;
    private LinearLayout internet;
    private Button retry;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subcategories_main);


        mSubscriptions = new CompositeSubscription();
        intent = getIntent();

        heading = (TextView) findViewById(R.id.category_txt);
        heading.setText(intent.getStringExtra("category"));
        rvItem = findViewById(R.id.subcategory_container);
        back_img_btn = findViewById(R.id.back_btn);
        update = findViewById(R.id.cart_fill_update_txt);
        internet=findViewById(R.id.no_internet_container);
        retry=findViewById(R.id.retry_btn);

        search = (TextInputEditText) findViewById(R.id.search_edit_text);

        cart_btn = findViewById(R.id.cart_Img);

        cart_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Subcategory_main.this, CartActivity.class);
                startActivity(i);
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(Subcategory_main.this);
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_loading);
                Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

                FETCH_ITEMS(intent.getStringExtra("category"));
            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_loading);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        FETCH_ITEMS(intent.getStringExtra("category"));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        back_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        try{
            UpdateNumber(CART());
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }

        SEARCH();

    }

    private  void FETCH_ITEMS(String category){

        mSubscriptions.add(networkUtils.getRetrofit().GET_FOOD_LIST(category)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }


    private void handleResponse(Food_Response response) {
        rvItem.setVisibility(View.VISIBLE);
        internet.setVisibility(View.GONE);
        list=new ArrayList<>();
            list=response.getResult();

        itemAdapter = new ItemAdapter(buildItemList(list,""),intent.getStringExtra("category"),this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(Subcategory_main.this);
        rvItem.setAdapter(itemAdapter);
        rvItem.setLayoutManager(layoutManager);
        progressDialog.dismiss();


    }

    private void handleError(Throwable error) {
        progressDialog.dismiss();

        if (error instanceof HttpException) {

            Gson gson = new Gson();

            try {

                assert ((HttpException) error).response().errorBody() != null;
                String errorBody = ((HttpException) error).response().errorBody().string();
                com.siddhantdrk.restaurantApp.models.Response response = gson.fromJson(errorBody, Response.class);


            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            rvItem.setVisibility(View.GONE);
            internet.setVisibility(View.VISIBLE);

        }
    }

    private List<Item> buildItemList(List<subcategory_list> l,String s) {
        itemList = new ArrayList<>();

        for(subcategory_list x:l)
        {
            if(buildSubItemList(x.getDetails(),s).size()!=0)
            {   Item item = new Item(x.getSubcategory(), buildSubItemList(x.getDetails(),s));
                itemList.add(item);
            }
        }

        return itemList;
    }

    private List<SubItem> buildSubItemList(List<Food_Details> list,String s) {
        subItemList = new ArrayList<>();
        for (Food_Details y:list) {
            SubItem subItem;
            if(y.getFood_name().toLowerCase().contains(s))
            {   if(y.getCategory().equals("Pizzas"))
            {
                subItem = new SubItem(y.getID(), BASE + y.getFood_image(), y.getFood_name(), y.getPrice(), y.getLarge_price(), y.getCategory(), y.getSubcategory(), y.getDescription());
            }
            else
            {
                subItem = new SubItem(y.getID(), BASE+ y.getFood_image(), y.getFood_name(), y.getPrice(), y.getCategory(), y.getSubcategory(), y.getDescription());
            }
                subItemList.add(subItem);
            }
        }
        return subItemList;
    }

    private void  SEARCH(){

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                    filter(s.toString());
            }


        });
    }

    private void filter(String s) {
        itemAdapter = new ItemAdapter(buildItemList(list,s),intent.getStringExtra("category"),this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(Subcategory_main.this);
        rvItem.setAdapter(itemAdapter);
        rvItem.setLayoutManager(layoutManager);
    }


    @Override
    public void UpdateNumber(List<FoodCart> list) {
        int quantity=0;
            for(FoodCart x:list)
            {
                quantity+=x.getQuantity();
            }
          if(quantity==0)
          {
              update.setVisibility(View.GONE);
          }
          else
          {
              update.setVisibility(View.VISIBLE);
              update.setText(String.valueOf(quantity));
          }

    }

    private List<FoodCart>  CART()
    {
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        Gson gson = new Gson();
        String cart=mSharedPreferences.getString("cart", null);
        Type type = new TypeToken<ArrayList<FoodCart>>() {
        }.getType();
        ArrayList<FoodCart> foodCart;
        foodCart=gson.fromJson(cart,type);

        return  foodCart;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSubscriptions!=null)
        {
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mSubscriptions!=null)
        {
            mSubscriptions.unsubscribe();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try
        { itemAdapter = new ItemAdapter(buildItemList(list,""),intent.getStringExtra("category"),this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(Subcategory_main.this);
            rvItem.setAdapter(itemAdapter);
            rvItem.setLayoutManager(layoutManager);
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }


        try{
            UpdateNumber(CART());
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }
}
