# API Spec

## 회원가입

### 기본 정보

| Method | URL          | 출력 포멧 | 설명     |
| ------ | ------------ | --------- | -------- |
| POST   | /auth/signup | JSON      | 회원가입 |

### 요청 헤더

| key          | 필수 여부 | 설명             |
| ------------ | --------- | ---------------- |
| Content-Type | O         | application/json |

### 요청 본문

| 변수명   | 데이터 타입 | 제약 사항       | 필수 여부 | 기본값 | 설명            |
| -------- | ----------- | --------------- | --------- | ------ | --------------- |
| email    | String      | 이메일 형식     | O         |        | 사용자 이메일   |
| password | String      | 최소 6자리 이상 | O         |        | 사용자 비밀번호 |
| name     | String      |                 | O         |        | 사용자 이름     |

### 요청 예시

```http
POST /auth/signup HTTP/1.1
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

### 응답

```http
HTTP/1.1 200 OK

{
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동"
  }
}
```

## 로그인

### 기본 정보

| Method | URL          | 출력 포멧 | 설명   |
| ------ | ------------ | --------- | ------ |
| POST   | /auth/signin | JSON      | 로그인 |

### 요청 헤더

| key          | 필수 여부 | 설명             |
| ------------ | --------- | ---------------- |
| Content-Type | O         | application/json |

### 요청 본문

| 변수명   | 데이터 타입 | 제약 사항   | 필수 여부 | 기본값 | 설명            |
| -------- | ----------- | ----------- | --------- | ------ | --------------- |
| email    | String      | 이메일 형식 | O         |        | 사용자 이메일   |
| password | String      |             | O         |        | 사용자 비밀번호 |

### 요청 예시

```http
POST /auth/signin HTTP/1.1
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### 응답

```http
HTTP/1.1 200 OK

{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "grantType": "Bearer"
  }
}
```

## 액세스 토큰 갱신

### 기본 정보

| Method | URL           | 출력 포멧 | 설명             |
| ------ | ------------- | --------- | ---------------- |
| POST   | /auth/refresh | JSON      | 액세스 토큰 갱신 |

### 요청 헤더

| key          | 필수 여부 | 설명             |
| ------------ | --------- | ---------------- |
| Content-Type | O         | application/json |

### 요청 본문

| 변수명       | 데이터 타입 | 제약 사항 | 필수 여부 | 기본값 | 설명          |
| ------------ | ----------- | --------- | --------- | ------ | ------------- |
| refreshToken | String      | NotBlank  | O         |        | 리프레시 토큰 |

### 요청 예시

```http
POST /auth/refresh HTTP/1.1
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 응답

```http
HTTP/1.1 200 OK

{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "grantType": "Bearer"
  }
}
```

## 방 등록

### 기본 정보

| Method | URL    | 출력 포멧 | 설명           |
| ------ | ------ | --------- | -------------- |
| POST   | /rooms | JSON      | 새로운 방 등록 |

### 요청 헤더

| key          | 필수 여부 | 설명             |
| ------------ | --------- | ---------------- |
| Content-Type | O         | application/json |

### 요청 본문

| 변수명      | 데이터 타입                   | 제약 사항 | 필수 여부 | 기본값 | 설명                                                         |
| ----------- | ----------------------------- | --------- | --------- | ------ | ------------------------------------------------------------ |
| title       | String                        | NotBlank  | O         |        | 방 제목                                                      |
| description | String                        |           | X         |        | 방 설명                                                      |
| address     | String                        | NotBlank  | O         |        | 방 주소                                                      |
| area        | Double                        |           | X         |        | 방 면적 (m²)                                                 |
| roomType    | RoomTypeDto                   | NotNull   | O         |        | 방 종류<br />- input 으로 들어올 수 있는 값은 [데이터 타입 참고](#데이터 타입 참고) |
| deals       | List<DealRegistrationRequest> |           | O         |        | 거래 정보 목록                                               |

#### DealRegistrationRequest

| 변수명      | 데이터 타입 | 제약 사항 | 필수 여부 | 기본값 | 설명                                                         |
| ----------- | ----------- | --------- | --------- | ------ | ------------------------------------------------------------ |
| dealType    | DealTypeDto | NotNull   | O         |        | 거래 유형<br />- input 으로 들어올 수 있는 값은 [데이터 타입 참고](#데이터 타입 참고) |
| deposit     | BigDecimal  |           | X         |        | 보증금                                                       |
| monthlyRent | BigDecimal  |           | X         |        | 월세                                                         |

### 요청 예시

```http
POST /rooms HTTP/1.1
Content-Type: application/json

{
  "title": "깨끗한 원룸",
  "description": "신축 건물의 깨끗한 원룸입니다",
  "address": "서울시 강남구 역삼동",
  "area": 25.5,
  "roomType": "ONE_ROOM",
  "deals": [
    {
      "dealType": "MONTHLY_RENT",
      "deposit": 20000000,
      "monthlyRent": 700000
    }
  ]
}
```

### 응답

```http
HTTP/1.1 200 OK

{
  "data": {
    "id": 1,
    "title": "깨끗한 원룸",
    "description": "신축 건물의 깨끗한 원룸입니다",
    "address": "서울시 강남구 역삼동",
    "area": 25.5,
    "roomType": "ONE_ROOM",
    "deals": [
      {
        "dealType": "MONTHLY_RENT",
        "deposit": 20000000,
        "monthlyRent": 700000
      }
    ]
  }
}
```

## 방 삭제

### 기본 정보

| Method | URL         | 출력 포멧 | 설명       |
| ------ | ----------- | --------- | ---------- |
| DELETE | /rooms/{id} | -         | 내 방 삭제 |

### 요청 헤더

**없음**

### 경로 변수

| 변수명 | 데이터 타입 | 제약 사항 | 필수 여부 | 설명      |
| ------ | ----------- | --------- | --------- | --------- |
| id     | Long        | NotNull   | O         | 방 아이디 |

### 요청 예시

```http
DELETE /rooms/1 HTTP/1.1
```

### 응답

```http
HTTP/1.1 204 No Content
```

## 방 수정

### 기본 정보

| Method | URL         | 출력 포멧 | 설명            |
| ------ | ----------- | --------- | --------------- |
| PATCH  | /rooms/{id} | JSON      | 내 방 정보 수정 |

### 요청 헤더

| key          | 필수 여부 | 설명             |
| ------------ | --------- | ---------------- |
| Content-Type | O         | application/json |

### 경로 변수

| 변수명 | 데이터 타입 | 제약 사항 | 필수 여부 | 설명      |
| ------ | ----------- | --------- | --------- | --------- |
| id     | Long        | NotNull   | O         | 방 아이디 |

### 요청 본문

모든 필드가 선택사항이며, 변경하고자 하는 필드만 포함하여 요청합니다.

| 변수명      | 데이터 타입             | 제약 사항 | 필수 여부 | 기본값 | 설명                                                         |
| ----------- | ----------------------- | --------- | --------- | ------ | ------------------------------------------------------------ |
| title       | String                  |           | X         |        | 방 제목                                                      |
| description | String                  |           | X         |        | 방 설명                                                      |
| address     | String                  |           | X         |        | 방 주소                                                      |
| area        | Double                  |           | X         |        | 방 면적 (m²)                                                 |
| roomType    | RoomTypeDto             |           | X         |        | 방 종류<br />- input 으로 들어올 수 있는 값은 [데이터 타입 참고](#데이터 타입 참고) |
| deals       | List<DealUpdateRequest> |           | X         |        | 거래 정보 목록                                               |

#### DealUpdateRequest

| 변수명      | 데이터 타입 | 제약 사항 | 필수 여부 | 기본값 | 설명                                                         |
| ----------- | ----------- | --------- | --------- | ------ | ------------------------------------------------------------ |
| dealType    | DealTypeDto |           | X         |        | 거래 유형<br />- input 으로 들어올 수 있는 값은 [데이터 타입 참고](#데이터 타입 참고) |
| deposit     | BigDecimal  |           | X         |        | 보증금                                                       |
| monthlyRent | BigDecimal  |           | X         |        | 월세                                                         |

### 요청 예시

```http
PATCH /rooms/1 HTTP/1.1
Content-Type: application/json

{
  "title": "업데이트된 원룸",
  "deals": [
    {
      "dealType": "MONTHLY_RENT",
      "deposit": 5000000,
      "monthlyRent": 600000
    }
  ]
}
```

### 응답

```http
HTTP/1.1 200 OK

{
  "data": {
    "id": 1,
    "title": "업데이트된 원룸",
    "description": "신축 건물의 깨끗한 원룸입니다",
    "address": "서울시 강남구 역삼동",
    "area": 25.5,
    "roomType": "ONE_ROOM",
    "deals": [
      {
        "dealType": "MONTHLY_RENT",
        "deposit": 50000000,
        "monthlyRent": 600000
      }
    ]
  }
}
```

## 내 방 단건 조회

### 기본 정보

| Method | URL         | 출력 포멧 | 설명                 |
| ------ | ----------- | --------- | -------------------- |
| GET    | /rooms/{id} | JSON      | 내 방 상세 정보 조회 |

### 요청 헤더

**없음**

### 경로 변수

| 변수명 | 데이터 타입 | 제약 사항 | 필수 여부 | 설명      |
| ------ | ----------- | --------- | --------- | --------- |
| id     | Long        | NotNull   | O         | 방 아이디 |

### 요청 예시

```http
GET /rooms/1 HTTP/1.1
```

### 응답

```http
HTTP/1.1 200 OK

{
  "data": {
    "id": 1,
    "title": "깨끗한 원룸",
    "description": "신축 건물의 깨끗한 원룸입니다",
    "address": "서울시 강남구 역삼동",
    "area": 25.5,
    "roomType": "ONE_ROOM",
    "deals": [
      {
        "dealType": "MONTHLY_RENT",
        "deposit": 10000000,
        "monthlyRent": 800000
      }
    ]
  }
}
```

## 내 방 목록 조회

### 기본 정보

| Method | URL       | 출력 포멧 | 설명                     |
| ------ | --------- | --------- | ------------------------ |
| GET    | /rooms/my | JSON      | 내가 등록한 방 목록 조회 |

### 요청 헤더

**없음**

### 요청 파라미터

**없음**

### 요청 예시

```http
GET /rooms/my HTTP/1.1
```

### 응답

```http
HTTP/1.1 200 OK

{
  "data": [
    {
      "id": 1,
      "title": "깨끗한 원룸",
      "description": "신축 건물의 깨끗한 원룸입니다",
      "address": "서울시 강남구 역삼동",
      "area": 25.5,
      "roomType": "ONE_ROOM",
      "deals": [
        {
          "dealType": "MONTHLY_RENT",
          "deposit": 10000000,
          "monthlyRent": 800000
        }
      ]
    },
    {
      "id": 2,
      "title": "넓은 투룸",
      "description": "가족이 살기 좋은 투룸입니다",
      "address": "서울시 서초구 서초동",
      "area": 45.0,
      "roomType": "TWO_ROOM",
      "deals": [
        {
          "dealType": "JEONSE",
          "deposit": 50000000,
          "monthlyRent": 0
        }
      ]
    }
  ]
}
```

## 전체 방 목록 조회 및 검색

검색 파라미터가 있으면 해당 파라미터 값을 기준으로 필터링을 하고, 페이징 값을 기반으로 데이터를 조회합니다. 검색 파라미터가 없으면 페이징 값을 기반으로 데이터를 조회합니다.

### 기본 정보

| Method | URL    | 출력 포멧 | 설명                      |
| ------ | ------ | --------- | ------------------------- |
| GET    | /rooms | JSON      | 전체 방 목록 조회 및 검색 |

### 요청 헤더

**없음**

### 요청 파라미터

| 변수명         | 데이터 타입  | 제약 사항            | 필수 여부 | 기본값 | 설명                                                         |
| -------------- | ------------ | -------------------- | --------- | ------ | ------------------------------------------------------------ |
| roomTypes      | List<String> |                      | X         |        | 방 종류 목록 (다중 선택 가능하며, 전체 선택시에는 파라미터 자체를 넣지 않으면 됨)<br />- input 으로 들어올 수 있는 값은 [데이터 타입 참고](#데이터 타입 참고) |
| dealTypes      | List<String> |                      | X         |        | 거래 유형 목록 (다중 선택 가능, 전체 선택시에는 파라미터 자체를 넣지 않으면 됨)<br />- input 으로 들어올 수 있는 값은 [데이터 타입 참고](#데이터 타입 참고) |
| minDeposit     | BigDecimal   | 0 이상의 값          | X         |        | 최소 보증금                                                  |
| maxDeposit     | BigDecimal   | 0 이상의 값          | X         |        | 최대 보증금                                                  |
| minMonthlyRent | BigDecimal   | 0 이상의 값          | X         |        | 최소 월세                                                    |
| maxMonthlyRent | BigDecimal   | 0 이상의 값          | X         |        | 최대 월세                                                    |
| page           | int          | 0 이상의 값          | X         | 0      | 페이지 번호 (0부터 시작)                                     |
| size           | int          | 0 이상 100 이하의 값 | X         | 20     | 페이지 크기 (최대 100개까지 조회 가능)                       |

### 요청 예시

```http
GET /rooms?roomTypes=ONE_ROOM,TWO_ROOM&dealTypes=MONTHLY_RENT&minDeposit=5000000&maxDeposit=20000000&page=0&size=10 HTTP/1.1
```

### 응답

```http
HTTP/1.1 200 OK

{
  "data": [
    {
      "id": 1,
      "title": "깨끗한 원룸",
      "description": "신축 건물의 깨끗한 원룸입니다",
      "address": "서울시 강남구 역삼동",
      "area": 25.5,
      "roomType": "ONE_ROOM",
      "deals": [
        {
          "dealType": "MONTHLY_RENT",
          "deposit": 10000000,
          "monthlyRent": 800000
        }
      ]
    },
    {
      "id": 3,
      "title": "교통편리한 원룸",
      "description": "지하철역 도보 3분 거리",
      "address": "서울시 마포구 홍대입구",
      "area": 20.0,
      "roomType": "ONE_ROOM",
      "deals": [
        {
          "dealType": "MONTHLY_RENT",
          "deposit": 15000000,
          "monthlyRent": 700000
        }
      ]
    }
  ]
}
```

## 공통 에러 응답

에러 메세지의 형식은 에러가 발생하는 위치와 관계없이 동일하며, 에러의 성격에 따라 400 ~ 500번대 HTTP 응답 코드로 반환됩니다.

- 예시1) 401 Unauthorized

  ```http
  HTTP/1.1 401 Unauthorized
  
  {
    "errorCode": "UNAUTHORIZED",
    "errorMessage": "인증 정보가 존재하지 않습니다."
  }
  ```

- 예시2) 403 Forbidden

  ```http
  HTTP/1.1 403 Forbidden
  
  {
    "errorCode": "FORBIDDEN",
    "errorMessage": "접근 권한이 없습니다."
  }
  ```

- 예시3) 404 Not Found

  ```http
  HTTP/1.1 404 Not Found
  
  {
    "errorCode": "ROOM_NOT_FOUND",
    "errorMessage": "방을 찾을 수 없습니다."
  }
  ```

- 예시4) 500 Internal Server Error

  ```http
  HTTP/1.1 500 Internal Server Error
  
  {
    "errorCode": "INTERNAL_SERVER_ERROR",
    "errorMessage": "서버 오류가 발생했습니다."
  }
  ```

## 데이터 타입 참고

### RoomTypeDto

```
ONE_ROOM, TWO_ROOM, THREE_ROOM
```

### DealTypeDto

```
MONTHLY_RENT, YEAR_RENT
```
