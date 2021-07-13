import React from 'react';
import { Text, View } from 'react-native';
import { Switch, ListItem, Body, Right } from 'native-base';
import { Background } from '../nativeModules/get';
import Screen from '../components/screen';
import Button from '../components/button';
import Title from '../components/title';
import SimpleText from '../components/text';
import Paragraph from '../components/paragraph';
import Italic from '../components/italic';
import listenToAppStateUseEffect from '../useEffectHooks/appState';
import useCheckIfAccessibilityIsEnabledEffect from '../useEffectHooks/nativeModules/accessibilityService/sendMessage/checkIfAccessibilityIsEnabled';
import useCheckIfCanDrawOverlayIsEnabledEffect from '../useEffectHooks/nativeModules/accessibilityService/sendMessage/checkIfCanDrawOverlayIsEnabled';

function Authorizations({ navigation }) {
  const appState = listenToAppStateUseEffect();

  const accessibilityServiceIsEnabled = useCheckIfAccessibilityIsEnabledEffect(
    appState
  );

  const canDrawOverlayIsChecked = useCheckIfCanDrawOverlayIsEnabledEffect(
    appState
  );

  return (
    <Screen>
      <View
        style={{
          flex: 1,
          flexDirection: 'column',
          justifyContent: 'space-around',
        }}
      >
        <Title>Finaliser l'installation</Title>
        <Paragraph>
          Pour fonctionner, Dismoi nécessite encore les autorisations
          d'interagir avec votre navigateur.
        </Paragraph>
        <View>
          <ListItem icon style={{ marginBottom: 15 }}>
            <Body>
              <SimpleText left letterSpacing={0.9} fontSize={14}>
                Paramètres d'accessibilité
              </SimpleText>
            </Body>
            <Right>
              <Switch
                trackColor={{ false: '#767577', true: '#2855a2' }}
                style={{ transform: [{ scaleX: 1.6 }, { scaleY: 1.5 }] }}
                thumbColor={'#f4f3f4'}
                onValueChange={() => {
                  Background.redirectToAppAccessibilitySettings().then(
                    () => {}
                  );
                }}
                value={accessibilityServiceIsEnabled}
              />
            </Right>
          </ListItem>
          <ListItem icon style={{ marginTop: 15 }}>
            <Body>
              <SimpleText left letterSpacing={0.9} fontSize={14}>
                Superposition
              </SimpleText>
            </Body>
            <Right>
              <Switch
                onValueChange={() => {
                  Background.redirectToAppAdvancedSettings().then(() => {});
                }}
                style={{ transform: [{ scaleX: 1.6 }, { scaleY: 1.5 }] }}
                trackColor={{ false: '#767577', true: '#2855a2' }}
                thumbColor={'#f4f3f4'}
                value={canDrawOverlayIsChecked}
              />
            </Right>
          </ListItem>
        </View>
        <Italic>
          Pour rappel, DisMoi n'exploite et ne revend aucune donnée personnelle.
        </Italic>
      </View>
      <View
        style={{
          flexDirection: 'column',
          justifyContent: 'flex-end',
        }}
      >
        <Button
          text={'Terminer'}
          backgroundColor={
            accessibilityServiceIsEnabled === false ||
            canDrawOverlayIsChecked === false
              ? '#a9a9a9'
              : '#2855a2'
          }
          onPress={() => {
            return navigation.navigate('Finished');
          }}
          disabled={
            accessibilityServiceIsEnabled === false ||
            canDrawOverlayIsChecked === false
          }
        />
      </View>
    </Screen>
  );
}

export default Authorizations;
