package com.siddhantdrk.restaurantApp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.siddhantdrk.restaurantApp.models.Response;
import com.siddhantdrk.restaurantApp.models.User;
import com.siddhantdrk.restaurantApp.network.networkUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import java.util.Objects;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.siddhantdrk.restaurantApp.utils.validation.validateFields;
import static com.siddhantdrk.restaurantApp.utils.validation.validatePhone;

public class OtpBottomSheetFragment extends BottomSheetDialogFragment {
    private EditText Number;
    private Button button;
    private TextView textView;
    private CompositeSubscription mSubscriptions;
    private User user;
    private ProgressDialog progressDialog;

    public OtpBottomSheetFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.login_bottom_sheet, container, false);
        initViews(view);
        mSubscriptions = new CompositeSubscription();
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;


    }

    private void initViews(View v) {



        Number=(EditText)v.findViewById(R.id.number);
        button=(Button)v.findViewById(R.id.loginWithOtp_btn);
        textView=(TextView)v.findViewById(R.id.error);

        button.setOnClickListener(view -> otp());


    }

    private void otp() {



        String number = Objects.requireNonNull(Number.getText()).toString();


        int err = 0;

        if (!validateFields(number)) {

            err++;
            textView.setText("Phone Number is required");
            textView.setVisibility(View.VISIBLE);
        }
        else if(!validatePhone(number))
        {
            err++;
            textView.setText("Enter Valid Phone Number!");
            textView.setVisibility(View.VISIBLE);
        }





        if (err == 0) {
           user = new User();
            user.setEmail("harish.jartarghar@gmail.com");
            user.setMobile_no(number);

            progressDialog=new ProgressDialog(getContext());
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_loading);
            Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            SEND_OTP(user);

        } else {
            Toast.makeText(getActivity(), "Enter Valid Details !", Toast.LENGTH_SHORT).show();

        }
    }

    private void SEND_OTP(User user)
    {
                mSubscriptions.add(networkUtils.getRetrofit().NUMBER_OTP(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        GoToOtp(response.getOtp(),user);
    }

    private void handleError(Throwable error) {

        progressDialog.dismiss();

        if (error instanceof HttpException) {

            Gson gson = new Gson();

            try {

                assert ((HttpException) error).response().errorBody() != null;
                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);


            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("error",error.toString());
            Toast.makeText(getActivity(), "Network Error !", Toast.LENGTH_SHORT).show();

        }
    }

    private void GoToOtp(String otp,User user){

        Intent intent = new Intent(getActivity(), otp_Activity.class);
        intent.putExtra("type","login_otp");
        intent.putExtra("otp",otp);
        intent.putExtra("phone", user.getMobile_no());
        intent.putExtra("email", user.getEmail());
        startActivity(intent);
        progressDialog.dismiss();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

}