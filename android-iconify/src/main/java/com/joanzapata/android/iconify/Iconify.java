/**
 * Copyright 2013 Joan Zapata
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * It uses FontAwesome font, licensed under OFL 1.1, which is compatible
 * with this library's license.
 *
 *     http://scripts.sil.org/cms/scripts/render_download.php?format=file&media_id=OFL_plaintext&filename=OFL.txt
 */
package com.joanzapata.android.iconify;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spanned;
import android.widget.TextView;

import java.io.IOException;

import static android.text.Html.fromHtml;
import static android.text.Html.toHtml;
import static com.joanzapata.android.iconify.Utils.replaceIcons;
import static com.joanzapata.android.iconify.Utils.resourceToFile;
import static java.lang.String.valueOf;

public final class Iconify {

    private static final String TTF_FILE = "fc-storymaker-1.0.ttf";

    public static final String TAG = Iconify.class.getSimpleName();

    private static Typeface typeface = null;

    private Iconify() {
        // Prevent instantiation
    }

    /**
     * Transform the given TextViews replacing {icon_xxx} texts with icons.
     */
    public static final void addIcons(TextView... textViews) {
        for (TextView textView : textViews) {
            textView.setTypeface(getTypeface(textView.getContext()));
            textView.setText(compute(textView.getText()));
        }
    }

    public static CharSequence compute(CharSequence charSequence) {
        if (charSequence instanceof Spanned) {
            String text = toHtml((Spanned) charSequence);
            return fromHtml(replaceIcons(new StringBuilder((text))).toString());
        }
        String text = charSequence.toString();
        return replaceIcons(new StringBuilder(text));
    }

    public static final void setIcon(TextView textView, IconValue value) {
        textView.setTypeface(getTypeface(textView.getContext()));
        textView.setText(valueOf(value.character));
    }

    /**
     * The typeface that contains FontAwesome icons.
     *
     * @return the typeface, or null if something goes wrong.
     */
    public static final Typeface getTypeface(Context context) {
        if (typeface == null) {
            try {
                typeface = Typeface.createFromFile(resourceToFile(context, TTF_FILE));
            } catch (IOException e) {
                return null;
            }
        }
        return typeface;
    }

    public static enum IconValue {
        fa_clip_ex_action('\uf145'),
        fa_clip_ex_character('\uf146'),
        fa_clip_ex_place('\uf147'),
        fa_clip_ex_result('\uf148'),
        fa_clip_ex_signature('\uf149'),
        fa_ic_action_circle('\uf14a'),
        fa_ic_action('\uf14b'),
        fa_ic_arrow_back('\uf14c'),
        fa_ic_arrow_down('\uf14d'),
        fa_ic_card_add_card('\uf14e'),
        fa_ic_card_capture_photo('\uf14f'),
        fa_ic_card_capture_video('\uf150'),
        fa_ic_card_check('\uf151'),
        fa_ic_card_checked('\uf152'),
        fa_ic_card_import('\uf153'),
        fa_ic_card_jumble('\uf154'),
        fa_ic_card_narrate('\uf155'),
        fa_ic_card_new('\uf156'),
        fa_ic_card_order('\uf157'),
        fa_ic_card_thumbs_down('\uf158'),
        fa_ic_card_thumbs_up('\uf159'),
        fa_ic_card_upload('\uf15a'),
        fa_ic_char_circle('\uf15b'),
        fa_ic_char('\uf15c'),
        fa_ic_check('\uf15d'),
        fa_ic_clip_action('\uf15e'),
        fa_ic_clip_character('\uf15f'),
        fa_ic_clip_other('\uf160'),
        fa_ic_clip_place('\uf161'),
        fa_ic_clip_result('\uf162'),
        fa_ic_clip_signature('\uf163'),
        fa_ic_home('\uf164'),
        fa_ic_info_outline('\uf165'),
        fa_ic_list('\uf166'),
        fa_ic_menu('\uf167'),
        fa_ic_more_vert('\uf168'),
        fa_ic_order('\uf169'),
        fa_ic_other_circle('\uf16a'),
        fa_ic_other('\uf16b'),
        fa_ic_place_circle('\uf16c'),
        fa_ic_place('\uf16d'),
        fa_ic_play_photo('\uf144'),
        fa_ic_play_vid_aud('\uf16e'),
        fa_ic_result_circle('\uf16f'),
        fa_ic_result('\uf170'),
        fa_ic_sig_circle('\uf171'),
        fa_ic_sig('\uf172');

        char character;

        IconValue(char character) {
            this.character = character;
        }

        public String formattedName() {
            return "{" + name() + "}";
        }

        public char character() {
            return character;
        }
    }
}
