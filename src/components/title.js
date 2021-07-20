import React from 'react';
import { Text } from 'react-native';

function Title({ children, left, fontSize }) {
  return (
    <Text
      style={{
        textAlign: left ? 'left' : 'center',
        fontFamily: 'Helvetica-Bold',
        fontSize: fontSize || 18,
        letterSpacing: 0.73,
        color: '#000000',
      }}
    >
      {children}
    </Text>
  );
}

export default Title;
