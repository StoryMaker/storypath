
import csv
import yaml
import json

#download csv from: http://docs.google.com/feeds/download/spreadsheets/Export?key=1S_6CeqR5sfHtdp0jiSzF61liIPUR2OKzfr0JVcQsR7M&exportFormat=csv&gid=0

# edit the spreadsheet: https://docs.google.com/a/scal.io/spreadsheets/d/1S_6CeqR5sfHtdp0jiSzF61liIPUR2OKzfr0JVcQsR7M/edit#gid=0

with open('Tip Cards - Sheet1.csv', 'rb') as csvfile:
    reader = csv.reader(csvfile, delimiter=',', quotechar='"')
        
    objs = []    
    
    first_row = True
    for row in reader:
        if first_row:
            first_row = False
            continue
        text = row[0]
        tags = row[1].split(", ")
        #print "text: %s, tags: %s" % (text, tags)
        objs.append({ 'text': row[0], 'tags': tags})
        
    yaml_string = yaml.dump(objs, indent=4)
    tips_file = open("tips.yaml", 'w')
    tips_file.write(yaml_string)
    tips_file.close()
        
"""
for file_name in json_files:
    file_id = file_name.split('.')[0]
    test_spl = "%s_library.json" % file_id 
    print test_spl
    f = open("json/test/%s" % test_spl, 'w')
    f.write(spl_template % (file_id, file_id, file_name, file_id))
"""


