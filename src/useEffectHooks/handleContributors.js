import React from 'react';
import SharedPreferences from 'react-native-shared-preferences';

function HandleContributorsEffect(
  radioButtonThatIsActivated,
  setItemIds,
  itemIds
) {
  const [contributors, setContributors] = React.useState([]);
  const [filteredContributors, setFilteredContributors] = React.useState([]);

  function sortNumberOfPublishedNoticeFromHighestToLowest(contributorsToSort) {
    return contributorsToSort.sort(function (previous, next) {
      return (
        next.contribution.numberOfPublishedNotices -
        previous.contribution.numberOfPublishedNotices
      );
    });
  }

  React.useEffect(() => {
    let cancelled = false;

    async function getContributors() {
      SharedPreferences.getAll(async function (values) {
        const ids = [
          ...new Set(
            values
              .map((res) => {
                if (res[0] !== 'url') {
                  return res[1];
                }
              })
              .filter(Boolean)
          ),
        ];

        const contributorsFromV3api = await fetch(
          'https://notices.bulles.fr/api/v3/contributors'
        ).then((response) => {
          return response.json();
        });
        const contributorsSorted = sortNumberOfPublishedNoticeFromHighestToLowest(
          contributorsFromV3api.filter(
            (contributor) => !ids.includes(String(contributor.id))
          )
        );
        const contributorsFollowedSorted = sortNumberOfPublishedNoticeFromHighestToLowest(
          contributorsFromV3api.filter((contributor) =>
            ids.includes(String(contributor.id))
          )
        );
        if (!cancelled) {
          setContributors([
            ...contributorsFollowedSorted,
            ...contributorsSorted,
          ]);
          setItemIds(ids);
        }
      });
    }
    getContributors();

    return () => {
      cancelled = true;
    };
  }, [radioButtonThatIsActivated, setItemIds]);

  React.useEffect(() => {
    let cancelled = false;

    if (radioButtonThatIsActivated !== 'ALL') {
      const filteredContributorsByCategories = contributors.filter((res) =>
        res.categories.includes(radioButtonThatIsActivated)
      );

      const contributorsSorted = sortNumberOfPublishedNoticeFromHighestToLowest(
        filteredContributorsByCategories.filter(
          (contributor) => !itemIds.includes(String(contributor.id))
        )
      );
      const contributorsFollowedSorted = sortNumberOfPublishedNoticeFromHighestToLowest(
        filteredContributorsByCategories.filter((contributor) =>
          itemIds.includes(String(contributor.id))
        )
      );

      if (!cancelled) {
        setFilteredContributors([
          ...contributorsFollowedSorted,
          ...contributorsSorted,
        ]);
      }
    }
    return () => {
      cancelled = true;
    };
  }, [radioButtonThatIsActivated, contributors, itemIds]);

  React.useEffect(() => {
    function setUrl() {
      const uniqueIds = [...new Set(itemIds)];

      let queryURL = '';

      uniqueIds.forEach((contributorId, index) => {
        if (contributorId) {
          if (index === 0) {
            queryURL = queryURL + `contributors[]=${contributorId}`;
          } else {
            queryURL = queryURL + `&contributors[]=${contributorId}`;
          }
        }
      });

      SharedPreferences.setItem('url', queryURL);
    }

    setUrl();
  }, [itemIds]);

  return { filteredContributors, contributors };
}

export default HandleContributorsEffect;
