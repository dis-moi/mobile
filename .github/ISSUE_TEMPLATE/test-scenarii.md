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

| APP	| TYPE | TEST | EXPECTED OUTCOME | ACTUAL OUTCOME |
| ----------- | ----------- | ----------- | ----------- | ----------- |
 | `Dismoi` | Install | install App | app installs |  | 
 | `Dismoi` | Install | Click "open" when app is dowloaded | app opens on tutorial page 1 |  | 
 | `Dismoi` | Install | Click "done" when app is dowloaded | closes installation |  | 
 | `Dismoi` | Tutorial | open app from app icon | app opens on tutorial page 1 |  | 
 | `Dismoi` | Tutorial | on tuto page 1, click suivant | goes to tuto page 2 |  | 
 | `Dismoi` | Tutorial | on tuto page 1, click back (native button) | minimizes Dismoi app |  | 
 | `Dismoi` | Tutorial | on tuto page 2, click suivant | goes to tuto page 3 |  | 
 | `Dismoi` | Tutorial | on tuto page 3, click suivant | goes to tuto page 4 |  |
 | `Dismoi` | Tutorial | on tuto page 4, click suivant | goes to accessibility options, 2 x toggles are off, finish button inactive (grey) |  |
  | `Dismoi` | Overlay | click on overlay toggle | go to Dismoi overlay setting |  | 
 | `Settings` | Overlay settings | turn overlay ON > navigate back to Dismoi | overlay toggle is ON, finish button is still inactive (grey) |  | 
 | `Dismoi` | Accessibility | click on accessibility toggle | goes to Settings > Accessibility menu |  | 
 | `Settings` | Accessibility settings | go to services > Dismoi > turn accessibility ON > confirm > navigate back to Dismoi | accessibility toggle is ON, finish button is active (blue) |  | 
 | `Dismoi` | Contributors | click finish button | goes to contributor selection page |  | 
 | `Dismoi` | Contributors | click on category radio button 1 | contributor list updates |  | 
 | `Dismoi` | Contributors | click on category radio button 2 | contributor list updates |  | 
 | `Dismoi` | Contributors | click on category radio button 3 | contributor list updates |  | 
 | `Dismoi` | Contributors | click on category radio button 4 | contributor list updates |  | 
 | `Dismoi` | Contributors | click on category radio button 5 | contributor list updates |  | 
 | `Dismoi` | Contributors | click on follow button for 1 contributor | button label changes to subscribed |  | 
 | `Dismoi` | Contributors | click on subscribed button for same contributor | button label changes back to subscribe |  | 
 | `Dismoi` | Subscribe popup | while unsubscribed, click on the example link | opens subscribe popup |  | 
 | `Dismoi` | Subscribe popup | close subscribe popup | popup closes |  | 
 | `Dismoi` | Subscribe popup | subscribe from subscribe popup | button label changes to see example |  | 
 | `Dismoi` | Subscribe popup | close popup after subscribe from popup | popup closes, subscribed to contributor |  | 
 | `Dismoi` | Subscribe popup | click on see example button from popup | opens Chrome |  | 
 | `Settings` | App settings | settings > apps > Dismoi > Force stop | app closes |  | 
 | `Settings` | App settings | settings > apps > Dismoi > uninstall | app uninstalls |  | 
 | `Settings` | App settings | settings > apps > Dismoi > appear on top > Disable | Dismoi app opens on authorisations screens with Overlay disabled if you already did the onboarding |  | 
 | `Settings` | App settings | settings > accessibility > services > Dismoi > Disable | Dismoi app opens on authorisations screens with Accessibility disabled if you already did the onboarding |  | 
 | `Chrome` | Notice | subscribe to alertoo and open example | Nespresso site opens in Chrome with Dismoi bubble on top |  | 
 | `Chrome` | Notice | Drag Dismoi bubble to the right if the screen and release | Dismoi bubble snaps to the right of the screen |  | 
 | `Dismoi` | Contributors | subscribe to captain fact and open example | Youtube site opens in Chrome with Dismoi bubble on top |  | 
 | `Chrome` | Notice | click on Dismoi bubble | opens contribution screen over Chrome |  | 
 | `Chrome` | Contribution | click on link inside the captainfact contribution | opens captainfact site in new Chrome tab |  | 
 | `Chrome` | Contribution | go back to previous tab, close contribution window (X) | contribution window closes, Dismoi Bubble notification on youtube is grey not red |  | 
 | `both` | Notice | subscribe to Le Kaba, iboycott, amazon antidote, Ar memestra e breizh then open https://amazon.fr | Dismoi bubble appears OVER WEBSITE with 3 contributions |  | 
 | `Chrome` | Contribution | click on Dismoi bubble | contribution screen appears over Chrome with 3 contributions |  | 
 | `Chrome` | Contribution | swipe between contributions | contributions move from left to right and lock in the center |  | 
 | `Chrome` | Contribution | scroll inside contribution | scroll works |  | 
 | `Chrome` | Contribution | click on bin | deleted contribution disappears, next contribution comes center |  | 
 | `Chrome` | Contribution | visit https://www.boulanger.com/c/toutes-les-yaourtieres-fromageres, click on bubble, click on bin | contribution window closes, no Dismoi bubble over page |  | 
 | `Chrome` | Notice | open https://amazon.fr, drag Dismoi bubble into the close (X) area | Dismoi bubble disappears |  | 
 | `Chrome` | Notice | Reopen amazon.fr | Dismoi bubble does not appear |  | 
 | `Chrome` | Notice | Go to the link see an example of Colibri pour la planête type backmarket.fr in url press enter | Dismoi bubble should appear |  | 

