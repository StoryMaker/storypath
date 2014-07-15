package scal.io.liger;

import android.app.Activity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<String> mDataset;
    private FragmentTransaction mFragTransaction;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.txt);
        }
    }

    //provide a suitable constructor (depends on the dataset type)
    public MyAdapter(FragmentTransaction fragTransaction, List<String> myDataset) {
        mFragTransaction = fragTransaction;
        mDataset = myDataset;
    }

    //create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v2 = View.inflate(R.layout.fragment_placeholder, parent);

        //create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_placeholder, parent, false);

        AudioFragment audioFragment = new AudioFragment();
        mFragTransaction.add(v, audioFragment).commit();

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    //replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position));
    }

    //return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
