liger-prototype
================

---
# Testing
---
#### Espresso Testing:
To run the test suite, you simply need add the correct instrumentation runner to the Run config.  In Android Studio, open the 'Run' -> 'Edit Configurations' -> 'General tab.' Expand 'Android Tests' on the left and click 'MainActivityTest.'  Under 'Specific instrumentation runner (optional)', paste the following: com.google.android.apps.common.testing.testrunner.GoogleInstrumentationTestRunner.

---
# Story Path format spec
---

#### Story path fields:

**id** - string value, an identifier for the story path which is unique within the file system  
**title** - string value, a title for the story path  
**dependencies** - array of dependency structures, see format below  

*no dependency is required if the cards in a story path only reference other cards in the same story path*  

**cards** - array of card structures, see format below

#### Dependency fields:

**dependencyId** - string value, the identifier of another story path which is referenced by cards within this story path  
**dependencyFile** - string value, the path of the json file of another story path which is referenced by cards within this story path  

*the path must be relative to either the root of the sd card or the asset directory of the app*  

#### Common card fields (included in all card types):

**id** - string value, an identifier for the card which is unique within the story path  
**title** - string value, a title for the card  
**references** - array of string values, references to values in other cards/story paths  

*required format:* `<story path id>::<card id>::<key>::<value>`

**values** - array of string values, values to be serialized with the card/story path and referenced by other cards/story paths

*required format:* `<key>::<value>`


#### Introduction card fields:

**references** - string value, must equal scal.io.liger.model.IntroCardModel  
**headline** - string value, a headline to display on the card, most likely the lesson title  
**level** - string value, the difficulty of the lesson, most likely a numeric value  
**time** - string value, the approximate time that the lesson will take to complete  

#### Instruction list card fields:

**references** - string value, must equal scal.io.liger.model.ClipInstructionListCardModel  
**media_path** - string value, the path of an image file to display on the card  
**header** - string value, a header to display on the card  
**bullet_list** - array of string values, a list of instructions for capturing media  

#### Instruction type card fields:

**references** - string value, must equal scal.io.liger.model.ClipInstructionTypeCardModel  
**media_path** - string value, the path of an image file to display on the card  
**header** - string value, a header to display on the card  
**clip_types** - array of string values, a list of media capture types  

#### Self evaluation card fields:

**references** - string value, must equal scal.io.liger.model.SelfEvalCardModel  
**header** - string value, a header to display on the card  
**checklist** - array of string values, a list of self evaluation options  

#### Congratulations card fields:

**references** - string value, must equal scal.io.liger.CongratsCardModel  
**headline** - string value, a headline to display on the card, most likely a congratulatory message  
**text** - string value, text to display on the card  
**story_paths** - array of string values, a list of optional follow-up story paths  


#### Sample YAML

```
---
  id: story_path
  title: STORY PATH
  dependencies: 
    - 
      dependencyId: completed_quiz
      dependencyFile: completed_quiz.json
  cards: 
    - 
      type: scal.io.liger.model.IntroCardModel
      id: intro_card
      title: INTRO CARD
      headline: Headline that creates interest for what's coming
      level: Basic 1
      time: 20 minutes
    - 
      type: scal.io.liger.model.ClipInstructionListCardModel
      id: instruction_list_card
      title: INSTRUCTION LIST CARD
      media_path: /path/to/media.file
      header: Character
      bullet_list: 
        - Bullet point structure of qualities & tips
        - Another quality
        - Another quality
    - 
      type: scal.io.liger.model.ClipInstructionTypeCardModel
      id: instruction_type_card
      title: INSTRUCTION TYPE CARD
      media_path: /path/to/media.file
      header: This card tells the maker which clip to capture.
      clip_types: 
        - Photo
        - Video
        - Import
    - 
      type: scal.io.liger.model.SelfEvalCardModel
      id: eval_card
      title: EVAL CARD
      header: What's next?
      checklist: 
        - Option 1
        - Option 2
        - Option 3
      values: 
        - option_1::true
    - 
      type: scal.io.liger.model.IntroCardModel
      id: eval_check_1
      title: EVAL CHECK 1
      headline: YOU CHECKED THE FIRST BOX
      references: 
        - story_path::eval_card::option_1::true
    - 
      type: scal.io.liger.model.IntroCardModel
      id: eval_check_2
      title: EVAL CHECK 2
      headline: YOU CHECKED THE SECOND BOX
      references: 
        - story_path::eval_card::option_2::true
    - 
      type: scal.io.liger.model.IntroCardModel
      id: eval_check_3
      title: EVAL CHECK 3
      headline: YOU CHECKED THE THIRD BOX
      references: 
        - story_path::eval_card::option_3::true
    - 
      type: scal.io.liger.CongratsCardModel
      id: congrats_card
      title: CONGRATS CARD
      headline: Congratulations!
      text: NEXT UP Create a Compelling Narrative
      story_paths: 
        - Recording stable shots
        - Photo Stories
        - Replay this path with a new story

```    

## Sample JSON:

*This is a combination of several files, the syntax should be correct, but it is not necessarily logical.*

    {
      "id": "story_path",
      "title": "STORY PATH",
      "dependencies": [
        {
          "dependencyId": "completed_quiz",
          "dependencyFile": "completed_quiz.json"
        }
      ],
      "cards": [
        {
          "type": "scal.io.liger.model.IntroCardModel",
          "id": "intro_card",
          "title": "INTRO CARD",
          "headline": "Headline that creates interest for what's coming",
          "level": "Basic 1",
          "time": "20 minutes"
        },
        {
          "type": "scal.io.liger.model.ClipInstructionListCardModel",
          "id": "instruction_list_card",
          "title": "INSTRUCTION LIST CARD",
          "media_path": "/path/to/media.file",
          "header": "Character",
          "bullet_list": [
            "Bullet point structure of qualities & tips",
            "Another quality",
            "Another quality"
          ]
        },
        {
          "type": "scal.io.liger.model.ClipInstructionTypeCardModel",
          "id": "instruction_type_card",
          "title": "INSTRUCTION TYPE CARD",
          "media_path": "/path/to/media.file",
          "header": "This card tells the maker which clip to capture.",
          "clip_types": [
            "Photo",
            "Video",
            "Import"
          ]
        },
        {
          "type": "scal.io.liger.model.SelfEvalCardModel",
          "id": "eval_card",
          "title": "EVAL CARD",
          "header": "What's next?",
          "checklist": [
            "Option 1",
            "Option 2",
            "Option 3"
          ],
          "values": [
            "option_1::true"
          ]
        },
        {
          "type": "scal.io.liger.model.IntroCardModel",
           "id": "eval_check_1",
          "title": "EVAL CHECK 1",
          "headline": "YOU CHECKED THE FIRST BOX",
          "references": [
            "story_path::eval_card::option_1::true"
          ]
        },
        {
          "type": "scal.io.liger.model.IntroCardModel",
          "id": "eval_check_2",
          "title": "EVAL CHECK 2",
          "headline": "YOU CHECKED THE SECOND BOX",
          "references": [
            "story_path::eval_card::option_2::true"
          ]
        },
        {
          "type": "scal.io.liger.model.IntroCardModel",
          "id": "eval_check_3",
          "title": "EVAL CHECK 3",
          "headline": "YOU CHECKED THE THIRD BOX",
          "references": [
            "story_path::eval_card::option_3::true"
          ]
        },
        {
          "type": "scal.io.liger.CongratsCardModel",
          "id": "congrats_card",
          "title": "CONGRATS CARD",
          "headline": "Congratulations!",
          "text": "NEXT UP Create a Compelling Narrative",
          "story_paths": [
            "Recording stable shots",
            "Photo Stories",
            "Replay this path with a new story"
          ]
        }
      ]
    }
    
## Additional card types

These were created to support aspects of the prototype, and don't necessarily correspond to any of the card types described in the design documents.

#### Clip type card fields:

**references** - string value, must equal scal.io.liger.ClipTypeCardModel  
**clip_types** - array of string values, a list of supported media types  

#### Quiz card fields:

**references** - string value, must equal scal.io.liger.model.QuizCardModel  
**description** - string value, text to display on the card, most likely a question  
**options** - array of string values, a list of possible answers to a question  

#### Quiz report card fields:

**references** - string value, must equal scal.io.liger.model.QuizReportCardModel  
**description** - string value, text to display on the card, most likely feedback on the quiz results  
**results** - array of string values, references to options selected in previous quiz cards  

*this field is obsolete now that the "references" field has been added to the base class*  

#### Video capture type card fields:

**references** - string value, must equal scal.io.liger.model.VideoCaptureTypeCardModel  
**body** - array of widget structures, see possible formats below  

*these widgets will be objects with functionality such as media capture or image display*

#### Video capture widget fields:

**references** - string value, must equal scal.io.liger.widget.VideoCaptureWidget  
**camera_references** - string value, the camera type to use for the capture, most likely front/back  

#### Markdown widget fields:

**references** - string value, must equal scal.io.liger.widget.MarkdownWidget  
**text** - string value, markdown text to display on the card 

*Android flips bold and italic from the markdown standard in the textview for some reason.  Also, here is the subset of HTML and hence Markdown that is supported: [HTML Tags Supported By TextView](http://commonsware.com/blog/2010/05/26/html-tags-supported-by-textview.html)*

#### Image widget fields:

**references** - string value, must equal scal.io.liger.widget.ImageWidget  
**path** - string value, the path of an image file to display on the card  

## Sample YAML:

```
---
  id: story_path
  title: Story Path
  cards: 
    - 
      type: scal.io.liger.model.VideoCaptureTypeCardModel
      id: video_capture
      title: Video Capture
      body: 
        - 
          type: scal.io.liger.widget.MarkdownWidget
          text: Some markdown text here
        - 
          type: scal.io.liger.widget.ImageWidget
          path: file:///sdcard/foo.jpg
        - 
          type: scal.io.liger.widget.MarkdownWidget
          text: Some more markdown
        - 
          type: scal.io.liger.widget.VideoCaptureWidget
          camera_type: front
        - 
          type: scal.io.liger.widget.MarkdownWidget
          text: Final markdown

```

## Sample JSON:

*this covers the video capture type card only, the other card types are generally similar to those covered in the previous example.*

    {
      "id": "story_path",
      "title": "Story Path",
      "cards": [
        {
          "type": "scal.io.liger.model.VideoCaptureTypeCardModel",
          "id": "video_capture",
          "title": "Video Capture",
          "body": [ 
            { 
              "type": "scal.io.liger.widget.MarkdownWidget",
              "text": "Some markdown text here"
            },
            { 
              "type": "scal.io.liger.widget.ImageWidget",
              "path": "file:///sdcard/foo.jpg"
            },
            { 
              "type": "scal.io.liger.widget.MarkdownWidget",
              "text": "Some more markdown"
            },
            { 
              "type": "scal.io.liger.widget.VideoCaptureWidget",
              "camera_type": "front"
            },
            { 
              "type": "scal.io.liger.widget.MarkdownWidget",
              "text": "Final markdown"
            }
          ]
        }
      ]
    }


## Tips

To convert YAML to JSON before you push to your phone to test, you can run a command like this:

```
python -c 'import sys, yaml, json; json.dump(yaml.load(sys.stdin), sys.stdout, indent=4)' < my.yaml > my.json

```

Then you can push this to your phone for testing:

```
adb push my.json  /sdcard/Liger/
```

## Android Iconify

Liger utilizes icon webfonts instead of the traditional path of generating multiple drawables per screen resolution.
Here are the steps to creating/editing icons:

1. Generate your desired icons in SVG format
2. Convert your SVG files to a .TTF file
  * There are several ways to do this online.  We use the command line tool [Font Custom](http://fontcustom.com/)
3. After generating your .TFF file, you need to make 3 changes:
  * Add your .TFF file to the android-iconify folder "src/main/assets/"
  * In "Iconify.java", change the 'TFF_FILE' variable to reflect your new file
    
    ```java
    private static final String TTF_FILE = "YOUR_FILE_NAME.ttf";
    ```
  * Still in "Iconify.java", change 'IconValue' variable to include your font names and unicode values
    
    ```java
    public static enum IconValue {
        fa_clip_ex_action('\uf145'),
        fa_clip_ex_character('\uf146');
    }
    ```
    *Note: Your enum keys* **must** *begin with "fa"*
4. You are now ready to use your new icons
  
    ```xml
    <IconTextView
    android:text="{fa-clip_ex_action}"
    android:shadowColor="#22000000"
    android:shadowDx="3"
    android:shadowDy="3"
    android:shadowRadius="1"
    android:textSize="90dp"
    android:textColor="#FF33B5E5"
    ... />
    ```


