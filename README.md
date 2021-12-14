# Score Counter Android App
#### About
Having troubles to find a pen and paper at your game night? Tired of having to put points together in your head? Score Counter will help you easily keep score in any games, sports or other activities. Will do all the math for tracking initiative, HP, hit points, spell slots or even count win streaks in your bar game night!

[<img width=760 src="https://user-images.githubusercontent.com/16646251/146039858-865f614a-a773-4c89-b967-7262d1126dc6.png" style="float: left; padding-right: 10
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

[<img src="https://play-lh.googleusercontent.com/22qjCfwppjUYUqcOmaYw0T3WEthlYYYJnCPgaqdvZVGzUI0ggd3iyvTmlWk2UL-EcA=w4096-h2134" width=250>]()
[<img src="https://play-lh.googleusercontent.com/do8VnBuMdzCRs4pbG_FY11WHbkSR_c1H2BcNknU3Be2q3KABXInQmwTqMTSlz0DiJq1c=w4096-h2134" width=250>]()
[<img src="https://play-lh.googleusercontent.com/NA85YCtSkVvbdh0OYv-k9SQwdSm3dCZHqM_Fvs_xIoDt4tOvZ044p-hSbLzeEGKg_HA=w4096-h2134" width=250>]()


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
