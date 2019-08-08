package com.dims.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {
    public static final int PICTURE_REQUEST_CODE = 42;
    View mConstraintLayout;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    EditText mEditTextTitle;
    EditText mEditTextPrice;
    EditText mEditTextDescription;

    ImageView mImageViewDeal;

    TravelDeal mDeal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mConstraintLayout = findViewById(R.id.insert_constraint_layout);

        mFirebaseDatabase = FirebaseUtils.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtils.mDatabaseReference;

        mEditTextTitle = findViewById(R.id.editTextTitle);
        mEditTextPrice = findViewById(R.id.editTextPrice);
        mEditTextDescription = findViewById(R.id.editTextDescription);

        mImageViewDeal = findViewById(R.id.imageViewDealFullSize);

        //Receiving sent data
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("selectedDeal");
        if (deal == null)
            deal = new TravelDeal();
        mDeal = deal;

        mEditTextTitle.setText(deal.getTitle());
        mEditTextDescription.setText(deal.getDescription());
        mEditTextPrice.setText(deal.getPrice());

        showImage(mDeal.getImageUrl());

        Button imageUploadButton = findViewById(R.id.btnUploadImage);
        imageUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadIntent = new Intent(Intent.ACTION_GET_CONTENT);
                uploadIntent.setType("image/jpeg");
                uploadIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(uploadIntent.createChooser(uploadIntent, "Insert Picture"), PICTURE_REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deal_menu, menu);

        if (FirebaseUtils.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditText(true);
            findViewById(R.id.btnUploadImage).setEnabled(true);
        }
        else{
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditText(false);
            findViewById(R.id.btnUploadImage).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show();
                clearScreen();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                if (mDeal.getId() == null) {
                    Toast.makeText(this, "Save deal before attempting to delete", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "Deal DELETED", Toast.LENGTH_LONG).show();
                }
                clearScreen();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_REQUEST_CODE && resultCode == RESULT_OK){
            Uri uploadUri = data.getData();
            final StorageReference storageReference = FirebaseUtils.mStorageReference.child(uploadUri.getLastPathSegment());
            //Upload the picture to the firebase storage
            storageReference.putFile(uploadUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    String imageName =taskSnapshot.getStorage().getPath();
                    mDeal.setImageName(imageName);
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            mDeal.setImageUrl(imageUrl);
                        }
                    });

                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(), "Unable to upload, try again", Toast.LENGTH_LONG).show();
                }
            });

        }
    }


    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void clearScreen() {
        mEditTextTitle.setText("");
        mEditTextPrice.setText("");
        mEditTextDescription.setText("");

        mEditTextTitle.requestFocus();
    }

    private void saveDeal() {
        mDeal.setTitle(mEditTextTitle.getText().toString());
        mDeal.setDescription(mEditTextDescription.getText().toString());
        mDeal.setPrice(mEditTextPrice.getText().toString());
        if (mDeal.getId() == null)//New TravelDeal
            mDatabaseReference.push().setValue(mDeal);//add
        else//Existing TravelDeal
            mDatabaseReference.child(mDeal.getId()).setValue(mDeal);//edit
    }

    private void deleteDeal(){
        if (mDeal.getId() == null) {
            return;
        }
        mDatabaseReference.child(mDeal.getId()).removeValue();
        Log.d("image name", mDeal.getImageName());
        if(mDeal.getImageName() != null && !mDeal.getImageName().isEmpty()) {
            StorageReference pictureReference = FirebaseUtils.mFirebaseStorage.getReference().child(mDeal.getImageName());
            pictureReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image", e.getMessage());
                }
            });
        }
    }

    private void enableEditText(boolean isEnabled){
        mEditTextTitle.setEnabled(isEnabled);
        mEditTextDescription.setEnabled(isEnabled);
        mEditTextPrice.setEnabled(isEnabled);
    }

    private void showImage(String url){
        if (url != null && !url.isEmpty()){
            //for 16:9, height = width*9/16
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, (width*9/16))
                    .centerCrop()
                    .into(mImageViewDeal);
        }
    }
}