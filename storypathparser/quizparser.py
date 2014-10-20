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
        
      
j = json.dumps(objs, indent=4)

print j

#outfile = open(jsonfilename, 'w')
#outfile.write(j)
#outfile.close()
