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
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
    private DatabaseReference mDatabase;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

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
                textToVoiceCall("Sup%20bitch");
                String text = new String(ndefRecord.getPayload()).substring(3);
                Log.e(TAG, "ndefRecord string : " + text);
                Toast.makeText(MainActivity.this, "NFC Tag Detected", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void textToVoiceCall(String textToVoice) {
        String url = getResources().getString(R.string.url_text_to_voice) + textToVoice;
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
        scroll.fullScroll(View.FOCUS_DOWN);
        init();
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
                editText.getLayoutParams().height = entireChatScreen.getHeight()*1/6;

            }
        });


    }
    protected void init(){
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
            }
            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    protected void displaySentMessage(String s){
        s = addNewline(s);
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15);
        t.setTextColor(Color.BLACK);
        t.setPadding(10,10,10,10);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        t.setBackgroundResource(R.drawable.usermessagebg);
        chat.addView(t,params);
        scroll.fullScroll(View.FOCUS_DOWN);
    }
    protected void displayReceivedMessaged(String s){
       s = addNewline(s);
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(15);
        t.setTextColor(Color.WHITE);
        t.setPadding(10,10,10,10);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.LEFT;
        t.setBackgroundResource(R.drawable.botmessagebg);
        chat.addView(t,params);
        scroll.fullScroll(View.FOCUS_DOWN);
    }

    protected String addNewline(String s){
        double count = 0;
        double charCount= 0;
        int len = s.length();
        for (int i = 0; i < len; i++){
            if(s.charAt(i)==' '){
                count++;
                continue;
            }
            charCount ++;
            if(count==4 ){
                s = s.substring(0, i) + "\n" + s.substring(i,len);
                count=0;
                charCount =0;
                continue;
            }
            if(charCount ==20){
                s = s.substring(0, i) + "\n" + s.substring(i,len);
                count=0;
                charCount =0;
            }
        }
        return s;
    }
    protected void sendUserMessage(String s){
        s = s.toLowerCase();
        String reply = "";
        if(s.contains("contribute")||s.contains("add")|| s.contains("deposit")||s.contains("save")){
            double add = Double.parseDouble(s.replaceAll("[\\D]", ""));
            String money = String.valueOf(add);
            if(money.equals("")){
                reply="Please enter a number amount. Eg $10.00";
                return;
            }
            if(balance-add <0){
                reply = "You do not have enough balance to add $"+add+" towards your goal";
                return;
            }
                else{
                updateContribution(add);
                reply = "You've added $"+add+" towards goal."+" You have $"+(goal-contributed)+" until you reach your goal of $"+goal+
                        ". Your current balance is $"+balance;
                return;
            }
        }
        if(s.contains("balance")){
            reply = "Your balance is $"+balance;
        }
        displayReceivedMessaged(reply);
    }
//database
protected void updateBalance(double num){
    balance +=num;
    mDatabase.child("account").child("balance").setValue(balance);
}
    protected void updateContribution(double num){
        balance+=num;
        contributed+=num;
        mDatabase.child("account").child("balance").setValue(balance);
        mDatabase.child("goal").child("contributed").setValue(contributed);
    }
    protected void setGoal(double num){
        goal = num;
        mDatabase.child("goal").child("contributed").setValue(goal);
    }
}
