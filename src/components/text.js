import React from 'react';
import { Text } from 'react-native';

function SimpleText({
  children,
  left,
  letterSpacing = 0.73,
  fontSize = 17,
  color = '#000000',
}) {
  return (
    <Text
      style={{
        textAlign: left ? 'left' : 'center',
        fontFamily: 'Helvetica',
        fontSize: fontSize,
        letterSpacing: letterSpacing || 0.73,
        color: color,
      }}
    >
      {children}
    </Text>
  );
}

export default SimpleText;
