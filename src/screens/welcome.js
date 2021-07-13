import React from 'react';
import { View, Image } from 'react-native';
import Title from '../components/title';
import Paragraph from '../components/paragraph';
import Screen from '../components/screen';
import Button from '../components/button';

function Welcome({ navigation }) {
  return (
    <Screen>
      <View
        style={{
          flex: 1,
          flexDirection: 'column',
          justifyContent: 'space-around',
        }}
      >
        <Title>Merci d'avoir téléchargé l'application DisMoi.</Title>
        <View>
          <Paragraph>
            Grâce aux contributeurs de DisMoi, entrez dans un web plus sûr, plus
            transparent et plus ouvert aux alternatives.
          </Paragraph>
          <View
            style={{
              flexDirection: 'row',
              justifyContent: 'space-around',
              flexWrap: 'wrap',
            }}
          >
            <Image
              style={{ height: 80, width: 80, margin: 5 }}
              source={require('../assets/images/quechoisir.png')}
            />
            <Image
              style={{ height: 80, width: 80, margin: 5 }}
              source={require('../assets/images/lemonde.png')}
            />
            <Image
              style={{ height: 80, width: 80, margin: 5 }}
              source={require('../assets/images/lesinrocks.png')}
            />
            <Image
              style={{ height: 80, width: 80, margin: 5 }}
              source={require('../assets/images/60millions.png')}
            />
            <Image
              style={{ height: 80, width: 80, margin: 5 }}
              source={require('../assets/images/selonMicode.png')}
            />
            <Image
              style={{ height: 80, width: 80, margin: 5 }}
              source={require('../assets/images/lesNumeriques.png')}
            />
            <Image
              style={{ height: 80, width: 80, margin: 5 }}
              source={require('../assets/images/wirecutter.png')}
            />
          </View>
        </View>
      </View>
      <View
        style={{
          flexDirection: 'column',
          justifyContent: 'flex-end',
        }}
      >
        <Button
          onPress={() => {
            return navigation.navigate('Tuto1');
          }}
          text={'Suivant'}
        />
      </View>
    </Screen>
  );
}

export default Welcome;
