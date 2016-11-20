package com.example.reimu.finhacks2016;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    LinearLayout chat;
    ScrollView scroll;
    double balance=0;
    double goal =0;
    double contributed =0;
    boolean goalReached = false;
    private DatabaseReference mDatabase;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    String reason;
    boolean lightOn = true;
    boolean setGoal= false;
    int leds=0;

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        getTagInfo(intent);
    }

    /*
    * Function called on NFC tag read!!!!!!!!!!!!
    * */
    private void getTagInfo(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.e(TAG, "tag : " + tag.toString());

        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            // NDEF is not supported by this Tag.
            return;
        }
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            short tnf = ndefRecord.getTnf();
            String type = new String(ndefRecord.getType());
            if (tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(type.getBytes(), NdefRecord.RTD_TEXT)) {
                String s = "you have contributed " + contributed + " dollars to your goal of " + goal + " dollars";
                if (contributed >= goal && goal > 0) {
                    s = "Congratulations   Youve reached your goal";
                    textToVoiceCall(s, false);
                } else {
                    textToVoiceCall(s, false);

                }
                displayReceivedMessage(s);
                Toast.makeText(MainActivity.this, "NFC Tag Detected", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void textToVoiceCall(String textToVoice,boolean light) {

        String fixedString = null;
        String url2 = null;
        if (textToVoice.contains(" "))
            fixedString = textToVoice.replace(" ", "%20");

        String url = null;
        if(!light) {
            url2 = getResources().getString(R.string.url_set_leds) + "*";
            if (fixedString != null)
                url = getResources().getString(R.string.url_text_to_voice) + fixedString;
            else
                url = getResources().getString(R.string.url_text_to_voice) + textToVoice;
        }

        if(light) {
            if(textToVoice.equals("ledLights")){
                url = getResources().getString(R.string.url_set_leds) + leds+"!";
            }
            else if (goalReached) {
                goalReached = false;
                url = getResources().getString(R.string.url_set_leds) + "{";
            }
            else if(lightOn){
                url = getResources().getString(R.string.url_set_leds) + "$";
            }
            else{
                url = getResources().getString(R.string.url_set_leds) + "~";
            }
        }

        Log.e(TAG, "getToken, string URL: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        if(url2!=null){
            StringRequest stringRequest2 = new StringRequest(Request.Method.GET, url2,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            VolleySingleton.getInstance().getRequestQueue().add(stringRequest2);
        }


        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().setAppContext(getApplication());

        setContentView(R.layout.chatlayout);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            //nfc not support your device.
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        final EditText editText = (EditText)findViewById(R.id.editText);
        final RelativeLayout entireChatScreen = (RelativeLayout)findViewById(R.id.whole);
        chat = (LinearLayout) findViewById(R.id.chat);
        scroll = (ScrollView)findViewById(R.id.scroll);
        init();
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });;
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String s = v.getText().toString();
                    if(s!="") {
                        editText.setText("");
                        displaySentMessage(s);
                        sendUserMessage(s);
                        handled = true;
                    }
                }
                return handled;
            }
        });
        entireChatScreen.post(new Runnable()
        {
            @Override
            public void run() {
                scroll.getLayoutParams().height = entireChatScreen.getHeight()*9/10;

            }
        });


    }
    protected void init(){

        displayReceivedMessage("Hello. What can I do for you today?");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            String res;
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object ob =  snapshot.child("account").child("balance").getValue();
                    if (ob == null) {
                        mDatabase.child("account").child("balance").setValue(0);
                    }
                    else {
                        balance = (((Number) snapshot.child("account").child("balance").getValue())).doubleValue();
                    }
                ob =  snapshot.child("goal").child("goal").getValue();
                if (ob == null) {
                    mDatabase.child("goal").child("goal").setValue(0);
                }
                else {
                    goal = (((Number) snapshot.child("goal").child("goal").getValue())).doubleValue();
                }
                ob =  snapshot.child("goal").child("contributed").getValue();
                if (ob == null) {
                    mDatabase.child("goal").child("contributed").setValue(0);
                }
                else {
                    contributed = (((Number) snapshot.child("goal").child("contributed").getValue())).doubleValue();
                }
                if(goal<=contributed && !goalReached&&goal>0){
                    goalReached=true;
                    textToVoiceCall("Congratulations  Youve reached your goal",true);
                }
                reason = (String)snapshot.child("goal").child("reason").getValue();
                if( snapshot.child("LEDOn").getValue()==null) {
                    mDatabase.child("LEDOn").setValue(Boolean.TRUE);
                }
                else {
                    lightOn = (boolean) snapshot.child("LEDOn").getValue();
                }
                leds =(int)(contributed/goal*14);
                textToVoiceCall("ledLights",true);

            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    protected void displaySentMessage(String s){
        s = addNewline(s);
        LinearLayout sub = new LinearLayout(this);
        sub.setOrientation(LinearLayout.HORIZONTAL);
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15);
        t.setTextColor(Color.BLACK);
        t.setPadding(20,15,15,15);
        LinearLayout.LayoutParams paramslay = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramslay.gravity = Gravity.RIGHT;
        sub.setBaselineAligned(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        params.setMargins(0,30,20,30);
        t.setBackgroundResource(R.drawable.usermessagebg);
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.clientlogo);
        LinearLayout.LayoutParams paramsLogo = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsLogo.gravity = Gravity.BOTTOM;
        paramsLogo.setMargins(0,30,20,30);
        sub.addView(t,params);
        sub.addView(iv,paramsLogo);

        chat.addView(sub,paramslay);
        scroll.post(new Runnable() {

            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });;

    }
    protected void displayReceivedMessage(String s){
       s = addNewline(s);
        LinearLayout sub = new LinearLayout(this);
        sub.setOrientation(LinearLayout.HORIZONTAL);
        sub.setBaselineAligned(false);
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15);
        t.setTextColor(Color.WHITE);
        t.setPadding(20,15,15,15);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;

        params.setMargins(20,0,0,0);
        t.setBackgroundResource(R.drawable.botmessagebg);
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.tdlogo);
        LinearLayout.LayoutParams paramsLogo = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsLogo.gravity = Gravity.BOTTOM;
        paramsLogo.setMargins(20,0,0,0);
        sub.addView(iv,paramsLogo);
        sub.addView(t,params);
        chat.addView(sub);
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });;
    }
    protected void changeLEDStatus(Boolean on){
        if(!on){
            mDatabase.child("LEDOn").setValue(Boolean.FALSE);
            lightOn=false;
            textToVoiceCall("",true);

        }
        else{
            mDatabase.child("LEDOn").setValue(Boolean.TRUE);
            lightOn=true;
            textToVoiceCall("",true);

        }
    }

    protected String addNewline(String s){
        Log.i("fddfdda",s);
        double count = 0;
        double charCount= 0;
        int len = s.length();
        for (int i = 0; i < len; i++){
            if(count==4 ){
                s = s.substring(0, i) + "\n" + s.substring(i,len);
                count=0;
                len++;
                charCount =0;
                continue;
            }
            if(s.charAt(i)==' '){
                count++;
                charCount ++;
                continue;
            }
            charCount ++;

            if(charCount==20){
                if(s.charAt(i)!=' '){
                    continue;
                }
                s = s.substring(0, i) + "\n" + s.substring(i,len);
                len++;
                count=0;
                charCount =0;
                continue;
            }
        }
        Log.i("fdd",s);
        return s;
    }
    protected void sendUserMessage(String s){
        s = s.toLowerCase();
        String reply = "";
        String money = s.replaceAll("[\\D]", "");
        String lastWord = s.substring(s.lastIndexOf(" ")+1);
        if(s.equals("reset")){
           mDatabase.child("goal").removeValue();
            return;
        }
        else if(s.contains("encourage")){
            if(goal==0){
                reply="Set up a goal! I believe you can achieve it!";

            }
            else{
                reply = "Almost there! You can do this!";
            }
        }
        else if(setGoal){
            if(money.equals(s)) {
                goal=Double.valueOf(money);
                mDatabase.child("goal").child("goal").setValue(goal);
                if(reason==null){
                    reply="What is this for?";
                }
                else{
                    setGoal=false;
                    reply = "You have set a goal for "+reason+" for $"+goal+".";
                }

            }
            else{
                reason = s;
                mDatabase.child("goal").child("reason").setValue(reason);
                if(goal==0){
                    reply="How much do you want to save?";
                }
                else{
                    setGoal=false;
                    reply = "You have set a goal for "+reason+" for $"+goal+".";
                }
            }
        }
        else if(s.contains("goal")){

            if(reason==null){
                setGoal = true;
                if(!money.equals("")) {
                    reply = "What will this be used for?";
                    mDatabase.child("goal").child("goal").setValue(Integer.valueOf(money));
                }
                //add item

                else {
                    reply = "How much do you want to set your goal for?";
                    if (!lastWord.equals("goal") || !s.replaceAll("\\s+", "").equals("goal")) {
                        mDatabase.child("goal").child("reason").setValue(lastWord);
                    }
                }
                }

            else{
                reply= "You have saved $"+contributed+" towards your $"+goal+" for a "+reason+".";
            }
        }
        else if(s.contains("contribute")||s.contains("add")|| s.contains("deposit")||s.contains("save")) {

            if (money.equals("")) {
                reply = "Please enter a number amount. Eg $10.00";
            } else {
                int add = Integer.valueOf(money);
                if (balance - add < 0) {
                    reply = "You do not have enough balance to add $" + add + " towards your goal";
                } else {
                    updateContribution(add);
                    reply = "You've added $" + add + " towards goal." + " You have $" + (goal - contributed) + " until you reach your goal of $" + goal +
                            ". Your current balance is $" + balance;
                }
            }
        }
        else if(s.contains("balance")){
            reply = "Your balance is $"+balance;
        }
        else if(s.contains("on")||s.contains("off")) {
            boolean on = s.contains("on");
            if (!on) {
                reply = "You have turned off the LED";
            } else {
                reply = "You have turned on the LED";
            }
            changeLEDStatus(on);

        }

        else if(s.contains("contributed")||(s.contains("contribute")&&s.contains("did"))){
            reply= "You have saved $"+contributed+" towards your $"+goal+" for a "+reason+".";
        }
        else{
            reply="Sorry,I do not understand that command";
        }
        displayReceivedMessage(reply);

    }
//database
protected void updateBalance(double num){
    balance +=num;
    mDatabase.child("account").child("balance").setValue(balance);
}
    protected void updateContribution(double num){
        balance-=num;
        contributed+=num;
        mDatabase.child("account").child("balance").setValue(balance);
        mDatabase.child("goal").child("contributed").setValue(contributed);
    }
    protected void setGoal(double num){
        goal = num;
        mDatabase.child("goal").child("contributed").setValue(goal);
    }
}
