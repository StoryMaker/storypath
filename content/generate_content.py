import sys
import os
import yaml
import json
#stream = open("test.yaml", 'r')
#stream = open("lesson_test.yaml", 'r')

#infile = ""

#if len(sys.argv) <= 1:
#    sys.exit("you need to tell me what file to parse...")
#infile = sys.argv[1]
#stream = open(infile, 'r')

cardcounts = {}

def get_count(key):
    if not cardcounts.has_key(key):
        cardcounts[key] = 0
    ret = cardcounts[key]
    cardcounts[key] = cardcounts[key] + 1
    return ret

def set_id(key, card):
    if not card.has_key('id'):
        card['id'] = "%s_%s" % (key, get_count(key))

# TODO export directly to the real assets/ folder
# TODO warn that fields existed in card but not newcard

#def id, extract_string(
'''
def parse_file2(stream):
    objs = yaml.load(stream)
    cards = []
    strings = {}
    translatable = [
        'medium',
        'text',
        we need to extract each goal from the list, so it cant just be a flat key.  
        
        also tips 
        
        
        
    ]
    for card in objs['cards']:
        if 
'''


def parse_file(in_filename, json_out_filename, strings_out_filename):
    stream = open(in_filename, 'r')
    objs = yaml.load(stream)
    cards = []
    strings = {}
    for card in objs['cards']:
        #newcard = {}
        newcard = card
        if card['type'] == 'MarkDownCard':
            set_id("markdown_card", newcard)
            newcard['type'] = 'MarkdownCard'
            newcard['text'] = card['body']
            
            #strings
            card_fqid = "%s::%s" % (objs['id'], newcard['id'])
            strings["%s::text" % card_fqid] = newcard['text']
            
        elif card['type'] == 'PreviewCard':
            set_id("preview_card", newcard)
            newcard['type'] = 'ExampleCard'
            newcard['header'] = card['title']
            newcard['medium'] = 'video'     # FIXME should be deduced from the mimetype of the file
            newcard['clipType'] = 'character'   # FIXME not needed at all?
            newcard['exampleMediaPath'] = card['media'][0]['media'] # for now we only grab the first media
            
            #strings
            card_fqid = "%s::%s" % (objs['id'], newcard['id'])
            strings["%s::header" % card_fqid] = newcard['header']
            
        elif card['type'] == 'TextCard':
            set_id("text_card", newcard)
            newcard['type'] = 'TextCard'
            newcard['text'] = card['text']
            
            #strings
            card_fqid = "%s::%s" % (objs['id'], newcard['id'])
            strings["%s::text" % card_fqid] = newcard['text']
            
        elif card['type'] == 'QuizCard':
            set_id("quiz_card", newcard)
    #        newcard['question'] = card['questions'][0] # FIXME for now quiz cards are single page
            
            #strings
            card_fqid = "%s::%s" % (objs['id'], newcard['id'])
            strings["%s::question" % card_fqid] = newcard['question']

            for choice in newcard['choices']:
                choice_key = "%s::choices" % card_fqid
                strings["%s[%s]::text" % (choice_key, get_count(choice_key))] = choice['text']

        elif card['type'] == 'ClipCard':
            newcard['type'] = 'ClipCard'
            set_id("clip_card", newcard)
            newcard['goals'] = card['goals']
            newcard['length'] = card['length']
            newcard['medium'] = card['medium']
            
            #strings
            card_fqid = "%s::%s" % (objs['id'], newcard['id'])
            for choice in newcard['goals']:
                choice_key = "%s::goals" % card_fqid
                strings["%s[%s]" % (choice_key, get_count(choice_key))] = choice

        elif card['type'] == 'ReviewCard':
            newcard['type'] = 'ReviewCard'
            set_id("review_card", newcard)
            newcard['medium'] = card['medium']
            
            #no translatable strings

        elif card['type'] == 'EvaluationCard':
            newcard['type'] = 'EvaluationCard'
            set_id("evaluation_card", newcard)
            newcard['text'] = card['text']
            
            #strings
            card_fqid = "%s::%s" % (objs['id'], newcard['id'])
            strings["%s::text" % card_fqid] = newcard['text']

        elif card['type'] == 'PublishCard':
            newcard['type'] = 'PublishCard'
            newcard['id'] = 'publish_card_1'
            newcard['medium'] = card['medium']
            
            #no translatable strings

        elif card['type'] == 'NextUpCard':
            newcard['type'] = 'NextUpCard'
            set_id("nextup_card", newcard)
            newcard['medium'] = card['medium']
            
            #no translatable strings

        elif card['type'] == 'TipCard':
            newcard['type'] = 'TipCard'
            set_id("tip_card", newcard)
            newcard['tags'] = card['tags']
            
            #no translatable strings

        elif card['type'] == 'TipCollectionHeadlessCard':
            #newcard['type'] = 'TipCollectionHeadlessCard'
            set_id("tip_collection_card", newcard)
            #newcard['tags'] = card['tags']
            
            #strings
            card_fqid = "%s::%s" % (objs['id'], newcard['id'])

            for tip in newcard['tips']:
                tip_key = "%s::tips" % card_fqid
                strings["%s[%s]::text" % (tip_key, get_count(tip_key))] = tip['text']
            

        if newcard: cards.append(newcard) 
          
    doc = {
        'title': objs['title'],
        'classPackage': 'scal.io.liger.model',
        'id': objs['id'],
        'cards': cards
    }      
    
    
    if objs.has_key('storyPathTemplateFiles'): 
        doc['storyPathTemplateFiles'] = objs['storyPathTemplateFiles']
          
    j = json.dumps(doc, indent=4)
    json_outfile = open(json_out_filename, 'w')
    json_outfile.write(j)
    json_outfile.close()

    # TODO save strings
    #for k,v in strings.iteritems():
    #    print "%s %s" % (k,v)
    
    # y = yaml.dump(strings)
    # strings_outfile = open(strings_out_filename, 'w')
    # strings_outfile.write(y)
    # strings_outfile.close()

    strings_json = json.dumps(strings, indent=2)
    strings_outfile = open(strings_out_filename, 'w')
    strings_outfile.write(strings_json)
    strings_outfile.close()

    
yaml_dir = os.getcwd() + "/default_library/yaml"
json_dir = os.getcwd() + "/default_library/json"
strings_dir = os.getcwd() + "/default_library/strings"

'''
fileName, fileExtension = os.path.splitext(name)

#in_file = "default_library/yaml/event_discussion_audio_question_1.yaml"
in_file = "default_library/yaml/default_library.yaml"
parse_file(open(in_file, 'r'))


'''
for f in os.listdir(yaml_dir):
    #print name
    cardcounts = {}
    fileName, fileExtension = os.path.splitext(f)
    if fileExtension == ".yaml":
        print "parsing %s" % f
        in_file = yaml_dir + "/" + f
        json_out_file = "%s/%s.json" % (json_dir, fileName)
        strings_out_file = "%s/%s.json" % (strings_dir, fileName)
        parse_file(in_file, json_out_file, strings_out_file)
#'''
