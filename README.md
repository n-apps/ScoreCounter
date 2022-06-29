# Score Counter Android App
Having troubles to find a pen and paper at your game night? Tired of having to put points together in your head? Score Counter will help you easily keep score in any games, sports or other activities. Will do all the math for tracking initiative, HP, hit points, spell slots or even count win streaks in your bar game night!

[<img width=760 src="https://user-images.githubusercontent.com/16646251/176421725-5702cf4d-1790-4a8b-a2cb-91388d49c201.jpg" style="float: left; padding-right: 10
px;">](https://play.google.com/store/apps/details?id=ua.napps.scorekeeper)

#### Key features
* Easy to use
*  No ads at all. NEVER
*  Great for on the fly score counting
*  Intuitive and simple design
*  Quickly adding any amount of points
*  Suitable for any game
*  Custom increments
*  Comes with a virtual dice roll
*  Dark theme
*  Works for big numbers
*  Open sourced


[<img src="https://user-images.githubusercontent.com/16646251/176419200-f4fd3bb5-b8fa-4d25-aecd-261491766fd8.jpg" width=250>]()
[<img src="https://user-images.githubusercontent.com/16646251/176419196-8d68d64e-4c9e-4ca0-b26d-5322dbb04a66.jpg" width=250>]()
[<img src="https://user-images.githubusercontent.com/16646251/176419182-a001cac7-75e5-4795-8b3e-ee06654d7994.jpg" width=250>]()


If you like this app, please leave feedback or make a donation – this will boost my mood and will help improve the app. If you want to drop me a line, I'm happy to hear from you scorekeeper.feedback@gmail.com. Thank you! 

## Contribution
If you have any ideas or feature requests – don't hesitate to reach out to me. Every contribution is welcome 😉


#### Development
To start working on the app yourself, you have to follow these steps:

1. Clone the repository (or a fork of it). Follow the instructions [here](http://help.github.com/fork-a-repo/)
2. The app uses Google Firebase, to be able to build it you need to:
   1. Create a Project on https://console.firebase.google.com/ 
   2. Download the `google-services.json`
   3. Place it in the projects `app` directory as shown on the firebase page
3. If you get an error related to signingConfigs go to your `app\build.gradle` and remove all settings related to `signingConfigs` since you do need to build signed releses of the app (alternatively create appropriate configs).
4. Open the project in Android Studio an start working
5. Send me a "pull request" from your repo - see instructions [here](https://help.github.com/articles/creating-a-pull-request-from-a-fork/)

#### Helping Translate

If you want to help translate the App you can do this in multiple ways.

With Android Studio open any of the `string.xml` files in the `res\values` directory and add a new locale using the translation editor. 
**Don't forget** to add your locale code to the `resConfig` setting in `app\build.gradle` (otherwise it wont be applied).

If you've never used git and not familiar with Android development, download [this file](https://raw.githubusercontent.com/n-apps/ScoreCounter/master/app/src/main/res/values/strings.xml) and edit it using the text editor of your choice (notepad etc...) and just email your modified files to me.

#### #StandWithUkraine
[<a target="_blank" href="https://www.standwithukraine.how/"><img src="https://user-images.githubusercontent.com/16646251/176420453-e24a0d68-72cc-4c71-8b28-f040a623ed1e.jpg" width=400></a>]()




