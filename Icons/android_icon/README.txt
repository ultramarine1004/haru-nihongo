일본어 1일 학습 앱 - 안드로이드 아이콘 리소스

[적용 방법]
1) res 폴더 안의 drawable, mipmap-* 폴더를 앱 모듈의 src/main/res 에 그대로 복사 (같은 이름이 있으면 덮어쓰기)
2) AndroidManifest.xml 의 <application> 에 아래 속성 확인
     android:icon="@mipmap/ic_launcher"
     android:roundIcon="@mipmap/ic_launcher_round"
3) 구글 플레이 콘솔 등록용: ic_launcher-playstore.png (512x512)

[구성]
- drawable/ic_launcher_background.xml : 후지산 배경 레이어 (108dp 벡터)
- drawable/ic_launcher_foreground.xml : 말풍선 + あ 레이어 (108dp 벡터)
- drawable/ic_launcher_monochrome.xml : Android 13 테마 아이콘용 단색 레이어
- mipmap-anydpi-v26/ic_launcher(.round).xml : 어댑티브 아이콘 정의 (API 26+)
- mipmap-*dpi/ic_launcher(.round).png : 구형 기기용 (API 25 이하)
- source/icon_master.svg : 원본 디자인 SVG
