package com.dims.travelmantics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ArrayList<TravelDeal> mTravelDeals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtils.detachlistener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtils.openFirebaseReference("traveldeals", this);
        RecyclerView recyclerViewDeals = findViewById(R.id.recyclerViewDeals);
        RecyclerDealAdapter recyclerDealAdapter = new RecyclerDealAdapter();
        recyclerViewDeals.setAdapter(recyclerDealAdapter);

        LinearLayoutManager dealsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerViewDeals.setLayoutManager(dealsLayoutManager);

        FirebaseUtils.attachListener();
        showMenu();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);

        MenuItem insertMenuItem = menu.findItem(R.id.insert_menu);
        if (FirebaseUtils.isAdmin)
            insertMenuItem.setVisible(true);
        else
            insertMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.insert_menu:
                Intent intent = new Intent(this, DealActivity.class);
                startActivity(intent);
                break;
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Logout", "User logged out");
                                FirebaseUtils.attachListener();//will call the login page if user is not logged in
                            }
                        });
                FirebaseUtils.detachlistener();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMenu(){
        invalidateOptionsMenu();
    }
}
