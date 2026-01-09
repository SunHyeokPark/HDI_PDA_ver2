# HDI PDA 빌드 및 배포 가이드

## 📦 빌드 명령어

### 로컬 개발 환경

```bash
# 1. 프로젝트 클론 (또는 파일 복사)
cd HDI-PDA

# 2. Gradle Wrapper 실행 권한 부여
chmod +x gradlew

# 3. Debug 빌드 (서명 불필요)
./gradlew assembleDebug

# 4. Release 빌드 (서명 필요)
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="hdi-pda"
export KEY_PASSWORD="your_key_password"
./gradlew assembleRelease

# 5. 빌드 결과 확인
ls -lh app/build/outputs/apk/debug/app-debug.apk
ls -lh app/build/outputs/apk/release/app-release.apk
```

### Android Studio

1. Android Studio에서 프로젝트 열기
2. Build → Build Bundle(s) / APK(s) → Build APK(s)
3. 빌드 완료 후 "locate" 클릭하여 APK 위치 확인

### CI/CD (GitHub Actions)

1. GitHub에 코드 푸시
2. Actions 탭에서 빌드 진행 상황 확인
3. 빌드 완료 후 Artifacts에서 APK 다운로드

## 🔑 Keystore 생성 및 관리

### 1. Keystore 생성

```bash
# 프로젝트 루트에서 실행
keytool -genkey -v -keystore app/keystore.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias hdi-pda

# 입력 사항:
# - Keystore password: [강력한 비밀번호]
# - Key password: [강력한 비밀번호, 또는 keystore와 동일]
# - 이름: Hyundai Industrial Development
# - 조직 단위: IT Development Team
# - 조직: HDI
# - 구/군/시: Seoul
# - 시/도: Seoul
# - 국가 코드: KR
```

### 2. Keystore 정보 저장

```bash
# keystore-info.txt 파일 생성 (안전한 곳에 보관)
cat > keystore-info.txt << EOF
Keystore File: app/keystore.jks
Keystore Password: [비밀번호]
Key Alias: hdi-pda
Key Password: [비밀번호]
Created: $(date)
Valid Until: $(date -d "+10000 days")
EOF

# 파일 권한 설정 (본인만 읽기)
chmod 600 keystore-info.txt
```

### 3. GitHub Secrets 설정

```bash
# Keystore를 Base64로 인코딩
base64 -w 0 app/keystore.jks > keystore_b64.txt

# 또는 Mac에서:
base64 app/keystore.jks | tr -d '\n' > keystore_b64.txt

# GitHub Repository → Settings → Secrets and variables → Actions
# 다음 Secrets 추가:
# - KEYSTORE_B64: (keystore_b64.txt 파일 내용 전체 복사)
# - KEYSTORE_PASSWORD: (keystore 비밀번호)
# - KEY_ALIAS: hdi-pda
# - KEY_PASSWORD: (key 비밀번호)
```

## 📱 APK 설치

### USB 디버깅을 통한 설치

```bash
# 1. 기기 연결 확인
adb devices

# 2. 기존 앱 제거 (선택사항)
adb uninstall com.hdi.pda

# 3. APK 설치
adb install -r app/build/outputs/apk/release/app-release.apk

# 4. 앱 실행
adb shell am start -n com.hdi.pda/.MainActivity
```

### 무선 설치 (ADB over WiFi)

```bash
# 1. USB로 기기 연결 후 WiFi 모드 활성화
adb tcpip 5555

# 2. 기기의 IP 주소 확인
adb shell ip addr show wlan0 | grep inet

# 3. WiFi로 연결
adb connect <기기_IP>:5555

# 4. APK 설치
adb install -r app/build/outputs/apk/release/app-release.apk
```

### 직접 설치

```bash
# 1. APK를 기기로 전송 (USB, 이메일, 클라우드 등)

# 2. 기기에서 "알 수 없는 출처" 허용
# 설정 → 보안 → 알 수 없는 출처 허용

# 3. 파일 관리자에서 APK 파일 클릭하여 설치
```

## 🚀 배포 프로세스

### 1. 버전 업데이트

```kotlin
// app/build.gradle.kts 파일 수정
defaultConfig {
    versionCode = 2        // 1씩 증가
    versionName = "1.0.1"  // 시맨틱 버저닝
}
```

### 2. 변경사항 커밋

```bash
git add .
git commit -m "Release v1.0.1: 스캔 성능 개선"
git tag v1.0.1
git push origin main --tags
```

### 3. GitHub Release 생성

```bash
# GitHub 웹에서:
# 1. Releases → Create a new release
# 2. Tag: v1.0.1 선택
# 3. Release title: HDI PDA v1.0.1
# 4. Description: 변경사항 작성
# 5. APK 파일 첨부
# 6. Publish release
```

### 4. 테스트 배포

```bash
# 테스트 기기에 설치
adb install -r app-release.apk

# 테스트 체크리스트 확인 (README.md 참조)
# - WebView 상태 보존
# - 바코드 스캔 정상 동작
# - 권한 관리
# - 네트워크 연결
```

## 🔍 빌드 검증

### APK 정보 확인

```bash
# APK 정보 출력
aapt dump badging app/build/outputs/apk/release/app-release.apk

# 버전 정보 확인
aapt dump badging app/build/outputs/apk/release/app-release.apk | grep version

# 권한 확인
aapt dump badging app/build/outputs/apk/release/app-release.apk | grep permission

# 최소/타겟 SDK 확인
aapt dump badging app/build/outputs/apk/release/app-release.apk | grep sdkVersion
```

### APK 서명 확인

```bash
# 서명 정보 확인
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# 상세 서명 정보
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk
```

### APK 크기 분석

```bash
# APK 분석
./gradlew assembleRelease
./gradlew :app:analyzeReleaseBundle

# Android Studio에서:
# Build → Analyze APK → app-release.apk 선택
```

## 🐛 빌드 오류 해결

### Gradle Sync 실패

```bash
# Gradle 캐시 정리
./gradlew clean
rm -rf ~/.gradle/caches/

# Gradle Wrapper 재다운로드
./gradlew wrapper --gradle-version 8.2
```

### 빌드 오류

```bash
# 1. 빌드 캐시 삭제
./gradlew clean

# 2. 빌드 디렉토리 삭제
rm -rf app/build

# 3. 재빌드
./gradlew assembleRelease --stacktrace --info
```

### Keystore 오류

```bash
# Keystore 파일 존재 확인
ls -l app/keystore.jks

# 환경변수 확인
echo $KEYSTORE_PASSWORD
echo $KEY_ALIAS
echo $KEY_PASSWORD

# Keystore 정보 확인
keytool -list -v -keystore app/keystore.jks
```

## 📊 빌드 성능 최적화

### Gradle 설정 최적화

```properties
# gradle.properties 파일
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=true
```

### 빌드 시간 측정

```bash
# 빌드 시간 프로파일링
./gradlew assembleRelease --profile

# 결과: build/reports/profile/
```

## 🔐 보안 체크리스트

- [ ] Keystore 파일을 안전한 곳에 백업
- [ ] Keystore 비밀번호를 별도 문서에 기록
- [ ] keystore.jks를 .gitignore에 추가
- [ ] GitHub Secrets에 keystore 정보 등록
- [ ] ProGuard 규칙 적용 확인
- [ ] 네트워크 보안 설정 확인

## 📝 체크리스트

배포 전 필수 확인 사항:

- [ ] 버전 코드/이름 업데이트
- [ ] 변경사항 문서화
- [ ] 빌드 성공 확인
- [ ] APK 서명 확인
- [ ] 테스트 기기에서 설치 확인
- [ ] 주요 기능 동작 확인
- [ ] 권한 요청 정상 동작
- [ ] 네트워크 연결 확인
- [ ] 바코드 스캔 정상 동작
- [ ] WebView 상태 보존 확인
- [ ] Release 노트 작성
