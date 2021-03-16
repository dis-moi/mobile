import React from 'react';
import {FloatingBubble} from '../../nativeModules/get';

function useFloatingBubbleRequestPermissionEffect() {
  React.useEffect(() => {
    function createListener() {
      // You can await here
      // To display the bubble over other apps you need to get 'Draw Over Other Apps' permission from android.
      // If you initialize without having the permission App could crash
      FloatingBubble.requestPermission()
        .then(() => console.log('Permission Granted'))
        .catch(() => console.log('Permission is not granted'));
    }

    createListener();
  }, []);
}

export default useFloatingBubbleRequestPermissionEffect;
