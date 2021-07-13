import React from 'react';
import { View, Image, useWindowDimensions } from 'react-native';
import Title from '../components/title';
import Button from '../components/button';

function Tuto2({ navigation }) {
  const window = useWindowDimensions();

  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <View style={{ marginBottom: 10 }}>
        <Title>Comment ça marche ?</Title>
      </View>
      <Image
        style={{
          width: window.width,
          height: window.height - 250,
          marginBottom: 20,
        }}
        source={require('../assets/images/tuto-3.png')}
        resizeMode={'contain'}
      />
      <View
        style={{
          flexDirection: 'column',
          justifyContent: 'flex-end',
        }}
      >
        <Button
          onPress={() => {
            return navigation.navigate('Authorizations');
          }}
          text={'Suivant'}
        />
      </View>
    </View>
  );
}

export default Tuto2;
