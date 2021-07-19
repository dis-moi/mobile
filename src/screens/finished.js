import React from 'react';
import {
  FlatList,
  View,
  TouchableOpacity,
  ActivityIndicator,
} from 'react-native';
import { Body, Left, CardItem } from 'native-base';
import Title from '../components/title';
import Button from '../components/button';
import SharedPreferences from 'react-native-shared-preferences';
import Paragraph from '../components/paragraph';

import useEverythingIsReadyEffect from '../useEffectHooks/everythingIsReady';
import useHandleContributorsEffect from '../useEffectHooks/handleContributors';
import PopUp from '../components/popUp';
import ItemFromList from '../components/itemFromList';
import ContributorLogo from '../components/contributorLogo';
import SimpleText from '../components/text';
import RadioButtons from '../components/radioButtons';
import { FloatingModule } from '../nativeModules/get';

function Finished() {
  const [itemIds, setItemIds] = React.useState([]);
  const [
    radioButtonThatIsActivated,
    setRadioButtonThatIsActivated,
  ] = React.useState('ALL');

  const [modalVisible, setModalVisible] = React.useState(false);

  const [contributorForModal, setContributorForModal] = React.useState({});

  useEverythingIsReadyEffect();

  const { contributors, filteredContributors } = useHandleContributorsEffect(
    radioButtonThatIsActivated,
    setItemIds,
    itemIds
  );

  function contributorItemFromList({ item }) {
    if (item.name && item?.avatar?.normal?.url) {
      return (
        <ItemFromList borderRadius={15}>
          <CardItem
            style={{ borderTopLeftRadius: 15, borderTopRightRadius: 15 }}
          >
            <Left>
              <ContributorLogo source={item?.avatar?.normal.url} />
              <Body>
                <Title left fontSize={20}>
                  {item.name}
                </Title>
                <View style={{ marginTop: 12 }}>
                  {itemIds.includes(item.id) ||
                  itemIds.includes(String(item.id)) ? (
                    <Button
                      small
                      text={'Abonné(e)'}
                      icon
                      border
                      backgroundColor={'white'}
                      color={
                        itemIds.includes(item.id) ||
                        itemIds.includes(String(item.id))
                          ? 'black'
                          : 'white'
                      }
                      onPress={() => {
                        setItemIds(itemIds.filter((id) => id !== item.id));
                        SharedPreferences.removeItem(item.name);
                      }}
                    />
                  ) : (
                    <Button
                      small
                      text={'Suivre'}
                      onPress={() => {
                        setItemIds([...itemIds, String(item.id)]);
                        SharedPreferences.setItem(
                          item.name,
                          `{"id": ${item.id}}`
                        );
                      }}
                    />
                  )}
                </View>
              </Body>
            </Left>
          </CardItem>
          <CardItem cardBody>
            <Paragraph>{item.title}</Paragraph>
          </CardItem>
          <CardItem
            style={{ borderBottomLeftRadius: 15, borderBottomRightRadius: 15 }}
          >
            <TouchableOpacity
              onPress={() => {
                if (itemIds.includes(item.id)) {
                  FloatingModule.openLink(
                    item.contribution.example.exampleMatchingUrl
                  );
                } else {
                  setContributorForModal({
                    id: item.id,
                    name: item.name,
                    exampleMatchingUrl:
                      item.contribution.example.exampleMatchingUrl,
                  });
                  setModalVisible(true);
                }
              }}
            >
              <SimpleText color={'#1e52b4'} letterSpacing={0.3}>
                Voir un exemple réel >
              </SimpleText>
            </TouchableOpacity>
          </CardItem>
        </ItemFromList>
      );
    }
  }

  return (
    <View style={{ backgroundColor: '#e9eaef' }}>
      <PopUp modalVisible={modalVisible} setModalVisible={setModalVisible}>
        {itemIds.includes(contributorForModal.id) ? (
          <View style={{ marginBottom: 30 }}>
            <SimpleText>
              Vous êtes abonné(e) à {contributorForModal.name}.
            </SimpleText>
          </View>
        ) : (
          <View style={{ marginBottom: 30 }}>
            <SimpleText>
              Veuillez suivre {contributorForModal.name} pour voir ses
              contributions.
            </SimpleText>
          </View>
        )}
        {itemIds.includes(contributorForModal.id) ? (
          <Button
            small
            text={"Voir l'exemple"}
            backgroundColor={'#07224a'}
            onPress={() => {
              setModalVisible(false);
              FloatingModule.openLink(contributorForModal.exampleMatchingUrl);
            }}
          />
        ) : (
          <Button
            small
            text={'Suivre'}
            onPress={() => {
              setItemIds([...itemIds, String(contributorForModal.id)]);
              SharedPreferences.setItem(
                contributorForModal.name,
                `{"id": ${contributorForModal.id}}`
              );
            }}
          />
        )}
      </PopUp>
      <View
        style={{
          height: 50,
          justifyContent: 'center',
        }}
      >
        <Title>Choix des contributeurs</Title>
      </View>
      <RadioButtons
        onPress={(value) => {
          setRadioButtonThatIsActivated(value);
        }}
      />
      {contributors.length === 0 && (
        <ActivityIndicator isVisible size={40} color={'#2855a2'} />
      )}

      {contributors.length > 0 && (
        <FlatList
          style={{ marginTop: 15 }}
          data={
            radioButtonThatIsActivated === 'ALL'
              ? contributors
              : filteredContributors.sort((res) => res.contributions)
          }
          renderItem={contributorItemFromList}
          keyExtractor={(item) => String(item.id)}
        />
      )}
    </View>
  );
}

export default Finished;
