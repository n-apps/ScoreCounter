# Score Counter + 🎲

Having troubles to find a pen and paper at your fingertips when playing? Score Counter + 🎲 will help you easily keep score in any games.

[<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/c/cd/Get_it_on_Google_play.svg/500px-Get_it_on_Google_play.svg.png" style="float: left; padding-right: 10px;" height="70">](https://play.google.com/store/apps/details?id=ua.napps.scorekeeper)

## Key features
* Easy to use
* Modern and simple design
* Suitable for any game. Multiple counters on one screen
* You can change the name, initial value, step and even color for any counter
* Can't find a dice? There are d6, d8, d20, and even d88 :)
* Shake to roll
* Dark theme will save your eyes in the evening :)
* No ads at all. NEVER

I 🖤 board games and appreciate your feedback. I regularly use this application for Carcassone, Star Realms or even UNO :)

## Screenshots
[<img src="https://lh3.googleusercontent.com/sP04i1HuOFsjz-fstQh-2ZD_admLQZcqoeQfV0CDjUKbzx6HfosxxXcD8vNUCQffxw=s0" width=250>]()
[<img src="https://lh3.googleusercontent.com/g4yJZHDimvB1DWzNnTzpU2E8DxgLfaNeFct9DucvWxZ-iypYg7aMG6r5bfm4khBBmy6C=s0" width=250>]()
[<img src="https://lh3.googleusercontent.com/nEdxwJLKelY22x_VM6ahYE7QZ1X1Ld0faLhjBFR586Wpr-v6EoWj1See2uldOD3dnvQ_=s0" width=250>]()

## Contribution
If you have any ideas or feature requests, don't hesitate to reach out to me. Every contribution is welcome 😉

### Development

To start working on the app yourself, you have to follow these steps

1. Clone the repository (or a fork of it). Follow the instructions [here](http://help.github.com/fork-a-repo/)
2. The app uses Google Firebase, to be able to build it you need to:
   1. Create a Project on https://console.firebase.google.com/ 
   2. Download the `google-services.json`
   3. Place it in the projects `app` directory as shown on the firebase page
3. If you get an error related to signingConfigs go to your `app\build.gradle` and remove all settings related to `signingConfigs` since you do need to build signed releses of the app (alternatively create appropriate configs).
4. Open the project in Android Studio an start working
5. Send me a "pull request" from your repo - see instructions [here](https://help.github.com/articles/creating-a-pull-request-from-a-fork/)

### Helping Translate

If you want to help translate the App you can do this in multiple ways.

With Android Studio open any of the `string.xml` files in the `res\values` directory and add a new locale using the translation editor. Without Android Studio just create a `values` folder with the desired country code and translate in the xml file itself.

**Don't forget** to add your locale code to the `resConfig` setting in `app\build.gradle` (otherwise it wont be applied).

If you've never used git and not familiar with Android development, download [this file](https://raw.githubusercontent.com/n-apps/ScoreCounter/master/app/src/main/res/values/strings.xml) and edit it using the text editor of your choice (notepad etc...) and just email your modified files to me.
