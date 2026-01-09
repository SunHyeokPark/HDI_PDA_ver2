# HDI PDA - Android WebView with Native Barcode Scanner

현대산업개발 ERP 시스템용 네이티브 바코드 스캐너가 통합된 Android WebView 앱

## 🎯 주요 기능

- **WebView 상태 보존**: 스캐너 사용 후에도 현재 페이지 유지
- **네이티브 바코드 스캐너**: ML Kit + CameraX 기반 고성능 스캐닝
- **HTTP 지원**: cleartext traffic 지원으로 HTTP 환경 접속 가능
- **JavaScript Bridge**: 웹 페이지에서 네이티브 기능 호출
- **자동 CI/CD**: GitHub Actions를 통한 자동 빌드

## 📋 기술 스택

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Barcode Library**: ML Kit Barcode Scanning (Google Play Services)
- **Camera**: CameraX 1.3.1
- **Build Tool**: Gradle 8.2 + Android Gradle Plugin 8.2.2

## 🏗️ 아키텍처

```
Web Page (ASP)
    ↓
window.Native.openScanner()
    ↓
MainActivity (WebView)
    ↓
ScannerActivity (CameraX + ML Kit)
    ↓
GUID Validation
    ↓
Result via ActivityResult API
    ↓
WebView.evaluateJavascript()
    ↓
scan_bar input filled
```

## 🚀 빌드 및 설치

### 1. 사전 요구사항

- JDK 17
- Android Studio Hedgehog (2023.1.1) 이상 **또는** Gradle 8.2+
- Android SDK 34

**중요**: Gradle Wrapper가 포함되지 않은 경우, 다음 중 하나를 실행하세요:

```bash
# Gradle이 설치된 경우
gradle wrapper --gradle-version 8.2

# 또는 Android Studio에서 프로젝트 열기
# → Gradle이 자동으로 wrapper 생성

# 또는 GitHub Actions에 push
# → CI/CD가 자동으로 wrapper 생성 및 빌드
```

### 2. Keystore 생성 (최초 1회)

```bash
keytool -genkey -v -keystore app/keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias hdi-pda

# 정보 입력
# Keystore password: [안전한 비밀번호]
# Key password: [안전한 비밀번호]
# 조직 정보 입력
```

### 3. GitHub Secrets 설정

```bash
# Keystore를 Base64로 인코딩
cat app/keystore.jks | base64 | tr -d '\n' > keystore_b64.txt

# GitHub Repository → Settings → Secrets → Actions에 추가:
# - KEYSTORE_B64: (keystore_b64.txt 내용)
# - KEYSTORE_PASSWORD: (keystore 비밀번호)
# - KEY_ALIAS: hdi-pda
# - KEY_PASSWORD: (key 비밀번호)
```

### 4. 로컬 빌드

```bash
# Debug 빌드
./gradlew assembleDebug

# Release 빌드 (서명 필요)
./gradlew assembleRelease

# 출력 위치
# Debug: app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release.apk
```

### 5. APK 설치

```bash
# USB 디버깅으로 연결된 기기에 설치
adb install -r app/build/outputs/apk/release/app-release.apk

# 기존 앱 제거 후 설치
adb uninstall com.hdi.pda
adb install app/build/outputs/apk/release/app-release.apk
```

## 📱 사용 방법

### 앱 사용

1. 앱 실행 → ERP 메인 페이지 로드
2. 입고스캔 페이지 이동
3. 카메라 버튼 클릭 → 네이티브 스캐너 실행
4. GUID 바코드 스캔
5. 자동으로 scan_bar에 값 입력 및 처리

### 웹 페이지 통합

기존 ASP 페이지 수정 (아래 섹션 참조)

## 🔧 웹 페이지 수정

### barcode_scan_camera_ver.asp 수정

기존 `openImageScanner()` 함수를 다음과 같이 수정:

```javascript
function openImageScanner() {
    // 네이티브 앱 체크
    if (window.Native && typeof window.Native.openScanner === 'function') {
        try {
            // 네이티브 스캐너 호출
            window.Native.openScanner();
            return;
        } catch (e) {
            console.error('Native scanner error:', e);
        }
    }
    
    // Fallback: 기존 웹 스캐너
    $('#image-scanner').addClass('active');
    resetScanner();
}
```

### 네이티브 앱 감지 (선택사항)

```javascript
// 앱 실행 여부 확인
if (window.Native && window.Native.isNativeApp()) {
    console.log('Native app detected');
    // 웹 스캐너 UI 숨기기 등
}
```

## 📋 테스트 체크리스트

### WebView 상태 보존
- [ ] 특정 페이지로 이동 → 스캔 → 같은 페이지 유지
- [ ] 입력 필드 값 유지
- [ ] 스크롤 위치 유지

### 바코드 스캐닝
- [ ] GUID 형식 바코드 정상 스캔
- [ ] 중괄호 포함/미포함 GUID 처리
- [ ] 잘못된 형식 거부
- [ ] scan_bar에 값 정확히 입력
- [ ] 기존 로직 (조회/저장) 동작

### 연속 스캔
- [ ] 5회 연속 스캔 (열기 → 스캔 → 닫기)
- [ ] 메모리 누수 없음
- [ ] 충돌 없음

### 권한 관리
- [ ] 초기 실행 시 카메라 권한 요청
- [ ] 권한 거부 후 재요청
- [ ] 설정에서 권한 변경 후 재시작

### 네트워크
- [ ] HTTP cleartext 접속 정상
- [ ] 오프라인 처리
- [ ] 온라인 복귀 후 정상 동작

## 🐛 문제 해결

### 1. Cleartext Traffic 오류

**증상**: `ERR_CLEARTEXT_NOT_PERMITTED`

**해결**:
- AndroidManifest.xml에 `usesCleartextTraffic="true"` 확인
- network_security_config.xml에 도메인 추가 확인

### 2. WebView 초기화 문제

**증상**: 스캔 후 첫 페이지로 돌아감

**해결**:
- MainActivity의 `launchMode="singleTask"` 확인
- `onActivityResult`에서 `loadUrl()` 호출하지 않는지 확인
- `saveState()`/`restoreState()` 동작 확인

### 3. JavaScript Interface 미동작

**증상**: `window.Native`가 `undefined`

**해결**:
- JavaScript 활성화 확인: `javaScriptEnabled = true`
- `@JavascriptInterface` 어노테이션 확인
- ProGuard 규칙 확인 (Release 빌드)

### 4. 카메라 권한 거부

**증상**: 스캐너 화면이 검은색

**해결**:
- 설정 → 앱 → HDI PDA → 권한 → 카메라 활성화
- 앱 재시작

### 5. ML Kit 모델 다운로드 실패

**증상**: 바코드 인식 안 됨

**해결**:
- Google Play Services 최신 버전 확인
- 인터넷 연결 확인
- 번들 모델로 전환 (app/build.gradle.kts 주석 참조)

## 📊 성능 최적화

- Google Play Services 모델 사용 → 앱 크기 ~2-3MB 절감
- R8 full mode 활성화 → 코드 최적화
- WebView 캐시 활성화
- 화면 회전 제한 → 불필요한 재생성 방지

## 🔐 보안 고려사항

- Network Security Config로 특정 도메인만 HTTP 허용
- WebView 데이터 백업 제외
- ProGuard로 코드 난독화
- Keystore 안전 보관 (GitHub Secrets)

## 📝 라이선스

Copyright (c) 2024 Hyundai Industrial Development Co., Ltd.

## 👥 문의

기술 지원: HDI 시스템 개발팀
