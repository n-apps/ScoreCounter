# Score Counter Android App


[<a href="https://play.google.com/store/apps/details?id=ua.napps.scorekeeper"><img src="https://user-images.githubusercontent.com/16646251/228914788-19aadb36-28ec-4954-a630-ecdd71460fc5.png" width=600></a>]()

Score Counter – Count Anything is the perfect solution for game nights, sports competitions, or any activity where you need to keep track of scores on the fly. With its simple and customizable design, this app makes score counting quick and easy, without any interruptions from ads. Whether you're tracking initiative, hit points (HP), spell slots, or even win streaks in your bar game night, Score Counter has you covered.

Score Counter is a free app without any ads and with all its features available. However, if you would like to support an indie developer from Ukraine (that's me!), you can make a one-time or multiple contributions within the Score Counter app. If you have any feedback, questions, or notice any mistakes in the app's translation, please don't hesitate to reach out to me via email at scorekeeper.feedback@gmail.com. Thank you!


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

Or I can prepare a Google Spreadsheet for you :)

#### #StandWithUkraine
[<a target="_blank" href="https://www.standwithukraine.how/"><img src="https://user-images.githubusercontent.com/16646251/228910177-a799b0c1-0e68-499f-8212-c1bc650d3bd8.png" width=400></a>]()

## Donate
Score Counter is a free app without any ads and with all its features available. However, if you would like to support an indie developer from Ukraine (that's me!), you can make a one-time or multiple contributions within the Score Counter app. If you have any feedback, questions, or notice any mistakes in the app's translation, please don't hesitate to reach out to me via email at scorekeeper.feedback@gmail.com. Thank you!

<a href="https://www.paypal.com/donate/?hosted_button_id=QCHWF4FJLKQ34"><img src="https://raw.githubusercontent.com/andreostrovsky/donate-with-paypal/master/blue.svg" height="56"></a>  
If you enjoyed this project — or just feeling generous, consider buying me a coffee/borsch. Cheers! :beers:


