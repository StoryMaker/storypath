package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;

import scal.io.liger.MediaHelper;
import scal.io.liger.R;
import scal.io.liger.Utility;
import scal.io.liger.model.Card;
import scal.io.liger.model.ReviewCard;


public class TagCardView implements DisplayableCard {

    private ReviewCard mCardModel;
    private Context mContext;

    public TagCardView(Context context, Card cardModel) {
        Log.d("TagCardView", "constructor");
        mContext = context;
        mCardModel = (ReviewCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        Log.d("TagCardViews", "getCardView");
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_tag, null);
        final ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));
        final String mediaPath = mCardModel.getMediaPath();

        Button btnCharacter =((Button) view.findViewById(R.id.btn_character));
        Button btnAction =((Button) view.findViewById(R.id.btn_action));
        Button btnResult =((Button) view.findViewById(R.id.btn_result));
        Button btnPlace =((Button) view.findViewById(R.id.btn_place));
        Button btnSignature =((Button) view.findViewById(R.id.btn_signature));
        Button btnSkip =((Button) view.findViewById(R.id.btn_skip));


        btnCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });


        //TODO find better way of checking file is valid
        final File mediaFile = MediaHelper.loadFileFromPath(mediaPath, mContext);
        if(mediaFile.exists() && !mediaFile.isDirectory()) {

            //TODO check media type

            /*
            Uri mediaUri = Uri.parse(mediaFile.getPath());
            //set up image as preview
            Bitmap videoFrame = Utility.getFrameFromVideo(mediaUri.getPath());
            if(null != videoFrame) {
                ivCardPhoto.setImageBitmap(videoFrame);
            }
            */
        } else {
            System.err.println("INVALID MEDIA FILE: " + mediaFile.getPath());
        }

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
