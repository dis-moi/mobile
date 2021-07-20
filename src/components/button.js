import React from 'react';
import { TouchableOpacity, Dimensions, View, Text } from 'react-native';
import { Icon } from 'native-base';

function Button({
  onPress,
  text,
  backgroundColor = '#2855a2',
  disabled,
  small,
  height,
  width,
  border,
  borderRadius,
  icon,
  color = 'white',
}) {
  return (
    <TouchableOpacity
      style={{
        height: height ? height : small ? 50 : 60,
        backgroundColor: backgroundColor,
        borderColor: border ? 'black' : backgroundColor,
        borderWidth: 2,
        borderRadius: borderRadius || 10,
        justifyContent: 'space-around',
        width: width
          ? width
          : small
          ? 130
          : Dimensions.get('window').width - 50,
        flexDirection: 'row',
      }}
      onPress={onPress}
      disabled={disabled}
    >
      {icon && (
        <View style={{ justifyContent: 'center', paddingLeft: 5 }}>
          <Icon type="Entypo" name="check" style={{ fontSize: 18 }} />
        </View>
      )}
      <View style={{ justifyContent: 'center', paddingRight: icon ? 5 : 0 }}>
        <Text
          style={{
            letterSpacing: 0.9,
            fontFamily: 'Helvetica',
            fontWeight: 'bold',
            color: color,
            fontSize: 15,
          }}
        >
          {text}
        </Text>
      </View>
    </TouchableOpacity>
  );
}

export default Button;
