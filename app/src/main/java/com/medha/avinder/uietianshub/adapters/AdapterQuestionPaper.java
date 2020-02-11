package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.medha.avinder.uietianshub.ActivityFullscreenImage;
import com.medha.avinder.uietianshub.ActivityPdfViewer;
import com.medha.avinder.uietianshub.ActivityQuestionPapers;
import com.medha.avinder.uietianshub.R;
import com.medha.avinder.uietianshub.models.QuestionPaper;
import com.medha.avinder.uietianshub.utils.Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.medha.avinder.uietianshub.ActivityQuestionPapers.popupWindow;

public class AdapterQuestionPaper extends RecyclerView.Adapter<AdapterQuestionPaper.MyViewHolder> {
    private Context context;
    private ArrayList<QuestionPaper> papersList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvCredits;
        ImageView ivThumbnail;
        ImageButton ibDownload;
        ProgressBar pbDownload;

        MyViewHolder(View view) {
            super(view);
            tvCredits = view.findViewById(R.id.tvTitle);
            ivThumbnail = view.findViewById(R.id.ivThumbnail);
            ibDownload = view.findViewById(R.id.ibDownload);
            pbDownload = view.findViewById(R.id.pbDownload);
            tvTimestamp = view.findViewById(R.id.tvTimestamp);
        }
    }

    public AdapterQuestionPaper(Context context, ArrayList<QuestionPaper> papersList) {
        this.context = context;
        this.papersList = papersList;
    }

    @NonNull
    @Override
    public AdapterQuestionPaper.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_paper, parent, false);
        return new AdapterQuestionPaper.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterQuestionPaper.MyViewHolder holder, int position) {
        final QuestionPaper questionPaper = papersList.get(position);

        final Intent intent = new Intent(context, ActivityPdfViewer.class);

        File file;
        if (questionPaper.getPdfImage() == 0) {
            file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Question Papers/" + questionPaper.getTitle() + "_" + questionPaper.getId() + ".pdf");
        } else {
            file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Question Papers/" + questionPaper.getTitle() + "_" + questionPaper.getId() + ".jpg");
        }

        if (file.exists()) {
            holder.ibDownload.setVisibility(View.GONE);
        } else {
            holder.ibDownload.setVisibility(View.VISIBLE);
        }

        DateFormat originalFormat = new SimpleDateFormat("dd.MM.yy 'at' h:mm a", Locale.getDefault());
        DateFormat targetFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
        try {
            Date date = originalFormat.parse(questionPaper.getTimestamp());
            holder.tvTimestamp.setText(targetFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvCredits.setText("By: " + questionPaper.getCredits());

        if (questionPaper.getPdfImage() == 0) {
            Glide.with(context).load(R.drawable.ic_pdf).into(holder.ivThumbnail);
        } else {
            Glide.with(context).load(R.drawable.ic_image).into(holder.ivThumbnail);
        }
        holder.ibDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.pbDownload.setVisibility(View.VISIBLE);
                holder.ibDownload.setBackgroundResource(R.drawable.ic_close_white);
                holder.ibDownload.setScaleX(0.4f);
                holder.ibDownload.setScaleY(0.4f);
                if (questionPaper.getPdfImage() == 0) {
                    new DownloadFileToDevice(holder.pbDownload, holder.ibDownload).execute(questionPaper.getLink(), questionPaper.getTitle() + "_" + questionPaper.getId(), "PDF");
                } else {
                    new DownloadFileToDevice(holder.pbDownload, holder.ibDownload).execute(questionPaper.getLink(), questionPaper.getTitle() + "_" + questionPaper.getId(), "Image");
                }
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    ActivityQuestionPapers.popWindowToggle = false;
                }
            }
        });

        intent.putExtra("Link", questionPaper.getLink());
        intent.putExtra("Paper Name", questionPaper.getTitle() + "_" + questionPaper.getId());
        intent.putExtra("Item", "Paper");

        holder.ivThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionPaper.getPdfImage() == 0) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        context.startActivity(intent);
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(questionPaper.getLink())));
                    }
                } else {
                    Intent imageIntent = new Intent(context, ActivityFullscreenImage.class);
                    imageIntent.putExtra("Link", questionPaper.getLink());
                    imageIntent.putExtra("Paper Name", questionPaper.getTitle() + "_" + questionPaper.getId());
                    imageIntent.putExtra("Item", "Paper");
                    context.startActivity(imageIntent);
                }
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    ActivityQuestionPapers.popWindowToggle = false;
                }
            }
        });
        holder.tvTimestamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionPaper.getPdfImage() == 0) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        context.startActivity(intent);
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(questionPaper.getLink())));
                    }
                } else {
                    Intent imageIntent = new Intent(context, ActivityFullscreenImage.class);
                    imageIntent.putExtra("Link", questionPaper.getLink());
                    imageIntent.putExtra("Paper Name", questionPaper.getTitle() + "_" + questionPaper.getId());
                    imageIntent.putExtra("Item", "Paper");
                    context.startActivity(imageIntent);
                }
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    ActivityQuestionPapers.popWindowToggle = false;
                }
            }
        });
        holder.tvCredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionPaper.getPdfImage() == 0) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        context.startActivity(intent);
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(questionPaper.getLink())));
                    }
                } else {
                    Intent imageIntent = new Intent(context, ActivityFullscreenImage.class);
                    imageIntent.putExtra("Link", questionPaper.getLink());
                    imageIntent.putExtra("Paper Name", questionPaper.getTitle() + "_" + questionPaper.getId());
                    imageIntent.putExtra("Item", "Paper");
                    context.startActivity(imageIntent);
                }
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    ActivityQuestionPapers.popWindowToggle = false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return papersList.size();
    }

    /**
     * This class checks if file exists in directory, it creates directory and open pdf Viewer app.
     */
    private class DownloadFileToDevice extends AsyncTask<String, String, Void> {
        private boolean success = false;
        private boolean isOnline = true;
        private ProgressBar pbDownload;
        private ImageButton ibDownload;

        private DownloadFileToDevice(ProgressBar pbDownload, ImageButton ibDownload) {
            this.pbDownload = pbDownload;
            this.ibDownload = ibDownload;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected Void doInBackground(String... strings) {
            if (Tools.createDirIfNotExists("UIETians Hub/Question Papers")) {
                File file = new File(Environment.getExternalStorageDirectory(), "UIETians Hub/Question Papers");
                if (strings[2].equals("PDF")) {
                    file = new File(file, strings[1] + ".pdf");
                } else {
                    file = new File(file, strings[1] + ".jpg");
                }
                if (!Tools.isOnline(context)) {
                    isOnline = false;
                } else {
                    try {
                        URL url = new URL(strings[0]);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        InputStream inputStream = connection.getInputStream();
                        int lengthOfFile = connection.getContentLength();

                        long total = 0;
                        FileOutputStream outputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) != -1) {
                            total += length;
                            publishProgress(""+(int)((total*100)/lengthOfFile));
                            outputStream.write(buffer, 0, length);
                        }
                        outputStream.close();
                        inputStream.close();

                        success = true;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            pbDownload.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!isOnline) {
                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
            }
            if (success) {
                Toast.makeText(context, "File saved on device", Toast.LENGTH_SHORT).show();
                pbDownload.setVisibility(View.GONE);
                ibDownload.setVisibility(View.GONE);
            } else {
                Toast.makeText(context, "Unable to download", Toast.LENGTH_SHORT).show();
                pbDownload.setVisibility(View.GONE);
                ibDownload.setVisibility(View.VISIBLE);
                ibDownload.setBackgroundResource(R.drawable.ic_download_white);
                ibDownload.setScaleX(0.7f);
                ibDownload.setScaleY(0.7f);
            }
        }
    }
}