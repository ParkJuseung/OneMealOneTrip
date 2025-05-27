<div align="center">
  <br />
  <img src="https://user-images.githubusercontent.com/60915285/188914541-6cc0df3f-91a3-4edf-b0da-578263f5fae4.png" width="50%" />
  <br />
</div>

<div align="center">
 <h3><b>같이집관 ⚽️</b></h3>
실시간으로 사람들과 같이 스포츠 경기를 응원할 수 있는 화상 서비스<br><br>
  삼성 청년 SW 아카데미 공통 프로젝트 <b> - 우수 프로젝트 🏆</b><br>
  2022.07.11 ~ 2022.08.19
  <br><br>

  [Notion](https://sunset-offer-934.notion.site/5fed7156282141f1b50c59e557fa04b6)  |  [UCC](https://www.youtube.com/watch?v=-wafC3AMRKM)

</div>
<br/>

## 서비스 배경 💡
스포츠 경기를 현장에서 직접 응원한 경험이 있나요? 📣<br>
스포츠 경기를 현장에서 직접 관람을 하면, 사람들과 같은 팀을 응원하면서 소속감/유대감을 느낄 수 있어 재밌게 응원할 수 있습니다. <br>그러나 최근 코로나의 여파, 티켓팅 실패, 시간/거리 상의 이유로 직관을 하기가 어려운 경우가 많았습니다. <br>이러한 이유로 <b>‘같이집관’</b>을 서비스하게 되었습니다.
<br><br><br>

## 서비스 이름 및 설명 🖥
직접 관람(직관)이 아닌 집에서 관람(”집관”)한다는 특징을 이름에 담았습니다.<br> 또한 여러 사람들과 다같이 응원한다는 의미에서 “같이”를 넣었습니다. 

<b>‘같이 집관’</b>은 직관을 가지 못하더라도 화상에서 실시간으로 사람들과 경기 화면을 보면서 응원할 수 있습니다. <br>또한 현장감과 소속감을 느낄 수 있도록 응원가, 전광판, 실시간 경기 정보, 승부예측 시스템 등도 구현되어 있습니다.

<br><br>

## 개발 환경 및 기술 스택 🔨

### FE
`HTML`  <br>
`CSS`  <br>
`JavaScript` <br>
`Node.js 16.16.0` <br>
`Vue 3.2.13` <br>
`Pinia 2.0.14`  <br>
`Vuetify 3.0.0.Beta` <br>

### BE
`Java 8`  <br>
`Spring Boot 2.7.2`  <br>
`Gradle 7.5` <br>
`JPA 2.7.2`  <br>
`MySQL 8.0.29` <br>
`Swagger2 3.0.0` <br>
`Openvidu 2.22` <br>

### CI/CD
`AWS EC2`  <br>
`Ubuntu 20.0.4 LTS` <br>
`Docker 20.10.17`  <br>
`Jenkins 2.346.2`  <br>
`Nginx 1.23.1`  <br>
`Nginx-rmtp 1.18.0` <br>

### 버전/이슈 관리

`Jira` <br>
`GitLab` <br>
`Sourcetree` <br>

### TOOL

`Postman` <br>
`Figma` <br>

### 협업

`Cisco Webex Meetings` <br>
`Mattermost` <br>
`Notion` <br>

### IDE

`Visual Studio Code` <br>
`IntelliJ`

<br><br>

## 서비스 구조도 🔍
<img width="750" alt="image" src="https://user-images.githubusercontent.com/60915285/188927329-3cf2a1a5-1e58-4896-9163-2049ab7cfa5f.png">
<br><br>

## 주요 기능 📚

### 1. 소셜로그인
<img width="700" src="https://user-images.githubusercontent.com/60915285/189544140-0e26ce69-2191-4abf-a5df-54f915a86d72.gif">
일반 로그인 및 카카오 로그인이 가능합니다.

<br><br>
        
### 2. 응원방
<img width="700" src="https://user-images.githubusercontent.com/60915285/223932054-64bbdda6-03de-4ba4-a044-32875e6ba8fa.gif">
다같이 경기 화면을 보면서 화상으로 응원할 수 있습니다.<br>
방장이 응원가를 재생하면, 참가자들은 실시간으로 해당 응원가를 들을 수 있습니다.

<br><br>

### 3. 응원방 - 전광판
<img width="700" src="https://user-images.githubusercontent.com/60915285/189544287-de42403c-4c30-48b5-93bf-dbb20486d092.gif">
외부 축구 API를 사용하여 실시간으로 경기중인 경기의 정보를 가져옵니다.<br>
경기 스코어와 골 정보를 확인할 수 있습니다.

<br><br>

### 4. 응원방 - 승부예측
<img width="700" src="https://user-images.githubusercontent.com/60915285/189544282-23bd0402-7276-4a07-8508-fc2be2691ec6.gif">
승부예측 시스템은 경기 시작 전부터 경기 10분까지 예측할 수 있습니다.<br>
보유하고 있는 포인트(공)를 원하는 만큼 원하는 팀에 걸 수 있으며, 경기가 종료되면 결과에 따라 배당만큼 돌려받게됩니다.

<br><br>

### 5. 응원방 - 강제퇴장
<img width="700" src="https://user-images.githubusercontent.com/60915285/189544280-2bf99a41-5bda-40bd-b92e-63248777c0c1.gif">
방장은 방의 참가자들을 강퇴시킬 수 있는 권한을 가지고 있습니다.

<br><br>

### 6. 응원방 - 채팅
<img width="700" src="https://user-images.githubusercontent.com/60915285/189544288-b9496177-6e94-4ed5-a95b-9f2f0904e9c1.gif">
방의 모든 참가자들은 채팅 기능을 사용할 수 있습니다.<br>
여기에는 비속어 필터링이 존재하여, 비속어가 섞인 채팅을 보내게 되면 '[삭제된 메세지]' 라고 대체되어 보내집니다.

<br><br>
  
### 7. 경기 일정 및 결과
<img width="700" src="https://user-images.githubusercontent.com/60915285/189544710-06188f36-3d1a-47c9-bc9e-5debb37c3906.gif">
리그별/월별 경기 일정을 제공합니다.<br>
종료된 경기는 경기에 대한 결과를 제공합니다.

<br><br>


### 8. 경기 일정 - 오늘의 경기
<img width="700" src="https://user-images.githubusercontent.com/60915285/189544711-ebf86e6e-6c1f-4581-90ee-54cedf9592a3.gif">
오늘 있을 경기들을 리그별로 제공합니다.

<br><br>

        
### 9. 커뮤니티
<img width="700" src="https://user-images.githubusercontent.com/60915285/189544846-7c464775-a072-4dd8-ad3c-b0568909ae49.gif">
![커뮤니티 글작성]()
리그별 분류를 가진 커뮤니티가 있습니다.<br>
오른쪽 위에는 최신 스포츠 뉴스를 제공하고 (네이버 뉴스 api), 아래에는 오늘의 경기를 제공합니다.<br>
왼쪽 사이드바 아래에는 명예의 전당(포인트 랭킹)을 제공합니다.

<br><br>


## 프로젝트 산출물 📝
- [와이어프레임(Figma)](https://www.figma.com/file/ePhUEm8n0WOKCVpdS3znoY/Untitled?node-id=10%3A34)
- [요구사항 명세서](https://docs.google.com/spreadsheets/d/1xPJsyiMqSk_K8tLmQkwHjnBlbztizLL380-utx67S0Y/edit?usp=sharing)
- [API 명세서](https://docs.google.com/spreadsheets/d/1DsIVlvEm14OrcFZ1Z_UtRkTFgNZIzEe8x1uVugoBL0o/edit?usp=sharing)
- [ERD](https://www.erdcloud.com/d/qgQJ5vEiGkjDeJmgy)
- [회의록](https://sunset-offer-934.notion.site/caac1e153e834de1aa797a7745dbd0a1)
- [데일리 스크럼](https://sunset-offer-934.notion.site/74a1a2fd88574e98a9b18b3a0547d4c8)
- [포팅 메뉴얼](https://github.com/Cheer-Together/Cheer-Together/blob/main/exec/01_%EA%B3%B5%ED%86%B5PJT_%EB%8C%80%EC%A0%842%EB%B0%98_B204_%EB%B9%8C%EB%93%9C_%EB%B0%8F_%ED%8F%AC%ED%8C%85_%EB%AC%B8%EC%84%9C.pdf)
- [외부 API 문서](https://github.com/Cheer-Together/Cheer-Together/blob/main/exec/02_%EA%B3%B5%ED%86%B5PJT_%EB%8C%80%EC%A0%842%EB%B0%98_B204_%EC%99%B8%EB%B6%80_API_%EC%82%AC%EC%9A%A9.pdf)
- [시연 시나리오](https://github.com/Cheer-Together/Cheer-Together/blob/main/exec/04_%EA%B3%B5%ED%86%B5PJT_%EB%8C%80%EC%A0%842%EB%B0%98_B204_%EC%8B%9C%EC%97%B0_%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4.pdf) 


<br><br>
## 개발 문화 💁‍♂️
- 데일리 스크럼을 매일 진행
- 간단한 회의부터 중요한 회의, 모든것을 노션에 기록하는 습관 📝
- 의견은 자유롭게!
- 재밌게, 함께 성장하는 개발하기
- 머지 리퀘스트 열심히 작성하고 서로 코드 리뷰 
<img width="800" alt="스크린샷 2022-10-13 오전 4 03 17" src="https://user-images.githubusercontent.com/60915285/195427955-51e7b2b8-ec0f-4225-8d29-63e458df37ba.png">
<img width="800" alt="스크린샷 2022-10-13 오전 4 04 08" src="https://user-images.githubusercontent.com/60915285/195427670-f1d7b0f1-4e36-4554-953a-2c00c075c7f2.png">
<img width="600" alt="스크린샷 2022-10-13 오전 4 04 35" src="https://user-images.githubusercontent.com/60915285/195427335-866764a7-b3ba-4423-ac88-384ec78a42eb.png">



<br><br>
## 팀원 소개 👪
<table>
    <tr>
        <td height="140px" align="center"> <a href="https://github.com/Qulip">
            <img src="https://avatars.githubusercontent.com/Qulip" width="140px" /> <br><br> 👑 유일권 <br>(Back-End) </a> <br></td>
        <td height="140px" align="center"> <a href="https://github.com/BoyeonK">
            <img src="https://avatars.githubusercontent.com/BoyeonK" width="140px" /> <br><br> 🐱 김보연 <br>(Front-End) </a> <br></td>
        <td height="140px" align="center"> <a href="https://github.com/js0828">
            <img src="https://avatars.githubusercontent.com/js0828" width="140px" /> <br><br> 🐻 최종수 <br>(Back-End) </a> <br></td>
        <td height="140px" align="center"> <a href="https://github.com/choijoohee213">
            <img src="https://avatars.githubusercontent.com/choijoohee213" width="140px" /> <br><br> 🐰 최주희 <br>(Back-End) </a> <br></td>
        <td height="140px" align="center"> <a href="https://github.com/niinp28">
            <img src="https://avatars.githubusercontent.com/niinp28" width="140px" /> <br><br> 🐯 최준혁 <br>(Front-End) </a> <br></td>
        <td height="140px" align="center"> <a href="https://github.com/HSungDuk">
            <img src="https://avatars.githubusercontent.com/HSungDuk" width="140px" /> <br><br> 🐹 홍성덕 <br>(Front-End) </a> <br></td>
    </tr>
</table>

<br><br><br>
