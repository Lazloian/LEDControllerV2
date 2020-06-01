package com.example.ledcontroller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PatternListAdapter extends RecyclerView.Adapter<PatternListAdapter.PatternViewHolder>  {

    // holder for each pattern in recyclerView
    public static class PatternViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final TextView patternNameView;
        OnPatternListener onPatternListener;

        private PatternViewHolder(View itemView, OnPatternListener onPatternListener)
        {
            super(itemView);
            patternNameView = itemView.findViewById(R.id.textView);
            this.onPatternListener = onPatternListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onPatternListener.onPatternClick(getAdapterPosition());
        }
    }

    private final LayoutInflater mInflater;
    private List<ColorPattern> mPatterns;
    private OnPatternListener mOnPatternListener;

    // get inflater and onPatternListener
    PatternListAdapter(Context context, OnPatternListener onPatternListener)
    {
        mInflater = LayoutInflater.from(context);
        this.mOnPatternListener = onPatternListener;

    }

    // inflate each viewHolder when it is created
    @Override
    public PatternViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new PatternViewHolder(itemView, mOnPatternListener);
    }

    // a pattern is put into each newly bound viewHolder based on its position
    @Override
    public void onBindViewHolder(PatternViewHolder holder, int position)
    {
        // make sure there are patterns in the list
        if (mPatterns != null)
        {
            ColorPattern current = mPatterns.get(position);
            holder.patternNameView.setText(current.getName());
        }
        else
        {
            holder.patternNameView.setText("No Pattern");
        }
    }

    // sets a new list of patterns to use
    void setPatterns(List<ColorPattern> patterns)
    {
        mPatterns = patterns;
        notifyDataSetChanged();
    }

    // returns the number of patterns in mPatterns
    public int getItemCount()
    {
        if (mPatterns != null)
        {
            return mPatterns.size();
        }
        else
        {
            return 0;
        }
    }

    // interface for getting the clicked pattern
    public interface OnPatternListener
    {
        void onPatternClick(int position);
    }
}
