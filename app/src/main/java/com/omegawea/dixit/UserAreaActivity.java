package com.omegawea.dixit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.omegawea.dixit.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.color.background_dark;
import static android.R.color.holo_orange_light;
import static com.omegawea.dixit.R.id.hsvCard;
import static java.sql.Types.NULL;

public class UserAreaActivity extends AppCompatActivity implements Animation.AnimationListener {
    //FOR TEST PURPOSE
    private static final String TAG = "UserAreaActivity";

    private static final String MYSQLERROR = "MySQL server has gone away";
    //Timer
    Timer timer = new Timer(true);
    final int TimerPeriod = 100;                //in 1mS,
    final int requestTimerLimit = 15;           //in 100mS,
    int requestTimer = 0;
    //Animation
    Animation animblink;
    Animation animrotate;
    Animation animslide;

    //Notification
    NotificationManager manager;
    // 建立NotificationCompat.Builder物件
    NotificationCompat.Builder builder;

    //Internal Memory
    private String fnMusic = "Music";
    private String fnSound = "Sound";
    private FileIOStream fileIOStream = new FileIOStream(UserAreaActivity.this);

    //CONSTANT
    private int CARDWIDHT = 540;
    private int CARDHEIGHT = 720;
    private int SCOREWIDTH = 270;
    private int SCOREHEIGHT = 360;

    //TEMPERORY PARAMETERS
    private Dialog dialog;
    private Button button;
    private TextView textView;
    private EditText editText;
    private ImageButton imageButton;
    private ImageView imageView;
    private int temp;
    private String tString;

    //PERIODIC PHP REQUEST
    private Response.Listener<String> responseListener;
    private RWRequest rwRequest;
    private QuitRequest quitRequest;
    final int PHPREQUESTPEROID = 1000;
    final int PHPRETRYTIMEOUT = 5;        //in second, will cancel queue
    private int PHPRETRYCOUNT = 0;

    //SCORE TABLE  DIALOG
    private TableLayout.LayoutParams row_layout = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private TableRow.LayoutParams view_layout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private TableRow.LayoutParams image_layout = new TableRow.LayoutParams(SCOREWIDTH, SCOREHEIGHT);
    private TableLayout tlScore;

    //SOUND EFFECT
    private boolean MUSICEN = true;
    private boolean SOUNDEN = true;
    private MediaPlayer mpsound;
    private MediaPlayer mpBackground;
    //OTHER PARAMETERS
    private Button bGame;
    private Button bScore;
    private Button bOK;

    private TextView tvStatus;
    private TextView tvStoryteller;
    private TextView tvStory;

    HorizontalScrollView hsvcard;

    int count = 0;
    private Handler handler = new Handler();

    private int myindex = 0;                    // For easy access operation
    private int numofjoinedplayer = 0;          // For start the game
    private int mydface = 0;                    // temporary face before sending out
    private int mydguess = 0;                   // temporary guess before sending out
    List<Integer> card = new ArrayList<>();

    // MYSQL PARAMETERS
    final int MAXSCORE = 20;
    final int MAXHAND = 6;
    final int MINPLAYER = 3;
    final int MAXPLAYER = 8;
    final private int OFFSETCARD = 10;
    private LinearLayout llCard;
    final private int OFFSETHAND = 20;
    private LinearLayout llHand;

    private String myname;
    private int myroom = 0;
    private String mystory;
    private int myface = 0;
    private int myguess = 0;
    private int numofplayer = 0;

    private String[] player = new String[MAXPLAYER];
    private int[] guess = new int[MAXPLAYER];
    private int[] lastguess = new int[MAXPLAYER];
    private int[] face = new int[MAXPLAYER];
    private int[] lastface = new int[MAXPLAYER];
    private int[] score = new int[MAXPLAYER];
    private int[] lastscore = new int[MAXPLAYER];

    private String story;
    private String laststory;
    private int tellerindex;
    private int lasttellerindex;
    private int numofnullguess;
    private int numofnullface;

    private int[] myhand = new int[MAXHAND];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);
        //Timer
        timer.schedule(new timerTask(), TimerPeriod, TimerPeriod);
        requestTimer = 0;
        //Animation
        animblink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animblink);
        animblink.setAnimationListener(UserAreaActivity.this);
        animrotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animrotate);
        animrotate.setAnimationListener(UserAreaActivity.this);
        animslide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animslide);
        animslide.setAnimationListener(UserAreaActivity.this);

        //Background Music
        MUSICEN = true;
        if(fileIOStream.OutputStream(fnMusic).equals("F")){
            MUSICEN = false;
        }
        SOUNDEN = true;
        if(fileIOStream.OutputStream(fnSound).equals("F")){
            SOUNDEN = false;
        }
        mpBackground = MediaPlayer.create(UserAreaActivity.this, R.raw.loop);
        mpBackground.setLooping(true); // Set looping
        mpBackground.setVolume(0.8f, 0.8f);
        card.clear();                                   // Clear cards at startup
        // Screen Resolution
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
//        int screenHeight = displayMetrics.heightPixels;

        CARDWIDHT = screenWidth * 80 / 100;
        CARDHEIGHT = CARDWIDHT * 4 / 3;
        SCOREWIDTH = screenWidth * 30 / 100;
        SCOREHEIGHT = SCOREWIDTH * 5 / 3;

//        Log.d(TAG, "CARDWIDHT: " + CARDWIDHT + " &  CARDHEIGHT: " + CARDHEIGHT + " &  SCOREWIDTH: " + SCOREWIDTH + " & SCOREHEIGHT: " + SCOREHEIGHT);

        row_layout = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        view_layout = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        image_layout = new TableRow.LayoutParams(SCOREWIDTH, SCOREHEIGHT);

        //Button Control
        bGame = (Button) findViewById(R.id.bGame);
        bGame.setEnabled(false);
        bScore = (Button) findViewById(R.id.bScore);

        hsvcard = (HorizontalScrollView) findViewById(R.id.hsvCard);

        llCard = (LinearLayout)findViewById(R.id.llCard);

        Intent intent = getIntent();
        myname = intent.getStringExtra("username");
        myroom = intent.getIntExtra("room", NULL);
        // Response received from the server
        responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.i(TAG, response);
                    if(response.contains(MYSQLERROR)) {
                        Log.d(TAG, "OMG!! MYSQLERROR!!!");                      // ignore that message
                    }
                    else{
//                        int jsonStart = response.indexOf("{");
//                        int jsonEnd = response.lastIndexOf("}");
//                        if (jsonStart >= 0 && jsonEnd >= 0 && jsonEnd > jsonStart) {
//                            response = response.substring(jsonStart, jsonEnd + 1);
//                        } else {
//                            // deal with the absence of JSON content here
//                        }
                        Log.i(TAG, response);
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            Log.d(TAG, " requestTimer: " + requestTimer + " mydface: " + mydface +  " & myface: " + myface + " &  mydguess: " + mydguess + " &  myguess: " + myguess);
                            requestTimer = 0;                                           // reset request timer

                            // Getting JSON values
                            story = jsonResponse.getString("story");
                            laststory = jsonResponse.getString("laststory");
                            tellerindex = jsonResponse.getInt("teller");
                            numofplayer = jsonResponse.optInt("numofplayer");
                            numofnullguess = jsonResponse.optInt("numofnullguess");
                            numofnullface = jsonResponse.optInt("numofnullface");

                            // Getting JSON Array node
                            numofjoinedplayer = 0;
                            for (count = 0; count < MAXPLAYER; count++) {
                                if (jsonResponse.getJSONArray("player").length() >= (count + 1)) {
                                    player[count] = jsonResponse.getJSONArray("player").getString(count);
                                    if (!jsonResponse.getJSONArray("player").isNull(count)) {
                                        numofjoinedplayer++;                        // Keep checking during the game in case sby left
                                    }
                                    if (myname.equals(player[count]))               // name is matched
                                        myindex = count;                            // Obtain my index
                                }
                                if (jsonResponse.getJSONArray("guess").length() >= (count + 1)) {
                                    guess[count] = jsonResponse.getJSONArray("guess").optInt(count, 0);
                                }
                                if (jsonResponse.getJSONArray("face").length() >= (count + 1)) {
                                    face[count] = jsonResponse.getJSONArray("face").optInt(count, 0);
                                }
                                if (jsonResponse.getJSONArray("score").length() >= (count + 1)) {
                                    score[count] = jsonResponse.getJSONArray("score").optInt(count, 0);
                                }
                                if (jsonResponse.getJSONArray("guess").length() >= (count + 1)) {
                                    lastguess[count] = jsonResponse.getJSONArray("lastguess").optInt(count, 0);
                                }
                                if (jsonResponse.getJSONArray("face").length() >= (count + 1)) {
                                    lastface[count] = jsonResponse.getJSONArray("lastface").optInt(count, 0);
                                }
                                if (jsonResponse.getJSONArray("score").length() >= (count + 1)) {
                                    lastscore[count] = jsonResponse.getJSONArray("lastscore").optInt(count, 0);
                                }
                            }

                            for (count = 0; count < MAXHAND; count++) {
                                myhand[count] = jsonResponse.getJSONArray("myhand").optInt(count, 0);
                            }

                            // Clear buffer after response
                            if (face[myindex] > 0) {                                // Server has face
                                if (myface > 0)                                     // not to clear during selection peroid
                                    mydface = 0;                                    // clear mydface
                                myface = 0;                                         // clear myface
                            }
                            if (guess[myindex] > 0) {                               // Server has guess
                                if (myguess > 0)                                    // not to clear during selection peroid
                                    mydguess = 0;                                   // clear mydguess
                                myguess = 0;                                        // clear myguess
                            }
                            if (myname.equals(player[tellerindex])) {               // At Storyteller turn: has to be after the upper check
                                myguess = 0;                                        // Clear command every message sent
                            } else{                                                 // not Storyteller
                                if(face[tellerindex] == 0){                         // Storyteller have not put his face yet
                                    mydface = 0;                                    // clear mydface at new round
                                    myface = 0;                                     // clear myface at new round
                                }
                            }
                            // Show cards after response
                            if ((numofnullface == 0) && (card.isEmpty())) {
                                for (count = 0; count < numofplayer; count++) {
                                    card.add(face[count]);
                                }
                                Collections.shuffle(card);                              // Shuffle cards
                                if ((guess[myindex] == 0) && (myindex != tellerindex))   // no selected guess before
                                    llCard.startAnimation(animblink);                 // Start Blinking on Cards
                                GameSound(R.raw.flip);                                 // Play sounds
                            }

                            //Show your hands
                            for (count = 0; count < numofplayer; count++) {
                                //Card is not shown on table - add view for this card
                                if (numofplayer > 0) {                                              //Game is started
                                    imageButton = (ImageButton) findViewById(OFFSETCARD + count);
                                    if (face[count] != 0) {                                         // other play has placed his face
                                        if (imageButton == null) {                                  // image button is not created yet
                                            GameSound(R.raw.place);
                                            imageButton = new ImageButton(UserAreaActivity.this);
                                            imageButton.setLayoutParams(new LayoutParams(CARDWIDHT, CARDHEIGHT));
                                            imageButton.setId(OFFSETCARD + count);
                                            imageButton.setPadding(20, 20, 20, 20);
                                            imageButton.setScaleType(ImageView.ScaleType.FIT_XY);
                                            imageButton.setBackgroundResource(background_dark);
                                            imageButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    // Guess a card
                                                    if (!myname.equals(player[tellerindex])         // you are not storyteller
                                                            && (card.size() == numofplayer)                // Cards are ready for guess
                                                            && (guess[myindex] == 0)                       // no guess in cloud
                                                            && (myguess == 0)) {                            // have not confirm with a guess before
                                                        temp = view.getId();
                                                        for (count = 0; count < numofplayer; count++) {
                                                            ImageButton otherButton = (ImageButton) findViewById(OFFSETCARD + count);
                                                            if (temp == (OFFSETCARD + count)) {
                                                                llCard.clearAnimation();
                                                                mydguess = card.get(count);
                                                                bGame.setText("OK");
                                                                otherButton.setBackgroundResource(holo_orange_light);
                                                            } else {
                                                                otherButton.setBackgroundResource(background_dark);
                                                            }
                                                        }
                                                        int scrollX = (view.getLeft() - (hsvcard.getWidth() / 2)) + (view.getWidth() / 2);
                                                        hsvcard.smoothScrollTo(scrollX, 0);
                                                    }
                                                }
                                            });
                                            llCard.addView(imageButton);
                                        }
                                        if (card.size() == numofplayer) {                      // Cards are ready for guessing
                                            imageButton.setImageResource(getResources().getIdentifier(
                                                    "com.cherrimon.dixit:drawable/card_s0_" + card.get(count), null, null));
                                            if (card.get(count) == face[myindex]) {              // Oh, this is my card
                                                imageButton.setClickable(false);             // Not allow to be clicked
                                                imageButton.setColorFilter(Color.RED, PorterDuff.Mode.SCREEN);
                                            }
                                        } else
                                            imageButton.setImageResource(getResources().getIdentifier(
                                                    "com.cherrimon.dixit:drawable/card_s0_0", null, null));
                                    }
                                    // Usually, this point for new round
                                    else if (numofnullface >= numofplayer) {
                                        if (llCard.getChildCount() > 0) {                               // a round is completed
                                            showScoreDialog();                                          // show score
                                            if (lastscore[lasttellerindex] == score[lasttellerindex])   // storyteller lost
                                                GameSound(R.raw.laugh);
                                            else if (lastguess[myindex] == lastface[lasttellerindex])   // my guess was right
                                                GameSound(R.raw.right);
                                            else                                                        // my guess was wrong
                                                GameSound(R.raw.wrong);
                                            card.clear();                                               // Clear cards for next round
                                        }
                                        llCard.removeAllViews();                                        // remove all cards
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "RW Failed");
                            AlertDialog.Builder builder = new AlertDialog.Builder(UserAreaActivity.this);
                            builder.setMessage("RW Failed")
                                    .setNegativeButton("Retry", null)
                                    .create()
                                    .show();
                        }
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        };

        //取得此Button的實體, 實做OnClickListener界面
        bScore.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   showScoreDialog();
                   }
           });

        //取得此Button的實體, 實做OnClickListener界面
        bGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Host is allowed to start the game since minimum player requirement is met
                if(myname.equals(player[tellerindex]) && (numofplayer == 0) && (numofjoinedplayer >= MINPLAYER)) {
                    myguess = 1;                                        // command to start up game
                }
                // Card is picked by guess and this button is pressed
                else if((mydguess > 0) && (myguess == 0) && (guess[myindex] == 0) ){
                    myguess = mydguess;
                }
                // Normally, view your hands
                else{
                    dialog = new Dialog(UserAreaActivity.this);
                    dialog.setContentView(R.layout.dialog_hand);
                    //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before

                    editText = (EditText) dialog.findViewById(R.id.etStory);
                    textView = (TextView) dialog.findViewById(R.id.tvNotice);
                    bOK = (Button) dialog.findViewById(R.id.bOK);

                    // assume you have done what you need but you just wanna check your cards
                    // you have not pick a card yet
                    if(myname.equals(player[tellerindex]) && (face[myindex] == 0)) {                // you are storyteller
                        textView.setText("Pick a card and tell a story.");                              // you are allowed to tell a story
                        editText.setVisibility(View.VISIBLE);
                        bOK.setVisibility(View.VISIBLE);
                    }
                    // you are not storyteller, he has put a card and you have not yet
                    else{
                        textView.setText(story);
                        editText.setVisibility(View.GONE);
                        bOK.setVisibility(View.VISIBLE);
                    }
                    //Show your hands
                    final HorizontalScrollView hsvHand = (HorizontalScrollView) dialog.findViewById(R.id.hsvHand);
                    llHand = (LinearLayout) dialog.findViewById(R.id.llHand);
                    mydface = 0;                                        // Clear temporary face
                    for (count = 0; count < MAXHAND; count++) {
                        imageButton = new ImageButton(UserAreaActivity.this);
                        imageButton.setImageResource(getResources().getIdentifier(
                                "com.cherrimon.dixit:drawable/card_s0_" + myhand[count], null, null));
                        imageButton.setLayoutParams(new LayoutParams(CARDWIDHT, CARDHEIGHT));
                        imageButton.setPadding(20, 20, 20, 20);
                        imageButton.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageButton.setBackgroundResource(background_dark);
                        imageButton.setId(OFFSETHAND + count);
                        imageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                temp = view.getId();
                                for (count = 0; count < MAXHAND; count++) {
                                    ImageButton otherButton = (ImageButton) dialog.findViewById(OFFSETHAND + count);
                                    if (temp == (OFFSETHAND + count)) {
                                        bOK.startAnimation(animblink);
                                        llHand.clearAnimation();
                                        mydface = myhand[count];
                                        otherButton.setBackgroundResource(holo_orange_light);
                                    } else {
                                        otherButton.setBackgroundResource(background_dark);
                                    }
                                }
                                int scrollX = (view.getLeft() - (hsvHand.getWidth() / 2)) + (view.getWidth() / 2);
                                hsvHand.smoothScrollTo(scrollX, 0);
                            }
                        });
                        imageButton.setEnabled(false);                                  // Default as not allow to select face
                        if((mydface == 0) && (face[myindex] == 0)){                     // face is not selected
                            imageButton.setEnabled(true);                               // do not allow to select face
                            llHand.startAnimation(animblink);
                        }
                        llHand.addView(imageButton);
                    }

                    bOK.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            bOK.clearAnimation();
                            editText = (EditText) dialog.findViewById(R.id.etStory);
                            if(myname.equals(player[tellerindex])){                     // Only Storyteller need to input story
                                mystory = editText.getText().toString();                // sent mystory and myface together
                            }
                            myface = mydface;
                            dialog.dismiss();
                            llHand.removeAllViews();                                    // remove all hands

                        }
                    });
                    dialog.show();
                }
            }
        });
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onPause() {
        super.onPause();
//        Log.i(TAG, "onPause");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        Log.i(TAG, "onStop");
        handler.removeCallbacks(timedTask);             // Cancel background periodic function
        mpBackground.pause();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onRestart() {
        super.onRestart();
//        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.i(TAG, "onStart");
        handler.post(timedTask);
        if((MUSICEN == true) && (SOUNDEN == true))
            mpBackground.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.i(TAG, "onResume");
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        Log.i(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Log.i(TAG, "onDestroy");
    }
    // Game Sound

    protected void GameSound(int file) {
        if(SOUNDEN == true) {
            mpsound = MediaPlayer.create(UserAreaActivity.this, file);
            mpsound.setVolume(0.6f, 0.6f);
            mpsound.start();
        }
    }

    @Override
    public void onBackPressed() {
        if((mydguess > 0) && (myguess == 0)){               // allow to change mind only if guess is not sent
            mydguess = 0;                                   // clear mydguess
            llCard.startAnimation(animblink);               // Start Blinking on Cards again
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you sure");
            builder.setMessage("Press Back to rejoin later\nPress Leave to quit this room. ");
            builder.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Response received from the server
                    Response.Listener<String> quitresponseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success) {
                                    mpBackground.stop();                        // stop music
                                    handler.removeCallbacks(timedTask);         // Cancel background periodic function
                                    UserAreaActivity.super.onBackPressed();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(UserAreaActivity.this);
                                    builder.setMessage("Quit Failed")
                                            .setNegativeButton("Retry", null)
                                            .create()
                                            .show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    SingletonRequest.getInstance(UserAreaActivity.this).cancelQueue();                  // Remove other requests
                    quitRequest = new QuitRequest(myname, myroom, quitresponseListener);
                    SingletonRequest.getInstance(UserAreaActivity.this).addToRequestQueue(quitRequest);
                }
            });
            builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mpBackground.stop();                        // stop music
                    handler.removeCallbacks(timedTask);         // Cancel background periodic function
                    UserAreaActivity.super.onBackPressed();
                }
            });
            builder.show();
        }
    }


    // Display Dislog
    protected void showScoreDialog() {
        dialog = new Dialog(UserAreaActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        dialog.setContentView(R.layout.dialog_score);
        //Prepare Table
        tlScore = (TableLayout) dialog.findViewById(R.id.tlScore);
        tlScore.setStretchAllColumns(true);
        for(count = 0; count < numofplayer; count++) {
            //Set Title
            if(lastguess[count] == 0) {
                textView = (TextView) dialog.findViewById(R.id.tvScore);
                textView.setText("Last Story\n" + player[count] + " said:\n" + laststory);
            }
            // set card
            TableRow trScore = new TableRow(UserAreaActivity.this);
            trScore.setLayoutParams(row_layout);
            trScore.setGravity(Gravity.CENTER_HORIZONTAL);
            //  Show last face referring to Player
            imageView = new ImageView(UserAreaActivity.this);
            imageView.setImageResource(getResources().getIdentifier(
                    "com.cherrimon.dixit:drawable/card_s0_" + lastface[count], null, null));
            imageView.setLayoutParams(image_layout);

            trScore.addView(imageView);
            //  Prepare String display next to the cards
            textView = new TextView(UserAreaActivity.this);
            tString = "\n     By " + player[count] ;
            if(score[count] >= MAXSCORE) {
                textView.setTextColor(Color.YELLOW);
                tString += " (WINNER)";
            }
            tString += "\n     ( " + lastscore[count] + " -> " + score[count] + " )\n     ";
            for(int i = 0; i < numofplayer; i++) {
                if(lastguess[i] == lastface[count])
                    tString += player[i] + "\n";
            }
            textView.setText(tString);

            if(lastguess[count] == 0) {                                     // this was storyteller
                textView.setTextColor(Color.BLACK);
            }else{                                                          // this was just a player
                textView.setTextColor(Color.WHITE);
            }

            textView.setLayoutParams(view_layout);
            trScore.addView(textView);

            if(lastguess[count] == 0) {                                     // this is storyteller
                trScore.setBackgroundColor(Color.GREEN);
                lasttellerindex = count;
            }else                                                           // this was just a player
                trScore.setBackgroundColor(Color.BLACK);

            //  Add row to table
            tlScore.addView(trScore);
        }

        // Check if no view was added
        if(tlScore.getChildCount() == 0){
            TableRow trScore = new TableRow(UserAreaActivity.this);
            trScore.setLayoutParams(row_layout);
            trScore.setGravity(Gravity.CENTER_HORIZONTAL);
            textView = new TextView(UserAreaActivity.this);
            textView.setText("You are at the first round");
            textView.setLayoutParams(view_layout);
            trScore.addView(textView);
            //  Add row to table
            tlScore.addView(trScore);
        }

        // Toggle Music
        button = (Button) dialog.findViewById(R.id.bMusic);
        if(MUSICEN == false)
            button.setTextColor(Color.parseColor("gray"));
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                button = (Button) dialog.findViewById(R.id.bMusic);
                MUSICEN = !MUSICEN;
                if(MUSICEN == false) {
                    tString = "F";
                    button.setTextColor(Color.parseColor("gray"));
                    if((MUSICEN == false) || (SOUNDEN == false))
                        mpBackground.pause();
                }else{
                    tString = "T";
                    button.setTextColor(Color.parseColor("white"));
                    if((MUSICEN == true) && (SOUNDEN == true))
                        mpBackground.start();
                }
                fileIOStream.InputStream(fnMusic, tString);
            }
        });

        // Toggle Sound
        button = (Button) dialog.findViewById(R.id.bSound);
        if(SOUNDEN == false)
            button.setTextColor(Color.parseColor("gray"));
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                button = (Button) dialog.findViewById(R.id.bSound);
                SOUNDEN = !SOUNDEN;
                if(SOUNDEN == false) {
                    tString = "F";
                    button.setTextColor(Color.parseColor("gray"));
                    if((MUSICEN == false) || (SOUNDEN == false))
                        mpBackground.pause();
                }else{
                    tString = "T";
                    button.setTextColor(Color.parseColor("white"));
                    if((MUSICEN == true) && (SOUNDEN == true))
                        mpBackground.start();
                }
                fileIOStream.InputStream(fnSound, tString);
            }
        });
        dialog.show();
    }

    // Timer
    public class timerTask extends TimerTask
    {
        public void run()
        {
            if(requestTimer < requestTimerLimit)
                requestTimer++;
        }
    };
    // Periodic Request
    private Runnable timedTask = new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
//        public void freeMemory(){
            System.runFinalization();
            Runtime.getRuntime().gc();
            System.gc();
//        }
//            Log.d(TAG, "myname: " + myname + " &  myroom: " + myroom + " &  mystory: " + mystory + " & myface: " + myface + " & myguess: " + myguess);
            // only if last request is done
            if(requestTimer < requestTimerLimit) {
                PHPRETRYCOUNT = 0;                                          // Clear retry count to keep track of connection
                Log.d(TAG, " myname: " + myname + " &  myroom: " + myroom + " &  mystory: " + mystory);
                rwRequest = new RWRequest(myname, myroom, mystory, myface, myguess, responseListener);
                SingletonRequest.getInstance(UserAreaActivity.this).addToRequestQueue(rwRequest);
            }else{
                PHPRETRYCOUNT++;                                            // Clear retry count to keep track of connection
                // SERVER ERROR
                if(PHPRETRYCOUNT > PHPRETRYTIMEOUT){
                    PHPRETRYCOUNT = 0;                                      // Clear retry count to keep track of connection
                    requestTimer = 0;                                       //
                    Toast.makeText(getApplicationContext(), "Server Error, retrying", Toast.LENGTH_LONG).show();
                    SingletonRequest.getInstance(UserAreaActivity.this).cancelQueue();
                }
            }

            handler.postDelayed(timedTask, PHPREQUESTPEROID);

            // Animation
            bGame.clearAnimation();                                     // No Animation as default
            // Display user details
            // Host is allowed to start the game since minimum player requirement is met
            if(myname.equals(player[tellerindex]) && (numofplayer == 0) && (numofjoinedplayer >= MINPLAYER)) {
                bGame.setEnabled(true);
                bGame.setText("Start the Game!");
                bGame.startAnimation(animblink);
            }
            // Card is picked by guessing and not guess previously - Storyteller should not be here
            else if((numofplayer >= MINPLAYER) && (mydguess > 0) && (guess[myindex] == 0)){
                bGame.setEnabled(true);
                bGame.setText("OK");
                bGame.startAnimation(animblink);
            }
            // Not enough player
            else if(numofplayer >= MINPLAYER){
                if(((myface > 0) && (mydface > 0)) || ((myguess > 0) && (mydguess > 0))){
                    bGame.setEnabled(false);
                    bGame.setText("Putting onto Table");
                }else{
                    bGame.setEnabled(true);
                    bGame.setText("Look at my cards");
                }
            }
            // Not enough player - probably
            else{
                bGame.setEnabled(false);
                bGame.setText("Waiting for player...");
            }

            tvStoryteller = (TextView) findViewById(R.id.tvStoryteller);
            tvStoryteller.setText(player[tellerindex] + " said : ");

            tvStory = (TextView) findViewById(R.id.tvStory);
            tvStory.setText(story);

            tvStatus = (TextView) findViewById(R.id.tvStatus);
            tvStatus.setTextColor(Color.WHITE);

            // Hints:
            if(numofplayer < MINPLAYER){
                tString = "Joined player : ";
                for(count = 0; count < numofjoinedplayer; count++){
                    if(tString.equals("Joined player : "))
                        tString += player[count];
                    else
                        tString += " , " + player[count];
                }
            }
            else if(myname.equals(player[tellerindex])){                // you are storyteller
                if(face[tellerindex] == 0) {                            // you have not tell your story yet
                    tvStatus.setTextColor(Color.YELLOW);
                    tString = "Storyteller, everyone is waiting!";
                    bGame.startAnimation(animblink);
                }
                else if(face[tellerindex] != 0 && numofnullface > 0) {  // someone has not put their face
                    tString = "waiting for... ";
                    for(count = 0; count < numofplayer; count++){
                        if(face[count] == 0)
                            tString += player[count] + " , ";
                    }
                }
                else if(numofnullguess > 1) {                           // someone has not said their guess
                    tString = "waiting for... ";
                    for(count = 0; count < numofplayer; count++){
                        if(guess[count] == 0 && count != tellerindex)  // except for storyteller
                            tString += player[count] + " , ";
                    }
                }
            }
            else{                                                       // you are not storyteller
                if(face[tellerindex] == 0) {                            // you have not tell your story yet
                    tString = "waiting for  storyteller  " + player[tellerindex];
                }
                else if((face[tellerindex] != 0 && face[myindex] == 0)                         // you have not put your face yet
                    || (numofnullface == 0 && numofnullguess > 1 && guess[myindex] == 0)){     // Guessing stage but you have not put your guess yet
                    tvStatus.setTextColor(Color.RED);
                    tString = "YOUR TURN:  select a suitable card.";
                    if(face[myindex] == 0)                                                      // you are going to pick a card
                        bGame.startAnimation(animblink);
                }
                else if(face[tellerindex] != 0 && numofnullface > 0) {                          // someone has not put their face
                    tString = "waiting for... ";
                    for(count = 0; count < numofplayer; count++){
                        if(face[count] == 0)
                            tString += player[count] + " , ";
                    }
                }
                else if(numofnullguess > 1) {                               // someone has not said their guess
                    tString = "waiting for... ";
                    for(count = 0; count < numofplayer; count++){
                        if(guess[count] == 0 && count != tellerindex)        // except for storyteller
                            tString += player[count] + " , ";
                    }
                }
            }
            tvStatus.setText(tString);

            // PLAYER LEFT CHECK
            if(numofjoinedplayer < numofplayer && numofplayer >= MINPLAYER){
                tvStoryteller.setTextColor(Color.RED);
                tvStoryteller.setText("Someone has fucking left!!!");
                tvStory.setTextColor(Color.RED);
                tvStory.setText("Game cannot be go on");
                tvStatus.setTextColor(Color.YELLOW);
                tvStatus.setText("You may now leave the room.");
                handler.removeCallbacks(timedTask);             // Cancel background periodic function
            }

            // END GAME CHECK
            tString = "";
            for(count = 0; count < numofplayer; count++) {
                if(score[count] >= MAXSCORE){
                    GameSound(R.raw.clap);                          // Clap once only
                    tvStoryteller.setTextColor(Color.RED);
                    tvStoryteller.setText("");
                    tvStory.setTextColor(Color.YELLOW);
                    tvStory.setText("This game is finished");
                    tvStatus.setTextColor(Color.RED);
                    tvStatus.setText("You may now leave the room.");
                    handler.removeCallbacks(timedTask);             // Cancel background periodic function
                }
            }
        }
    };

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(animation == animblink){

        }
        if(animation == animrotate){

        }
        if(animation == animslide){

        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
