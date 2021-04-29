package com.melecio.facebookpublisher;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int TAKE_PHOTO = 101;

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private ShareDialog shareDialog;

    private Button btnEnlace;
    private Button btnImagen;

    private String mUrl;
    private Uri imageUri;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Login Facebook*/
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        loginButton = findViewById(R.id.login_button);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Toast.makeText(MainActivity.this, "Autentificación con éxito!", Toast.LENGTH_SHORT).show();
                Log.d("FACESKD", "Exito");

            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(MainActivity.this, "Autentificación cancelada!", Toast.LENGTH_SHORT).show();
                Log.d("FACESKD", "Cancelado");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(MainActivity.this, "Error al autentificar!", Toast.LENGTH_SHORT).show();
                Log.d("FACESKD", exception.getMessage());
            }
        });

        /*Boton para compartir enlace*/
        btnEnlace = findViewById(R.id.btnEnlace);
        btnEnlace.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ingrese URL:");
            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mUrl = input.getText().toString();
                    if(ShareDialog.canShow(ShareLinkContent.class)){
                        ShareLinkContent content = new ShareLinkContent.Builder()
                                .setContentUrl(Uri.parse(mUrl))
                                .setQuote("Compartido desde App externa")
                                .build();
                        shareDialog.show(content);
                    }
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        });

        /*Boton para compartir imagen*/
        btnImagen = findViewById(R.id.btnImagen);
        btnImagen.setOnClickListener(v -> {
            selectImage(this);
        });

    }

    private void abrirGaleria() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { "Tomar foto", "Escoger de la galería","Cancelar" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Escoge una imagen");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Tomar foto")) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePicture.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePicture, TAKE_PHOTO);
                    }
                } else if (options[item].equals("Escoger de la galería")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , PICK_IMAGE);

                } else if (options[item].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        Log.d("FACESKD", "Request Code: " + requestCode + " /// Result Code:" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && null != data) {
            imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                if(ShareDialog.canShow(SharePhotoContent.class)){
                    SharePhoto photo = new SharePhoto.Builder()
                            .setBitmap(imageBitmap)
                            .setCaption("Imagen compartida desde una App externa")
                            .build();
                    SharePhotoContent content = new SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build();
                    shareDialog.show(content);
                }
            } catch (IOException e) {
                Log.d("FACESKD", e.getMessage());
            }
        }else if(resultCode == RESULT_OK && requestCode == TAKE_PHOTO && null != data){
            imageBitmap = (Bitmap) data.getExtras().get("data");
            if(ShareDialog.canShow(SharePhotoContent.class)){
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(imageBitmap)
                        .setCaption("Imagen compartida desde una App externa")
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }
    }
}