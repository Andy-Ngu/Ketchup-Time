package com.ketchuptime;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.ketchuptime.R;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;



public class MainActivity extends AppCompatActivity {

    private TextView mTvDesc;
    private TextView mTvDesc2;
    private TextView mTvDesc3;
    private TextView mTvDesc4;
    private TextView mTvTime;
    private TextView mTvSbval;
    private TextView mTvBreakval;
    private TextView mTvCompval;
    private TextView mTvTotst;
    private TextView mTvPeriod;
    private TextView mTvPausecount;
    private TextView mTvCalmonth;
    private TextView mTvCaldisp;
    private TextView mTvCaldate;
    private Button mBtnStart;
    private Button mBtnPause;
    private Button mBtnStop;
    private SeekBar mSbVal;

    private MediaPlayer mpshort;
    private MediaPlayer mplong;

    private int mBreaksCounter = 0;
    private int mStudiedCounter=0;
    private int mPauseCounter = 0;

    private int sbStep = 1;
    private int sbMax = 45;
    private int sbMin = 1;                  // seekbar value: 1 for demo | 5 for ship
    private int sbVal = sbMin;

    private int sessionBreakVal = 1;        // break duration: 1 for demo | 5 for ship
    private int sessionSbVal = 0;
    private int totalCompletions = 2;       // total study periods: 2 for demo | 5 for ship
    private int totalBreaks = totalCompletions - 1;
    private int totalPauses = 5;

    private long sessionLength = 0;
    private long globmill = 0;
    private long currPause = 0;
    private long currLength = 0;

    private boolean studyTime = true;
    private boolean isPaused = false;

    private CounterClass timer;


    //calendar
    CompactCalendarView compactCalendarView;
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMMM- yyyy", Locale.getDefault());
    private Calendar currentCalender = Calendar.getInstance(Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tab Setup
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        // Tab Main Create
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("main");
        tabSpec.setContent(R.id.tabMain);
        tabSpec.setIndicator("", getResources().getDrawable(R.drawable.home2)); // " " required or picture doesn't show up

        tabHost.addTab(tabSpec);

        // Tab Timer Create
        tabSpec = tabHost.newTabSpec("timer");
        tabSpec.setContent(R.id.tabTimer);
        tabSpec.setIndicator("", getResources().getDrawable(R.drawable.alarm));
        tabHost.addTab(tabSpec);

        // Tab Calendar Create
        tabSpec = tabHost.newTabSpec("calendar");
        tabSpec.setContent(R.id.tabCal);
        tabSpec.setIndicator("", getResources().getDrawable(R.drawable.calendar));
        tabHost.addTab(tabSpec);

        // ID Partner
        mTvDesc = (TextView) findViewById(R.id.tv_desc);
        mTvDesc2 = (TextView) findViewById(R.id.tv_desc2);
        mTvDesc3 = (TextView) findViewById(R.id.tv_desc3);
        mTvDesc4 = (TextView) findViewById(R.id.tv_desc4);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvSbval = (TextView) findViewById(R.id.tv_sbval);
        mTvBreakval = (TextView) findViewById(R.id.tv_breakval);
        mTvCompval = (TextView) findViewById(R.id.tv_Compval);
        mTvTotst = (TextView) findViewById(R.id.tv_totst);
        mTvPeriod = (TextView) findViewById(R.id.tv_period);
        mTvPausecount = (TextView) findViewById(R.id.tv_pausecount);
        mTvCalmonth = (TextView) findViewById(R.id.tv_calmonth);
        mTvCaldate = (TextView) findViewById(R.id.tv_caldate);
        mTvCaldisp = (TextView) findViewById(R.id.tv_caldisp);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnPause = (Button) findViewById(R.id.btn_pause);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mSbVal = (SeekBar) findViewById(R.id.sb_val);
        mSbVal.setMax((sbMax - sbMin)/ sbStep);

        mpshort = MediaPlayer.create(this, R.raw.alarmshort);
        mplong = MediaPlayer.create(this, R.raw.alarmlong);

        // Setup
        descText();
        String valOfComplete = String.format("Studied\n%d / %d", mStudiedCounter, totalCompletions);
        mTvCompval.setText(valOfComplete);
        String valofBreaks = String.format("Breaks\n%d / %d", mBreaksCounter, totalBreaks);
        mTvBreakval.setText(valofBreaks);
        String valOfSb = String.format("Timer\n%02d:00", sbVal);
        mTvSbval.setText(valOfSb);
        sessionLength = ((sessionBreakVal * totalBreaks) + (sbVal * totalCompletions)) * 60 * 1000;
        String hms = String.format("Session Length\n%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(sessionLength),
                TimeUnit.MILLISECONDS.toMinutes(sessionLength) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(sessionLength)),
                TimeUnit.MILLISECONDS.toSeconds(sessionLength) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sessionLength)));
        mTvTotst.setText(hms);
        String valofPauses = String.format("Pauses\n%d / %d", mPauseCounter, totalPauses);
        mTvPausecount.setText(valofPauses);


        //calendar stuff
        compactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        //compactCalendarView.drawSmallIndicatorForEvents(true);
        compactCalendarView.setUseThreeLetterAbbreviation(true);
        String cm = dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth());
        cm = cm.replace("-", "");
        mTvCalmonth.setText(cm);

        // DEMO DATES
        addDummyDates();

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                List<Event> events = compactCalendarView.getEvents(dateClicked);
                String temp = String.format("%s", dateClicked);
                String day = temp.substring(0, 3);
                String month = temp.substring(4, 7);
                String date = temp.substring(8, 10);
                String year = temp.substring(24, 28);
                temp = day + ", " + month + " " + date + ", " + year + '\n';
                mTvCaldate.setText(temp);

                String disp = "";
                if (!events.isEmpty()) {
                    for (int i = 0; i < events.size(); i++) {
                        String str = events.get(i).toString();
                        int endindex = str.indexOf("}");
                        int startindex = str.indexOf("data=") + 5;
                        str = str.substring(startindex, endindex);
                        long longstr = Long.parseLong(str);
                        String dispstudy = String.format("\nSession %d: %02d:%02d:%02d",
                                i,
                                TimeUnit.MILLISECONDS.toHours(longstr),
                                TimeUnit.MILLISECONDS.toMinutes(longstr) -
                                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(longstr)),
                                TimeUnit.MILLISECONDS.toSeconds(longstr) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(longstr)));

                        disp += dispstudy;
                    }
                }
                mTvCaldisp.setText(disp);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                String cm = dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth());
                cm = cm.replace("-", "");
                mTvCalmonth.setText(cm);

            }

        });



        mSbVal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sbVal = sbMin + (progress * sbStep);
//                Toast.makeText(getApplicationContext(), String.valueOf(sbVal), Toast.LENGTH_LONG).show();
                String valOfSb = String.format("Timer\n%02d:00", sbVal);
                mTvSbval.setText(valOfSb);

                if (timer == null) {
                    sessionLength = ((sessionBreakVal * totalBreaks) + (sbVal * totalCompletions)) * 60 * 1000;
                    String hms = String.format("Session Length\n%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(sessionLength),
                            TimeUnit.MILLISECONDS.toMinutes(sessionLength) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(sessionLength)),
                            TimeUnit.MILLISECONDS.toSeconds(sessionLength) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sessionLength)));
                    mTvTotst.setText(hms);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Button Start Pressed
                if (timer == null && !isPaused) {
                    sessionSbVal = sbVal;
                    sessionLength = ((sessionBreakVal * totalBreaks) + (sessionSbVal * totalCompletions)) * 60 * 1000;
                    String hms = String.format("Session Length\n%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(sessionLength),
                            TimeUnit.MILLISECONDS.toMinutes(sessionLength) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(sessionLength)),
                            TimeUnit.MILLISECONDS.toSeconds(sessionLength) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sessionLength)));
                    mTvTotst.setText(hms);

                    studyTime = true;
                    mTvPeriod.setText("Study Period");

                    isPaused = false;
                    mPauseCounter = 0;
                    String valofPauses = String.format("Pauses\n%d / %d", mPauseCounter, totalPauses);
                    mTvPausecount.setText(valofPauses);

                    mBreaksCounter = 0;
                    String valofBreaks = String.format("Breaks\n%d / %d", mBreaksCounter, totalBreaks);
                    mTvBreakval.setText(valofBreaks);

                    mStudiedCounter = 0;
                    String valOfComplete = String.format("Studied\n%d / %d", mStudiedCounter, totalCompletions);
                    mTvCompval.setText(valOfComplete);

                    timer = new CounterClass((60 * 1000 * sessionSbVal), 1000);
                    timer.start();
                }

            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;

                    if (mBreaksCounter > 0 || mStudiedCounter > 0) {
                        currLength = ((sessionBreakVal * mBreaksCounter) + (sessionSbVal * mStudiedCounter)) * 60 * 1000;
                        studyToCal(currLength);
                    }
                    currLength = 0;

                    mTvTime.setText("00:00");

                    studyTime = true;
                    mTvPeriod.setText("Study Period");

                    isPaused = false;
                    mPauseCounter = 0;
                    String valofPauses = String.format("Pauses\n%d / %d", mPauseCounter, totalPauses);
                    mTvPausecount.setText(valofPauses);
                    mBtnPause.setText("Pause");

                    mBreaksCounter = 0;
                    String valofBreaks = String.format("Breaks\n%d / %d", mBreaksCounter, totalBreaks);
                    mTvBreakval.setText(valofBreaks);

                    mStudiedCounter = 0;
                    String valOfComplete = String.format("Studied\n%d / %d", mStudiedCounter, totalCompletions);
                    mTvCompval.setText(valOfComplete);

                    sessionLength = ((sessionBreakVal * totalBreaks) + (sbVal * totalCompletions)) * 60 * 1000;
                    String hms = String.format("Session Length\n%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(sessionLength),
                            TimeUnit.MILLISECONDS.toMinutes(sessionLength) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(sessionLength)),
                            TimeUnit.MILLISECONDS.toSeconds(sessionLength) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sessionLength)));
                    mTvTotst.setText(hms);
                }
            }
        });

        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer == null) {
                    return;
                }

                if (!isPaused) {
                    if (mPauseCounter < totalPauses) {
                        mPauseCounter++;
                        String valofPauses = String.format("Pauses\n%d / %d", mPauseCounter, totalPauses);
                        mTvPausecount.setText(valofPauses);

                        currPause = globmill;
                        timer.cancel();

                        isPaused = true;
                        mBtnPause.setText("Resume");

                    } else {
                        mTvPausecount.setText("Pauses\nToo many!");
                    }
                }
                else {
                    timer = new CounterClass((currPause), 1000);
                    timer.start();

                    isPaused = false;
                    mBtnPause.setText("Pause");
                }
            }
        });
    }




    public class CounterClass extends CountDownTimer {

        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onTick(long millisUntilFinished) {
            globmill = millisUntilFinished;
            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            //System.out.println(hms);
            mTvTime.setText(hms);
        }

        @Override
        public void onFinish() {
            // stop timer
            timer.cancel();
            timer = null;     //to prevent bug of resting on completed time and not allowing start button to start another timer

            if (studyTime) {
                // Increment completed and change
                mStudiedCounter++;
                String valOfComplete = String.format("Studied\n%d / %d", mStudiedCounter, totalCompletions);
                mTvCompval.setText(valOfComplete);

                if (mStudiedCounter >= totalCompletions) {
                    mTvPeriod.setText("Session Ended");
                    mTvTime.setText("Great!");
                    mpshort.start();
                    studyToCal(sessionLength);

                    studyTime = true;

                    isPaused = false;
                    mPauseCounter = 0;
                    String valofPauses = String.format("Pauses\n%d / %d", mPauseCounter, totalPauses);
                    mTvPausecount.setText(valofPauses);

                    mBreaksCounter = 0;
                    String valofBreaks = String.format("Breaks\n%d / %d", mBreaksCounter, totalBreaks);
                    mTvBreakval.setText(valofBreaks);

                    mStudiedCounter = 0;
                    valOfComplete = String.format("Studied\n%d / %d", mStudiedCounter, totalCompletions);
                    mTvCompval.setText(valOfComplete);

                    notifyEndSession();

                    return;
                }

                // Notify end of study period
                notifEndStudy();

                // Create break timer and swap period text
                timer = new CounterClass((60 * 1000 * sessionBreakVal), 1000);
                timer.start();

                mTvPeriod.setText("Break Period");
                mplong.start();

                studyTime = false;

            }
            else {
                mBreaksCounter++;
                String valofBreaks = String.format("Breaks\n%d / %d", mBreaksCounter, totalBreaks);
                mTvBreakval.setText(valofBreaks);

                notifEndBreak();

                timer = new CounterClass((60 * 1000 * sessionSbVal), 1000);
                timer.start();

                mTvPeriod.setText("Study Period");
                mplong.start();

                studyTime = true;
            }

        }
    }

    public void updateTimerText(final String time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvTime.setText(time);
            }
        });
    }

    private void descText() {
        String s = "For many people, time is an enemy. We race against the clock to finish assignments and meet deadlines. " +
                "Ketchup Time helps you to work with time, instead of struggling against it.";
        String s2 = String.format("Essential to Ketchup Time is the notion that taking short, %d minute breaks between study sessions eliminates the “running on fumes” feeling you get when you’ve pushed yourself too hard.", sessionBreakVal);
        String s3 = String.format("Whether it’s a call, or a Facebook message, many distracting thoughts and events come up when you’re at work. Ketchup Time allows you %d pauses per study session to deal with distractions.", totalPauses);
        String s4 = "If we haven’t had a productive day, it’s pretty easy to end up feeling like we can’t enjoy our free time. " +
                "Becoming a Ketchup Time Master involves creating an effective timetable, allowing you to truly enjoy your time off.";


        mTvDesc.setText(s);
        mTvDesc2.setText(s2);
        mTvDesc3.setText(s3);
        mTvDesc4.setText(s4);
    }

    private void notifEndStudy() {
        NotificationCompat.Builder endStudyNoteBuild =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.alarm)
                        .setContentTitle("Study Period Over")
                        .setContentText("Entering Break Period");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        endStudyNoteBuild.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(0, endStudyNoteBuild.build());
    }

    private void notifEndBreak() {
        NotificationCompat.Builder endBreakNoteBuild =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.alarm)
                        .setContentTitle("Break Period Over")
                        .setContentText("Entering Study Period");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        endBreakNoteBuild.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(0, endBreakNoteBuild.build());

    }

    private void notifyEndSession() {
        NotificationCompat.Builder endSessionNoteBuild =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.alarm)
                        .setContentTitle("Study Session Ended")
                        .setContentText("Good job!");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        endSessionNoteBuild.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(0, endSessionNoteBuild.build());
    }

    private void addDummyDates() {
        Event ev2 = new Event(Color.RED, 1480794443000L, "18263000");
        compactCalendarView.addEvent(ev2, true);

        Event ev1 = new Event(Color.RED, 1480765643000L, "14400000");
        compactCalendarView.addEvent(ev1, true);

        Event ev3 = new Event(Color.RED, 1480880843000L, "18000000");
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);
        compactCalendarView.addEvent(ev3, true);

        Event ev4 = new Event(Color.RED, 1481027166000L, "2320000");
        compactCalendarView.addEvent(ev4, true);

        Event ev5 = new Event(Color.RED, 1481199966000L, "1320000");
        compactCalendarView.addEvent(ev5, true);

        Event ev6 = new Event(Color.RED, 1481459166000L, "2120000");
        compactCalendarView.addEvent(ev6, true);

        Event ev7 = new Event(Color.RED, 1481545566000L, "920000");
        compactCalendarView.addEvent(ev7, true);

    }

    private void studyToCal(long x) {
        // get epoch date to 13digits
        long epoch = System.currentTimeMillis() / 1000;
        String epochstr = Long.toString(epoch);
        while (epochstr.length() < 13) {
            epochstr += "0";
        }
        epoch = Long.parseLong(epochstr);

        //add event
        compactCalendarView.addEvent(new Event(Color.RED, epoch, x), true);
    }
}

