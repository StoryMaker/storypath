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


    private static final String TTF_FILE = "fc_storymaker_main_1.1.ttf";

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
        fa_audio_action('\uf100'),
        fa_audio_char('\uf101'),
        fa_audio_place('\uf102'),
        fa_audio_result('\uf103'),
        fa_audio_sig('\uf104'),
        fa_clip_ex_action('\uf105'),
        fa_clip_ex_character('\uf106'),
        fa_clip_ex_place('\uf107'),
        fa_clip_ex_result('\uf108'),
        fa_clip_ex_signature('\uf109'),
        fa_clip_ex_video_signature('\uf10a'),
        fa_ic_action_48px('\uf10b'),
        fa_ic_action_72px('\uf10c'),
        fa_ic_arrow_back_48px('\uf10d'),
        fa_ic_arrow_down_48px('\uf10e'),
        fa_ic_card_add_card('\uf10f'),
        fa_ic_card_capture_photo('\uf110'),
        fa_ic_card_capture_video('\uf111'),
        fa_ic_card_check('\uf112'),
        fa_ic_card_checked('\uf113'),
        fa_ic_card_import('\uf114'),
        fa_ic_card_jumble('\uf115'),
        fa_ic_card_launch('\uf116'),
        fa_ic_card_narrate('\uf117'),
        fa_ic_card_new('\uf118'),
        fa_ic_card_order('\uf119'),
        fa_ic_card_thumbs_down('\uf11a'),
        fa_ic_card_thumbs_up('\uf11b'),
        fa_ic_card_upload('\uf11c'),
        fa_ic_char_48px('\uf11d'),
        fa_ic_char_72px('\uf11e'),
        fa_ic_check_48px('\uf11f'),
        fa_ic_clip_action('\uf120'),
        fa_ic_clip_character('\uf121'),
        fa_ic_clip_other('\uf122'),
        fa_ic_clip_place('\uf123'),
        fa_ic_clip_result('\uf124'),
        fa_ic_clip_signature('\uf125'),
        fa_ic_comp_fail('\uf126'),
        fa_ic_comp_processing('\uf127'),
        fa_ic_context_facebook('\uf128'),
        fa_ic_context_flickr('\uf129'),
        fa_ic_context_iarchive('\uf12a'),
        fa_ic_context_server_on('\uf12b'),
        fa_ic_context_soundcloud('\uf12c'),
        fa_ic_context_ssh('\uf12d'),
        fa_ic_context_twitter('\uf12e'),
        fa_ic_context_vimeo('\uf12f'),
        fa_ic_context_wordpress('\uf130'),
        fa_ic_context_youtube('\uf131'),
        fa_ic_cover_01('\uf132'),
        fa_ic_cover_02('\uf133'),
        fa_ic_cover_03('\uf134'),
        fa_ic_home_48px('\uf135'),
        fa_ic_info_outline_48px('\uf136'),
        fa_ic_list_48px('\uf137'),
        fa_ic_medal('\uf138'),
        fa_ic_menu_48px('\uf139'),
        fa_ic_more_vert_48px('\uf13a'),
        fa_ic_order_48px('\uf13b'),
        fa_ic_other_48px('\uf13c'),
        fa_ic_other_72px('\uf13d'),
        fa_ic_place_48px('\uf13e'),
        fa_ic_place_72px('\uf13f'),
        fa_ic_play_photo_64px('\uf140'),
        fa_ic_play_vid_aud_64px('\uf141'),
        fa_ic_pub_error('\uf142'),
        fa_ic_result_48px('\uf143'),
        fa_ic_result_72px('\uf144'),
        fa_ic_sig_48px('\uf145'),
        fa_ic_sig_72px('\uf146'),
        fa_photo_action('\uf147'),
        fa_photo_char('\uf148'),
        fa_photo_place('\uf149'),
        fa_photo_result('\uf14a'),
        fa_photo_sig('\uf14b'),
        fa_video_action('\uf14c'),
        fa_video_char('\uf14d'),
        fa_video_place('\uf14e'),
        fa_video_result('\uf14f'),
        fa_video_sig('\uf150');

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
