package scal.io.liger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;
import com.fima.cardsui.views.CardUI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import scal.io.liger.view.CardView;
import scal.io.liger.view.IntroCardView;


public class MainActivity extends Activity {

    Context mContext = this;
    ArrayList<Card> mListCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCardList();
    }

    private void initCardList()
    {
        CardUI mCardView = (CardUI) findViewById(R.id.cardsview);
        mListCards = new ArrayList<Card>();

        if (mCardView == null)
            return;

        mCardView.clearCards();
        mCardView.setSwipeable(false);

        addIntroCard();


        //add cardlist to view
        for (Card card : mListCards)
            mCardView.addCard(card);

        //draw cards
        mCardView.refresh();
    }

    private void addIntroCard() {
        IntroCardModel introCardModel = new IntroCardModel();
        introCardModel.setHeadline("Welcome to an Intro Card!");
        introCardModel.setLevel("Basic 1");
        introCardModel.setTime("2 hours");

        IntroCardView introCardView = new IntroCardView(introCardModel);
        introCardView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Intro Card click", Toast.LENGTH_SHORT).show();
            }
        });

        mListCards.add(introCardView);
    }
}
