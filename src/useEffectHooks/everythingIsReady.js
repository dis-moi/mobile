import React from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

function EverythingIsReady() {
  React.useEffect(() => {
    let cancelled = false;

    AsyncStorage.getItem('alreadyLaunched').then((value) => {
      if (value == null && !cancelled) {
        AsyncStorage.setItem('alreadyLaunched', 'true');
      }
    });

    return () => {
      cancelled = true;
    };
  }, []);
}

export default EverythingIsReady;
