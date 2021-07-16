import { Background, FloatingModule } from '../nativeModules/get';
import { DeviceEventEmitter } from 'react-native';
import { Linking } from 'react-native';

import { formatDate } from '../libraries';

import SharedPreferences from 'react-native-shared-preferences';

import 'moment/min/locales';

let _notices = [];

async function getNoticeIds(eventMessageFromChromeURL, matchingContexts, HTML) {
  const noticeIds = [];
  for (const matchingContext of matchingContexts) {
    const addWWWForBuildingURL = `www.${eventMessageFromChromeURL}`;

    if (addWWWForBuildingURL.match(new RegExp(matchingContext.urlRegex, 'g'))) {
      if (matchingContext.xpath) {
        const result = await Background.testWithXpath(
          HTML,
          matchingContext.xpath
        );

        if (result === 'true') {
          noticeIds.push(matchingContext.noticeId);
        }
        continue;
      }
      noticeIds.push(matchingContext.noticeId);
    }
  }

  return noticeIds;
}

function callActionListeners() {
  DeviceEventEmitter.addListener('floating-dismoi-bubble-press', (e) => {
    return FloatingModule.showFloatingDisMoiMessage(
      _notices,
      1500,
      _notices.length
    ).then(() => {
      // What to do when user press on the bubble
    });
  });
  DeviceEventEmitter.addListener('floating-dismoi-message-press', (e) => {
    // What to do when user press on the message
    return FloatingModule.initialize().then(() => {
      return FloatingModule.hideFloatingDisMoiMessage().then(() => {});
    });
  });

  DeviceEventEmitter.addListener('floating-dismoi-bubble-remove', (e) => {
    // What to do when user press on the message
    console.log('FLOATING DISMOI BUBBLE REMOVE');
  });

  DeviceEventEmitter.addListener('URL_CLICK_LINK', (event) => {
    FloatingModule.initialize().then(() => {
      FloatingModule.hideFloatingDisMoiBubble().then(() =>
        FloatingModule.hideFloatingDisMoiMessage()
      );
    });
    Linking.openURL(event);
  });

  DeviceEventEmitter.addListener('DELETE_NOTICE', (event) => {
    if (_notices && _notices.length === 1) {
      FloatingModule.hideFloatingDisMoiMessage();
      return;
    }

    const foundIn = [parseInt(event)];
    var res = _notices.filter(function (eachElem, index) {
      return foundIn.indexOf(index) === -1;
    });

    FloatingModule.showFloatingDisMoiMessage(res, 1500, res.length).then(() => {
      // What to do when user press on the bubble
      _notices = res;
    });
  });
}

let matchingContextFetchApi =
  'https://notices.bulles.fr/api/v3/matching-contexts?';

async function callMatchingContext(savedUrlMatchingContext) {
  console.log('_________________CALL MATHING CONTEXT____________________');

  return await fetch(matchingContextFetchApi + savedUrlMatchingContext)
    .then((response) => {
      console.log(
        '_________________END CALL MATHING CONTEXT____________________'
      );
      return response.json();
    })
    .catch((error) => {
      console.log('ERROR MATCHING CONTEXT');
      console.log(error);
    });
}

async function getHTMLOfCurrentChromeURL(eventMessageFromChromeURL) {
  return await fetch(`https://www.${eventMessageFromChromeURL}`).then(function (
    response
  ) {
    // The API call was successful!
    return response.text();
  });
}

let i = 0;
let url = '';

const HeadlessTask = async (taskData) => {
  if (i === 0) {
    callActionListeners();
    FloatingModule.initialize();
    i++;
  }
  if (taskData.hide === 'true') {
    FloatingModule.hideFloatingDisMoiBubble().then(() =>
      FloatingModule.hideFloatingDisMoiMessage()
    );
    return;
  }
  if (taskData.url !== url) {
    url = taskData.url;
    SharedPreferences.getItem('url', async function (savedUrlMatchingContext) {
      const res = await Promise.all([
        await callMatchingContext(savedUrlMatchingContext),
        await getHTMLOfCurrentChromeURL(taskData.url),
      ]);
      const matchingContexts = res[0];
      const HTML = res[1];
      const eventMessageFromChromeURL = taskData.url;
      if (eventMessageFromChromeURL) {
        if (taskData.eventText === '') {
          let noticeIds = await getNoticeIds(
            eventMessageFromChromeURL,
            matchingContexts,
            HTML
          );
          const uniqueIds = [...new Set(noticeIds)];
          let notices = await Promise.all(
            uniqueIds.map((noticeId) =>
              fetch(
                `https://notices.bulles.fr/api/v3/notices/${noticeId}`
              ).then((response) => response.json())
            )
          );
          if (notices.length > 0) {
            const noticesToShow = notices.map((result) => {
              const formattedDate = formatDate(result);
              res.modified = formattedDate;
              return result;
            });
            _notices = noticesToShow;
            FloatingModule.showFloatingDisMoiBubble(
              10,
              1500,
              notices.length,
              eventMessageFromChromeURL
            ).then(() => {
              noticeIds = [];
            });
          }
        }
      }
    });
  }
};

export default HeadlessTask;
