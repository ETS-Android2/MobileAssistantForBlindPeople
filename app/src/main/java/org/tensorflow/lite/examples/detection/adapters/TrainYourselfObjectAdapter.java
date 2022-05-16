package org.tensorflow.lite.examples.detection.adapters;

import static org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities.TrainYourselfActivity.checkAndRequestPermissions;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities.TrainYourselfActivity;
import org.tensorflow.lite.examples.detection.models.TrainYourselfObject;

import java.io.IOException;
import java.util.List;

public class TrainYourselfObjectAdapter extends RecyclerView.Adapter<TrainYourselfObjectAdapter.MyViewHolder>  {

    private Activity activity;
    private List<TrainYourselfObject> trainYourselfObjects;
    private ImageAdapter imageAdapter;


    public TrainYourselfObjectAdapter(List<TrainYourselfObject> trainYourselfObjects, Activity activity) {
        this.trainYourselfObjects = trainYourselfObjects;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.layout_adapter_train_yourself_object_item, parent, false);

        return new TrainYourselfObjectAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainYourselfObjectAdapter.MyViewHolder holder, int position) {
        TrainYourselfObject trainYourselfObject = trainYourselfObjects.get(position);
        int objectPosition = position;

        holder.trainYourselfObjectNameTv.setText(trainYourselfObject.getObjectName());

        holder.addImagesFromGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        getImages("https://www.google.com/search?biw=1366&bih=675&tbm=isch&sa=1&ei=qFSJWsuTNc-wzwKFrZHoCw&q=" + trainYourselfObject.getObjectName());
                    }
                });
            }
        });

        holder.addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAndRequestPermissions(activity))
                    ((TrainYourselfActivity)activity).chooseImage(activity, objectPosition);
            }
        });

        imageAdapter = new ImageAdapter(trainYourselfObject.getImagesList(),
                                        trainYourselfObject.getImageUrisList(),
                                        activity,
                                        position);
        holder.imagesRecyclerView.setAdapter(imageAdapter);
        holder.imagesRecyclerView.setLayoutManager(new LinearLayoutManager(activity));

    }

    private void getImages(String url) {
        Document doc = null;
        try{
            doc = Jsoup.connect(url).get();
        }catch (IOException e){
            e.printStackTrace();
        }
        Elements imgs = doc.select("img");
        for (Element img : imgs){
            Log.i("image-src", img.attr("src"));
        }

    }

    @Override
    public int getItemCount() {
        return trainYourselfObjects.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        RecyclerView imagesRecyclerView;
        AppCompatButton addImageButton;
        AppCompatButton addImagesFromGoogleButton;
        TextView trainYourselfObjectNameTv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            trainYourselfObjectNameTv = itemView.findViewById(R.id.trainYourselfObjectNameTv);
            imagesRecyclerView = itemView.findViewById(R.id.images_recyclerView);
            addImageButton = itemView.findViewById(R.id.add_image_button);
            addImagesFromGoogleButton = itemView.findViewById(R.id.add_images_from_google_button);

        }
    }

}
