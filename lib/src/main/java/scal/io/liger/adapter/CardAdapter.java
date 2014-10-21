package scal.io.liger.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twotoasters.android.support.v7.widget.CardView;
import com.twotoasters.android.support.v7.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.view.DisplayableCard;

/**
 * Created by davidbrodsky on 10/12/14.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private List<Card> mDataset;
    private HashMap<String, Integer> mCardIdToPosition; // Keep track of Card position by Card#id
    private Activity mHostActivity;

    // Provide a reference to the type of views that you are using
    // (custom viewholder)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** List of clipViews in initial z order.
         * e.g: The first element has highest z order (appears unobscured)
        */
        public CardView cardView;
        public Card boundCard;
//        public boolean expanded = false;

        public ViewHolder(View v) {
            super(v);
            cardView = (CardView) v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CardAdapter(Activity host, List<Card> myDataset) {
        mDataset = myDataset;
        mHostActivity = host;
        populateCardIdMap();
    }

    private void populateCardIdMap() {
        mCardIdToPosition = new HashMap<>();
        for (int x = 0; x < mDataset.size(); x++) {
            addCardToMap(mDataset.get(x), x);
        }
    }

    private void addCardToMap(Card card, int pos) {
        mCardIdToPosition.put(card.getId(), pos);
    }

    public void appendCard(Card cardToAdd) {
        mDataset.add(cardToAdd);
        int newCardPosition = mDataset.size() - 1;
        addCardToMap(cardToAdd, newCardPosition);
        notifyItemInserted(newCardPosition);
    }

    /**
     * Add a card to this adapter at a position relative to
     * the list passed to this adapter's constructor
     * see {@link #CardAdapter(android.app.Activity, java.util.List)}
    */
    public void addCardAtPosition(Card cardToAdd, int position) {
        mDataset.add(position, cardToAdd);
        addCardToMap(cardToAdd, position);
        notifyItemInserted(position);
    }

    public void removeCard(Card cardToRemove) {
        int indexToRemove = mCardIdToPosition.get(cardToRemove.getId());
        mDataset.remove(indexToRemove);
        notifyItemRemoved(indexToRemove);
        mCardIdToPosition.remove(cardToRemove.getId());
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_base, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Card newCard = mDataset.get(position);

        if ( false /*holder.boundCard.getType().equals(newCard.getType())*/) {
            // TODO
            // Recycle the view if possible
        } else {
            // Rebuild the view
            Context context = holder.cardView.getContext();
            DisplayableCard cardsuiCard = newCard.getDisplayableCard(holder.cardView.getContext());
            holder.cardView.removeAllViews();
            ViewGroup oldCardContainer = (ViewGroup) cardsuiCard.getCardView(context);
            holder.cardView.addView(oldCardContainer);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}