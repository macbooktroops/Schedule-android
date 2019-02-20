package com.playgilround.schedule.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.playgilround.schedule.client.R;
import com.playgilround.schedule.client.adapter.TutorialAdapter;
import com.playgilround.schedule.client.singin.SignInActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 19-01-31
 * Tutorial Activity
 */
public class TutorialActivity extends AppCompatActivity {

    @BindView(R.id.recycler_image)
    RecyclerView mRecyclerView;

    @BindView(R.id.btn_next)
    ImageView mNextBtn;

    int retPosition;

    TutorialAdapter adapter;

    static final String TAG = TutorialActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        ButterKnife.bind(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setHasFixedSize(true);
        adapter = new TutorialAdapter(this);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager mLinear = (LinearLayoutManager) recyclerView.getLayoutManager();

                retPosition = mLinear.findFirstVisibleItemPosition();
            }
        });

        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);

    }

    @OnClick(R.id.btn_next)
    void onButtonClick() {
        if (adapter.getItemCount() -1 == retPosition) {
            startActivity(new Intent(this, SignInActivity.class));
            this.overridePendingTransition(R.anim.enter, R.anim.exit);
            finish();
        } else {
            mRecyclerView.smoothScrollToPosition(retPosition + 1);
        }
    }
}
