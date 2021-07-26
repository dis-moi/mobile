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
    FloatingModule.showFloatingDisMoiMessage(
      _notices,
      1500,
      _notices.length
    ).then(() => {
      // What to do when user press on the bubble
    });
  });
  DeviceEventEmitter.addListener('floating-dismoi-message-press', (e) => {
    // What to do when user press on the message
    return FloatingModule.hideFloatingDisMoiMessage().then(() => {});
  });

  DeviceEventEmitter.addListener('URL_CLICK_LINK', (event) => {
    FloatingModule.hideFloatingDisMoiBubble().then(() =>
      FloatingModule.hideFloatingDisMoiMessage()
    );
    Linking.openURL(event);
  });

  DeviceEventEmitter.addListener('DELETE_NOTICE', (event) => {
    const contributorName = _notices[parseInt(event)].contributor.name;

    const noticeId = _notices[parseInt(event)].id;
    SharedPreferences.getItem(contributorName, function (value) {
      const json = JSON.parse(value);
      json.noticeDeleted = noticeId;

      const stringifyJson = JSON.stringify(json);

      SharedPreferences.setItem(contributorName, stringifyJson);

      const foundIn = [parseInt(event)];
      var res = _notices.filter(function (eachElem, index) {
        return foundIn.indexOf(index) === -1;
      });

      if (_notices && _notices.length === 1) {
        FloatingModule.hideFloatingDisMoiMessage();
        return;
      }

      FloatingModule.showFloatingDisMoiMessage(res, 1500, res.length).then(
        () => {
          // What to do when user press on the message
          _notices = res;
        }
      );
    });
  });
}

async function initializeFloatingLayout() {
  return FloatingModule.initializeBubble().then(async () => {
    return await FloatingModule.initializeMessage();
  });
}

let matchingContextFetchApi =
  'https://notices.bulles.fr/api/v3/matching-contexts?';

let matchingContextIsCalling = false;

async function callMatchingContext(savedUrlMatchingContext) {
  console.log('_________________CALL MATHING CONTEXT____________________');

  matchingContextIsCalling = true;

  return await fetch(matchingContextFetchApi + savedUrlMatchingContext)
    .then((response) => {
      console.log(
        '_________________END CALL MATHING CONTEXT____________________'
      );
      matchingContextIsCalling = false;
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

function getNoticeIdsThatAreNotDeleted(contributors, noticesToShow) {
  const noticesIdToDelete = contributors
    .map((contributor) => {
      if (contributor?.noticeDeleted) {
        return contributor.noticeDeleted;
      }
    })
    .filter(Boolean);

  const noticesIdFromNoticesToShow = noticesToShow
    .map((notice) => {
      return notice.id;
    })
    .filter(Boolean);

  return noticesIdFromNoticesToShow.filter(
    (e) => !noticesIdToDelete.find((a) => e === a)
  );
}

let i = 0;

const HeadlessTask = async (taskData) => {
  if (i === 0) {
    callActionListeners();
    await initializeFloatingLayout();
    i++;
  }

  if (taskData.hide === 'true') {
    FloatingModule.hideFloatingDisMoiBubble().then(() =>
      FloatingModule.hideFloatingDisMoiMessage()
    );
    return;
  }
  SharedPreferences.getItem('url', async function (savedUrlMatchingContext) {
    if (matchingContextIsCalling === false) {
      const res = await Promise.all([
        await callMatchingContext(savedUrlMatchingContext),
        await getHTMLOfCurrentChromeURL(taskData.url),
      ]);
      const matchingContexts = res[0];
      const HTML = res[1];
      const eventMessageFromChromeURL = taskData.url;
      if (eventMessageFromChromeURL) {
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

            result.modified = formattedDate;
            return result;
          });

          SharedPreferences.getAll(function (values) {
            const contributors = [
              ...new Set(
                values
                  .map((result) => {
                    if (result[0] !== 'url') {
                      return JSON.parse(result[1]);
                    }
                  })
                  .filter(Boolean)
              ),
            ];

            const noticeIdNotDeleted = getNoticeIdsThatAreNotDeleted(
              contributors,
              noticesToShow
            );

            if (noticeIdNotDeleted.length > 0) {
              _notices = noticeIdNotDeleted.map((id) => {
                return noticesToShow.find(
                  (noticeToShow) => noticeToShow.id === id
                );
              });

              console.log('POST');

              FloatingModule.showFloatingDisMoiBubble(
                10,
                1500,
                notices.length,
                eventMessageFromChromeURL
              ).then(() => {
                noticeIds = [];
              });
            }
          });
        }
      }
    }
  });
};

export default HeadlessTask;
