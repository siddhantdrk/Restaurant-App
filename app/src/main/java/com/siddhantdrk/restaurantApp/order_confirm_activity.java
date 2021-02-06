package com.siddhantdrk.restaurantApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.siddhantdrk.restaurantApp.Adapters.confirmOrderAdapter;
import com.siddhantdrk.restaurantApp.models.PaymentDetails;
import com.siddhantdrk.restaurantApp.models.Address;
import com.siddhantdrk.restaurantApp.models.FinalOrder;
import com.siddhantdrk.restaurantApp.models.FoodCart;
import com.siddhantdrk.restaurantApp.models.Response;
import com.siddhantdrk.restaurantApp.models.Token;
import com.siddhantdrk.restaurantApp.models.User;
import com.siddhantdrk.restaurantApp.network.networkUtils;
import com.siddhantdrk.restaurantApp.utils.constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;


import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static java.lang.Integer.parseInt;

public class order_confirm_activity extends AppCompatActivity {
    private String TAG = "order_confirm_activity";
    private TextView total;
    private SharedPreferences mSharedPreferences;
    private confirmOrderAdapter cofirmOrderAdapter;
    private RecyclerView rvItem;
    private Button proceed;
    private CompositeSubscription mSubscriptions, Subscriptions;
    private ProgressDialog progressDialog;
    private Integer ActivityRequestCode = 2;
    private TextView payment_error;
    private RadioGroup payment_mode;
    private RadioButton mode;
    private ImageView back;
    private TextView confirm_address;
    private int sum,deliveryfees;
    private double taxamount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_details_confirm);
        total = findViewById(R.id.bill_total_txt);
        mSubscriptions = new CompositeSubscription();
        Subscriptions = new CompositeSubscription();
        try {
            TOTAL(CART());

        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        rvItem=findViewById(R.id.final_list_container);
        proceed=findViewById(R.id.proceed);
        payment_mode=findViewById(R.id.payment_mode);
        payment_error=findViewById(R.id.payment_error);
        confirm_address=findViewById(R.id.confirm_address_txt);

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        Type type=new TypeToken<Address>(){}.getType();
        String address=mSharedPreferences.getString("address", null);
        Address confirm=new Address();
        confirm= gson.fromJson(address, type);
        String ConfirmAddress=confirm.getHouse_details()+" "+confirm.getAddress()+" Type: "+confirm.getType();
        confirm_address.setText(ConfirmAddress);

        cofirmOrderAdapter=new confirmOrderAdapter(CART());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvItem.setAdapter(cofirmOrderAdapter);
        rvItem.setLayoutManager(layoutManager);
        back=(ImageView)findViewById(R.id.back_btn);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        proceed.setOnClickListener(view->ORDER());

    }

    private void ORDER() {

        if(payment_mode.getCheckedRadioButtonId()==-1)
        {
            payment_error.setVisibility(View.VISIBLE);
        }
        else
        {

            if(payment_mode.getCheckedRadioButtonId()==R.id.Cash_On_delivery)
            {

                new AlertDialog.Builder(this)
                        .setTitle("Order Confirm??")
                        .setMessage("Place the Order...?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                progressDialog=new ProgressDialog(order_confirm_activity.this);
                                progressDialog.show();
                                progressDialog.setContentView(R.layout.progress_loading);
                                Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                                CASH_ON_DELIVERY();
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();



            }
            else
            {
                GET_TOKEN();

            }

        }
    }

    private void CASH_ON_DELIVERY() {
        Gson gson = new Gson();
        List<FoodCart> Final_Order=new ArrayList<>();
        Final_Order=CART();
        for(FoodCart x:Final_Order)
        {
            x.setAlt_price(0);


        }

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        FinalOrder final_order=new FinalOrder();

        //final order
        final_order.setFoodCarts(Final_Order);
        Type type=new TypeToken<Address>(){}.getType();
        String address=mSharedPreferences.getString("address", null);

        //address
        final_order.setAddress(gson.fromJson(address, type));

        //user
        User user=new User();
        user.setEmail(mSharedPreferences.getString(constants.EMAIL, null));
        user.setMobile_no(mSharedPreferences.getString(constants.PHONE, null));
        user.setName(mSharedPreferences.getString(constants.NAME, null));
        final_order.setUser(user);

        final_order.setCoupon_applied(false);
        final_order.setTotal(Double.parseDouble(total.getText().toString()));
        final_order.setOrderId(GENERATE_ID());
        Date date=new Date();
        final_order.setDate(date);
        final_order.setItemtotal(sum);
        final_order.setTax(taxamount);
        final_order.setDelivery(deliveryfees);

        if(mSharedPreferences.getString("discount", null)!=null && !Objects.equals(mSharedPreferences.getString("reorder", null), "yes"))
        {
            final_order.setCoupon_applied(true);
            final_order.setCoupon_name(mSharedPreferences.getString("coupon_name", null));
            final_order.setDiscount(Double.parseDouble(Objects.requireNonNull(mSharedPreferences.getString("discount", null))));
        }

        String token = mSharedPreferences.getString(constants.TOKEN, null);
        mSubscriptions.add(networkUtils.getRetrofit(token)
                .PLACE_OFFLINE_ORDER(final_order)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse_COD,this::handleError_COD));

    }

    private void GET_TOKEN() {

        Gson gson = new Gson();
        List<FoodCart> Final_Order=new ArrayList<>();
        Final_Order=CART();
        for(FoodCart x:Final_Order)
        {
            x.setAlt_price(0);


        }

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        FinalOrder final_order=new FinalOrder();

        //final order
        final_order.setFoodCarts(Final_Order);
        Type type=new TypeToken<Address>(){}.getType();
        String address=mSharedPreferences.getString("address", null);

        //address
        final_order.setAddress(gson.fromJson(address, type));

        //user
        User user=new User();
        user.setEmail(mSharedPreferences.getString(constants.EMAIL, null));
        user.setMobile_no(mSharedPreferences.getString(constants.PHONE, null));
        user.setName(mSharedPreferences.getString(constants.NAME, null));
        final_order.setUser(user);
        final_order.setCoupon_applied(false);
        final_order.setTotal(Double.parseDouble(total.getText().toString()));
        Date date=new Date();
        final_order.setDate(date);
        final_order.setItemtotal(sum);
        final_order.setTax(taxamount);
        final_order.setDelivery(deliveryfees);

        if(mSharedPreferences.getString("discount", null)!=null  && !Objects.equals(mSharedPreferences.getString("reorder", null), "yes"))
        {
            final_order.setCoupon_applied(true);
            final_order.setCoupon_name(mSharedPreferences.getString("coupon_name", null));
            final_order.setDiscount(Double.parseDouble(Objects.requireNonNull(mSharedPreferences.getString("discount", null))));
        }

        String token = mSharedPreferences.getString(constants.TOKEN, null);
        mSubscriptions.add(networkUtils.getRetrofit(token).GET_TOKEN(constants.MID,GENERATE_ID(),total.getText().toString(),final_order)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));


        progressDialog=new ProgressDialog(order_confirm_activity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_loading);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
    }



    private void handleResponse(Token token) {
        progressDialog.dismiss();

        START_PAYMENT(token.getToken(),token.getOrderId());

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
            Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();

        }
    }



    private List<FoodCart> CART() {
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        Gson gson = new Gson();
        String cart = mSharedPreferences.getString("cart", null);
        Type type = new TypeToken<ArrayList<FoodCart>>() {
        }.getType();
        ArrayList<FoodCart> foodCart;
        foodCart = gson.fromJson(cart, type);

        return foodCart;
    }


    public void TOTAL(List<FoodCart> list) {
        {
            if (!list.isEmpty()) {


                sum = 0;
                for (FoodCart x : list) {
                    sum += x.getPrice() * x.getQuantity();
                }

                taxamount = 0.05 * sum;
                DecimalFormat df=new DecimalFormat("#.##");
                taxamount=Double.parseDouble(df.format(taxamount));
                deliveryfees = sum > 1000 ? 0 : 40;

                if(mSharedPreferences.getString("discount", null)!=null  && !Objects.equals(mSharedPreferences.getString("reorder", null), "yes") )
                {
                    total.setText(String.valueOf(sum + taxamount + deliveryfees-Double.parseDouble(Objects.requireNonNull(mSharedPreferences.getString("discount", null)))));
                }
                else
                { total.setText(String.valueOf(sum + taxamount + deliveryfees));

                }


            }

        }
    }

    private String GENERATE_ID()
    {
        String ts=String.valueOf(System.currentTimeMillis());
        String rand= UUID.randomUUID().toString();
        return ts;
    }

    private void START_PAYMENT(String token, String orderId) {


        String txnAmountString = total.getText().toString();
        String midString = constants.MID ;
        String orderIdString = orderId;
        String txnTokenString = token;

        // for test mode use it
        String host = "https://securegw-stage.paytm.in/";
        // for production mode use it
        //String host = "https://securegw.paytm.in/";
        String orderDetails = "MID: " + midString + ", OrderId: " + orderIdString + ", TxnToken: " + txnTokenString
                + ", Amount: " + txnAmountString;
        Log.e(TAG, "order details "+ orderDetails);

        String callBackUrl = host + "theia/paytmCallback?ORDER_ID="+orderIdString;
        Log.e(TAG, " callback URL "+callBackUrl);
        PaytmOrder paytmOrder = new PaytmOrder(orderIdString, midString, txnTokenString, txnAmountString, callBackUrl);
        TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback(){
            @Override
            public void onTransactionResponse(Bundle bundle) {
                Log.e(TAG, "Response (onTransactionResponse) : "+bundle.toString());
                PaymentDetails paymentDetails=new PaymentDetails();


                    paymentDetails.setBankname(bundle.getString("BANKNAME"));
                    paymentDetails.setOrderId(bundle.getString("ORDERID"));
                    paymentDetails.setAmountpaid(Double.parseDouble(bundle.getString("TXNAMOUNT")));
                    paymentDetails.setDate(bundle.getString("TXNDATE"));
                    paymentDetails.setTransactionId(bundle.getString("TXNID"));
                    paymentDetails.setPaymentType(bundle.getString("PAYMENTMODE"));
                    paymentDetails.setBankTransactionId(bundle.getString("BANKTXNID"));
                    progressDialog=new ProgressDialog(order_confirm_activity.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_loading);
                    Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
                    PLACE_ORDER(paymentDetails);





            }

            @Override
            public void networkNotAvailable() {
                Alert();
            }

            @Override
            public void onErrorProceed(String s) {
                Alert();
            }

            @Override
            public void clientAuthenticationFailed(String s) {
                Alert();
            }

            @Override
            public void someUIErrorOccurred(String s) {
                Alert();
            }

            @Override
            public void onErrorLoadingWebPage(int i, String s, String s1) {
                Alert();
            }

            @Override
            public void onBackPressedCancelTransaction() {
                Alert();
            }

            @Override
            public void onTransactionCancel(String s, Bundle bundle) {
                Alert();
            }
        });

        transactionManager.setShowPaymentUrl(host + "theia/api/v1/showPaymentPage");
        transactionManager.startTransaction(this, ActivityRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG ," result code "+resultCode);
        // -1 means successful  // 0 means failed
        // one error is - nativeSdkForMerchantMessage : networkError
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityRequestCode && data != null) {

            Log.e(TAG, " data "+  data.getStringExtra("nativeSdkForMerchantMessage"));
            Log.e(TAG, " data response - "+data.getStringExtra("response"));

            Toast.makeText(this, data.getStringExtra("nativeSdkForMerchantMessage")
                    + data.getStringExtra("response"), Toast.LENGTH_SHORT).show();
        }else{
            Log.e(TAG, " payment failed");
            Alert();
        }
    }

    private void PLACE_ORDER(PaymentDetails paymentDetails) {

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);




        FinalOrder final_order=new FinalOrder();
        final_order.setPaymentDetails(paymentDetails);
        final_order.setOrderId(paymentDetails.getOrderId());
        //user
        User user=new User();
        user.setEmail(mSharedPreferences.getString(constants.EMAIL, null));
        user.setMobile_no(mSharedPreferences.getString(constants.PHONE, null));
        user.setName(mSharedPreferences.getString(constants.NAME, null));
        final_order.setUser(user);


        String token = mSharedPreferences.getString(constants.TOKEN, null);
        Subscriptions.add(networkUtils.getRetrofit(token)
                .PLACE_ONLINE_ORDER(final_order,constants.MID,String.valueOf(paymentDetails.getOrderId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse_COD,this::handleError_COD));

    }

    private void handleResponse_COD(Response response) {

        if(progressDialog!=null)
        {
            progressDialog.dismiss();
        }

        if(!response.getOtp().equals("-1"))
        {
            GO_TO_PROFILE(response.getToken());

        }
        else
        {
            Alert();

        }

    }

    private void handleError_COD(Throwable error) {

        //mProgressBar.setVisibility(View.GONE);
        progressDialog.dismiss();

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);

                Alert();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Network Error !", Toast.LENGTH_SHORT).show();
        }
    }


    private void GO_TO_PROFILE(String id){
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("cart",null);
        editor.putString("address",null);
        editor.putString("discount",null);
        editor.putString("coupon_name",null);
        editor.apply();

        Intent intent=new Intent(order_confirm_activity.this,order_sucess_activity.class);
        intent.putExtra("id",id);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("reorder",null);
        editor.apply();
    }

    private void Alert()
    {

        new AlertDialog.Builder(order_confirm_activity.this)
                .setTitle("Something Went Wrong!")
                .setMessage("If Amount is Debited from your bank account, Check the status of the payment in My Transactions, else place the order again after sometime :) ")
                .setNeutralButton("Okay", (DialogInterface.OnClickListener) (dialog, which) -> {
                    Intent i=new Intent(order_confirm_activity.this,my_transaction_activity.class);
                    startActivity(i);
                    finish();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();





    }

}