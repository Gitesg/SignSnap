package com.example.myapplication;

//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.media.ThumbnailUtils;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.ml.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public String detected_text;
    private static final String API_KEY = "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";
    private static final String MODEL = "text-davinci-002";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient();
    TextView result, confidence,textView;
    ImageView imageView;
    Button picture;
    int imageSize = 224;
    TextToSpeech textToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get the Intent that started this activity
//        Intent intent = getIntent();

// Get the selected language extra from the Intent
//        String selectedLanguage = intent.getStringExtra("SELECTED_LANGUAGE");

// Use the selected language as needed in your second activity

        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String selectedLanguage = preferences.getString("selected_language", "default_language");


        // Use selected language here
        Log.d("MainActivity", "Selected language: " + selectedLanguage);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.button);
        textView=findViewById(R.id.textView);

        picture.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                // Launch camera if we have permission
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    //Request camera permission if we don't have it.
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set the language for speech synthesis (here we use the default language)



                    int result = textToSpeech.setLanguage(new Locale("mr", "IN"));



                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }

        });

    }


    public void classifyImage(Bitmap image){
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int [] intValues =new int[imageSize * imageSize];
            image.getPixels(intValues, 0,image.getWidth(), 0, 0, image.getWidth(), image.getWidth());
            int pixel = 0;
            for (int i = 0; i< imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; //RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF)*(1.f/255.f));
                    byteBuffer.putFloat((val & 0xFF)*(1.f/255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i=0; i < confidences.length;  i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence= confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"I like You", "Thank You", "Yes"};
//            String t=classes[maxPos];);
            result.setText(classes[maxPos]);
            MainActivity obj=new MainActivity();
//            obj.callOpenAI(classes[maxPos]);
            /////////////////////////////////////////////////////////////////////////////////

            JSONObject jsonObject = new JSONObject();
            try {
                String selectedLanguage = getIntent().getStringExtra("selected_language");
                System.out.println(selectedLanguage);

               // jsonObject.put("prompt", prompt);

                jsonObject.put("model", MODEL);
                String prompt = "Translate the " + classes[maxPos] + " into " + selectedLanguage + " and give me the response in " + selectedLanguage;
                jsonObject.put("max_tokens", 100);
                jsonObject.put("temperature", 0);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            RequestBody requestBody = RequestBody.create(jsonObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/completions")
                    .header("Authorization", "Bearer " + API_KEY)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //textView.setText("An error occurred while calling the OpenAI API");
                            System.out.println("An error occurred while calling the OpenAI API");
                            //Toast.makeText(MainActivity.this, "An error occurred while calling the OpenAI API", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("An error occurred while calling the OpenAI API");
//                            textView.setText("An error occurred while calling the OpenAI API");
                                //Toast.makeText(MainActivity.this, "An error occurred while calling the OpenAI API", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                            textView = findViewById(R.id.textView);
//                            textView.setText(result.trim());
//                            System.out.println(result.trim());
                                String  open=result.trim();
                                detected_text=result.trim();
//                                System.out.println(detected_text);
                                textView.setText(result.trim());


                                int res = textToSpeech.setLanguage(new Locale("mr", "IN"));
                                textToSpeech.speak(result.trim().toString(), TextToSpeech.QUEUE_FLUSH, null);
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("An error occurred while parsing the OpenAI API response");
                                //textView.setText("An error occurred while parsing the OpenAI API response");
                                //Toast.makeText(MainActivity.this, "An error occurred while parsing the OpenAI API response", Toast.LENGTH_SHORT).show();
                            }
                        });
                        e.printStackTrace();
                    }
                }
            });
            ///////////////////////////////////////////////////////////////////////////////

            String s = "";
           // textToSpeech.speak(classes[maxPos], TextToSpeech.QUEUE_FLUSH, null);
            System.out.println(textView.getText().toString());
            //

            for(int i = 0; i < classes.length; i++){
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }
            confidence.setText(s);





            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            imageView.setImageBitmap(image);

            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyImage(image);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}