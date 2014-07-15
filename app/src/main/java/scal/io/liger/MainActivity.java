package scal.io.liger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        //improve performance if you know that changes in content
        //do not change the size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        //use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        //create dummy dataset
        List<String> myDataset = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            myDataset.add(i, "this is another test : " + i);
        }

        //specify the adapter
        mAdapter = new MyAdapter(transaction, myDataset);
        mRecyclerView.setAdapter(mAdapter);
    }
}
