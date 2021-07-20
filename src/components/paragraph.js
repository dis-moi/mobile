import React from 'react';
import { Text } from 'react-native';

function Paragraph({ children, left, color, bold }) {
  return (
    <Text
      style={{
        letterSpacing: 0.9,
        textAlign: left ? 'left' : 'center',
        fontFamily: 'Helvetica',
        fontWeight: bold ? 'bold' : 'normal',
        color: color || '#000000',
        fontSize: 15,
        marginTop: 15,
        marginBottom: 15,
      }}
    >
      {children}
    </Text>
  );
}

export default Paragraph;
