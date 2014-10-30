import sys
import os
import yaml
import json

languages = [
    #'ar',
    #'fa',
    #FIXME fill in the rest
    'de'
]

# TODO mkdir the dirs you need as they wont exist on clean checkout

# FIXME make sure there's no translations present or we are going to double translate them, maybe go into a loop
json_dir = os.getcwd() + "/default_library/json"
translations_dir = os.getcwd() + "/default_library/translated_strings"

def parse_file(original_json_file_path, translated_strings_file_name, out_file_path):
    in_stream = open(original_json_file_path, 'r')
    doc = json.load(in_stream)

    strings_stream = open(translated_strings_file_name, 'r')
    strings = json.load(strings_stream)


    for k,v in strings.iteritems():
        splits = k.split('::')
        path_id = splits[0]
        # check we are in teh right file by checking the pathid
        if not doc['id'] == path_id:
            print "!! key in translation file '%s' doesn't match this path's id: '%s'" % (splits[0], doc['id'])
            exit(-1)
        card_id = splits[1]
        prop_key = splits[2]
        for card in doc['cards']:
            if card['id'] == card_id:
                if len(splits) == 3:
                    key_splits = prop_key.split('[')
                    if len(key_splits) == 1:
                        key = key_splits[0]
                        # of form  "event_discussion_audio_question1::evaluation_card_0::text"
                        print "at %s, replacing '%s' with '%s'" % (k, card[key], v)
                        card[key] = v
                    elif len(key_splits) == 2:
                        # of form "event_discussion_audio_question1::clip_card_5::goals[0]"
                        key = key_splits[0]
                        array_index = int(key_splits[1].split(']')[0]) # FIXME skip this step by initially splitting on [ | ] in a regex
                        print "at %s, replacing '%s' with '%s'" % (k, card[key][array_index], v)
                        card[key][array_index] = v
                        # index into an array (or dict?)
                else:
                    print "!! deeper nesting we need to handle"
                    exit(-1)
                    pass # TODO there are some deeper nestings we need to watch out for
                break

    json_string = json.dumps(doc, indent=4)
    out_file = open(out_file_path, 'w')
    out_file.write(json_string)
    out_file.close()

for translated_strings_file_name in os.listdir(translations_dir):
    file_name, file_extension = os.path.splitext(translated_strings_file_name)
    (json_file_name, lang) = file_name.split('-')
    translated_json_file_name = "%s-%s.json" % (json_file_name, lang)
    out_file_path = "%s/%s" % (json_dir, translated_json_file_name)
    original_json_file_path = "%s/%s.json" % (json_dir, json_file_name)
    translated_strings_file_path = "%s/%s" % (translations_dir, translated_strings_file_name)

    print "inserting localized strings into %s" % out_file_path

    # strings_file_name = "%s/%s.json" % (translations_dir, file_name)
    parse_file(original_json_file_path, translated_strings_file_path, out_file_path)
