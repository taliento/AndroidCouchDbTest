package com.taliento.davide.couchdbtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.couchbase.lite.*;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;


public class TestActivity extends AppCompatActivity {

    private static final String REPLICA_URL = "http://10.0.3.2:5984/android";//your replica couchdb server

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final String TAG = "HelloWorld";
        Log.d(TAG, "Begin Hello World App");


        // create a manager
        Manager manager;
        try {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);


            Log.d(TAG, "Manager created");
        } catch (IOException e) {
            Log.e(TAG, "Cannot create manager object");
            return;
        }

        // create a name for the database and make sure the name is legal
        String dbname = "hello";
        if (!Manager.isValidDatabaseName(dbname)) {
            Log.e(TAG, "Bad database name");
            return;
        }
        // create a new database
        Database database;
        try {
            database = manager.getDatabase(dbname);
            Log.d(TAG, "Database created");
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot get database");
            return;
        }

        //setting replica
        URL url = null;
        try {
            url = new URL(REPLICA_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if(url != null){
            Replication push = database.createPushReplication(url);
            Replication pull = database.createPullReplication(url);
            pull.setContinuous(true);
            push.setContinuous(true);

            push.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    // will be called back when the push replication status changes
                    Log.d(TAG,"push replication status changes: "+event);
                }
            });
            pull.addChangeListener(new Replication.ChangeListener() {
                @Override
                public void changed(Replication.ChangeEvent event) {
                    // will be called back when the pull replication status changes
                    Log.d(TAG,"pull replication status changes: "+event);
                }
            });
            push.start();
            pull.start();
//            this.push = push;
//            this.pull = pull;
        }




        // get the current date and time
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());
        // create an object that contains data for a document
        Map<String, Object> docContent = new HashMap<String, Object>();
        docContent.put("message", "Hello Couchbase Lite");
        docContent.put("creationDate", currentTimeString);
        // display the data for the new document
        Log.d(TAG, "docContent=" + String.valueOf(docContent));
        // create an empty document
        Document document = database.createDocument();
        // add content to document and write the document to the database
        try {
            document.putProperties(docContent);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }
        // save the ID of the new document
        String docID = document.getId();

        // retrieve the document from the database
        Document retrievedDocument = database.getDocument(docID);
        // display the retrieved document
        Log.d(TAG, "retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));

        // update the document
        Map<String, Object> updatedProperties = new HashMap<String, Object>();
        updatedProperties.putAll(retrievedDocument.getProperties());
        updatedProperties.put ("message", "We're having a heat wave!");
        updatedProperties.put ("temperature", "95");
        try {
            retrievedDocument.putProperties(updatedProperties);
            Log.d(TAG, "updated retrievedDocument=" + String.valueOf(retrievedDocument.getProperties()));
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot update document", e);
        }

        // delete the document
//        try {
//            retrievedDocument.delete();
//            Log.d(TAG, "Deleted document, deletion status = " + retrievedDocument.isDeleted());
//        } catch (CouchbaseLiteException e) {
//            Log.e(TAG, "Cannot delete document", e);
//        }

        Log.d(TAG, "End Hello World App");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
