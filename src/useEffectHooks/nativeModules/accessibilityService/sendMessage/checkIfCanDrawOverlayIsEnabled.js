import React from 'react';
import { Background } from '../../../../nativeModules/get';

function CheckIfCanDrawOverlayIsEnabledEffect(appState) {
  const [
    canDrawOverlayServiceEnabled,
    setCanDrawOverlayServiceEnabled,
  ] = React.useState(false);

  React.useEffect(() => {
    let cancelled = false;

    function callCheckIfOverlayEnabledMethod() {
      Background.canDrawOverlay((result) => {
        if (!cancelled) {
          if (result === '1') {
            setCanDrawOverlayServiceEnabled(true);
          }
          if (result === '0') {
            setCanDrawOverlayServiceEnabled(false);
          }
        }
      });
    }

    callCheckIfOverlayEnabledMethod();

    return () => {
      cancelled = true;
    };
  }, [appState]);

  return canDrawOverlayServiceEnabled;
}

export default CheckIfCanDrawOverlayIsEnabledEffect;
