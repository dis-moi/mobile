# Architecture

## Page Content Analysis

Origin objective was to use accessibility service to extract current DOM from Chrome, to be able to apply XPath rule on live content.

After a lot of trials and reading, we failed to meet this main goal.

What we managed to do is to deep analyze the DOM, but exposed as a tree of AccessibilityNodeInfo. Tag names and classes are not exposed (every tag is exposed as an Android component, View and so on …), but we seem to have the full hierarchy with ids and text contents.

As an example, whenever a product page is loaded in amazon (or when app becomes visible), irrespective of the URL, we can find the product title :

```kotlin
val webview = findWebview(root)
if (webview != null) {
  val titleExpanderContent = findById(webview, "titleExpanderContent")
  if (titleExpanderContent != null) {
    val titleView = findHeading(titleExpanderContent)
    val title = titleView?.text ?: titleView?.contentDescription
    Log.d(TAG, "Found Amazon page title : $title")
  }
}

```

gives

```shell
D/Accessibility: Event : TYPE_WINDOW_CONTENT_CHANGED, Package: com.google.android.apps.nexuslauncher, Source: null
D/Accessibility: Active window packageName : com.android.chrome, className: android.widget.FrameLayout
D/Accessibility: Found Amazon page title : 6S Casque Bluetooth sans Fil, écouteurs stéréo sans Fil stéréo Pliables Hi-FI Écouteurs avec Microphone intégré, Micro SD/TF, FM pour iPhone/Samsung/iPad/PC (Or Noir)
```

### How the application worked before 

Simplified:

- We use accessibility services, Kotlin side, to detect URL, and we send all URLs to React Native HeadlessTask
- In RN HeadlessTask we first fetch all matching contexts, and if current url matches and we have an xpath condition, we send it back to a Kotlin XPath module
- In the Kotlin XPath module, we fetch current url and try on apply xpath on fetched html, and then we send the response back to RN HeadlessTask <- Here we don’t have access to accessiblity informations anymore
- In RN HeadlessTask, we have some last checks (notice hasn’t been deleted, …) and then we call a Kotlin UI service that manages all the floating UI, to display found notices

### Target flow

The main work I did here (in 8ac3716 in particular) was to be able to analyze page content given a list of matching contexts to check.

The thing is, because current content analysis (XPath) is done later and only given an URL, I could not change the strategy in a drop-in manner …

I had to bring back most of the context matching process together, in the accessibility service, so that we can push screen content analysis further when needed

So, now, the BackgroundService (that deserves a better name) does the following :

- Fetch all matching contexts (only once, will be happen regularly)
- Every time screen change (well, with a 500 ms debounce), detect application, and if supported, current URL
- Find all corresponding matching contexts
- <-- If any has content condition, we’re ready to analyze here
- Then we send all Ids to the Floating UI service I adapted for this

I do not use RN Headless task anymore, but it’s still there because I did not port all the functionality yet. Same for some other files, as FloatingModule, FloatingCoordinator, XPathModule …


Used documentation and references :
- https://stuff.mit.edu/afs/sipb/project/android/docs/guide/topics/ui/accessibility/services.html
- https://medium.com/nerd-for-tech/track-web-browser-usage-in-android-using-accessibility-service-800bfa2745d2
- https://stackoverflow.com/questions/33318083/how-to-get-webview-from-accessibilitynodeinfo
- https://groups.google.com/a/chromium.org/g/chromium-dev/c/2VC16XswAaI
- https://stackoverflow.com/questions/7282789/is-there-any-way-to-get-access-to-dom-structure-in-androids-webview
- https://stackoverflow.com/questions/40522043/how-to-access-html-content-of-accessibilitynodeinfo-of-a-webview-element-using-a
- https://stackoverflow.com/questions/65326148/why-accessibilityservice-failed-to-retrieve-content-of-a-webview-but-works-prop
- https://github.com/google/talkback
- https://github.com/chromium/chromium/blob/master/content/public/android/java/src/org/chromium/content/browser/accessibility/WebContentsAccessibilityImpl.java
- https://www.py4u.net/discuss/630533
- https://stackoverflow.com/questions/10634908/accessibility-and-android-webview
- https://stackoverflow.com/questions/36793154/accessibilityservice-not-returning-view-ids

