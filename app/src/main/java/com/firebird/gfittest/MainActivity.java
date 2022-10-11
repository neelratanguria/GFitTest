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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    String TAG = "NEEL";
    GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                readHistoryData();
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
}