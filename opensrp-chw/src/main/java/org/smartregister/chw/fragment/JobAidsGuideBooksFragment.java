package org.smartregister.chw.fragment;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.smartregister.chw.R;
import org.smartregister.chw.adapter.GuideBooksAdapter;
import org.smartregister.chw.contract.GuideBooksFragmentContract;
import org.smartregister.chw.interactor.GuideBooksFragmentInteractor;
import org.smartregister.chw.presenter.GuideBooksFragmentPresenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class JobAidsGuideBooksFragment extends Fragment implements GuideBooksFragmentContract.View {

    protected RecyclerView.Adapter mAdapter;
    private List<GuideBooksFragmentContract.Video> videos = new ArrayList<>();
    protected GuideBooksFragmentContract.Presenter presenter;
    private ProgressBar progressBar;

    public static JobAidsGuideBooksFragment newInstance() {
        JobAidsGuideBooksFragment fragment = new JobAidsGuideBooksFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_job_aids_guide_books, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        progressBar = rootView.findViewById(R.id.progress_bar);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getViewContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new GuideBooksAdapter(videos, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        initializePresenter();
        return rootView;
    }

    @Override
    public void initializePresenter() {
        presenter = new GuideBooksFragmentPresenter(this, new GuideBooksFragmentInteractor());
    }

    @Override
    public GuideBooksFragmentContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void onDataReceived(List<GuideBooksFragmentContract.Video> videos) {
        this.videos.addAll(videos);
        this.mAdapter.notifyDataSetChanged();
        this.displayLoadingState(false);
    }

    @Override
    public Context getViewContext() {
        return getContext();
    }

    @Override
    public void displayLoadingState(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    @Override
    public void playVideo(GuideBooksFragmentContract.Video video) {
        final File videoFile = new File(video.getLocalPath());
        Uri fileUri = FileProvider.getUriForFile(getViewContext(), "{yourpackagename}.fileprovider", videoFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "video/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//DO NOT FORGET THIS EVER
        startActivity(intent);
    }

    @Override
    public void downloadVideo(GuideBooksFragmentContract.Video video) {
        if (!video.isDowloaded())
            getPresenter().requestVideoDownload(video.getServerUrl());
    }
}
