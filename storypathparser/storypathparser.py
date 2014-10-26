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
    cardcounts[key] = cardcounts[key] + 1
    return cardcounts[key]

def set_id(key, card):
    if not card.has_key('id'):
        card['id'] = "%s_%s" % (key, get_count(key))

def parse_file(stream):
    objs = yaml.load(stream)
    cards = []
    for card in objs['cards']:
        #newcard = {}
        newcard = card
        if card['type'] == 'MarkDownCard':
            set_id("markdown_card", newcard)
            newcard['type'] = 'MarkdownCard'
            newcard['text'] = card['body']
            
        elif card['type'] == 'PreviewCard':
            set_id("preview_card", newcard)
            newcard['type'] = 'ExampleCard'
            newcard['header'] = card['title']
            newcard['clipMedium'] = 'video'     # FIXME should be deduced from the mimetype of the file
            newcard['clipType'] = 'character'   # FIXME not needed at all?
            newcard['exampleMediaPath'] = card['media'][0]['media'] # for now we only grab the first media
            
        elif card['type'] == 'TextCard':
            set_id("text_card", newcard)
            newcard['type'] = 'TextCard'
            newcard['text'] = card['text']
            
        elif card['type'] == 'QuizCard':
            set_id("quiz_card", newcard)
    #        newcard['question'] = card['questions'][0] # FIXME for now quiz cards are single page

        elif card['type'] == 'ClipCard':
            newcard['type'] = 'ClipCard'
            set_id("clip_card", newcard)
            newcard['goals'] = card['goals']
            newcard['length'] = card['length']
            newcard['medium'] = card['medium']

        elif card['type'] == 'ReviewCard':
            newcard['type'] = 'ReviewCard'
            set_id("review_card", newcard)
            newcard['medium'] = card['medium']

        elif card['type'] == 'EvaluationCard':
            newcard['type'] = 'EvaluationCard'
            set_id("evaluation_card", newcard)
            newcard['text'] = card['text']

        elif card['type'] == 'PublishCard':
            newcard['type'] = 'PublishCard'
            newcard['id'] = 'publish_card_1'
            newcard['medium'] = card['medium']

        elif card['type'] == 'NextUpCard':
            newcard['type'] = 'NextUpCard'
            set_id("nextup_card", newcard)
            newcard['medium'] = card['medium']

        elif card['type'] == 'TipCard':
            newcard['type'] = 'TipCard'
            set_id("tip_card", newcard)
            newcard['tags'] = card['tags']
            
            

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

    outfile = open(jsonfilename, 'w')
    outfile.write(j)
    outfile.close()
    
    
for name in os.listdir(os.getcwd()):
    #print name
    cardcounts = {}
    fileName, fileExtension = os.path.splitext(name)
    if fileExtension == ".yaml":
        print "parsing %s" % name
        jsonfilename = "%s.json" % fileName
        parse_file(open(name, 'r'))

