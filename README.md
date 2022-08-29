# GuessComposer
Simple game app in quiz form - guess celebrity from image.
Information about composers is downloaded from https://www.imdb.com/list/ls053620576/ - Top 20 Movie Composers

Features:
-
- user is given four names for each photos,
- questions are displayed in random order, different during each app run,
- user must pick answer until correct one is selected,
- correct answer button turns green after click, incorrect turns red,
- summary screen displays number of correct answers and replay button.

Used technologies:
-
- AsyncTask for downloading list of composers names, image urls and ownloading images from url,
- finding views from layout with view binding.

App Screenshots
-
Question screen         |  Summary screen
:-------------------------:|:-------------------------:
<img src="https://github.com/pilichm/GuessComposer/blob/master/app/src/main/res/drawable/GuessComposerQuestionScreen.png" width="200" height="400">  |  <img src="https://github.com/pilichm/GuessComposer/blob/master/app/src/main/res/drawable/GuessComposerSummaryScreen.png" width="200" height="400"> 

App during usage
-
<img src="https://github.com/pilichm/GuessComposer/blob/master/app/src/main/res/drawable/GuessComposerDuringPlay.gif" width="200" height="400"> 
