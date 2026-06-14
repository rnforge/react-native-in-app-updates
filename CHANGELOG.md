## [1.0.1](https://github.com/rnforge/react-native-in-app-updates/compare/v1.0.0...v1.0.1) (2026-06-14)

### 🐛 Bug Fixes

* refresh published package documentation ([aa47817](https://github.com/rnforge/react-native-in-app-updates/commit/aa47817dd8fb45a0247a202cfe97617fa9887a8c))

### 📚 Documentation

* add TSDoc to all public exports ([6c94b3f](https://github.com/rnforge/react-native-in-app-updates/commit/6c94b3f2766b45e666fe0baeffd849387bc356b6))
* harden public api documentation ([44cbd1c](https://github.com/rnforge/react-native-in-app-updates/commit/44cbd1c8cd062a2e4e89887ab099be23f47e93e8))
* streamline package readme ([61686f6](https://github.com/rnforge/react-native-in-app-updates/commit/61686f673490dfdd9de9ade73635a6b911076825))

### 🛠️ Other changes

* add API Extractor for public API drift detection ([927893c](https://github.com/rnforge/react-native-in-app-updates/commit/927893c13ec648f66366770eaf63b5feb7d2e253))

## 1.0.0 (2026-06-07)

### ✨ Features

* add android asset pack deletion option ([db9f8dc](https://github.com/rnforge/react-native-in-app-updates/commit/db9f8dc5d27b80584e778bda13e88d8acb2601b4))
* add flexible update flow and listener ([c70c782](https://github.com/rnforge/react-native-in-app-updates/commit/c70c7828734a35036f660025120184baae4eb3bc))
* add immediate update flow ([8c04281](https://github.com/rnforge/react-native-in-app-updates/commit/8c04281384270111d51a20e4e4abd84411937a8f))
* add ios app store lookup ([b302799](https://github.com/rnforge/react-native-in-app-updates/commit/b302799a3caf94d09d5f94a6da635c9c8e46855b))
* add runnable example app and android test harness ([ee60f3b](https://github.com/rnforge/react-native-in-app-updates/commit/ee60f3b0c2d0a5915bd3eedc052bbe64bef48af4))
* add store page helper ([721d040](https://github.com/rnforge/react-native-in-app-updates/commit/721d040195ef2e4c2a756a13fb96aa3ca1efea37))
* add update status helper predicates ([3f95e9a](https://github.com/rnforge/react-native-in-app-updates/commit/3f95e9a618f5e58e8d33ac7d3312372a31cee0b7))
* add update status snapshot ([b162a72](https://github.com/rnforge/react-native-in-app-updates/commit/b162a72035325afa6aa826586a3c169c8e3be2c3))

### 🐛 Bug Fixes

* align release artifact and store capabilities ([58005a5](https://github.com/rnforge/react-native-in-app-updates/commit/58005a5035bf7b7b31d28017e9a814b890755b25))
* **android:** align namespace with dev rnforge ([9d35568](https://github.com/rnforge/react-native-in-app-updates/commit/9d35568b090e5ebf9a4801b960bda4167ca8f9a2))
* attach Android diagnostics to update flow results ([e7aa3f1](https://github.com/rnforge/react-native-in-app-updates/commit/e7aa3f19342a6c2cdf517fa3dcf0e466e1785c56))
* correct native integration crash blockers ([e8f07f1](https://github.com/rnforge/react-native-in-app-updates/commit/e8f07f1c684d5a466d30718366e0c91fc6719f1f))
* emit Android listener unavailable event ([8a51c3d](https://github.com/rnforge/react-native-in-app-updates/commit/8a51c3de75fbeb5d61008082cfe68eb797967372))
* handle canceled Android update flows ([687eebd](https://github.com/rnforge/react-native-in-app-updates/commit/687eebdad96015d6005672fdacd0848a9d025a81))
* preserve Android store fallback for unsupported states ([da4c944](https://github.com/rnforge/react-native-in-app-updates/commit/da4c94480212e7e4f2a9180426d112e821a945da))
* stabilize workflow typecheck cache key ([a7e6bfa](https://github.com/rnforge/react-native-in-app-updates/commit/a7e6bfa588a5ead6059fd8a9239e04fe0300f125))
* surface play core diagnostics ([58db991](https://github.com/rnforge/react-native-in-app-updates/commit/58db991ca746ebd5cba30b0c710f82dcb4826cfd))

### 🔄 Code Refactors

* **api:** remove raw Nitro hybrid object from public exports ([db44554](https://github.com/rnforge/react-native-in-app-updates/commit/db44554d86f0be73190afdbaac62ef8ad63353df))
* harden android play core services ([633f0ca](https://github.com/rnforge/react-native-in-app-updates/commit/633f0ca44846a1954a0332afe19538629514b64b))
* migrate Nitro internal namespace to segmented shape ([17596e3](https://github.com/rnforge/react-native-in-app-updates/commit/17596e30e2278352f10bb48679824dd0376d4e01))
* **native:** organize internal platform structure ([6153b2b](https://github.com/rnforge/react-native-in-app-updates/commit/6153b2b72530ecd693891eac226731a6bbba01f5))
* share native status mapping ([813629f](https://github.com/rnforge/react-native-in-app-updates/commit/813629f728c07a412bb113b07c7118e23e90a601))

### 📚 Documentation

* add manual play validation checklist and link from TESTING.md ([8934c34](https://github.com/rnforge/react-native-in-app-updates/commit/8934c349034d0ae345e960c4b2bfe75e14ea16ba))
* align README with current platform behavior ([9fc6620](https://github.com/rnforge/react-native-in-app-updates/commit/9fc662074777fe86364eb90bcb9ae66252656140))
* clarify capability semantics ([e7f6000](https://github.com/rnforge/react-native-in-app-updates/commit/e7f6000abc194b8fcae4e4ec75d096eb95139375))
* document canceled Android update flows ([35bd155](https://github.com/rnforge/react-native-in-app-updates/commit/35bd15538670b088b9f72596f6c424c4e1c7b497))
* fix broken example path in manual-play-validation and README ([b982e2e](https://github.com/rnforge/react-native-in-app-updates/commit/b982e2e643dfb3666f83df052aa716e5c8e465e7))
* fix example app README reference ([5d28f63](https://github.com/rnforge/react-native-in-app-updates/commit/5d28f6300ff81d32c2ce727294925cf29d7988ad))
* improve README DX for v1 ([b565711](https://github.com/rnforge/react-native-in-app-updates/commit/b5657111b290408d1185d811beeb9de34e20d248))
* refresh prerelease validation expectations ([e43bc06](https://github.com/rnforge/react-native-in-app-updates/commit/e43bc06fdbeebc01ec13584da2e648aa40a3e0c4))
* require native builds for Nitro changes ([2271a5c](https://github.com/rnforge/react-native-in-app-updates/commit/2271a5c530bf725c1291cabbdcc5495449510b09))
* **testing:** document final native source layout ([ec3defd](https://github.com/rnforge/react-native-in-app-updates/commit/ec3defd7169132e8496a3c6adb1e6bf61b6017ba))

### 🛠️ Other changes

* add android expected-state reasons ([0f754df](https://github.com/rnforge/react-native-in-app-updates/commit/0f754dfe1332f6b26f9626ea87ae8384d93e7800))
* add android play diagnostic labels ([3a320f5](https://github.com/rnforge/react-native-in-app-updates/commit/3a320f5a331d45fc3dbd4861dd4724f64546a33a))
* add android testability seams ([b155c35](https://github.com/rnforge/react-native-in-app-updates/commit/b155c359d018fd16b063137fb344654748b154be))
* audit package.json files for release artifact accuracy ([88aa727](https://github.com/rnforge/react-native-in-app-updates/commit/88aa727d2b20286f6dee4140cebbd0d6a6e674f6))
* **deps-dev:** bump react from 19.2.3 to 19.2.6 and @types/react from 19.2.0 to 19.2.14 ([e19a192](https://github.com/rnforge/react-native-in-app-updates/commit/e19a1925075286233f99776be3b3c01aaaa6a226))
* **deps:** bump actions/cache from 4 to 5 ([bb65f19](https://github.com/rnforge/react-native-in-app-updates/commit/bb65f1981961d67f6e93d725f73b1de82534662d))
* **deps:** bump actions/checkout from 4 to 6 ([e051a84](https://github.com/rnforge/react-native-in-app-updates/commit/e051a84185e083397dd7c129812133b79c252e53))
* **deps:** bump androidx.test:core from 1.6.1 to 1.7.0 in /android ([8ab22f3](https://github.com/rnforge/react-native-in-app-updates/commit/8ab22f31622fc144320c8da2bd597c01174ea045))
* **deps:** bump com.google.android.gms:play-services-base in /android ([#10](https://github.com/rnforge/react-native-in-app-updates/issues/10)) ([8145684](https://github.com/rnforge/react-native-in-app-updates/commit/81456847985dfd9a3c524bf550fb588096239157))
* **deps:** bump robolectric from 4.14.1 to 4.16.1 in /android ([c00197b](https://github.com/rnforge/react-native-in-app-updates/commit/c00197b142bdf9886b36a51cc79afa7835864c14))
* **deps:** update concurrent-ruby requirement from < 1.3.4 to < 1.3.7 in /example ([42e21ac](https://github.com/rnforge/react-native-in-app-updates/commit/42e21acad9cfd1aca1d0d9a3b735b625002bb5e5))
* **deps:** update xcodeproj requirement from < 1.26.0 to < 1.28.0 in /example ([71f8861](https://github.com/rnforge/react-native-in-app-updates/commit/71f8861bbcf5ab31f6203d206530ad83800dd8d4))
* enable reserved android unsupported reasons ([335835b](https://github.com/rnforge/react-native-in-app-updates/commit/335835b9323ba074471dd88ef6cab42e63dc2ae7))
* finish ios lookup and activity result review ([2cc7b8c](https://github.com/rnforge/react-native-in-app-updates/commit/2cc7b8c489b59cecc3f8731c6dc548b680476451))
* harden release package metadata ([472df38](https://github.com/rnforge/react-native-in-app-updates/commit/472df38a5b685f8586d506a4b059ea6afa2e11a2))
* populate installed version for android unsupported states ([5ea13e1](https://github.com/rnforge/react-native-in-app-updates/commit/5ea13e162699f7dcc7b80c8ac7b9fa0a99b4f509))
* populate installed version for ios fallback states ([148b5ed](https://github.com/rnforge/react-native-in-app-updates/commit/148b5edef81c00a9695d266d5c272ecbb2382e62))
* populate update version fields ([73df6e5](https://github.com/rnforge/react-native-in-app-updates/commit/73df6e5b5fcd39792b7769e6217df440af61f09a))
* post-review test and doc cleanup ([8a59e23](https://github.com/rnforge/react-native-in-app-updates/commit/8a59e23fcf15162a62a3d9e047dee4f21d27161d))
* **release:** 1.0.0-next.1 [skip ci] ([7e7803f](https://github.com/rnforge/react-native-in-app-updates/commit/7e7803f714a3fe4864704bc4595127b6e10c7ef9))
* **release:** 1.0.0-next.2 [skip ci] ([6f66bf7](https://github.com/rnforge/react-native-in-app-updates/commit/6f66bf7a88dbe97b53561cda5147f7b7a2b2a6fe))
* **release:** 1.0.0-next.3 [skip ci] ([a494ba7](https://github.com/rnforge/react-native-in-app-updates/commit/a494ba7457127118ccbe7a90a67725a015705227))
* **release:** 1.0.0-next.4 [skip ci] ([85faca4](https://github.com/rnforge/react-native-in-app-updates/commit/85faca45692f34e781b3eec2518bbc0a62e51210))
* **release:** 1.0.0-next.5 [skip ci] ([af07bd6](https://github.com/rnforge/react-native-in-app-updates/commit/af07bd67616bf0fa4d16e8b99fad689afa2e1384))
* **release:** 1.0.0-next.6 [skip ci] ([9b50646](https://github.com/rnforge/react-native-in-app-updates/commit/9b50646aec0fc4e57d1d6328f4f6aeefaf94a0f0))
* **release:** 1.0.0-next.7 [skip ci] ([4fa5778](https://github.com/rnforge/react-native-in-app-updates/commit/4fa577853949515a89df5c986f31e7c35e5cb31d))
* sync package version metadata ([682c9f4](https://github.com/rnforge/react-native-in-app-updates/commit/682c9f48acfdab7c66685df74ca9d4c621d65986))
* update lockfile after reverting typescript 6.0.3 ([0924f30](https://github.com/rnforge/react-native-in-app-updates/commit/0924f30177c31bf0308af7215ac1de9691e7eede))

## [1.0.0-next.7](https://github.com/rnforge/react-native-in-app-updates/compare/v1.0.0-next.6...v1.0.0-next.7) (2026-06-07)

### 🐛 Bug Fixes

* attach Android diagnostics to update flow results ([e7aa3f1](https://github.com/rnforge/react-native-in-app-updates/commit/e7aa3f19342a6c2cdf517fa3dcf0e466e1785c56))
* emit Android listener unavailable event ([8a51c3d](https://github.com/rnforge/react-native-in-app-updates/commit/8a51c3de75fbeb5d61008082cfe68eb797967372))
* handle canceled Android update flows ([687eebd](https://github.com/rnforge/react-native-in-app-updates/commit/687eebdad96015d6005672fdacd0848a9d025a81))
* preserve Android store fallback for unsupported states ([da4c944](https://github.com/rnforge/react-native-in-app-updates/commit/da4c94480212e7e4f2a9180426d112e821a945da))

### 📚 Documentation

* document canceled Android update flows ([35bd155](https://github.com/rnforge/react-native-in-app-updates/commit/35bd15538670b088b9f72596f6c424c4e1c7b497))

### 🛠️ Other changes

* harden release package metadata ([472df38](https://github.com/rnforge/react-native-in-app-updates/commit/472df38a5b685f8586d506a4b059ea6afa2e11a2))
* post-review test and doc cleanup ([8a59e23](https://github.com/rnforge/react-native-in-app-updates/commit/8a59e23fcf15162a62a3d9e047dee4f21d27161d))
* sync package version metadata ([682c9f4](https://github.com/rnforge/react-native-in-app-updates/commit/682c9f48acfdab7c66685df74ca9d4c621d65986))

## [1.0.0-next.6](https://github.com/rnforge/react-native-in-app-updates/compare/v1.0.0-next.5...v1.0.0-next.6) (2026-05-19)

### 📚 Documentation

* refresh prerelease validation expectations ([e43bc06](https://github.com/rnforge/react-native-in-app-updates/commit/e43bc06fdbeebc01ec13584da2e648aa40a3e0c4))

### 🛠️ Other changes

* populate installed version for ios fallback states ([148b5ed](https://github.com/rnforge/react-native-in-app-updates/commit/148b5edef81c00a9695d266d5c272ecbb2382e62))

## [1.0.0-next.5](https://github.com/rnforge/react-native-in-app-updates/compare/v1.0.0-next.4...v1.0.0-next.5) (2026-05-19)

### 🛠️ Other changes

* populate installed version for android unsupported states ([5ea13e1](https://github.com/rnforge/react-native-in-app-updates/commit/5ea13e162699f7dcc7b80c8ac7b9fa0a99b4f509))

## [1.0.0-next.4](https://github.com/rnforge/react-native-in-app-updates/compare/v1.0.0-next.3...v1.0.0-next.4) (2026-05-19)

### 📚 Documentation

* clarify capability semantics ([e7f6000](https://github.com/rnforge/react-native-in-app-updates/commit/e7f6000abc194b8fcae4e4ec75d096eb95139375))

### 🛠️ Other changes

* add android expected-state reasons ([0f754df](https://github.com/rnforge/react-native-in-app-updates/commit/0f754dfe1332f6b26f9626ea87ae8384d93e7800))
* add android play diagnostic labels ([3a320f5](https://github.com/rnforge/react-native-in-app-updates/commit/3a320f5a331d45fc3dbd4861dd4724f64546a33a))
* enable reserved android unsupported reasons ([335835b](https://github.com/rnforge/react-native-in-app-updates/commit/335835b9323ba074471dd88ef6cab42e63dc2ae7))
* finish ios lookup and activity result review ([2cc7b8c](https://github.com/rnforge/react-native-in-app-updates/commit/2cc7b8c489b59cecc3f8731c6dc548b680476451))
* populate update version fields ([73df6e5](https://github.com/rnforge/react-native-in-app-updates/commit/73df6e5b5fcd39792b7769e6217df440af61f09a))

## [1.0.0-next.3](https://github.com/rnforge/react-native-in-app-updates/compare/v1.0.0-next.2...v1.0.0-next.3) (2026-05-16)

### 🐛 Bug Fixes

* correct native integration crash blockers ([e8f07f1](https://github.com/rnforge/react-native-in-app-updates/commit/e8f07f1c684d5a466d30718366e0c91fc6719f1f))

### 📚 Documentation

* require native builds for Nitro changes ([2271a5c](https://github.com/rnforge/react-native-in-app-updates/commit/2271a5c530bf725c1291cabbdcc5495449510b09))

## [1.0.0-next.2](https://github.com/rnforge/react-native-in-app-updates/compare/v1.0.0-next.1...v1.0.0-next.2) (2026-05-13)

### 📚 Documentation

* align README with current platform behavior ([9fc6620](https://github.com/rnforge/react-native-in-app-updates/commit/9fc662074777fe86364eb90bcb9ae66252656140))

## 1.0.0-next.1 (2026-05-13)

### ✨ Features

* add android asset pack deletion option ([db9f8dc](https://github.com/rnforge/react-native-in-app-updates/commit/db9f8dc5d27b80584e778bda13e88d8acb2601b4))
* add flexible update flow and listener ([c70c782](https://github.com/rnforge/react-native-in-app-updates/commit/c70c7828734a35036f660025120184baae4eb3bc))
* add immediate update flow ([8c04281](https://github.com/rnforge/react-native-in-app-updates/commit/8c04281384270111d51a20e4e4abd84411937a8f))
* add ios app store lookup ([b302799](https://github.com/rnforge/react-native-in-app-updates/commit/b302799a3caf94d09d5f94a6da635c9c8e46855b))
* add runnable example app and android test harness ([ee60f3b](https://github.com/rnforge/react-native-in-app-updates/commit/ee60f3b0c2d0a5915bd3eedc052bbe64bef48af4))
* add store page helper ([721d040](https://github.com/rnforge/react-native-in-app-updates/commit/721d040195ef2e4c2a756a13fb96aa3ca1efea37))
* add update status helper predicates ([3f95e9a](https://github.com/rnforge/react-native-in-app-updates/commit/3f95e9a618f5e58e8d33ac7d3312372a31cee0b7))
* add update status snapshot ([b162a72](https://github.com/rnforge/react-native-in-app-updates/commit/b162a72035325afa6aa826586a3c169c8e3be2c3))

### 🐛 Bug Fixes

* align release artifact and store capabilities ([58005a5](https://github.com/rnforge/react-native-in-app-updates/commit/58005a5035bf7b7b31d28017e9a814b890755b25))
* **android:** align namespace with dev rnforge ([9d35568](https://github.com/rnforge/react-native-in-app-updates/commit/9d35568b090e5ebf9a4801b960bda4167ca8f9a2))
* stabilize workflow typecheck cache key ([a7e6bfa](https://github.com/rnforge/react-native-in-app-updates/commit/a7e6bfa588a5ead6059fd8a9239e04fe0300f125))
* surface play core diagnostics ([58db991](https://github.com/rnforge/react-native-in-app-updates/commit/58db991ca746ebd5cba30b0c710f82dcb4826cfd))

### 🔄 Code Refactors

* **api:** remove raw Nitro hybrid object from public exports ([db44554](https://github.com/rnforge/react-native-in-app-updates/commit/db44554d86f0be73190afdbaac62ef8ad63353df))
* harden android play core services ([633f0ca](https://github.com/rnforge/react-native-in-app-updates/commit/633f0ca44846a1954a0332afe19538629514b64b))
* migrate Nitro internal namespace to segmented shape ([17596e3](https://github.com/rnforge/react-native-in-app-updates/commit/17596e30e2278352f10bb48679824dd0376d4e01))
* **native:** organize internal platform structure ([6153b2b](https://github.com/rnforge/react-native-in-app-updates/commit/6153b2b72530ecd693891eac226731a6bbba01f5))
* share native status mapping ([813629f](https://github.com/rnforge/react-native-in-app-updates/commit/813629f728c07a412bb113b07c7118e23e90a601))

### 📚 Documentation

* add manual play validation checklist and link from TESTING.md ([8934c34](https://github.com/rnforge/react-native-in-app-updates/commit/8934c349034d0ae345e960c4b2bfe75e14ea16ba))
* fix broken example path in manual-play-validation and README ([b982e2e](https://github.com/rnforge/react-native-in-app-updates/commit/b982e2e643dfb3666f83df052aa716e5c8e465e7))
* fix example app README reference ([5d28f63](https://github.com/rnforge/react-native-in-app-updates/commit/5d28f6300ff81d32c2ce727294925cf29d7988ad))
* improve README DX for v1 ([b565711](https://github.com/rnforge/react-native-in-app-updates/commit/b5657111b290408d1185d811beeb9de34e20d248))
* **testing:** document final native source layout ([ec3defd](https://github.com/rnforge/react-native-in-app-updates/commit/ec3defd7169132e8496a3c6adb1e6bf61b6017ba))

### 🛠️ Other changes

* add android testability seams ([b155c35](https://github.com/rnforge/react-native-in-app-updates/commit/b155c359d018fd16b063137fb344654748b154be))
* audit package.json files for release artifact accuracy ([88aa727](https://github.com/rnforge/react-native-in-app-updates/commit/88aa727d2b20286f6dee4140cebbd0d6a6e674f6))
* **deps-dev:** bump react from 19.2.3 to 19.2.6 and @types/react from 19.2.0 to 19.2.14 ([e19a192](https://github.com/rnforge/react-native-in-app-updates/commit/e19a1925075286233f99776be3b3c01aaaa6a226))
* **deps:** bump actions/cache from 4 to 5 ([bb65f19](https://github.com/rnforge/react-native-in-app-updates/commit/bb65f1981961d67f6e93d725f73b1de82534662d))
* **deps:** bump actions/checkout from 4 to 6 ([e051a84](https://github.com/rnforge/react-native-in-app-updates/commit/e051a84185e083397dd7c129812133b79c252e53))
* **deps:** bump androidx.test:core from 1.6.1 to 1.7.0 in /android ([8ab22f3](https://github.com/rnforge/react-native-in-app-updates/commit/8ab22f31622fc144320c8da2bd597c01174ea045))
* **deps:** bump com.google.android.gms:play-services-base in /android ([#10](https://github.com/rnforge/react-native-in-app-updates/issues/10)) ([8145684](https://github.com/rnforge/react-native-in-app-updates/commit/81456847985dfd9a3c524bf550fb588096239157))
* **deps:** bump robolectric from 4.14.1 to 4.16.1 in /android ([c00197b](https://github.com/rnforge/react-native-in-app-updates/commit/c00197b142bdf9886b36a51cc79afa7835864c14))
* **deps:** update concurrent-ruby requirement from < 1.3.4 to < 1.3.7 in /example ([42e21ac](https://github.com/rnforge/react-native-in-app-updates/commit/42e21acad9cfd1aca1d0d9a3b735b625002bb5e5))
* **deps:** update xcodeproj requirement from < 1.26.0 to < 1.28.0 in /example ([71f8861](https://github.com/rnforge/react-native-in-app-updates/commit/71f8861bbcf5ab31f6203d206530ad83800dd8d4))
* update lockfile after reverting typescript 6.0.3 ([0924f30](https://github.com/rnforge/react-native-in-app-updates/commit/0924f30177c31bf0308af7215ac1de9691e7eede))
