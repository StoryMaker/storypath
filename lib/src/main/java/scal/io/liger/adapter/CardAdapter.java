package scal.io.liger.adapter;

import timber.log.Timber;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.view.DisplayableCard;

/**
 * Created by davidbrodsky on 10/12/14.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    public List<Card> mDataset;
    private HashMap<String, Integer> mCardIdToPosition; // Keep track of Card position by Card#id

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
    public CardAdapter(List<Card> myDataset) {
        mDataset = myDataset;
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

    private boolean hasCard(Card card) {
        return mCardIdToPosition.containsKey(card.getId());
    }

    /**
     * @return the current position of the passed card in the collection of items, or -1 if not present
     */
    public int getPositionForCard(Card card) {
        if (!hasCard(card)) return -1;

        return mCardIdToPosition.get(card.getId());
    }

    public void appendCard(Card cardToAdd) {
//        if (hasCard(cardToAdd)) return; // Hack to avoid duplicate card insertion
        mDataset.add(cardToAdd);
        int newCardPosition = mDataset.size() - 1;
        addCardToMap(cardToAdd, newCardPosition);
        notifyItemInserted(newCardPosition);
    }

    /**
     * Add a card to this adapter at a position relative to
     * the list passed to this adapter's constructor
     * see {@link #CardAdapter(java.util.List)}
    */
    public void addCardAtPosition(Card cardToAdd, int position) {
//        if (hasCard(cardToAdd)) return; // Hack to avoid duplicate card insertion
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

    public void changeCard(Card cardToChange) {
        int cardIndex = mCardIdToPosition.get(cardToChange.getId());
        if (cardIndex != -1)
            notifyItemChanged(cardIndex);
    }

    public void reorderCards (List<Card> cards)
    {
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            mDataset.set(i, card);
            mCardIdToPosition.put(card.getId(),i);
            notifyItemChanged(i);
        }
    }


    public void swapCards(Card cardOne, Card cardTwo) {
        int indexOne = mCardIdToPosition.get(cardOne.getId());
        int indexTwo = mCardIdToPosition.get(cardTwo.getId());
        mDataset.set(indexTwo, cardOne);
        mDataset.set(indexOne, cardTwo);
        mCardIdToPosition.put(cardOne.getId(), mDataset.indexOf(cardOne));
        mCardIdToPosition.put(cardTwo.getId(), mDataset.indexOf(cardTwo));
        notifyItemMoved(indexOne, indexTwo);
        notifyItemMoved(indexTwo+1, indexOne); // TODO verify this
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_base, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
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
            DisplayableCard displayableCard = newCard.getDisplayableCard(holder.cardView.getContext());
            holder.cardView.removeAllViews();
            ViewGroup oldCardContainer = (ViewGroup) displayableCard.getCardView(context);
            holder.cardView.addView(oldCardContainer);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
