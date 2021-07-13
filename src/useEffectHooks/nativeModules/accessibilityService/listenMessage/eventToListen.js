import React from 'react';
import { NativeEventEmitter } from 'react-native';
import { Background } from '../../../../nativeModules/get';

function useAccessibilityServiveEventToListenFromNativeModuleEffect(
  eventToListenFromNativeModule
) {
  let eventListener = React.useRef(null);

  const [eventMessage, setEventMessage] = React.useState('');

  React.useEffect(() => {
    let cancelled = false;
    function createListener() {
      const eventEmitter = new NativeEventEmitter(Background);
      eventListener.current = eventEmitter.addListener(
        eventToListenFromNativeModule,
        (event) => {
          if (!cancelled) {
            setEventMessage(event);
          }
        }
      );
    }

    createListener();

    return () => {
      eventListener.current.remove();
      cancelled = true;
    };
  }, [eventToListenFromNativeModule]);

  return eventMessage;
}

export default useAccessibilityServiveEventToListenFromNativeModuleEffect;
