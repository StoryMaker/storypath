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
        pass
#        newcard['id'] = 'quiz_card_1'
#        newcard['type'] = 'QuizCard'
#        newcard['question'] = card['questions'][0] # FIXME for now quiz cards are single page
    if newcard: cards.append(newcard) 
      
doc = {
    'title': 'Mad Libs 1',
    'classPackage': 'scal.io.liger.model',
    'id': 'genpath_1',
    'cards': cards
}      
      
j = json.dumps(doc, indent=4)

outfile = open(jsonfilename, 'w')
outfile.write(j)
outfile.close()
