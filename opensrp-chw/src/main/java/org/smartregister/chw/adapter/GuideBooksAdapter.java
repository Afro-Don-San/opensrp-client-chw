package org.smartregister.chw.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.chw.R;
import org.smartregister.chw.contract.GuideBooksFragmentContract;

import java.util.List;

public class GuideBooksAdapter extends RecyclerView.Adapter<GuideBooksAdapter.MyViewHolder> {
    private List<GuideBooksFragmentContract.Video> videos;
    private GuideBooksFragmentContract.View view;

    public GuideBooksAdapter(List<GuideBooksFragmentContract.Video> videos, GuideBooksFragmentContract.View view) {
        this.videos = videos;
        this.view = view;
    }

    @NonNull
    @Override
    public GuideBooksAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_job_aids_guide_books_item, viewGroup, false);
        return new GuideBooksAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideBooksAdapter.MyViewHolder myViewHolder, int position) {
        GuideBooksFragmentContract.Video video = videos.get(position);

        myViewHolder.icon.setVisibility(video.isDowloaded() ? View.VISIBLE : View.GONE);
        myViewHolder.progressBar.setVisibility(video.isDowloaded() ? View.GONE : View.VISIBLE);
        myViewHolder.icon.setOnClickListener(v -> {
            if (video.isDowloaded()) {
                view.playVideo(video);
            } else {
                myViewHolder.progressBar.setVisibility(View.VISIBLE);
                view.downloadVideo(video);
            }
        });
        myViewHolder.title.setText(video.getTitle());
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView icon;
        private ProgressBar progressBar;

        private MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvVideoTitle);
            icon = view.findViewById(R.id.ivIcon);
            progressBar = view.findViewById(R.id.progress_bar);
        }
    }

}
