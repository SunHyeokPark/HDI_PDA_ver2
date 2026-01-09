# GitHub 업로드 및 Actions 배포 가이드

## 📦 준비된 파일

이 zip 파일에는 GitHub에 바로 업로드할 수 있는 완전한 Android 프로젝트가 포함되어 있습니다.

## 🚀 GitHub에 업로드하기 (5분)

### 1단계: GitHub 리포지토리 생성

1. GitHub 웹사이트 접속 (https://github.com)
2. 우측 상단 `+` → `New repository` 클릭
3. 리포지토리 정보 입력:
   - **Repository name**: `HDI-PDA`
   - **Description**: HDI ERP 모바일 바코드 스캐너 앱
   - **Public** 또는 **Private** 선택
   - ⚠️ **Initialize this repository with:** 모두 체크 해제
4. `Create repository` 클릭

### 2단계: 로컬에 파일 준비

```bash
# zip 파일 압축 해제
unzip HDI-PDA.zip
cd HDI-PDA

# Git 초기화
git init
git add .
git commit -m "Initial commit: HDI PDA v1.0.0"
```

### 3단계: GitHub에 푸시

```bash
# 원격 리포지토리 연결 (URL은 GitHub에서 복사)
git remote add origin https://github.com/YOUR_USERNAME/HDI-PDA.git

# 메인 브랜치로 푸시
git branch -M main
git push -u origin main
```

## 🔑 GitHub Secrets 설정 (필수)

Release APK를 빌드하려면 Keystore 정보를 GitHub Secrets에 추가해야 합니다.

### 1단계: Keystore 생성

```bash
# 프로젝트 루트에서 실행
keytool -genkey -v -keystore keystore.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias hdi-pda

# 입력 정보:
# - Keystore password: [강력한 비밀번호 - 기억할 것!]
# - Key password: [강력한 비밀번호 - 기억할 것!]
# - 이름: Hyundai Industrial Development
# - 조직: HDI
# - 위치: Seoul
# - 국가 코드: KR
```

### 2단계: Keystore를 Base64로 인코딩

```bash
# Linux/Mac
base64 keystore.jks | tr -d '\n' > keystore_b64.txt

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore.jks")) | Out-File keystore_b64.txt
```

### 3단계: GitHub Secrets 추가

1. GitHub 리포지토리 페이지에서 `Settings` 탭 클릭
2. 왼쪽 메뉴에서 `Secrets and variables` → `Actions` 클릭
3. `New repository secret` 버튼 클릭
4. 다음 4개 Secret 추가:

#### Secret 1: KEYSTORE_B64
- **Name**: `KEYSTORE_B64`
- **Value**: `keystore_b64.txt` 파일의 내용 전체 복사/붙여넣기

#### Secret 2: KEYSTORE_PASSWORD
- **Name**: `KEYSTORE_PASSWORD`
- **Value**: keystore 생성 시 입력한 비밀번호

#### Secret 3: KEY_ALIAS
- **Name**: `KEY_ALIAS`
- **Value**: `hdi-pda`

#### Secret 4: KEY_PASSWORD
- **Name**: `KEY_PASSWORD`
- **Value**: key 생성 시 입력한 비밀번호

## ✅ GitHub Actions 자동 빌드 확인

### 빌드 트리거 조건

다음 상황에서 자동으로 빌드됩니다:

1. **main 브랜치에 push**: Release APK 생성
2. **develop 브랜치에 push**: Debug APK 생성
3. **Pull Request 생성**: Debug APK 생성
4. **수동 실행**: Actions 탭에서 "Run workflow"

### 빌드 진행 상황 확인

1. GitHub 리포지토리에서 `Actions` 탭 클릭
2. 최근 workflow 실행 목록 확인
3. 실행 중인 workflow 클릭하여 상세 로그 확인
4. 빌드 완료 후 `Artifacts` 섹션에서 APK 다운로드

### 첫 번째 빌드 실행

```bash
# Secrets 설정 후 코드 수정 (버전 테스트)
# app/build.gradle.kts 파일에서 versionName 수정

git add .
git commit -m "Test: Trigger first build"
git push origin main
```

## 📱 APK 다운로드 및 설치

### 1. GitHub Actions에서 다운로드

1. Actions 탭 → 성공한 workflow 클릭
2. 페이지 하단 `Artifacts` 섹션
3. `app-release` 클릭하여 다운로드
4. zip 압축 해제 → `app-release.apk` 파일 확인

### 2. 기기에 설치

```bash
# USB 디버깅으로 설치
adb install -r app-release.apk

# 또는 APK를 기기로 전송하여 직접 설치
```

## 🔄 코드 업데이트 및 재배포

### 버전 업데이트

```kotlin
// app/build.gradle.kts 파일 수정
defaultConfig {
    versionCode = 2        // 1씩 증가
    versionName = "1.0.1"  // 변경 내용 반영
}
```

### Git 커밋 및 푸시

```bash
git add .
git commit -m "Release v1.0.1: 스캔 성능 개선"
git tag v1.0.1
git push origin main --tags
```

### Actions에서 자동 빌드

- push 즉시 GitHub Actions가 자동으로 빌드 시작
- 5-10분 후 새로운 APK 다운로드 가능

## 🏷️ Release 생성 (권장)

### 수동으로 Release 생성

1. GitHub 리포지토리 → `Releases` 탭
2. `Create a new release` 클릭
3. 정보 입력:
   - **Tag**: `v1.0.0` (새로 생성)
   - **Release title**: `HDI PDA v1.0.0`
   - **Description**: 변경 사항 작성
4. Actions에서 빌드된 APK 파일 첨부
5. `Publish release` 클릭

### 자동으로 Release 생성 (선택사항)

`.github/workflows/android.yml` 파일에 release job이 포함되어 있습니다.
tag를 푸시하면 자동으로 Release가 생성됩니다:

```bash
git tag v1.0.0
git push origin v1.0.0
```

## 🐛 문제 해결

### "build failed" 오류

**확인 사항**:
1. GitHub Secrets가 모두 정확히 설정되었는지
2. KEYSTORE_B64가 올바르게 인코딩되었는지
3. Actions 탭에서 에러 로그 확인

### Keystore 관련 오류

```
Error: Keystore file not found
```

**해결**:
- GitHub Secrets의 KEYSTORE_B64 확인
- Base64 인코딩 다시 수행
- Secrets 재등록

### 빌드는 성공하지만 서명 안 됨

**원인**: Secrets 미설정

**해결**:
- 4개 Secrets 모두 추가했는지 확인
- Secret 이름이 정확한지 확인 (대소문자 구분)

## 📋 체크리스트

배포 전 확인:

- [ ] GitHub 리포지토리 생성
- [ ] 로컬에서 git init 및 첫 커밋
- [ ] GitHub에 push 완료
- [ ] Keystore 생성
- [ ] GitHub Secrets 4개 모두 추가
- [ ] Actions 탭에서 첫 빌드 성공 확인
- [ ] APK 다운로드 및 설치 테스트
- [ ] 앱 정상 동작 확인

## 💡 유용한 팁

### 1. Branch 전략

- **main**: 배포용 (Release APK)
- **develop**: 개발용 (Debug APK)
- **feature/***: 기능 개발

### 2. Pull Request 활용

```bash
# 새 기능 개발
git checkout -b feature/new-scanner
# 코드 수정
git add .
git commit -m "Add new scanner feature"
git push origin feature/new-scanner
# GitHub에서 PR 생성 → 자동으로 빌드 테스트
```

### 3. 로컬에서도 빌드 가능

```bash
# Debug 빌드 (서명 불필요)
./gradlew assembleDebug

# Release 빌드 (Keystore 필요)
export KEYSTORE_PASSWORD="your_password"
export KEY_ALIAS="hdi-pda"
export KEY_PASSWORD="your_password"
./gradlew assembleRelease
```

## 🔐 보안 주의사항

⚠️ **절대로 Git에 포함하면 안 되는 것들**:
- `keystore.jks` (Keystore 파일)
- `keystore_b64.txt` (Base64 인코딩 파일)
- `keystore-info.txt` (비밀번호 정보)
- `local.properties` (로컬 설정)

✅ `.gitignore`에 이미 포함되어 있습니다!

## 📞 추가 도움이 필요하시면

1. GitHub Actions 로그 확인
2. Issues 탭에서 이슈 생성
3. 또는 HDI 시스템 개발팀에 문의

---

**준비 완료!** 이제 GitHub에 업로드하고 자동 빌드를 시작하세요! 🚀
