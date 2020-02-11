package com.medha.avinder.uietianshub;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.medha.avinder.uietianshub.models.Committee;
import com.medha.avinder.uietianshub.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityAddCommittee extends AppCompatActivity {

    private EditText etName, etDetails, etContact, etAboutTeam, etWebPageLink, etOtherInfo;
    private ImageView imageView, ivCover, ivDetails;
    private TextView btnSave;

    private Uri uriCover, uriDetails;

    private String masterToken;
    private ProgressDialog pdUploading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_committee);

        init();
    }

    private void init() {
        ivDetails = findViewById(R.id.ivDetails);
        ivCover = findViewById(R.id.ivCover);
        etName = findViewById(R.id.etName);
        etDetails = findViewById(R.id.etDetails);
        etContact = findViewById(R.id.etContact);
        etAboutTeam = findViewById(R.id.etAboutTeam);
        etWebPageLink = findViewById(R.id.etWebPageLink);
        etOtherInfo = findViewById(R.id.etOtherInfo);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ivCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView = ivCover;
                requestPermissions();
            }
        });

        ivDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView = ivDetails;
                requestPermissions();
            }
        });

        etDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((TextView) findViewById(R.id.tvDetailsCharCount)).setText(getResources().getString(R.string.char_count, charSequence.length(), 4000));
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        etAboutTeam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((TextView) findViewById(R.id.tvAboutTeamCharCount)).setText(getResources().getString(R.string.char_count, charSequence.length(), 200));
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        etOtherInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((TextView) findViewById(R.id.tvOtherInfoCharCount)).setText(getResources().getString(R.string.char_count, charSequence.length(), 500));
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Tools.isOnline(getApplicationContext())) {
                    if (etName.length() == 0) {
                        Toast.makeText(ActivityAddCommittee.this, "Enter name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (etDetails.length() == 0) {
                        Toast.makeText(ActivityAddCommittee.this, "Enter details", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (etContact.length() == 0) {
                        Toast.makeText(ActivityAddCommittee.this, "Enter contact details", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (uriCover == null) {
                        Toast.makeText(ActivityAddCommittee.this, "Select an image for the committee", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pdUploading = getProgressDialogUploading();
                    uploadImage();
                    pdUploading.show();
                } else {
                    Toast.makeText(ActivityAddCommittee.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fetchMasterToken();
    }
    private void uploadImage() {
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Committees/" + etName.getText().toString() + "_cover.jpg");
        storageReference.putFile(uriCover).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final String coverUrl = uri.toString();
                        if (uriDetails != null) {
                            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Committees/" + etName.getText().toString() + "_details.jpg");
                            storageReference.putFile(uriDetails).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    pdUploading.setMessage("Please wait while we upload your image...   " + (int)progress + "/100");
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String detailsUrl = uri.toString();
                                            sendToFirebase(coverUrl, detailsUrl);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(getApplicationContext(), "We were unable to post your product", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            sendToFirebase(coverUrl, null);
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), "We were unable to post your product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendToFirebase(String coverUrl, String detailsUrl) {
        Committee committee = new Committee();
        committee.setName(etName.getText().toString());
        committee.setDetails(etDetails.getText().toString());
        committee.setContact(etContact.getText().toString());
        if (etAboutTeam.length() != 0) {
            committee.setAboutTeam(etAboutTeam.getText().toString());
        }
        if (etWebPageLink.length() != 0) {
            committee.setWebPageLink(etWebPageLink.getText().toString());
        }
        if (etOtherInfo.length() != 0) {
            committee.setOtherInfo(etOtherInfo.getText().toString());
        }
        committee.setCoverImage(coverUrl);
        committee.setDetailsImage(detailsUrl);

        Call<Committee> call = Tools.getApi(getApplicationContext()).insertCommittee(committee);
        call.enqueue(new Callback<Committee>() {
            @Override
            public void onResponse(@NonNull Call<Committee> call, @NonNull Response<Committee> response) {
                if (masterToken != null) {
                    Tools.sendDataNotification("Upload", "Committee", masterToken);
                }
                pdUploading.dismiss();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ActivityAddCommittee.this)
                        .setTitle("Success")
                        .setMessage("Your request has been sent for approval. It will be processed soon")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                alertDialog.show();
            }

            @Override
            public void onFailure(@NonNull Call<Committee> call, @NonNull Throwable t) {
            }
        });
    }

    private void fetchMasterToken() {
        Call<String> call = Tools.getApi(getApplicationContext()).getMasterToken();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                masterToken = response.body();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            }
        });
    }

    /**
     * This method is called for selecting image from camera or gallery.
     * It checks permissions and acts accordingly.
     */
    private void requestPermissions() {
        Dexter.withActivity(ActivityAddCommittee.this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final CharSequence[] items = {"Take Photo", "Choose from Library"};
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAddCommittee.this);
                            builder.setTitle("Add Photo");
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items[item].equals("Take Photo")) {
                                        cameraIntent();
                                    } else if (items[item].equals("Choose from Library")) {
                                        galleryIntent();
                                    }
                                }
                            });
                            builder.show();
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAddCommittee.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 0);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0)
                onSelectFromGalleryResult(data);
            else if (requestCode == 1)
                onCaptureImageResult(data);
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                Glide.with(getApplicationContext()).load(bm).into(imageView);
                if (imageView.getId() == ivCover.getId()) {
                    uriCover = data.getData();
                } else if (imageView.getId() == ivDetails.getId()) {
                    uriDetails = data.getData();
                    findViewById(R.id.tvAddCover).setVisibility(View.GONE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        Glide.with(getApplicationContext()).load(thumbnail).into(imageView);
        if (imageView.getId() == ivCover.getId()) {
            uriCover = getImageUri(getApplicationContext(), thumbnail);
        } else if (imageView.getId() == ivDetails.getId()) {
            uriDetails = getImageUri(getApplicationContext(), thumbnail);
            findViewById(R.id.tvAddCover).setVisibility(View.GONE);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.WEBP, 70, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private ProgressDialog getProgressDialogUploading() {
        pdUploading = new ProgressDialog(ActivityAddCommittee.this, R.style.MyAlertDialogStyle);
        pdUploading.setTitle("Uploading");
        pdUploading.setMessage("Please wait while we upload your notice");
        pdUploading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdUploading.setCancelable(false);
        pdUploading.setIndeterminate(true);
        return pdUploading;
    }
}
