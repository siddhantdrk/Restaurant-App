package com.siddhantdrk.restaurantApp.FoodList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.siddhantdrk.restaurantApp.R;
import com.siddhantdrk.restaurantApp.item_description_activity;
import com.siddhantdrk.restaurantApp.models.FoodCart;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartItemHolder>  {


    private List<FoodCart> CartItemList;
    private SharedPreferences mSharedPreferences;
    private ChangePrice price;


    public CartAdapter(List<FoodCart> cartItemList, ChangePrice price) {
        this.CartItemList = cartItemList;
        this.price=price;

    }


    @NonNull
    @Override
    public CartAdapter.CartItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pizza_cart_list_item, parent, false);

        return new CartAdapter.CartItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartItemHolder cartViewHolder, int i) {
        FoodCart Item  = CartItemList.get(i);
        cartViewHolder.foodname.setText(Item.getFood_name());
        cartViewHolder.price.setText(String.valueOf(Item.getPrice()*Item.getQuantity()));
        cartViewHolder.quantity.setText((String.valueOf(Item.getQuantity())));
        ArrayAdapter<CharSequence> adapter = null;

        cartViewHolder.foodname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(cartViewHolder.itemView.getContext(), item_description_activity.class);
                i.putExtra("id",Item.get_id());
                cartViewHolder.itemView.getContext().startActivity(i);
            }
        });


        if(Item.getFood_category().equals("Pizzas"))
        {

            cartViewHolder.size.setVisibility(View.VISIBLE);
        }


        adapter=ArrayAdapter.createFromResource(cartViewHolder.itemView.getContext(),R.array.PizzaSize,android.R.layout.simple_spinner_item);
        cartViewHolder.size.setAdapter(adapter);
        if(Item.getSize()!=null)
        {
            int spinerposition=adapter.getPosition(Item.getSize());
            cartViewHolder.size.setSelection(spinerposition);
        }

        if(Item.getFood_category().equals("Pizzas"))
        {
            cartViewHolder.size.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(cartViewHolder.itemView.getContext());

                    Gson gson = new Gson();
                    String cart=mSharedPreferences.getString("cart", null);
                    Type type=new TypeToken<ArrayList<FoodCart>>(){}.getType();
                    List<FoodCart> newfoodCarts=new ArrayList<>();
                    newfoodCarts=gson.fromJson(cart,type);
                    int flag=0;
                    assert newfoodCarts != null;


                    for(int i = newfoodCarts.size()-1; i>=0; --i)
                    {
                        if(newfoodCarts.get(i).getFood_name().equals(Item.getFood_name()) && parent.getItemAtPosition(position).toString().equals(newfoodCarts.get(i).getSize()) && newfoodCarts.get(i).getId()!=Item.getId() )
                        {flag=1;
                            newfoodCarts.get(i).setQuantity(newfoodCarts.get(i).getQuantity()+Item.getQuantity());
                            cartViewHolder.quantity.setText((String.valueOf(newfoodCarts.get(i).getQuantity())));
                            cartViewHolder.price.setText(String.valueOf(newfoodCarts.get(i).getQuantity()*newfoodCarts.get(i).getPrice()));

                        }
                    }

                    if(flag==1)
                    {
                        for(int i = newfoodCarts.size()-1; i>=0; --i)
                        {
                            if(newfoodCarts.get(i).getFood_name().equals(Item.getFood_name()) && newfoodCarts.get(i).getId()==Item.getId())
                            {
                                newfoodCarts.remove(i);
                                update(i);
                            }
                        }
                    }


                    if(flag==0)
                    {
                        for(FoodCart x:newfoodCarts)
                        {
                            if(x.getFood_name().equals(Item.getFood_name()) && !parent.getItemAtPosition(position).toString().equals(newfoodCarts.get(i).getSize()))
                            {
                                x.setSize(parent.getItemAtPosition(position).toString());
                                int price=x.getPrice();
                                x.setPrice(x.getAlt_price());
                                x.setAlt_price(price);
                                cartViewHolder.price.setText(String.valueOf(x.getPrice()*x.getQuantity()));


                            }
                        }
                        price.TOTAL(newfoodCarts);

                    }

                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    String newcart = gson.toJson(newfoodCarts);
                    Log.e("error",newcart);
                    editor.putString("cart",newcart);
                    editor.apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(cartViewHolder.itemView.getContext());

        Gson gson = new Gson();
        String cart=mSharedPreferences.getString("cart", null);
        Type type=new TypeToken<ArrayList<FoodCart>>(){}.getType();
        List<FoodCart> newfoodCarts=new ArrayList<>();
        newfoodCarts=gson.fromJson(cart,type);
        assert newfoodCarts != null;

        try{
            if(!Item.getFood_category().equals("Pizzas"))
            {
                for(FoodCart x:newfoodCarts)
                {
                    if(x.getFood_name().equals(Item.getFood_name()))
                    {
                        cartViewHolder.quantity.setText(String.valueOf(x.getQuantity()));
                        cartViewHolder.price.setText(String.valueOf(x.getQuantity()*x.getPrice()));
                    }
                }
            }
            else
            {
                for(FoodCart x:newfoodCarts)
                {
                    if(x.getFood_name().equals(Item.getFood_name()) && cartViewHolder.size.getSelectedItem().toString().equals(x.getSize()))
                    {
                        cartViewHolder.quantity.setText(String.valueOf(x.getQuantity()));
                        cartViewHolder.price.setText(String.valueOf(x.getQuantity()*x.getPrice()));
                    }
                }
            }

        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }


        cartViewHolder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new Gson();
                String cart=mSharedPreferences.getString("cart", null);
                Type type=new TypeToken<ArrayList<FoodCart>>(){}.getType();
                List<FoodCart> newfoodCarts=new ArrayList<>();
                newfoodCarts=gson.fromJson(cart,type);

                assert newfoodCarts != null;

                if(!Item.getFood_category().equals("Pizzas"))
                {
                    for(FoodCart x:newfoodCarts)
                    {
                        if(x.getFood_name().equals(Item.getFood_name()))
                        {
                            x.increment();
                            cartViewHolder.quantity.setText(String.valueOf(x.getQuantity()));
                            cartViewHolder.price.setText(String.valueOf(x.getPrice()*x.getQuantity()));
                        }
                    }
                }
                else
                {
                    for(FoodCart x:newfoodCarts)
                    {
                        if(x.getFood_name().equals(Item.getFood_name()) &&  cartViewHolder.size.getSelectedItem().toString().equals(x.getSize()))
                        {
                            x.increment();
                            cartViewHolder.quantity.setText(String.valueOf(x.getQuantity()));
                            cartViewHolder.price.setText(String.valueOf(x.getPrice()*x.getQuantity()));
                        }
                    }
                }


                price.TOTAL(newfoodCarts);



                SharedPreferences.Editor editor = mSharedPreferences.edit();
                String newcart = gson.toJson(newfoodCarts);
                Log.e("error",newcart);
                editor.putString("cart",newcart);
                editor.apply();





            }
        });

        cartViewHolder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Gson gson = new Gson();
                String cart=mSharedPreferences.getString("cart", null);
                Type type=new TypeToken<ArrayList<FoodCart>>(){}.getType();
                List<FoodCart> newfoodCarts=new ArrayList<>();
                newfoodCarts=gson.fromJson(cart,type);

                assert newfoodCarts != null;

                if(!Item.getFood_category().equals("Pizzas"))
                {
                    for(int i=newfoodCarts.size()-1;i>=0;--i)
                    {
                        if(newfoodCarts.get(i).getFood_name().equals(Item.getFood_name()))
                        {
                            newfoodCarts.get(i).decrement();
                            cartViewHolder.quantity.setText(String.valueOf(newfoodCarts.get(i).getQuantity()));
                            cartViewHolder.price.setText(String.valueOf(newfoodCarts.get(i).getQuantity()*newfoodCarts.get(i).getPrice()));
                        }
                        if(newfoodCarts.get(i).getQuantity()==0)
                        {   newfoodCarts.remove(i);
                            update(i);

                        }
                    }
                }
                else
                {
                    for(int i=newfoodCarts.size()-1;i>=0;--i)
                    {
                        if(newfoodCarts.get(i).getFood_name().equals(Item.getFood_name()) && newfoodCarts.get(i).getSize().equals(cartViewHolder.size.getSelectedItem().toString()))
                        {
                            newfoodCarts.get(i).decrement();
                            cartViewHolder.quantity.setText(String.valueOf(newfoodCarts.get(i).getQuantity()));
                            cartViewHolder.price.setText(String.valueOf(newfoodCarts.get(i).getQuantity()*newfoodCarts.get(i).getPrice()));
                        }
                        if(newfoodCarts.get(i).getQuantity()==0)
                        {
                            newfoodCarts.remove(i);
                            update(i);
                        }
                    }
                }


                price.TOTAL(newfoodCarts);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                String newcart = gson.toJson(newfoodCarts);
                Log.e("error",newcart);
                editor.putString("cart",newcart);
                editor.apply();

            }
        });



    }

    @Override
    public int getItemCount() {
        try{
            return CartItemList.size();
        }
        catch (NullPointerException e)
        {
            return 0;
        }
    }

    class CartItemHolder extends RecyclerView.ViewHolder {
        TextView foodname, price, quantity;
        Button plus,minus;
        Spinner size;

        CartItemHolder (View itemView) {
            super(itemView);

            foodname=(TextView)itemView.findViewById(R.id.item_name_txt);
            price=(TextView)itemView.findViewById(R.id.price_txt);
            plus=(Button)itemView.findViewById(R.id.plus_btn);
            minus=(Button)itemView.findViewById(R.id.minus_btn);
            quantity=(TextView)itemView.findViewById(R.id.quantity_text_view);
            size=(Spinner)itemView.findViewById(R.id.static_size_spinner);


        }
    }



    private void update(int i)
    {   CartItemList.remove(i);
        notifyDataSetChanged();
    }



}
