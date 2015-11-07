package scal.io.liger.view;

import timber.log.Timber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import scal.io.liger.R;
import scal.io.liger.Utility;
import scal.io.liger.ZipHelper;
import scal.io.liger.model.Card;
import scal.io.liger.model.MarkdownCard;

import com.commonsware.cwac.anddown.AndDown;


public class MarkdownCardView implements DisplayableCard {

    private MarkdownCard mCardModel;
    private Context mContext;

    public MarkdownCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (MarkdownCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        /*
        View view = LayoutInflater.from(context).inflate(R.layout.card_markdown, null);
        WebView webview = (WebView) view.findViewById(R.id.webView);

        AndDown andDown = new AndDown();
        String[] splits = "Line 1\n* bullet 1\n* bullet 2".split("\n");
        String html = "";
        for (String s: splits) {
            html += andDown.markdownToHtml(s);
        }
        webview.loadData(html, "text/html", "UTF-8");
        webview.invalidate();
        */


        View view = LayoutInflater.from(context).inflate(R.layout.card_markdown, null);
        TextView tvText = ((TextView) view.findViewById(R.id.tv_text));
        AndDown andDown = new AndDown();
        String html = andDown.markdownToHtml(mCardModel.getText());

        /*
        String[] splits = "Line 1\n## header 1\n### header 2".split("\n");
        String html = "";
        for (String s: splits) {
            html += andDown.markdownToHtml(s);
            Spanned htmlSpanned = Html.fromHtml(html);
            tvText.append(htmlSpanned);
        }
        */

        tvText.setLinksClickable(true);
        tvText.setMovementMethod(LinkMovementMethod.getInstance());
        Spanned htmlSpanned = Html.fromHtml(html, new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(final String source) {
                String absPath = mCardModel.getStoryPath().buildZipPath(source);
                Bitmap myBitmap = BitmapFactory.decodeStream(ZipHelper.getFileInputStream(absPath, mContext));
                Drawable d = new BitmapDrawable(mContext.getResources(), myBitmap);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                return d;
            }
        }, null);
        tvText.setText(Utility.trimTrailingWhitespace(htmlSpanned));

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
