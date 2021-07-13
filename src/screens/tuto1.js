import React from 'react';
import { View, Image, useWindowDimensions } from 'react-native';
import Button from '../components/button';
import Title from '../components/title';

function Tuto1({ navigation }) {
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
        source={require('../assets/images/tuto-2.png')}
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
            return navigation.navigate('Tuto2');
          }}
          text={'Suivant'}
        />
      </View>
    </View>
  );
}

export default Tuto1;
