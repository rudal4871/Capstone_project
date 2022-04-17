package com.emericoapp.upload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.emericoapp.upload.fragment.Frag1;
import com.emericoapp.upload.fragment.Frag2;
import com.emericoapp.upload.fragment.Frag3;
import com.emericoapp.upload.model.ApiModel;
import com.emericoapp.upload.network.ApiConstants;
import com.emericoapp.upload.network.ServiceInterface;
import com.emericoapp.upload.utils.FileUtil;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.navigation.NavigationBarItemView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
   //bottomNaviview
    private BottomNavigationItemView BottomNavigationView;
    private NavigationBarItemView NavigationBarItemView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Frag1 frag1;
    private Frag2 frag2;
    private Frag3 frag3;
//listview
    private ListView list;

    ImageView selectedImage;
    CircularProgressButton btnSubmit;
    ServiceInterface serviceInterface;
    List<Uri> files = new ArrayList<>();

    private LinearLayout parentLinearLayout;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //list
        {
            list=(ListView)findViewById(R.id.list);

             //1.배열안에다가  String 형태로 넣겠다
            List<String>data = new ArrayList<>();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,data
            ); //2.this=now activity, android basic form

            //3.list <-connect bridge(adapter)
              list.setAdapter(adapter);
            data.add("넣고 싶은 데이터 넣기");
            data.add("Android");
            adapter.notifyDataSetChanged(); //위의 데이터를 저장한다
        }
    //    BottomNavigationView= findViewById(R.id.bottomNavi);


//        //bottomNavi 프레그먼트를 트렉젝션 즉 교체를 해주는 작업
//        NavigationBarItemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                switch (NavigationBarItemView.getItemData()) {
//                    case R.id.action_work:
//                            setFrag(0);
//                        break;
//                    case R.id.action_home:
//                        setFrag(1);
//                        break;
//                    case R.id.action_quote:
//                        setFrag(2);
//                        break;
//                }
//            }
//        });


        frag1=new Frag1();
        frag2 =new Frag2();
        frag3 =new Frag3();
        setFrag(0);//첫 frag 화면을 무엇으로 지정해 줄지정하는 것

        }
       //프레그 먼트 교체 실행문
        private void setFrag(int n) {
            fm = getSupportFragmentManager();
            ft = fm.beginTransaction(); //트랜젝션이 실제적인 프레그먼트 교체가 이뤄지면 프레그 먼트를 가져와서 트렌젝션 하는 행위
            switch (n) {
                case 0:  //총3개의 fragment가 교체 된다는 뜻

                    ft.replace(R.id.main_frame, frag1);
                    ft.commit();
                    break;
                case 1:
                    ft.replace(R.id.main_frame, frag2);
                    ft.commit();
                    break;
                case 2:
                    ft.replace(R.id.main_frame, frag3);
                    ft.commit();
                    break;

            }

        parentLinearLayout= findViewById(R.id.parent_linear_layout);

        ImageView addImage = findViewById(R.id.iv_add_image);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addImage();
            }
        });

        btnSubmit = findViewById(R.id.submit_button);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
                uploadImages();
            }
        });


    }


    //===== add image in layout
    public void addImage() {
        LayoutInflater inflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView=inflater.inflate(R.layout.image, null);
        // Add the new row before the add field button.
        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - 1);
        parentLinearLayout.isFocusable();

        selectedImage = rowView.findViewById(R.id.number_edit_text);
        selectImage(MainActivity.this);
    }

    //===== select image
    private void selectImage(Context context) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("Choose a Media");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);//one can be replaced with any action code

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {

                        Bitmap img = (Bitmap) data.getExtras().get("data");
                        selectedImage.setImageBitmap(img);
                        Picasso.get().load(getImageUri(MainActivity.this,img)).into(selectedImage);

                        String imgPath = FileUtil.getPath(MainActivity.this,getImageUri(MainActivity.this,img));

                        files.add(Uri.parse(imgPath));
                        Log.e("image", imgPath);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri img = data.getData();
                        Picasso.get().load(img).into(selectedImage);

                        String imgPath = FileUtil.getPath(MainActivity.this,img);

                        files.add(Uri.parse(imgPath));
                        Log.e("image", imgPath);

                    }
                    break;
            }
        }
    }

    //===== bitmap to Uri
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "intuenty", null);
        Log.d("image uri",path);
        return Uri.parse(path);
    }

    //===== Upload files to server
    public void uploadImages(){

        btnSubmit.startAnimation();

        List<MultipartBody.Part> list = new ArrayList<>();

        for (Uri uri:files) {

            Log.i("uris",uri.getPath());

            list.add(prepareFilePart("file", uri));
        }

        serviceInterface = ApiConstants.getClient().create(ServiceInterface.class);


        Call<ApiModel> call = serviceInterface.uploadNewsFeedImages(list);
        call.enqueue(new Callback<ApiModel>() {
            @Override
            public void onResponse(Call<ApiModel> call, Response<ApiModel> response) {
                btnSubmit.revertAnimation();
                try {

                    ApiModel addMediaModel = response.body();
                    if(addMediaModel.getStatusCode().equals("200")){
                        Toast.makeText(MainActivity.this, "Files uploaded successfuly", Toast.LENGTH_SHORT).show();
                    }

                    Log.e("main", "the status is ----> " + addMediaModel.getStatusCode());
                    Log.e("main", "the message is ----> " + addMediaModel.getStatusMessage());

                }
                catch (Exception e){
                    Log.d("Exception","|=>"+e.getMessage());
//
                }
            }

            @Override
            public void onFailure(Call<ApiModel> call, Throwable t) {
                btnSubmit.revertAnimation();
                Log.i("my",t.getMessage());
            }
        });
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {

        File file = new File(fileUri.getPath());
        Log.i("here is error",file.getAbsolutePath());
        // create RequestBody instance from file

            RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse("image/*"),
                            file);

            // MultipartBody.Part is used to send also the actual file name
            return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);


    }


    // this is all you need to grant your application external storage permision
    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case REQUEST_CODE_ASK_PERMISSIONS:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    uploadImages();
                }
                else {
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }
}
