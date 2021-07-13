import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import {
  createStackNavigator,
  TransitionPresets,
} from '@react-navigation/stack';

import Welcome from '../screens/welcome';
import Tuto1 from '../screens/tuto1';
import Tuto2 from '../screens/tuto2';
import Finished from '../screens/finished';
import Authorizations from '../screens/authorizations';
import stackScreenOptions from './stackScreenOptions';

const Stack = createStackNavigator();

const TransitionScreenOptions = {
  ...TransitionPresets.SlideFromRightIOS, // This is where the transition happens
};

function StartOnboarding() {
  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={TransitionScreenOptions}
        initialRouteName={'Welcome'}
      >
        <Stack.Screen
          name="Welcome"
          component={Welcome}
          options={stackScreenOptions()}
        />
        <Stack.Screen
          name="Tuto1"
          component={Tuto1}
          options={stackScreenOptions()}
        />
        <Stack.Screen
          name="Tuto2"
          component={Tuto2}
          options={stackScreenOptions()}
        />
        <Stack.Screen
          name="Authorizations"
          component={Authorizations}
          options={stackScreenOptions()}
        />
        <Stack.Screen
          name="Finished"
          component={Finished}
          options={stackScreenOptions()}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default StartOnboarding;
