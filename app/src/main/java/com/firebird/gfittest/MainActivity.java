package com.firebird.gfittest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.HistoryClient;
import com.google.android.gms.fitness.SessionsClient;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    String TAG = "NEEL";
    GoogleSignInAccount account;

    static String PERIOD_START_DATE_TIME = "2020-08-9T12:00:00Z";
    static String PERIOD_END_DATE_TIME = "2020-08-17T12:00:00Z";

    static String HR_PERIOD_START_DATE_TIME = "2022-10-10T12:00:00Z";
    static String HR_PERIOD_END_DATE_TIME = "2022-10-14T12:00:00Z";

    private long periodStartMillis;
    private long periodEndMillis;

    private long periodStartMillisHR;
    private long periodEndMillisHR;

    private long millisFromRfc339DateString(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    .parse(dateString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return Long.parseLong(null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Defines the start and end of the period of interest in this example.
        periodStartMillis = millisFromRfc339DateString(PERIOD_START_DATE_TIME);
        periodEndMillis = millisFromRfc339DateString(PERIOD_END_DATE_TIME);

        periodStartMillisHR = millisFromRfc339DateString(HR_PERIOD_START_DATE_TIME);
        periodEndMillisHR = millisFromRfc339DateString(HR_PERIOD_END_DATE_TIME);

        Log.d("NEEL", "onCreate: start");
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.ACTIVITY_RECOGNITION},
                    200);

        } else {
            FitnessOptions fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                    .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                    .build();

            account = GoogleSignIn.getAccountForExtension(this,
                    fitnessOptions);


            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                GoogleSignIn.requestPermissions(
                        this, // your activity
                        200, // e.g. 1
                        account,
                        fitnessOptions);
                Log.d("NEEL", "onCreate: Request permission");
            } else {
//                accessExerciseGoogleFit(fitnessOptions);
                // readHistoryData();
                //readSleepSessions();
                readHeartRateData();
                Log.d("NEEL", "onCreate: Access data");
            }
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i("Neel", "Data returned for Data type: "+dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i("Neel","Data point:");
            Log.i("Neel","\tType: "+dp.getDataType().getName());
            Log.i("Neel","\tStart: "+dp.getStartTime(TimeUnit.DAYS));
            Log.i("Neel","\tEnd: "+dp.getEndTime(TimeUnit.DAYS));
            for (Field field : dp.getDataType().getFields()) {
                Log.i("Neel",
                        "\tField: "+field.getName()
                                + " Value: " +dp.getValue(field).asInt());
            }
        }
    }

/*
    private String getStartTimeString() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(1);
        long startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond();

        return Instant.ofEpochSecond(startSeconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime().toString();
    }


    private String getEndTimeString() {
        LocalDateTime end = LocalDateTime.now();
        long endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond();

        return Instant.ofEpochSecond(endSeconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime().toString();
    }

    void accessGoogleFit(FitnessOptions fitnessOptions) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(1);
        long endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond();
        long startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this,
                fitnessOptions);
        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(dataReadResponse -> {
                    Log.d("NEEL", "accessGoogleFit: Success");
                    Log.d("NEEL", "accessGoogleFit:"+dataReadResponse.getBuckets()
                            .get(0).getActivity());
                    while (dataReadResponse.getDataSets().iterator().hasNext()) {
                        Log.d("NEEL", "accessGoogleFit: data");
                    }
                    //dataReadResponse.getDataSets();
                })
                .addOnFailureListener(e -> {
                    Log.d("NEEL", "accessGoogleFit: Failure");
                });
    }

    void accessExerciseGoogleFit(FitnessOptions fitnessOptions) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(1);
        long endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond();
        long startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByActivityType(1, TimeUnit.SECONDS)
                .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(dataReadResponse -> {
                    Log.d("NEEL", "accessGoogleFit: Success");
                    for(DataSet dataset : dataReadResponse.getDataSets()) {
                        dumpDataSet(dataset);
                    }

                    //dataReadResponse.getDataSets();
                })
                .addOnFailureListener(e -> {
                    Log.d("NEEL", "accessGoogleFit: Failure");
                });
    }

     */

    /** Returns a [DataReadRequest] for all step count changes in the past week.  */
    DataReadRequest queryFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Date now = new Date();
        calendar.setTime(now);
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = calendar.getTimeInMillis();

//        Log.i(TAG, "Range Start: ${dateFormat.format(startTime)}");
//        Log.i(TAG, "Range End: ${dateFormat.format(endTime)}");

        return new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    private Task<DataReadResponse> readHistoryData() {
        // Begin by creating the query.
        DataReadRequest readRequest = queryFitnessData();

        // Invoke the History API to fetch the data with the query
        return Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(dataReadResponse -> {
                    printData(dataReadResponse);
                })
                .addOnFailureListener(e -> {

                });
    }

    private void printData( DataReadResponse dataReadResult) {
        if (!dataReadResult.getBuckets().isEmpty()) {
            Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult
                    .getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                for (DataSet dataSet : bucket.getDataSets()) {
                    dumpDataSet(dataSet);
                }

            }
        } else if (!dataReadResult.getDataSets().isEmpty()) {
            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult
                    .getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }



    // Sleep
    private void readSleepSessions() {
         SessionsClient client = Fitness.getSessionsClient(this, account);

         SessionReadRequest sessionReadRequest = new SessionReadRequest.Builder()
                .read(DataType.TYPE_SLEEP_SEGMENT)
                // By default, only activity sessions are included, not sleep sessions. Specifying
                // includeSleepSessions also sets the behaviour to *exclude* activity sessions.
                .includeSleepSessions()
                .readSessionsFromAllApps()
                .setTimeInterval(periodStartMillis, periodEndMillis, TimeUnit.MILLISECONDS)
                .build();

        client.readSession(sessionReadRequest)
                .addOnSuccessListener(sessionReadResponse -> {
                    Log.d(TAG, "readSleepSessions: Sleep session read successful");
                    try {
                        JSONObject sleepData = dumpSleepSessions(sessionReadResponse);
                        Log.d(TAG, "readSleepSessions: "+sleepData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "readSleepSessions: Sleep session read not successful");
                });
    }

    private JSONObject dumpSleepSessions(SessionReadResponse response) throws JSONException {
        JSONObject sleepData = new JSONObject();
        JSONArray sleepSessions = new JSONArray();
        for (Session session : response.getSessions()) {
            JSONObject sleepSession = dumpSleepSession(session, response.getDataSet(session));
            sleepSessions.put(sleepSession);
        }
        sleepData.put("sleep_data", sleepSessions);
        return sleepData;
    }

    private JSONObject dumpSleepSession(Session session,List<DataSet> dataSets) throws JSONException {
        JSONObject sleepSession = new JSONObject();
        Pair startEndTimePair = dumpSleepSessionMetadata(session);
        Long totalSleepForNight = calculateSessionDuration(session);
        JSONArray sleepDataPoints = dumpSleepDataSets(dataSets);
        sleepSession.put("start_time", startEndTimePair.first);
        sleepSession.put("end_time", startEndTimePair.second);
        sleepSession.put("total_sleep", totalSleepForNight.intValue());
        sleepSession.put("stages", sleepDataPoints);
        return sleepSession;
    }

    private Pair<String, String> dumpSleepSessionMetadata(Session session) {
        Pair<String, String> timePair = getSessionStartAndEnd(session);
        String startDateTime =  timePair.first;
        String endDateTime = timePair.second;

        Long totalSleepForNight = calculateSessionDuration(session);
        Log.i(TAG, startDateTime+" to "+endDateTime+" ("+totalSleepForNight+" mins)");
        return timePair;
    }

    List<String> SLEEP_STAGES = Arrays.asList(
            "Unused",
            "Awake (during sleep)",
            "Sleep",
            "Out-of-bed",
            "Light sleep",
            "Deep sleep",
            "REM sleep"
    );

    private JSONArray dumpSleepDataSets(List<DataSet> dataSets) throws JSONException {
        JSONArray sleepDataPointsJson = new JSONArray();
        for (DataSet dataSet : dataSets) {
            for (DataPoint dataPoint : dataSet.getDataPoints()) {
                int sleepStageOrdinal = dataPoint.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt();
                String sleepStage = SLEEP_STAGES.get(sleepStageOrdinal);

                Long durationMillis = dataPoint.getEndTime(TimeUnit.MILLISECONDS) - dataPoint.getStartTime(TimeUnit.MILLISECONDS);
                Long duration = TimeUnit.MILLISECONDS.toMinutes(durationMillis);
                Log.i(TAG, "\t"+sleepStage+": "+duration+" (mins)");

                JSONObject sleepDpJson = new JSONObject();
                sleepDpJson.put("type", sleepStage);
                sleepDpJson.put("mins", duration.intValue());
                sleepDataPointsJson.put(sleepDpJson);
            }
        }
        return sleepDataPointsJson;
    }

    private Pair<String, String> getSessionStartAndEnd(Session session)
    {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String startDateTime = dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS));
        String endDateTime = dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS));
        Pair<String, String> mPair = new Pair<>(startDateTime, endDateTime);
        return mPair;
    }

    private Long calculateSessionDuration(Session session) {
        Long total = session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);
        return TimeUnit.MILLISECONDS.toMinutes(total);
    }



    // Heart Rate
    private void readHeartRateData() {
        //SessionsClient client = Fitness.getSessionsClient(this, account);
        HistoryClient client = Fitness.getHistoryClient(this, account);

        DataReadRequest hrReadRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_HEART_RATE_BPM)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(periodStartMillisHR, periodEndMillisHR, TimeUnit.MILLISECONDS)
                .build();

        client.readData(hrReadRequest)
                .addOnSuccessListener(dataReadResponse -> {
                    Log.d(TAG, "readHeartRateData: Heart rate read successful");
                    try {
                        JSONObject hrData = dumpHeartRate(dataReadResponse);
                        Log.d(TAG, "readHeartRateData: "+hrData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(e -> {

                });
    }

    private JSONObject dumpHeartRate(DataReadResponse response) throws JSONException {
        JSONObject heartRateData = new JSONObject();
        JSONArray heartRates = new JSONArray();

        for (Bucket bucket : response.getBuckets()) {
            for (DataSet dataSet: bucket.getDataSets()) {
                for(DataPoint dataPoint : dataSet.getDataPoints()) {
                    JSONObject dataPointJson = new JSONObject();
                    DateFormat dateFormat = DateFormat.getDateTimeInstance();
                    String endDateTime = dateFormat.format(dataPoint.getEndTime(TimeUnit.MILLISECONDS));
                    dataPointJson.put("time", endDateTime);
                    for(Field field: dataPoint.getDataType().getFields()) {
                        Log.d(TAG, "dumpHeartRate: Time: "+endDateTime
                                +" Heart Rate "+field.getName()+": "+dataPoint.getValue(field));
                        dataPointJson.put(field.getName(), dataPoint.getValue(field));
                    }
                   heartRates.put(dataPointJson);
                }
            }
        }
        heartRateData.put("heart_rates", heartRates);
        return heartRateData;
    }

}