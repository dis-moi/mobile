# DisMoi - mobile

> _Dis moi_ means _Tell me_ in :fr: French.

**Dismoi** is a web extension that allows anyone to post information directly on any web page you browse.
If you follow an informer, his messages are displayed at the time you visit the pages he has commented on.

To learn more about the use cases, visit the :fr: [**Dismoi** website](https://www.dismoi.io/).

> At the time of writing, information are stored in database and exposed via the [**Dismoi** Backend](https://github.com/dis-moi/backend).

## Why DisMoi - mobile ?

This project should work the same way as web extension but for mobile: it shows a very simple DisMoi overlay that overlap the chrome app. The overlay can be removed when the user is leaving the app and is shown when the user enters an url that is matching an advisor. The standard option is that the message from the scout advisor should be displayed in the same screen as the chrome app.

Basically, we might want to do something close to amazon assistant by showing a panel above chrome, about a bit less than half the height of the screen.

## Goals and difficulties

This app should facilitate the navigation with the chrome app by providing contextual information to the user but we should do everything to not "hurt" too much the user experience. The difficulty here is that on a small mobile screen it's harder to ignore the scout advisor layout. The user might be bothered and our app might be a source of trouble for the concentration and the focus of the user.

If we want this app to work well, it has to be very intrusive by having a full control of the device. So our app can read all content on the screen and even harware sensors. Hence, dismoi should show trust to the user. We need to this app's acceptability.

An other thing we might think of is by doing a DisMoi for mobile apps, or even for spam, phishing messages. It can be transformed as a collaborative anti-virus for mobile.

## Architecure

This project is mainly coded in java to receive all the accessibility events. React-native is being used to display these events and the overlay is done with the help of XML documents.

## Development

1. Clone the repository

```bash
git clone git@github.com:dis-moi/mobile_poc.git dismoi-mobile
```

2. Environment setup

[Get started with react-native environment setup](https://reactnative.dev/docs/environment-setup)

3. Install [yarn](https://yarnpkg.com/)

> https://classic.yarnpkg.com/en/docs/install

4. Install dependencies

```bash
yarn
```

4. Start the project

```bash
yarn start
yarn run android
```

## Release

1. Increment `versionName` in `android/app/build.gradle`.
> For example set version to `1.0.23`: `versionName "1.0.23"`

2. Tag the commit accordingly and push it:
```
git tag 1.0.23
git push origin --tags 
```

## Contributing

You’re welcome to help ! Please read the [**DisMoi** Contributing Guidelines](CONTRIBUTING.md).

## Follow us

- 🌐 Web: https://www.dismoi.io
- 🐦 Twitter: https://twitter.com/DisMoiCompagnon

## LICENSE

[GNU AGPL v3](LICENSE)

> Copyright (C) 2020 INSITU SAS
>
> This program is free software: you can redistribute it and/or modify
> it under the terms of the GNU Affero General Public License as
> published by the Free Software Foundation, either version 3 of the
> License, or (at your option) any later version.

> This program is distributed in the hope that it will be useful,
> but WITHOUT ANY WARRANTY; without even the implied warranty of
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
> GNU Affero General Public License for more details.

> You should have received a copy of the GNU Affero General Public License
> along with this program.  If not, see <https://www.gnu.org/licenses/>.

