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

    private static final String TTF_FILE = "fc_storymaker_1.0.ttf";

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
        fa_clip_ex_action('\uf165'),
        fa_clip_ex_character('\uf166'),
        fa_clip_ex_place('\uf167'),
        fa_clip_ex_result('\uf168'),
        fa_clip_ex_signature('\uf169'),
        fa_ic_action_circle('\uf16a'),
        fa_ic_action('\uf16b'),
        fa_ic_arrow_back('\uf16c'),
        fa_ic_arrow_down('\uf16d'),
        fa_ic_card_add_card('\uf159'),
        fa_ic_card_capture_photo('\uf15a'),
        fa_ic_card_capture_video('\uf15b'),
        fa_ic_card_check('\uf15c'),
        fa_ic_card_checked('\uf15d'),
        fa_ic_card_import('\uf15e'),
        fa_ic_card_jumble('\uf154'),
        fa_ic_card_narrate('\uf15f'),
        fa_ic_card_new('\uf160'),
        fa_ic_card_order('\uf161'),
        fa_ic_card_thumbs_down('\uf162'),
        fa_ic_card_thumbs_up('\uf163'),
        fa_ic_card_upload('\uf164'),
        fa_ic_char_circle('\uf16e'),
        fa_ic_char('\uf16f'),
        fa_ic_check_square('\uf170'),
        fa_ic_clip_action('\uf171'),
        fa_ic_clip_character('\uf172'),
        fa_ic_clip_other('\uf173'),
        fa_ic_clip_place('\uf174'),
        fa_ic_clip_result('\uf175'),
        fa_ic_clip_signature('\uf176'),
        fa_ic_comp_fail('\uf177'),
        fa_ic_context_facebook('\uf178'),
        fa_ic_context_flickr('\uf179'),
        fa_ic_context_iarchive('\uf17a'),
        fa_ic_context_server_on('\uf17b'),
        fa_ic_context_soundcloud('\uf17c'),
        fa_ic_context_ssh('\uf17d'),
        fa_ic_context_twitter('\uf17e'),
        fa_ic_context_vimeo('\uf17f'),
        fa_ic_context_wordpress('\uf180'),
        fa_ic_context_youtube('\uf181'),
        fa_ic_home('\uf182'),
        fa_ic_info_outline_48px('\uf183'),
        fa_ic_list('\uf184'),
        fa_ic_menu('\uf185'),
        fa_ic_more_vert('\uf186'),
        fa_ic_order('\uf187'),
        fa_ic_other_circle('\uf188'),
        fa_ic_other('\uf189'),
        fa_ic_place_circle('\uf18a'),
        fa_ic_place('\uf18b'),
        fa_ic_play_photo('\uf18c'),
        fa_ic_play_vid_aud('\uf18d'),
        fa_ic_pub_error('\uf18e'),
        fa_ic_result_circle('\uf18f'),
        fa_ic_result('\uf190'),
        fa_ic_sig_circle('\uf191'),
        fa_ic_sig('\uf192');

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
