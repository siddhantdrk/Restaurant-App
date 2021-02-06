package com.siddhantdrk.restaurantApp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.siddhantdrk.restaurantApp.R;
import com.siddhantdrk.restaurantApp.models.FoodCart;

import java.util.List;

public class confirmOrderAdapter extends RecyclerView.Adapter<confirmOrderAdapter.OrderItemHolder>  {

    private List<FoodCart> list;

    public confirmOrderAdapter(List<FoodCart> list) {
        this.list=list;
    }


    @NonNull
    @Override
    public OrderItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.final_cart_list_item, parent, false);
        return new OrderItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemHolder orderItemHolder, int i) {
        FoodCart Item  = list.get(i);
        orderItemHolder.foodname.setText(Item.getFood_name());
        orderItemHolder.quantity.setText(String.valueOf(Item.getQuantity()));
        orderItemHolder.size.setText(Item.getSize());

    }

    @Override
    public int getItemCount() {
        return list.size();

    }

    class OrderItemHolder extends RecyclerView.ViewHolder {
        TextView foodname,size, quantity;

        OrderItemHolder(View itemView) {
            super(itemView);

            foodname=(TextView)itemView.findViewById(R.id.item_name_txt);
            quantity=(TextView)itemView.findViewById(R.id.quantity_txt);
            size=(TextView)itemView.findViewById(R.id.size);



        }
    }


}
