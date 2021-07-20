import React from 'react';
import { Thumbnail } from 'native-base';

function ContributorLogo({ source, size = 80 }) {
  return (
    <Thumbnail
      style={{ width: size, height: size, borderRadius: size / 2, right: 3 }}
      source={{ uri: source }}
    />
  );
}

export default ContributorLogo;
