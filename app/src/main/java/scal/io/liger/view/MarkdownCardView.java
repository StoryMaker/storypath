package scal.io.liger.view;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import scal.io.liger.R;
import scal.io.liger.model.MarkdownCardModel;
import scal.io.liger.model.CardModel;

import com.commonsware.cwac.anddown.AndDown;


public class MarkdownCardView extends Card {

    private MarkdownCardModel mCardModel;
    private Context mContext;

    public MarkdownCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (MarkdownCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

//        View view = LayoutInflater.from(context).inflate(R.layout.card_markdown, null);
//        WebView webview = (WebView) view.findViewById(R.id.webView);
//
//        AndDown andDown = new AndDown();
//        String[] splits = "Line 1\n* bullet 1\n* bullet 2".split("\n");
//        String html = "";
//        for (String s: splits) {
//            html += andDown.markdownToHtml(s);
//        }
//        webview.loadData(html, "text/html", "UTF-8");
//        webview.invalidate();


        View view = LayoutInflater.from(context).inflate(R.layout.card_markdown, null);
        TextView tvText = ((TextView) view.findViewById(R.id.tv_text));
        AndDown andDown = new AndDown();
        String html = andDown.markdownToHtml(mCardModel.getText());
//        String[] splits = "Line 1\n## header 1\n### header 2".split("\n");
//        String html = "";
//        for (String s: splits) {
//            html += andDown.markdownToHtml(s);
//            Spanned htmlSpanned = Html.fromHtml(html);
//            tvText.append(htmlSpanned);
//        } 
        Spanned htmlSpanned = Html.fromHtml(html);
        tvText.setText(htmlSpanned);

        return view;
    }
}
