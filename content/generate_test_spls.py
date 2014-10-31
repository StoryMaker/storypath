json_files = [
    "event_discussion_audio_question_1.json",
    "event_discussion_audio_question_2.json",
    "event_discussion_audio_question_3.json",
    "event_discussion_video_question_4.json",
    "event_discussion_video_question_5.json",
    "event_discussion_video_question_6.json",
    "event_highlights_audio.json",
    "event_highlights_photo.json",
    "event_highlights_video.json",
    "event_interview_audio.json",
    "event_interview_video.json",
    "event_process_audio.json",
    "event_process_photo.json",
    "event_process_video.json",
    "event_report_audio.json",
    "event_report_video.json",
    "event_series_photo_action.json",
    "event_series_photo_character.json",
    "event_series_photo_place.json",
    "event_series_photo_result.json",
    "issue_discussion_audio_question_7.json",
    "issue_discussion_audio_question_8.json",
    "issue_discussion_audio_question_9.json",
    "issue_discussion_video_question_10.json",
    "issue_discussion_video_question_11.json",
    "issue_discussion_video_question_12.json",
    "issue_highlights_audio.json",
    "issue_highlights_photo.json",
    "issue_highlights_video.json",
    "issue_interview_audio.json",
    "issue_interview_video.json",
    "issue_report_audio.json",
    "issue_report_video.json",
    "issue_series_photo_character.json",
    "issue_series_photo_place.json",
    "issue_series_photo_signature.json",
    "profile_discussion_audio_question_13.json",
    "profile_discussion_audio_question_14.json",
    "profile_discussion_audio_question_15.json",
    "profile_discussion_video_question_16.json",
    "profile_discussion_video_question_17.json",
    "profile_discussion_video_question_18.json",
    "profile_highlights_audio.json",
    "profile_highlights_photo.json",
    "profile_highlights_video.json",
    "profile_interview_audio.json",
    "profile_interview_video.json",
    "profile_process_audio.json",
    "profile_process_photo.json",
    "profile_process_video.json",
    "profile_report_audio.json",
    "profile_report_video.json",
    "profile_series_photo_action.json",
    "profile_series_photo_character.json"
]

spl_template = """
{
    "cards": [
        {
            "action": "LOAD", 
            "type": "LoaderHeadlessCard", 
            "references": [
            ], 
            "id": "loader_card", 
            "target": "%s"
        }
    ], 
    "storyPathTemplateFiles": {
        "%s": "../%s"
    }, 
    "classPackage": "scal.io.liger.model", 
    "id": "%s_library", 
    "title": "Start a New Story"
}
"""
for file_name in json_files:
    file_id = file_name.split('.')[0]
    test_spl = "%s_library.json" % file_id 
    print test_spl
    f = open("default_library/json/test/%s" % test_spl, 'w')
    f.write(spl_template % (file_id, file_id, file_name, file_id))
