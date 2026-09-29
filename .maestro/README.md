# Maestro UI flows

End-to-end smoke tests for the **debug** build (`com.thesunnahrevival.sunnahassistant.debug`).
They were written to verify R8 keep rules, and double as a pre-release check.

```sh
./gradlew :app:installProductionDebug
maestro test .maestro/                          # all flows, in file order
maestro test .maestro/ --exclude-tags network   # offline-safe subset
```

`01_first_launch.yaml` clears app data, so run the folder in order.
Flows tagged `network` need internet access.

Not covered (do manually): reminder notifications firing / snooze / share,
backup & restore (system file picker), home-screen widgets.
