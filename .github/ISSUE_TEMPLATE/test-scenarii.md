---
name: 🐛 Scenario testing
about: Test various scenarii on your device
---

### 1. Configuration

* Device brand `(Samsung, etc...)`:
* Device name `(Galaxy S8, etc...)`:
* Device model number `(settings > about phone)`:
* Android OS version `(settings > about phone > Android version)`:
* Browser: **Chrome**
* Browser version `(settings > apps > Chrome > bottom of page)`:
* Dismoi App version `(settings > apps > Dismoi > bottom of page)`:
---
### 2. Test scenarii

The test scenarii are listed on individual lines below. 
Each test case has 3 parts: 1 TEST NAME, 2. EXPECTED OUTCOME, 3. WORKS AS INTENDED.
Remove either the Y or the N from 3 once you have tested this on your device. 

If you chose N (i.e. it did not work as intended) please report it as a bug here: https://github.com/dis-moi/mobile_poc/issues/new?assignees=&labels=&template=Bugs.md

| TEST NAME | EXPECTED OUTCOME | WORKS? (Y/N) |
| ----------- | ----------- | ----------- | 
| Some example | Some outcome | Y |
| install App | app installs | Y/N |
| Click "open" when app is dowloaded | app opens | Y/N |
| Click "done" when app is dowloaded | closes installation | Y/N |
| open app from app icon | app opens on tutorial page 1 | Y/N |
| on tuto page 1, click suivant | goes to tuto page 2 | Y/N |
| on tuto page 1, click back (native button) | minimizes Dismoi app | Y/N |
| on tuto page 2, click suivant | goes to tuto page 3 | Y/N |
| on tuto page 3, click suivant | goes to tuto page 4 | Y/N |
| on tuto page 4, click suivant | goes to accessibility options, 2 x toggles are off, finish button inactive (grey) | Y/N |
| click on overlay toggle | go to Dismoi overlay setting | Y/N |
| turn overlay ON > navigate back to Dismoi | overlay toggle is ON, finish button is still inactive (grey) | Y/N |
| click on accessibility toggle | goes to Settings > Accessibility menu | Y/N |
| go to services > Dismoi > turn accessibility ON > confirm > navigate back to Dismoi | accessibility toggle is ON, finish button is active (blue) | Y/N |
| click finish button | goes to contributor selection page | Y/N |
| click on category radio button 2 | contributor list updates | Y/N |
| click on category radio button 3 | contributor list updates | Y/N |
| click on category radio button 4 | contributor list updates | Y/N |
| click on category radio button 5 | contributor list updates | Y/N |
| click on category radio button 1 | contributor list updates | Y/N |
| click on follow button for 1 contributor | button label changes to subscribed | Y/N |
| click on subscribed button for same contributor | button label changes back to subscribe | Y/N |
| while unsubscribed, click on the example link | opens subscribe popup | Y/N |
| close subscribe popup | popup closes | Y/N |
| click on the example link > subscribe from subscribe popup | button label changes to see example | Y/N |
| close popup after subscribe from popup | popup closes, subscribed to contributor | Y/N |
| click on see example button from popup | opens Chrome | Y/N |
| settings > apps > Dismoi > appear on top > Disable | Dismoi app opens on authorisations screens with Overlay toggle off and finish button greyed out | Y/N |
| click on overlay toggle > accept overlay setting | opens contributor selection page when back on Dismoi | Y/N |
| settings > accessibility > services > Dismoi > Disable | Dismoi app opens on authorisations screens with Accessibility toggle off and finish button greyed out | Y/N |
| click on accessibility toggle > accept accessibility setting | opens contributor selection page when back on Dismoi | Y/N |
| subscribe to alertoo and open example | Nespresso site opens in Chrome with Dismoi bubble on top | Y/N |
| Drag Dismoi bubble to the right if the screen and release | Dismoi bubble snaps to the right of the screen | Y/N |
| subscribe to captain fact and open example | Youtube site opens in Chrome with Dismoi bubble on top | Y/N |
| click on Dismoi bubble | opens contribution screen over Chrome | Y/N |
| click on link inside the captainfact contribution | opens captainfact site in new Chrome tab | Y/N |
| go back to previous tab, close contribution window (X) | contribution window closes, Dismoi Bubble notification on youtube is grey not red | Y/N |
| subscribe to Le Kaba, iboycott, amazon antidote, Ar memestra e breizh then open https://amazon.fr | Dismoi bubble appears OVER WEBSITE with 3 contributions | Y/N | 
click on Dismoi bubble | contribution screen appears over Chrome with 3 contributions | Y/N | 
swipe between contributions | contributions move from left to right and lock in the center | Y/N | 
| scroll inside contribution | scroll works | Y/N |
| click on bin | deleted contribution disappears, next contribution comes center | Y/N |
| visit https://www.boulanger.com/c/toutes-les-yaourtieres-fromageres, click on bubble, click on bin | contribution window closes, no Dismoi bubble over page | Y/N |
| open https://amazon.fr, drag Dismoi bubble into the close (X) area | Dismoi bubble disappears | Y/N |
| Reopen amazon.fr | Dismoi bubble does not appear | Y/N |
| go to recrute.leroymerlin.fr | Bubble should appear just one time | Y/N |
| Go to the link see an example of Colibri pour la planête type backmarket.fr in url press enter | Dismoi bubble should appear | Y/N |
| settings > apps > Dismoi > Force stop | app closes | Y/N |
| settings > apps > Dismoi > uninstall | app uninstalls | Y/N |
| settings > apps > Dismoi > uninstall | app uninstalls | Y/N |
