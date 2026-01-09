# 🚀 초간단 GitHub 웹 업로드 가이드

## 📦 준비물
- ✅ HDI-PDA.zip 파일
- ✅ GitHub 계정

**소요 시간**: 5분  
**복잡한 설정**: 없음!

---

## 🎯 3단계로 APK 받기

### 1단계: GitHub 리포지토리 생성 (2분)

1. https://github.com 접속 및 로그인
2. 우측 상단 **`+`** 버튼 → **`New repository`** 클릭
3. 정보 입력:
   - **Repository name**: `HDI-PDA`
   - **Description**: HDI 바코드 스캐너 (선택사항)
   - **Public** 또는 **Private** 선택
   - ⚠️ **"Add a README file" 체크 해제**
   - ⚠️ **".gitignore" None 선택**
   - ⚠️ **"Choose a license" None 선택**
4. **`Create repository`** 버튼 클릭

---

### 2단계: 파일 업로드 (2분)

#### A. zip 파일 압축 해제
```
HDI-PDA.zip → 압축 해제
```

#### B. GitHub 웹에서 파일 업로드

리포지토리 페이지에서:

1. **`uploading an existing file`** 링크 클릭
2. 압축 해제한 **모든 파일과 폴더를 드래그 앤 드롭**
   - ✅ `.github` 폴더
   - ✅ `app` 폴더
   - ✅ `gradle` 폴더
   - ✅ 모든 `.md`, `.txt` 파일
   - ✅ `build.gradle.kts`
   - ✅ `settings.gradle.kts`
   - ✅ `gradlew`, `gradlew.bat`
   - ✅ 기타 모든 파일
3. 페이지 하단:
   - Commit message: `Initial commit`
   - **`Commit changes`** 버튼 클릭

**💡 팁**: 
- 모든 파일을 **한 번에** 드래그하세요 (폴더 구조 유지)
- 업로드에 1-2분 소요될 수 있습니다

---

### 3단계: APK 다운로드 (5-10분 후)

#### A. 빌드 진행 확인

1. 리포지토리 상단 **`Actions`** 탭 클릭
2. **"Android Debug Build"** 워크플로우 클릭
3. 진행 상황 확인 (5-10분 소요)
   - 🔵 노란색: 빌드 진행 중
   - ✅ 초록색: 빌드 성공!
   - ❌ 빨간색: 오류 발생 (로그 확인)

#### B. APK 다운로드

빌드 완료 후:

1. 완료된 워크플로우 클릭
2. 페이지 하단 **`Artifacts`** 섹션 확인
3. **`app-debug-apk`** 클릭하여 다운로드
4. zip 압축 해제 → `app-debug.apk` 파일 확인

---

## 📱 APK 설치 방법

### 방법 1: USB로 설치 (권장)

```bash
# 기기를 USB로 연결
adb install -r app-debug.apk
```

### 방법 2: 직접 설치

1. APK 파일을 기기로 전송 (이메일, 클라우드, USB 등)
2. 기기에서 파일 찾기
3. APK 파일 탭하여 설치
4. "알 수 없는 출처" 허용 필요 시 설정에서 허용

---

## 🔄 코드 수정 후 재빌드

### GitHub 웹에서 파일 수정

1. 리포지토리에서 수정할 파일 클릭
2. 연필 아이콘 (Edit) 클릭
3. 코드 수정
4. 하단 **`Commit changes`** 클릭
5. → 자동으로 새로 빌드!
6. Actions 탭에서 진행 확인

### 여러 파일 수정

1. 리포지토리 메인 페이지
2. **`Add file`** → **`Upload files`** 클릭
3. 수정한 파일들을 드래그 앤 드롭
4. **`Commit changes`** 클릭
5. → 자동으로 새로 빌드!

---

## ✅ 체크리스트

- [ ] GitHub 리포지토리 생성
- [ ] zip 압축 해제
- [ ] 모든 파일 업로드 (폴더 구조 유지)
- [ ] Actions 탭에서 빌드 진행 확인
- [ ] 빌드 완료 (초록색 체크)
- [ ] Artifacts에서 APK 다운로드
- [ ] 기기에 설치

---

## 🎉 완료!

이제 코드를 수정할 때마다:
1. ✅ GitHub 웹에서 파일 수정 또는 업로드
2. ✅ 자동으로 빌드 시작
3. ✅ 5-10분 후 새 APK 다운로드

**복잡한 설정 없이 바로 사용 가능합니다!**

---

## 💡 자주 묻는 질문

### Q: Keystore가 필요한가요?
**A**: 아니요! Debug APK는 서명 불필요합니다.

### Q: 로컬에 Git이나 Android Studio가 필요한가요?
**A**: 아니요! GitHub 웹만 있으면 됩니다.

### Q: Release APK를 만들고 싶어요
**A**: GitHub Secrets에 Keystore 설정 필요 (별도 가이드 참조)

### Q: 빌드가 실패했어요
**A**: Actions 탭 → 실패한 워크플로우 클릭 → 로그 확인

### Q: 파일 업로드 시 폴더 구조가 깨져요
**A**: 폴더를 하나씩 올리지 말고 **모든 파일을 한 번에** 드래그하세요

---

## 📞 문제 해결

### "No workflows found" 메시지
- `.github/workflows/android.yml` 파일이 올바른 위치에 있는지 확인
- 파일을 다시 업로드해보세요

### 빌드 실패: "gradlew not found"
- `gradlew`, `gradlew.bat` 파일이 루트에 있는지 확인
- workflow가 자동으로 생성하므로 다시 시도하면 됩니다

### APK 다운로드 링크가 없어요
- 빌드가 완료될 때까지 기다리세요 (초록색 체크)
- 페이지 하단 Artifacts 섹션 확인

---

**모든 준비 완료! 파일만 업로드하면 APK가 자동으로 빌드됩니다!** 🚀
