# Restaurant App for ordering food online at your doorstep.

## Main Componets of the Project
- Android app
- Admin Panel
- Server

## Main Features used while Developing
- Splash screen
- Recycler Views with GridLayoutManager
- Fragments
- Bottom Chip Navigation Menu
- Side Navigation Menu
- ViewPager with Image Slider
- Bottom Sheet Dialogue, Alert Dialogue, DatePicker

## Development Approach
### Programming Languages Used
- xml
- Java
- JavaScript
- CSS
### Team project
  #### Members
  - Siddhant Khobragade - Working majorly over Front-end.
  - Harish Jartarghar - Lead Backend Developer
### Procedure
- The User Interface is designed with reference to the popular Apps Like Swiggy and Zomato.
- Problems and bugs faced while developing were resolved by searching the issues on websites like Stackoverflow, Youtube and Official Android developer site.
- themes, colors and icons used are taken from https://www.materialpalette.com/ 
- Main(starting) xml designs with respective front-end java code were created first and then connected with back-end respectively with complete cordination and frequent testing. 

## Main Technologies used while Developing
- Material Design Widgets
- API calls
- Networking and Jason Parsing
- User Login/Signup Authentication
- Otp verification Via Email ( Through Mobile Number Not yet Completed ).

## Server Side 
- The server is developed using nodejs backend tool
- Express Framework is used to implement rest apis.
- MongoDb is used as the data storage that is database.
- Mongoose package is used as agent to the mongodb database.
- Other packages are used based on the requirements in the applcation.
- Apis are implemented based on the requirements of the app.

## Implementations and Libraries used
### API request libraries
- implementation 'com.squareup.retrofit2:retrofit:2.4.0'
- implementation 'com.squareup.retrofit2:converter-gson:2.4.0'
- implementation 'com.squareup.retrofit2:adapter-rxjava:2.1.0'
- implementation 'io.reactivex:rxjava:1.2.0'
- implementation 'io.reactivex:rxandroid:1.2.1'
- testImplementation 'junit:junit:4.13'
### Image slider with Viewpager libraries
- implementation 'com.squareup.picasso:picasso:2.71828'
- implementation 'com.github.bumptech.glide:glide:4.11.0'
- annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
- implementation 'com.google.android.material:material:1.2.1'
### google signin 
- implementation 'com.google.android.gms:play-services-auth:18.1.0'
- implementation 'com.google.android.gms:play-services-location:17.0.0'

### facebook login
- implementation 'com.facebook.android:facebook-login:5.15.3'
( Not yet fully implemented )

### gif
- implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.17'

### paytm all in one sdk
- implementation 'com.paytm.appinvokesdk:appinvokesdk:1.3'
( Not yet fully implemented )

## Drive link to .apk
https://drive.google.com/file/d/1KfuZmMqWBALTz6aloIICBvK3ekF9gacG/view?usp=sharing

### User Instructions
- Till now Please Login with Google only As the facebook authentication is not yet fully implemented.
- Note:- OTP will be recieved on email provided by user respectively. ( via mobile number not yet fully implemented.)






