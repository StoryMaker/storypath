import sys
import os
import yaml
import json
#stream = open("test.yaml", 'r')
#stream = open("lesson_test.yaml", 'r')

infile = ""

if len(sys.argv) <= 1:
    sys.exit("you need to tell me what file to parse...")
infile = sys.argv[1]
stream = open(infile, 'r')

fileName, fileExtension = os.path.splitext(infile)
jsonfilename = "%s.json" % fileName

objs = yaml.load(stream)
cards = []
for card in objs['cards']:
    newcard = {}
    if card['card'] == 'MarkDownCard':
        newcard['id'] = 'markdown_card_1'
        newcard['type'] = 'MarkdownCard'
        newcard['text'] = card['body']
        
    elif card['card'] == 'PreviewCard':
        newcard['id'] = 'example_card_1'
        newcard['type'] = 'ExampleCard'
        newcard['header'] = card['title']
        newcard['clipMedium'] = 'video'     # FIXME should be deduced from the mimetype of the file
        newcard['clipType'] = 'character'   # FIXME not needed at all?
        newcard['exampleMediaPath'] = card['media'][0]['media'] # for now we only grab the first media
        
    elif card['card'] == 'TextCard':
        newcard['id'] = 'text_card_1'
        newcard['type'] = 'TextCard'
        newcard['text'] = card['text']
        
    elif card['card'] == 'QuizCard':
        newcard = card
        del newcard['card']
#        newcard['type'] = 'QuizCard'
#        newcard['id'] = 'quizcard_1'
#        newcard['id'] = 'quiz_card_1'
        newcard['type'] = 'QuizCard'
#        newcard['question'] = card['questions'][0] # FIXME for now quiz cards are single page

    elif card['card'] == 'ClipCard':
        newcard['type'] = 'ClipCard'
        newcard['id'] = 'clipcard_1'
        newcard['goals'] = card['goals']
        newcard['length'] = card['length']
        newcard['medium'] = card['medium']

    elif card['card'] == 'ReviewCard':
        newcard['type'] = 'ReviewCard'
        newcard['id'] = 'review_card_1'
        newcard['medium'] = card['medium']

    elif card['card'] == 'EvaluationCard':
        newcard['type'] = 'EvaluationCard'
        newcard['id'] = 'evaluation_card_1'
        newcard['text'] = card['text']

    elif card['card'] == 'PublishCard':
        newcard['type'] = 'PublishCard'
        newcard['id'] = 'publish_card_1'
        newcard['medium'] = card['medium']

    elif card['card'] == 'NextUpCard':
        newcard['type'] = 'NextUpCard'
        newcard['id'] = 'nextup_card_1'
        newcard['medium'] = card['medium']

    elif card['card'] == 'TipCard':
        newcard['type'] = 'TipCard'
        newcard['id'] = 'tip_card_1'
        newcard['tags'] = card['tags']
        
        

    if newcard: cards.append(newcard) 
      
doc = {
    'title': objs['title'],
    'classPackage': 'scal.io.liger.model',
    'id': objs['id'],
    'cards': cards
}      
      
j = json.dumps(doc, indent=4)

outfile = open(jsonfilename, 'w')
outfile.write(j)
outfile.close()
